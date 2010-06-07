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
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import testful.IConfigProject;
import testful.coverage.soot.Instrumenter.UnifiedInstrumentator;
import testful.coverage.soot.Skip;

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

	/* (non-Javadoc)
	 * @see testful.coverage.soot.Instrumenter.UnifiedInstrumentator#init(soot.Body, soot.Body, soot.util.Chain)
	 */
	@Override
	public void init(Body oldBody, Body newBody, Chain<Unit> newUnits) {
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

		if(op.containsInvokeExpr() || (target != null && previous.contains(target))) {
			NopStmt nop = Jimple.v().newNopStmt();
			newUnits.add(Jimple.v().newAssignStmt(tmp, Jimple.v().newStaticFieldRef(stopField.makeRef())));
			newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(tmp, IntConstant.v(0)), nop));
			newUnits.add(Jimple.v().newAssignStmt(exc, Jimple.v().newNewExpr(stoppedExceptionType)));
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(exc, stoppedExceptionInit)));
			newUnits.add(Jimple.v().newThrowStmt(exc));
			newUnits.add(nop);
		}
	}

	@Override
	public void processPost(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPostExc(Chain<Unit> newUnits, Stmt op, Local exception) { }

	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) { }

	@Override
	public void done(IConfigProject config) { }
}
