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
import java.util.HashSet;

/**
 * Represents the (only) exit point of a function. 
 * 
 * @author matteo
 */
public class BlockFunctionExit extends Block {

	private static final long serialVersionUID = -6797021434840618557L;
	private final String fullQualifiedName;
	private final BlockClass clazz;

	private final BitSet fieldMask;

	public BlockFunctionExit(String fullQualifiedName, BlockClass clazz) {
		this.fullQualifiedName = fullQualifiedName;
		this.clazz = clazz;

		pre = new HashSet<Edge>();
		post = new HashSet<Edge>();
		fieldMask = Factory.singleton.getFieldsMask();
	}

	public BlockClass getClazz() {
		return clazz;
	}

	public String getFullQualifiedName() {
		return fullQualifiedName;
	}

	@Override
	public boolean updateData() {
		BitSet oldIn = in;
		out = in = new BitSet();
		for(Edge e : pre)
			in.or(e.getFrom().out);

		// KILL all non-field defs
		in.and(fieldMask);

		oldIn.xor(in);
		return !oldIn.isEmpty();
	}
}
