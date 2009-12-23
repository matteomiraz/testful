package testful.mutation;

import testful.coverage.TrackerDatum;

public class MutationExecutionData implements TrackerDatum {
	private static final long serialVersionUID = 8054246654281871986L;

	static final String KEY = "mutationData";
	
	final String className;
	final int mutation;
	final long maxExecutionTime;

	public MutationExecutionData(String className, int mutation, long maxExecutionTime) {
		this.className = className;
		this.mutation = mutation;
		this.maxExecutionTime = maxExecutionTime;
	}

	@Override
	public String getKey() {
		return MutationExecutionData.KEY;
	}

	@Override
	public MutationExecutionData clone() throws CloneNotSupportedException {
		return (MutationExecutionData) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((className == null) ? 0 : className.hashCode());
		result = prime * result + (int) (maxExecutionTime ^ (maxExecutionTime >>> 32));
		result = prime * result + mutation;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof MutationExecutionData)) return false;
		MutationExecutionData other = (MutationExecutionData) obj;
		if(className == null) {
			if(other.className != null) return false;
		} else if(!className.equals(other.className)) return false;
		if(maxExecutionTime != other.maxExecutionTime) return false;
		if(mutation != other.mutation) return false;
		return true;
	}
}
