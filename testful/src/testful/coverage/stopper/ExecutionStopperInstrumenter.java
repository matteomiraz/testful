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

package testful.coverage.stopper;

import java.util.ArrayList;
import java.util.List;

import soot.Body;
import soot.Local;
import soot.Scene;
import soot.SootClass;
import soot.SootMethodRef;
import soot.Unit;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
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
public class ExecutionStopperInstrumenter implements UnifiedInstrumentator {

	public static final ExecutionStopperInstrumenter singleton = new ExecutionStopperInstrumenter();

	private static final SootMethodRef checkRef;
	static {
		Scene.v().loadClassAndSupport(TestStoppedException.class.getCanonicalName());
		SootClass stoppedException = Scene.v().getSootClass(TestStoppedException.class.getCanonicalName());
		checkRef = stoppedException.getMethodByName("check").makeRef();
	}

	private List<Unit> previous;

	@Override
	public void preprocess(SootClass sClass) { }

	/* (non-Javadoc)
	 * @see testful.coverage.soot.Instrumenter.UnifiedInstrumentator#init(soot.Body, soot.Body, soot.util.Chain)
	 */
	@Override
	public void init(Body oldBody, Body newBody, Chain<Unit> newUnits) {
		previous = new ArrayList<Unit>();
	}

	@Override
	public void processPre(Chain<Unit> newUnits, Stmt op) {
		if(previous == null) return;

		if(op.hasTag(Skip.NAME)) return;

		previous.add(op);

		Unit target = null;
		if(op instanceof GotoStmt) target = ((GotoStmt) op).getTarget();
		else if(op instanceof IfStmt) target = ((IfStmt) op).getTarget();

		if(op.containsInvokeExpr() || (target != null && previous.contains(target)))
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(checkRef)));
	}

	@Override
	public void processPost(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) { }

	@Override
	public void done(IConfigProject config) { }
}