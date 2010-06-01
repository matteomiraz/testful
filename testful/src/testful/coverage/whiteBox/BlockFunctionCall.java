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


package testful.coverage.whiteBox;

import java.util.BitSet;
import java.util.Set;

public class BlockFunctionCall extends BlockBasic {

	private static final long serialVersionUID = 7644484386896369190L;
	private final boolean isStatic;
	private final String className;
	private final String methodName;

	public BlockFunctionCall(Set<DataDef> defs, Set<DataUse> uses, String methodName, String className, boolean isStatic) {
		super(defs, uses);
		this.isStatic = isStatic;
		this.className = className;
		this.methodName = methodName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public boolean isStatic() {
		return isStatic;
	}
	
	public void updateGenKills(BitSet def) {
		defs.addAll(defs);
	}

	@Override
	public String toString() {
		return super.toString() + " " + (isStatic ? "static " : "") + className + "." + methodName;
	}

}
