/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.coverage.fault;

import java.util.logging.Logger;

import soot.Body;
import soot.BooleanType;
import soot.Local;
import soot.RefLikeType;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.Stmt;
import soot.util.Chain;
import testful.IConfigProject;
import testful.coverage.soot.Instrumenter.UnifiedInstrumentator;
import testful.model.faults.FaultyExecutionException;

/**
 * Instruments each method, in the following way:<br>
 * <br>
 * <b>try { </b> <br>
 * <i>originalMethod</i> <b><br>
 * catch (Throwable __bug_exc__) { <br>
 * if(withoutContracts && param == null) throw __bug_exc__; <br>
 * BugTracker.getTracker().process(__bug_exc__, hasContracts); <br>
 * throw __bug_exc__; <br>
 * </b>
 * 
 * @author matteo
 */
public class FaultInstrumenter implements UnifiedInstrumentator {

	private static final Logger logger = Logger.getLogger("testful.coverage.instrumenter.bug");

	public static final FaultInstrumenter singleton = new FaultInstrumenter();

	private final SootClass trackerClass;
	private final SootMethod trackerProcess;

	private final SootClass faultyException;
	private final SootClass nullPointerException;

	private FaultInstrumenter() {
		logger.config("Bug instrumenter loaded");

		final String COVERAGE_TRACKER = FaultTracker.class.getCanonicalName();
		Scene.v().loadClassAndSupport(COVERAGE_TRACKER);
		trackerClass = Scene.v().getSootClass(COVERAGE_TRACKER);
		trackerProcess = trackerClass.getMethodByName("process");

		final String FAULTY_EXCEPTION = FaultyExecutionException.class.getCanonicalName();
		Scene.v().loadClassAndSupport(FAULTY_EXCEPTION);
		faultyException = Scene.v().getSootClass(FAULTY_EXCEPTION);

		final String NULL_POINTER_EXCEPTION = NullPointerException.class.getCanonicalName();
		Scene.v().loadClassAndSupport(NULL_POINTER_EXCEPTION);
		nullPointerException = Scene.v().getSootClass(NULL_POINTER_EXCEPTION);
	}

	@Override
	public void preprocess(SootClass sClass) { }

	private Body body;
	private Local boolTmp;

	@Override
	public void init(Body oldBody, Body newBody, Chain<Unit> newUnits) {
		logger.finer(" processing " + newBody.getMethod().getName());
		body = newBody;
		boolTmp = null;
	}

	@Override
	public void processPre(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPost(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPostExc(Chain<Unit> newUnits, Stmt op, Local exception) { }

	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) {

		if(boolTmp == null) {
			boolTmp = Jimple.v().newLocal("__fault_tracker__", BooleanType.v());
			body.getLocals().add(boolTmp);
		}

		// Process fault: jump here if the exception is a fault!
		final Unit processFault = Jimple.v().newNopStmt();

		// The last operation: jump here if the exception is not a fault!
		final Unit notAbug = Jimple.v().newNopStmt();

		// if the exception has the fault marker, it is a bug!
		newUnits.add(Jimple.v().newAssignStmt(boolTmp, Jimple.v().newInstanceOfExpr(exc, faultyException.getType())));
		newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(boolTmp, IntConstant.v(1)), processFault));

		// if the exception has been declared as thrown, it is not a bug!
		for (SootClass declaredException : body.getMethod().getExceptions()) {
			newUnits.add(Jimple.v().newAssignStmt(boolTmp, Jimple.v().newInstanceOfExpr(exc, declaredException.getType())));
			newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(boolTmp, IntConstant.v(1)), notAbug));
		}

		// Last chance: if is a NullPointerException, check if a parameter is null, otherwise process the fault
		newUnits.add(Jimple.v().newAssignStmt(boolTmp, Jimple.v().newInstanceOfExpr(exc, nullPointerException.getType())));
		newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(boolTmp, IntConstant.v(0)), processFault));

		// check if one of the parameters is null
		int nParams = body.getMethod().getParameterCount();
		for(int i = 0; i < nParams; i++) {
			// if param is a reference, insert "if(param == null) goto :notAbug"
			if(body.getMethod().getParameterType(i) instanceof RefLikeType) {
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(body.getParameterLocal(i), NullConstant.v()), notAbug));
			}
		}
		newUnits.add(Jimple.v().newGotoStmt(processFault));


		// process the fault
		newUnits.add(processFault);
		newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(trackerProcess.makeRef(), exc)));

		// final nop
		newUnits.add(notAbug);
	}

	@Override
	public void done(IConfigProject config, String cutName) {
	}
}
