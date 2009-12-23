package testful.coverage;

import java.util.BitSet;

public abstract class BitSetCoverage implements CoverageInformation {
	private static final long serialVersionUID = 3484091845044514922L;

	protected final BitSet coverage;
	
	protected BitSetCoverage() {
		this.coverage = new BitSet();
	}
	
	protected BitSetCoverage(BitSet coverage) {
		this.coverage = coverage;
	}

	@Override
	public float getQuality() {
		return coverage.cardinality();
	}
	
	@Override
	public boolean contains(CoverageInformation other) {
		if(!other.getKey().equals(this.getKey())) return false;

		BitSet bs = ((BitSetCoverage)other).coverage;
		
		if(bs.cardinality() > coverage.cardinality()) return false;
		
		for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i+1))
	     if(!coverage.get(i)) return false;
		
		return true;
	}

	@Override
	public void merge(CoverageInformation other) {
		if(other.getKey().equals(this.getKey())) 
			this.coverage.or(((BitSetCoverage) other).coverage);
	}
	
	public BitSet getCoverage() {
		return (BitSet) coverage.clone();
	}
	
	@Override
	public abstract BitSetCoverage clone() throws CloneNotSupportedException;
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = coverage.nextSetBit(0); i >= 0; i = coverage.nextSetBit(i+1))
			sb.append(i).append("\n");
		
		return sb.toString();
	}
}
