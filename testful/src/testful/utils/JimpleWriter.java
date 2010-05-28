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


package testful.utils;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Unit;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.TableSwitchStmt;
import soot.tagkit.StringTag;

/**
 * Utility class: write the jimple code to the screen
 */
public class JimpleWriter extends BodyTransformer {

	public static final JimpleWriter singleton;

	static {
		singleton = new JimpleWriter();
	}

	private JimpleWriter() {}

	@Override
	@SuppressWarnings("rawtypes")
	public void internalTransform(Body body, String phaseName, Map options) {
		final SootMethod method = body.getMethod();

		System.out.println();
		System.out.println("Class: " + method.getDeclaringClass().getName());
		for(SootField f : method.getDeclaringClass().getFields()) {
			if(f.isFinal() && !f.isStatic()) System.out.println(" final " + f.getName());
		}

		System.out.println();
		System.out.println("Method: " + method.getBytecodeSignature());

		System.out.println("--- locals ---");
		for(Local l : body.getLocals())
			System.out.println(" " + l.getName() + " : " + l.getType());

		System.out.println("--- units ---");
		Unit[] units = body.getUnits().toArray(new Unit[0]);
		for(int i = 0; i < units.length; i++) {

			if(units[i].hasTag("StringTag")) {
				String info = ((StringTag) units[i].getTag("StringTag")).getInfo();
				if(info.endsWith("Pre")) System.out.println("\n      " + info);
			}

			System.out.printf("%4d ", i);

			if(units[i] instanceof GotoStmt) System.out.println("goto " + findIndex(units, ((GotoStmt) units[i]).getTarget()));
			else if(units[i] instanceof IfStmt) System.out.println("if " + ((IfStmt) units[i]).getCondition() + " goto " + findIndex(units, ((IfStmt) units[i]).getTarget()));
			else if(units[i] instanceof TableSwitchStmt) {
				TableSwitchStmt sw = (TableSwitchStmt) units[i];
				System.out.println("tswitch(" + sw.getKey() + ")");
				final int lowIndex = sw.getLowIndex();
				for(int t = 0; t <= sw.getHighIndex() - lowIndex; t++)
					System.out.println("      case " + (t + lowIndex) + ": goto " + findIndex(units, sw.getTarget(t)));
				System.out.println("      default: goto " + findIndex(units, sw.getDefaultTarget()));
			} else if(units[i] instanceof LookupSwitchStmt) {
				LookupSwitchStmt sw = (LookupSwitchStmt) units[i];
				System.out.println("lswitch(" + sw.getKey() + ")");
				for(int v = 0; v < sw.getTargetCount(); v++)
					System.out.println("      case " + sw.getLookupValue(v) + ": goto " + findIndex(units, sw.getTarget(v)));
				System.out.println("      default: goto " + findIndex(units, sw.getDefaultTarget()));
			} else System.out.println(units[i].toString()); // + " " + units[i].getClass().getCanonicalName());

			if(units[i].hasTag("StringTag")) {
				String info = ((StringTag) units[i].getTag("StringTag")).getInfo();

				if(!info.endsWith("Pre")) System.out.println("      " + info);
				if(info.endsWith("After")) System.out.println();
			}
		}

		System.out.println("--- traps ---");
		for(Trap t : body.getTraps())
			System.out.println(" " + findIndex(units, t.getBeginUnit()) + "-" + findIndex(units, t.getEndUnit()) + " -> (" + t.getException() + ") -> " + findIndex(units, t.getHandlerUnit()));

		System.out.println();
	}

	private int findIndex(Unit[] units, Unit target) {
		for(int i = 0; i < units.length; i++)
			if(units[i] == target) return i;

		return -1;
	}
}
