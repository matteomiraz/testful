package testful.mutation;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.IdentityUnit;
import soot.Local;
import soot.RefLikeType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.ThrowStmt;

/**
 * This class instruments bytecode to detect failures, using the method
 * Utils.processException
 * 
 * @author matteo
 */
public class BugFinder extends BodyTransformer {

	public static final BugFinder singleton = new BugFinder();

	private final SootClass throwableClass;
	private final SootMethod processException;

	private BugFinder() {
		Scene.v().loadClassAndSupport(org.jmlspecs.jmlrac.runtime.JMLCheckable.class.getCanonicalName());

		Scene.v().loadClassAndSupport(Throwable.class.getCanonicalName());
		throwableClass = Scene.v().getSootClass(Throwable.class.getCanonicalName());

		Scene.v().loadClassAndSupport(Utils.class.getCanonicalName());
		SootClass utilsClass = Scene.v().getSootClass(Utils.class.getCanonicalName());
		processException = utilsClass.getMethodByName("processException");
	}

	@Override
	@SuppressWarnings("rawtypes")
	protected void internalTransform(Body body, String phaseName, Map options) {
		SootClass sClass = body.getMethod().getDeclaringClass();

		// skip non-public methods!!!
		if(!body.getMethod().isPublic()) {
			System.out.println("Bug-tracking disabled on " + sClass.getName() + "::" + body.getMethod().getName() + " - method not public");
			return;
		}

		// skip JML-related methods
		boolean withContracts = sClass.implementsInterface(org.jmlspecs.jmlrac.runtime.JMLCheckable.class.getCanonicalName());
		if(withContracts && body.getMethod().getName().contains("$")) {
			System.out.println("Bug-tracking disabled on " + sClass.getName() + "::" + body.getMethod().getName() + " - JML-related method");
			return;
		}

		System.out.println("Bug-tracking of " + sClass.getName() + "::" + body.getMethod().getName());

		// saves the exception in a local variable
		Local exc = Jimple.v().newLocal("__bug_exc__", throwableClass.getType());
		body.getLocals().add(exc);

		Unit handler = Jimple.v().newIdentityStmt(exc, Jimple.v().newCaughtExceptionRef());
		body.getUnits().addLast(handler);

		// update the traps
		body.getTraps().add(Jimple.v().newTrap(throwableClass, firstOperation(body), body.getUnits().getLast(), handler));

		// The last istruction of the handler is "throw exc"
		ThrowStmt finalThrow = Jimple.v().newThrowStmt(exc);

		// if the class does not have contracts, check if has null parameters
		if(!withContracts) {
			int nParams = body.getMethod().getParameterCount();
			for(int i = 0; i < nParams; i++) {
				Type paramType = body.getMethod().getParameterType(i);

				// if param is a reference, insert "if(param == null) throw exc;
				if(paramType instanceof RefLikeType) body.getUnits().addLast(Jimple.v().newIfStmt(Jimple.v().newEqExpr(body.getParameterLocal(i), NullConstant.v()), finalThrow));

			}
		}

		// call the trackerProcess metrhod
		body.getUnits().addLast(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(processException.makeRef(), exc, IntConstant.v(withContracts ? 1 : 0))));

		// useless: rethrow the exception
		body.getUnits().addLast(finalThrow);
	}

	private Unit firstOperation(Body body) {

		for(Unit unit : body.getUnits())
			if(!(unit instanceof IdentityUnit)) return unit;

		throw new Error("ERROR: cannot locate the first valid unit");
	}
}
