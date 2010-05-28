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


package testful.coverage.bug;

import java.util.logging.Logger;

import soot.Body;
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
import soot.tagkit.StringTag;
import soot.util.Chain;
import testful.IConfigProject;
import testful.utils.Instrumenter.UnifiedInstrumentator;

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
public class BugInstrumenter implements UnifiedInstrumentator {

	private static final Logger logger = Logger.getLogger("testful.coverage.instrumenter.bug");

	public static final BugInstrumenter singleton = new BugInstrumenter();

	/** this local contains a copy of the tracker */
	private static final String LOCAL_TRACKER = "__testful_bugs_tracker__";

	private final String COVERAGE_TRACKER;
	private final SootClass trackerClass;
	private final SootMethod trackerSingleton;
	private final SootMethod trackerProcess;

	private BugInstrumenter() {
		logger.config("Bug instrumenter loaded");

		COVERAGE_TRACKER = BugTracker.class.getCanonicalName();
		Scene.v().loadClassAndSupport(COVERAGE_TRACKER);
		trackerClass = Scene.v().getSootClass(COVERAGE_TRACKER);
		trackerSingleton = trackerClass.getMethodByName("getTracker");
		trackerProcess = trackerClass.getMethodByName("process");
	}

	private boolean toSkip;
	private boolean classWithContracts;
	private Body body;
	private Local localTracker;

	@Override
	public void preprocess(SootClass sClass) { }

	@Override
	public void init(Chain<Unit> newUnits, Body newBody, Body oldBody, boolean classWithContracts, boolean contractMethod) {
		logger.finer(" processing " + newBody.getMethod().getName());

		// skip non-public methods!!!
		if(!newBody.getMethod().isPublic()) toSkip = true;

		// skip JML-related methods
		if(classWithContracts && newBody.getMethod().getName().contains("$"))
			toSkip = true;

		if(toSkip) return;

		this.classWithContracts = classWithContracts;
		body = newBody;

		// insert the local tracker variable
		localTracker = Jimple.v().newLocal(LOCAL_TRACKER, trackerClass.getType());
		body.getLocals().add(localTracker);
		body.getUnits().addLast(Jimple.v().newAssignStmt(localTracker, Jimple.v().newStaticInvokeExpr(trackerSingleton.makeRef())));
	}

	@Override
	public void processPre(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPost(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPostExc(Chain<Unit> newUnits, Stmt op, Local exception) { }


	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) {
		if(toSkip) return;

		final Unit end = Jimple.v().newNopStmt();
		end.addTag(new StringTag("bugTracker:end"));

		// if the class does not have contracts, check if has null parameters
		if(!classWithContracts) {
			int nParams = body.getMethod().getParameterCount();
			for(int i = 0; i < nParams; i++) {
				// if param is a reference, insert "if(param == null) throw exc;
				if(body.getMethod().getParameterType(i) instanceof RefLikeType)
					newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(body.getParameterLocal(i), NullConstant.v()), end));
			}
		}

		// call the trackerProcess metrhod
		body.getUnits().addLast(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackerProcess.makeRef(), exc, IntConstant.v(classWithContracts ? 1 : 0))));

		newUnits.add(end);
	}

	@Override
	public void done(IConfigProject config, String cutName) {
	}
}
