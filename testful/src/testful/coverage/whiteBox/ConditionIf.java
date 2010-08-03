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

public class ConditionIf extends Condition {

	private static final long serialVersionUID = -439884149725011654L;

	public enum ConditionType {
		LT, LE, EQ, NE, GE, GT
	}

	private final ConditionType condType;

	private EdgeConditional trueBranch;
	private EdgeConditional falseBranch;

	private transient String condition;

	public ConditionIf(String condition, int basicBlock, DataType dataType, Value v1, DataUse use1, ConditionType condType, Value v2, DataUse use2) {
		super(basicBlock, dataType, v1, use1, v2, use2);

		this.condType = condType;

		this.condition = condition;
	}

	/**
	 * @return the condType
	 */
	public ConditionType getConditionType() {
		return condType;
	}

	void setTrueBranch(EdgeConditional trueBranch) {
		this.trueBranch = trueBranch;
	}

	public EdgeConditional getTrueBranch() {
		return trueBranch;
	}

	void setFalseBranch(EdgeConditional falseBranch) {
		this.falseBranch = falseBranch;
	}

	public EdgeConditional getFalseBranch() {
		return falseBranch;
	}

	private transient int[] branches;
	@Override
	public int[] getBranches() {
		if(branches == null)
			branches = new int[] { trueBranch.getId(), falseBranch.getId() };
		return branches;
	}

	/** Avaliable only on "fresh" analysis (transient) */
	String getCondition() {
		return condition;
	}
}
