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
