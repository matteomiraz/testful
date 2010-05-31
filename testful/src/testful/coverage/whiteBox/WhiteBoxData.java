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

import testful.coverage.TrackerDatum;

public class WhiteBoxData implements TrackerDatum {
	private static final long serialVersionUID = -6727766936940389098L;

	static final String KEY = "testful.coverage.whiteBox.WhiteBoxData";

	private final BitSet blocks;
	private final BitSet branches;

	public WhiteBoxData(BitSet blocks, BitSet branches) {
		this.blocks = blocks;
		this.branches = branches;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	public BitSet getBlocks() {
		return blocks;
	}

	public BitSet getBranches() {
		return branches;
	}

	@Override
	public WhiteBoxData clone() throws CloneNotSupportedException {
		return (WhiteBoxData) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blocks == null) ? 0 : blocks.hashCode());
		result = prime * result + ((branches == null) ? 0 : branches.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof WhiteBoxData)) return false;
		WhiteBoxData other = (WhiteBoxData) obj;
		if(blocks == null) {
			if(other.blocks != null) return false;
		} else if(!blocks.equals(other.blocks)) return false;
		if(branches == null) {
			if(other.branches != null) return false;
		} else if(!branches.equals(other.branches)) return false;
		return true;
	}
}
