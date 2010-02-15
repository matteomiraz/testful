package testful.mutation;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.Stmt;
import soot.util.Chain;
import testful.IConfigProject;
import testful.utils.Instrumenter.UnifiedInstrumentator;

/**
 * This class instruments bytecode to detect failures, using the method
 * Utils.processException
 * 
 * @author matteo
 */
public class BugFinder implements UnifiedInstrumentator {

	public static final BugFinder singleton = new BugFinder();

	private static final SootMethod processException;

	static {
		Scene.v().loadClassAndSupport(org.jmlspecs.jmlrac.runtime.JMLCheckable.class.getCanonicalName());

		Scene.v().loadClassAndSupport(Utils.class.getCanonicalName());
		SootClass utilsClass = Scene.v().getSootClass(Utils.class.getCanonicalName());
		processException = utilsClass.getMethodByName("processException");
	}

	private BugFinder() { }

	@Override
	public void preprocess(SootClass sClass) { }

	private boolean withContracts;

	@Override
	public void init(Chain<Unit> newUnits, Body newBody, Body oldBody, boolean classWithContracts, boolean contractMethod) {
		withContracts = classWithContracts;
	}

	@Override
	public void processPre(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPost(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPostExc(Chain<Unit> newUnits, Stmt op, Local exception) { }

	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) {
		// if the class has contracts, call the processException method
		if(withContracts)
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(processException.makeRef(), exc, IntConstant.v(withContracts ? 1 : 0))));
	}

	@Override
	public void done(IConfigProject config, String cutName) { }
}
