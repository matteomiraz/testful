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

public abstract class Condition implements Serializable {

	private static final long serialVersionUID = -4617564017748515886L;

	private final int basicBlock;
	private final DataUse use1;
	private final DataUse use2;

	public Condition(int basicBlock, DataUse use1, DataUse use2) {
		this.basicBlock = basicBlock;
		if(use1 == null) {
			this.use1 = use2;
			this.use2 = null;
		} else if(use2 == null) {
			this.use1 = use1;
			this.use2 = null;
		} else {
			this.use1 = use1;
			this.use2 = use2;
		}
	}

	public Condition(int basicBlock, DataUse use) {
		this(basicBlock, use, null);
	}

	/**
	 * Returns the basicBlock the condition belongs to
	 * @return the basicBlock the condition belongs to
	 */
	public int getBasicBlock() {
		return basicBlock;
	}

	/**
	 * Returns the outgoing branches
	 * @return the outgoing branches
	 */
	public abstract int[] getBranches();

	public DataUse getUse1() {
		return use1;
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
