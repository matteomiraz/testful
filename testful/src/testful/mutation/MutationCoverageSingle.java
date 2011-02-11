package testful.mutation;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.BitSet;

import testful.coverage.CoverageInformation;

public class MutationCoverageSingle implements CoverageInformation {

	private static final long serialVersionUID = 4606980316467907320L;

	public static final boolean VERBOSE = false;

	public static final String KEY = "smut";

	@Override
	public String getKey() {
		return KEY;
	}

	public static final String NAME = "single-class mutation score";

	@Override
	public String getName() {
		return NAME;
	}

	/** alive = execAlive U notExecuted */
	private transient BitSet alive = null;

	private BitSet execAlive, killed, notExecuted;

	public MutationCoverageSingle() {
		execAlive = new BitSet();
		notExecuted = new BitSet();
		killed = new BitSet();
	}

	/**
	 * Creates a mutation coverage with an initial alive set (of not-executed
	 * mutants). This method is used to mark as alive mutants not executed by a
	 * test.
	 * 
	 * @param notExecuted the initial alive set
	 */
	public MutationCoverageSingle(BitSet notExecuted) {
		this();
		this.notExecuted.or(notExecuted);
	}

	public MutationCoverageSingle(int max) {
		execAlive = new BitSet(max);
		notExecuted = new BitSet();
		killed = new BitSet(max);
	}

	public MutationCoverageSingle(MutationCoverageSingle clone) {
		execAlive = (BitSet) clone.execAlive.clone();
		killed = (BitSet) clone.killed.clone();
		notExecuted = (BitSet) clone.notExecuted.clone();
	}

	void setKilled(int num) {
		synchronized(killed) {
			killed.set(num);
		}
	}

	void setAlive(int num) {
		alive = null;

		synchronized(execAlive) {
			execAlive.set(num);
		}
	}

	public BitSet getKilled() {
		return killed;
	}

	public int getKilledNum() {
		return killed.cardinality();
	}

	public BitSet getAlive() {
		BitSet tmp = alive;
		if(tmp == null) {
			tmp = (BitSet) execAlive.clone();
			tmp.or(notExecuted);
			alive = tmp;
		}
		return tmp;
	}

	public int getAliveNum() {
		return getAlive().cardinality();
	}

	/** returns the kill ratio */
	@Override
	public float getQuality() {
		int aSize = getAliveNum();
		int kSize = getKilledNum();
		int tot = aSize + kSize;
		return (100.0f * kSize) / tot;
	}

	@Override
	public MutationCoverageSingle createEmpty() {
		return new MutationCoverageSingle();
	}

	@Override
	public void merge(CoverageInformation otherInfo) {
		if(otherInfo instanceof MutationCoverageSingle)
			merge((MutationCoverageSingle) otherInfo);
	}

	private void merge(MutationCoverageSingle other) {
		// new.execalive = (this.execAlive + this.notExecuted +
		//                  other.execAlive + other.notExecuted)
		//               - (this.killed + other.killed + new.notExecuted)
		execAlive.or(other.execAlive);
		execAlive.or(notExecuted);
		execAlive.or(other.notExecuted);

		notExecuted.and(other.notExecuted);
		killed.or(other.killed);

		execAlive.andNot(killed);
		execAlive.andNot(notExecuted);
	}

	@Override
	public boolean contains(CoverageInformation otherInfo) {
		if(otherInfo instanceof MutationCoverageSingle)
			return contains((MutationCoverageSingle) otherInfo);

		return false;
	}

	private boolean contains(MutationCoverageSingle other) {
		if(killed.cardinality() >= other.killed.cardinality()) {

			BitSet tmp = new BitSet(killed.size());
			// just create a copy of other.killed
			tmp.or(other.killed);
			// calculate intersection with this
			tmp.and(killed);

			// this contains other iff the intersection is inclusive
			// i.e. tmp is equals to other.killed
			return tmp.cardinality() == other.killed.cardinality();
		}
		return false;
	}

	public void intersection(MutationCoverageSingle other) {
		execAlive.and(other.execAlive);
		killed.and(other.killed);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		int tot = getAliveNum() + getKilledNum();
		sb.append(String.format("There are %.2f%% (%d/%d) mutants killed", getQuality(), getKilledNum(), tot));

		if(VERBOSE) {
			sb.append("\n      A = alive (not executed)       a = alive (executed)       k = killed      \n");

			for(int i = 1; i <= tot; i++) {
				if(i % 80 == 0) sb.append("\n");

				char c = '?';
				if(notExecuted.get(i)) c = (c == '?' ? 'A' : 'e');
				if(execAlive.get(i)) c = (c == '?' ? 'a' : 'e');
				if(killed.get(i)) c = (c == '?' ? 'k' : 'e');
				sb.append(c);
			}

		}

		return sb.toString();
	}

	@Override
	public MutationCoverageSingle clone() {
		MutationCoverageSingle ret = new MutationCoverageSingle();

		ret.alive.or(alive);
		ret.execAlive.or(execAlive);
		ret.killed.or(killed);
		ret.notExecuted.or(notExecuted);

		return ret;
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(execAlive);
		out.writeObject(killed);
		out.writeObject(notExecuted);
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		execAlive = (BitSet) in.readObject();
		killed = (BitSet) in.readObject();
		notExecuted = (BitSet) in.readObject();
	}
}
