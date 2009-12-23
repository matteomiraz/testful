package testful.mutation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.BooleanType;
import soot.Local;
import soot.PatchingChain;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Unit;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.StaticFieldRef;
import testful.utils.Skip;

/**
 * Instruments the class, forcing the termination of the execution when a flag
 * is set to true.
 * 
 * @author matteo
 */
public class ExecutionStopper extends BodyTransformer {

	public static final ExecutionStopper singleton = new ExecutionStopper();

	private final RefType stoppedExceptionType;
	private final SootMethodRef stoppedExceptionInit;
	private final SootClass stoppedException;
	private final SootField stopField;

	private ExecutionStopper() {
		Scene.v().loadClassAndSupport(TestStoppedException.class.getCanonicalName());
		stoppedException = Scene.v().getSootClass(TestStoppedException.class.getCanonicalName());
		stopField = stoppedException.getFieldByName(TestStoppedException.STOP_NAME);
		stoppedExceptionType = stoppedException.getType();
		stoppedExceptionInit = stoppedException.getMethodByName(SootMethod.constructorName).makeRef();
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void internalTransform(Body body, String phaseName, Map options) {
		SootClass sClass = body.getMethod().getDeclaringClass();

		// checking if the class has (JML) contracts
		boolean contract;
		if(sClass.implementsInterface(org.jmlspecs.jmlrac.runtime.JMLCheckable.class.getCanonicalName())) if(body.getMethod().getName().startsWith("internal$")) contract = false;
		else contract = true;
		else contract = false;

		if(contract) {
			System.out.println("Skipping " + sClass.getName() + "::" + body.getMethod().getName() + " (" + (contract ? "contract" : "implementation") + ")");
			return;
		}

		System.out.println("Running execution stopper on " + sClass.getName() + "::" + body.getMethod().getName());

		Local exc = Jimple.v().newLocal("__stopper_exc__", stoppedExceptionType);
		body.getLocals().add(exc);
		Local tmp = Jimple.v().newLocal("__stopper_stop__", BooleanType.v());
		body.getLocals().add(tmp);

		StaticFieldRef stopExecution = Jimple.v().newStaticFieldRef(stopField.makeRef());

		List<Unit> previous = new ArrayList<Unit>();
		Iterator<Unit> iter = body.getUnits().snapshotIterator();
		while(iter.hasNext()) {
			Unit unit = iter.next();
			previous.add(unit);

			if(unit.hasTag(Skip.NAME)) continue;

			if(unit instanceof InvokeStmt) {
				insert(body.getUnits(), unit, exc, tmp, stopExecution);
				continue;
			}

			Unit target = null;
			if(unit instanceof GotoStmt) target = ((GotoStmt) unit).getTarget();
			else if(unit instanceof IfStmt) target = ((IfStmt) unit).getTarget();

			if(target == null || !previous.contains(target)) continue;
			insert(body.getUnits(), unit, exc, tmp, stopExecution);
		}
	}

	private void insert(PatchingChain<Unit> patchingChain, Unit unit, Local exc, Local tmp, StaticFieldRef stopExecution) {
		List<Unit> toInsert = new ArrayList<Unit>();

		NopStmt nop = Jimple.v().newNopStmt();
		toInsert.add(Jimple.v().newAssignStmt(tmp, stopExecution));
		toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(tmp, IntConstant.v(0)), nop));
		toInsert.add(Jimple.v().newAssignStmt(exc, Jimple.v().newNewExpr(stoppedExceptionType)));
		toInsert.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(exc, stoppedExceptionInit)));
		toInsert.add(Jimple.v().newThrowStmt(exc));
		toInsert.add(nop);

		for(int i = 0; i < toInsert.size(); i++)
			patchingChain.insertBefore(toInsert.get(i), unit);

		//patchingChain.insertBefore(toInsert, unit);
	}
}
