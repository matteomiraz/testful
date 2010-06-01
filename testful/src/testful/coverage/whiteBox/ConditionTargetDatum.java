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

import testful.coverage.TrackerDatum;


public class ConditionTargetDatum implements TrackerDatum {
	private static final long serialVersionUID = -6897105441337044078L;
	
	static final String KEY = "testful.coverage.whiteBox.ConditionTargetDatum";

	@Override
	public String getKey() {
		return KEY;
	}

	private final int branchId;
	
	public ConditionTargetDatum(int branchId) {
		this.branchId = branchId;
	}
	
	public int getBranchId() {
		return branchId;
	}

	@Override
	public ConditionTargetDatum clone() {
		return this;
	}
	
	@Override
	public String toString() {
		return "Branch: " + branchId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + branchId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof ConditionTargetDatum)) return false;
		ConditionTargetDatum other = (ConditionTargetDatum) obj;
		if(branchId != other.branchId) return false;
		return true;
	}
}
