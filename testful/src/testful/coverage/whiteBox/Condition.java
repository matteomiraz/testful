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

import testful.TestFul;

public abstract class Condition implements Serializable {

	public enum Type {
		Boolean, Character, Number, String, Array, Reference
	}

	private final int basicBlock;

	private final Type type;

	private final Value v1;
	private final DataUse use1;

	private final Value v2;
	private final DataUse use2;

	public Condition(int basicBlock, Type dataType, Value v1, DataUse use1, Value v2, DataUse use2) {
		this.basicBlock = basicBlock;
		type = dataType;
		if(use1 != null || use2 == null) {
			this.v1 = v1;
			this.use1 = use1;
			this.v2 = v2;
			this.use2 = use2;
		} else {
			this.v1 = v2;
			this.use1 = use2;
			this.v2 = v1;
			this.use2 = use1;
		}

		if(TestFul.DEBUG) {
			if(type == null) TestFul.debug("Null type in " + basicBlock);
		}
	}

	public Condition(int basicBlock, Type dataType, Value v, DataUse use) {
		this(basicBlock, dataType, v, use, null, null);
	}

	/**
	 * Returns the basicBlock the condition belongs to
	 * @return the basicBlock the condition belongs to
	 */
	public int getBasicBlock() {
		return basicBlock;
	}

	/**
	 * @return the type of the values involved in the comparison
	 */
	public Type getType() {
		return type;
	}

	/**
	 * Returns the outgoing branches
	 * @return the outgoing branches
	 */
	public abstract int[] getBranches();

	/**
	 * @return the 1st value
	 */
	public Value getV1() {
		return v1;
	}

	public DataUse getUse1() {
		return use1;
	}

	/**
	 * @return the 2nd value
	 */
	public Value getV2() {
		return v2;
	}

	public DataUse getUse2() {
		return use2;
	}

	@Override
	public final int hashCode() {
		return basicBlock;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;

		if(!(obj instanceof Condition)) return false;
		return basicBlock == ((Condition)obj).basicBlock;
	}

}
