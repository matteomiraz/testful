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

	private static final long serialVersionUID = -3003316502705186978L;
	private EdgeConditional trueBranch;
	private EdgeConditional falseBranch;
	
	private transient String condition;

	public ConditionIf(DataUse use1, DataUse use2, String condition) {
		super(use1, use2);
		this.condition = condition;
	}

	public ConditionIf(DataUse use) {
		super(use);
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

	/** Avaliable only on "fresh" analysis (transient) */
	String getCondition() {
		return condition;
	}
}
