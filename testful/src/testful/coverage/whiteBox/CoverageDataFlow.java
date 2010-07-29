package testful.coverage.whiteBox;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import testful.TestFul;
import testful.coverage.CoverageInformation;

public class CoverageDataFlow implements CoverageInformation {

	private static final long serialVersionUID = 5341830687067491212L;

	public static class DefUse implements Serializable {
		private static final long serialVersionUID = 2768652445227605157L;

		/** the definition being used. If null, it is the default value (e.g., the auto-assigned 0 value for integers */
		private final DataAccess def;
		/** the use. This must not be null */
		private final DataAccess use;

		private final int hashCode;

		public DefUse(DataAccess def, DataAccess use) {
			if(use == null) {
				NullPointerException e = new NullPointerException("The use cannot be null");
				TestFul.debug(e);
				throw e;
			}

			this.def = def;
			this.use = use;

			hashCode = 31 * ((def == null) ? 0 : def.hashCode()) + use.hashCode();
		}

		public DataAccess getDef() {
			return def;
		}

		public DataAccess getUse() {
			return use;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null) return false;

			if(!(obj instanceof DefUse)) return false;
			DefUse other = (DefUse) obj;

			if(def == null) {
				if(other.def != null) return false;
			} else if(!def.equals(other.def)) return false;

			if(!use.equals(other.use)) return false;

			return true;
		}

		@Override
		public String toString() {
			return (def==null?"default[]":def) + "-"+ use;
		}
	}

	public static String KEY = "du";
	public static String NAME = "Def-Use Pairs";

	private final Set<DefUse> duPairs;

	public CoverageDataFlow(Set<DefUse> duPairs) {
		this.duPairs = new LinkedHashSet<DefUse>(duPairs);
	}

	public Set<DataAccess> getDefsByUse(int useId) {
		final Set<DataAccess> ret = new LinkedHashSet<DataAccess>();

		for (DefUse du : duPairs)
			if(du.getUse().getId() == useId)
				ret.add(du.getDef());

		return ret;
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(other instanceof CoverageDataFlow) {
			final CoverageDataFlow coverageDataFlow = (CoverageDataFlow) other;

			if(!duPairs.containsAll(coverageDataFlow.duPairs)) return false;

			return true;
		}
		return false;
	}

	@Override
	public CoverageInformation createEmpty() {
		return new CoverageDataFlow(new LinkedHashSet<DefUse>());
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public float getQuality() {
		return duPairs.size();
	}

	@Override
	public void merge(CoverageInformation other) {
		if(other instanceof CoverageDataFlow) {
			CoverageDataFlow coverageDataFlow = (CoverageDataFlow) other;

			duPairs.addAll(coverageDataFlow.duPairs);
		}
	}

	@Override
	public CoverageDataFlow clone() {
		return new CoverageDataFlow(duPairs);
	}

	@Override
	public String toString() {
		String[] duStrings = new String[duPairs.size()];

		int i = 0;
		for(DefUse du : duPairs)
			duStrings[i++] = du.toString();

		Arrays.sort(duStrings);

		StringBuilder sb = new StringBuilder();
		for (i = 0; i < duStrings.length; i++) {
			if(i > 0) sb.append("\n");
			sb.append(duStrings[i]);
		}

		return sb.toString();
	}

}
