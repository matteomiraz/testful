package testful.coverage.whiteBox;

import java.util.BitSet;

import testful.coverage.TrackerDatum;

public class WhiteBoxData implements TrackerDatum {
	private static final long serialVersionUID = -6727766936940389098L;

	static final String KEY = "testful.coverage.whiteBox.WhiteBoxData";

	private final BitSet blocksCode, blocksContract;
	private final BitSet conditionsCode, conditionsContract;
	
	public WhiteBoxData(BitSet blocksCode, BitSet blocksContract, BitSet conditionsCode, BitSet conditionsContract) {
		this.blocksCode = blocksCode;
		this.blocksContract = blocksContract;
		this.conditionsCode = conditionsCode;
		this.conditionsContract = conditionsContract;
	}

	@Override
	public String getKey() {
		return KEY;
	}
	
	public boolean hasContracts() {
		return !(blocksContract.isEmpty() && conditionsContract.isEmpty());
	}
	
	public BitSet getBlocksCode() {
		return blocksCode;
	}

	public BitSet getBlocksCode(BitSet cov) {
		cov = (BitSet) cov.clone();
		cov.and(blocksCode);
		return cov;
	}

	public BitSet getBlocksContract() {
		return blocksContract;
	}

	public BitSet getBlocksContract(BitSet cov) {
		cov = (BitSet) cov.clone();
		cov.and(blocksContract);
		return cov;
	}

	
	public BitSet getConditionsCode() {
		return conditionsCode;
	}

	public BitSet getConditionsCode(BitSet cov) {
		cov = (BitSet) cov.clone();
		cov.and(conditionsCode);
		return cov;
	}

	public BitSet getConditionsContract() {
		return conditionsContract;
	}
	
	public BitSet getConditionsContract(BitSet cov) {
		cov = (BitSet) cov.clone();
		cov.and(conditionsContract);
		return cov;
	}

	
	@Override
	public WhiteBoxData clone() throws CloneNotSupportedException {
		return (WhiteBoxData) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((blocksCode == null) ? 0 : blocksCode.hashCode());
		result = prime * result + ((blocksContract == null) ? 0 : blocksContract.hashCode());
		result = prime * result + ((conditionsCode == null) ? 0 : conditionsCode.hashCode());
		result = prime * result + ((conditionsContract == null) ? 0 : conditionsContract.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof WhiteBoxData)) return false;
		WhiteBoxData other = (WhiteBoxData) obj;
		if(blocksCode == null) {
			if(other.blocksCode != null) return false;
		} else if(!blocksCode.equals(other.blocksCode)) return false;
		if(blocksContract == null) {
			if(other.blocksContract != null) return false;
		} else if(!blocksContract.equals(other.blocksContract)) return false;
		if(conditionsCode == null) {
			if(other.conditionsCode != null) return false;
		} else if(!conditionsCode.equals(other.conditionsCode)) return false;
		if(conditionsContract == null) {
			if(other.conditionsContract != null) return false;
		} else if(!conditionsContract.equals(other.conditionsContract)) return false;
		return true;
	}
}
