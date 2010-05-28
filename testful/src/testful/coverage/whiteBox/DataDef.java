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

import java.io.Serializable;
import java.util.BitSet;
import java.util.Set;

public class DataDef implements Serializable {

	private static final long serialVersionUID = -7674828835545817305L;

	private final int id;
	private static int idGenerator = 1;

	private final Data data;
	private final Block block;

	private BitSet killBitSet;

	/** if the assignment is to a static value, this is the value */
	private final Serializable value;

	public DataDef(Block block, Data data, Serializable value) {
		id = idGenerator++;
		this.block = block;
		
		this.data = data;
		this.value = value;
		data.addDef(this);
		Factory.singleton.updateFieldsMask(this);
	}

	public int getId() {
		return id;
	}

	public Data getData() {
		return data;
	}

	public boolean isField() {
		return data.isField();
	}

	public Block getBlock() {
		return block;
	}
	
	public Serializable getValue() {
		return value;
	}

	public static BitSet toGensBitSet(Set<DataDef> gens) {
		BitSet ret = new BitSet();

		for(DataDef g : gens)
			ret.set(g.id);

		return ret;
	}

	public BitSet toKillsBitSet() {
		if(killBitSet == null) {
			killBitSet = new BitSet();
			killBitSet.or(data.getMask());
			killBitSet.set(id, false);
		}
		return (BitSet) killBitSet.clone();
	}

	@Override
	public String toString() {
		return data.toString() + ":" + id;
	}
}
