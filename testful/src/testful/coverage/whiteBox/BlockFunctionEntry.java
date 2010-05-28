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
 * Represents the entry point of a function. 
 * 
 * @author matteo
 */
public class BlockFunctionEntry extends Block {

	private static final long serialVersionUID = -3816067359592430951L;
	
	private final BlockClass clazz;
	private final String fullQualifiedName;
	private final boolean isPublic;
	private final boolean contract;

	public BlockFunctionEntry(BlockClass clazz, String fullQualifiedName, boolean isPublic, boolean contract) {
		this.fullQualifiedName = fullQualifiedName;
		this.clazz = clazz;
		this.isPublic = isPublic;
		this.contract = contract;

		pre = new HashSet<Edge>();
		post = new HashSet<Edge>();

		clazz.addMethod(this);
	}

	public String getFullQualifiedName() {
		return fullQualifiedName;
	}

	public BlockClass getClazz() {
		return clazz;
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	
	public boolean isContract() {
		return contract;
	}
	
	@Override
	public boolean updateData() {
		BitSet oldIn = in;
		out = in = new BitSet();
		for(Edge e : pre)
			in.or(e.getFrom().out);

		oldIn.xor(in);
		return !oldIn.isEmpty();
	}

}
