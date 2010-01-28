package testful.mutation;

import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.BooleanType;
import soot.Local;
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
import soot.jimple.Stmt;
import soot.util.Chain;
import testful.IConfigProject;
import testful.utils.Skip;
import testful.utils.Instrumenter.UnifiedInstrumentator;

/**
 * Instruments the class, forcing the termination of the execution when a flag
 * is set to true.
 * 
 * @author matteo
 */
public class ExecutionStopper implements UnifiedInstrumentator {

	public static final ExecutionStopper singleton = new ExecutionStopper();

	private static final SootField stopField;
	private static final RefType stoppedExceptionType;
	private static final SootMethodRef stoppedExceptionInit;

	static {
		Scene.v().loadClassAndSupport(TestStoppedException.class.getCanonicalName());
		SootClass stoppedException = Scene.v().getSootClass(TestStoppedException.class.getCanonicalName());
		stopField = stoppedException.getFieldByName(TestStoppedException.STOP_NAME);
		stoppedExceptionType = stoppedException.getType();
		stoppedExceptionInit = stoppedException.getMethodByName(SootMethod.constructorName).makeRef();
	}

	private List<Unit> previous;

	private Local exc;
	private Local tmp;

	@Override
	public void preprocess(SootClass sClass) { }

	@Override
	public void init(Chain<Unit> newUnits, Body newBody, Body oldBody, boolean classWithContracts, boolean contractMethod) {
		if(classWithContracts) {
			previous = null;
			return;
		}

		previous = new ArrayList<Unit>();

		exc = Jimple.v().newLocal("__stopper_exc__", stoppedExceptionType);
		newBody.getLocals().add(exc);
		tmp = Jimple.v().newLocal("__stopper_stop__", BooleanType.v());
		newBody.getLocals().add(tmp);
	}

	@Override
	public void processPre(Chain<Unit> newUnits, Stmt op) {
		if(previous == null) return;

		if(op.hasTag(Skip.NAME)) return;

		previous.add(op);

		Unit target = null;
		if(op instanceof GotoStmt) target = ((GotoStmt) op).getTarget();
		else if(op instanceof IfStmt) target = ((IfStmt) op).getTarget();

		if((op instanceof InvokeStmt) || (target != null && previous.contains(target))) {
			NopStmt nop = Jimple.v().newNopStmt();
			newUnits.insertBefore(Jimple.v().newAssignStmt(tmp, Jimple.v().newStaticFieldRef(stopField.makeRef())), op);
			newUnits.insertBefore(Jimple.v().newIfStmt(Jimple.v().newEqExpr(tmp, IntConstant.v(0)), nop), op);
			newUnits.insertBefore(Jimple.v().newAssignStmt(exc, Jimple.v().newNewExpr(stoppedExceptionType)), op);
			newUnits.insertBefore(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(exc, stoppedExceptionInit)), op);
			newUnits.insertBefore(Jimple.v().newThrowStmt(exc), op);
			newUnits.insertBefore(nop, op);
		}
	}

	@Override
	public void processPost(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPostExc(Chain<Unit> newUnits, Stmt op, Local exception) { }

	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) { }

	@Override
	public void done(IConfigProject config, String cutName) { }
}
