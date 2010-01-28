package testful.mutation;

import soot.Body;
import soot.Local;
import soot.RefLikeType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
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
	private Body body;

	@Override
	public void init(Chain<Unit> newUnits, Body newBody, Body oldBody, boolean classWithContracts, boolean contractMethod) {
		withContracts = classWithContracts;
		body = newBody;
	}

	@Override
	public void processPre(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPost(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPostExc(Chain<Unit> newUnits, Stmt op, Local exception) { }

	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) {
		NopStmt nop = Jimple.v().newNopStmt();

		// if the class does not have contracts, check if has null parameters
		if(!withContracts) {
			int nParams = body.getMethod().getParameterCount();
			for(int i = 0; i < nParams; i++) {
				Type paramType = body.getMethod().getParameterType(i);

				// if param is a reference, insert "if(param == null) goto nop;" (i.e. skip the exception processing)
				if(paramType instanceof RefLikeType)
					newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(body.getParameterLocal(i), NullConstant.v()), nop));

			}
		}

		// call the trackerProcess method
		newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(processException.makeRef(), exc, IntConstant.v(withContracts ? 1 : 0))));

		newUnits.add(nop);

	}

	@Override
	public void done(IConfigProject config, String cutName) { }
}
