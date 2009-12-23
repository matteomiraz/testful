package testful.coverage.whiteBox;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

import testful.coverage.CoverageInformation;

public class CoverageDataFlow implements CoverageInformation {

	private static final long serialVersionUID = 5341830687067491212L;

	public static class DefUse implements Serializable {
		private static final long serialVersionUID = 2768652445227605157L;

		private final DataAccess def, use;

		public DefUse(DataAccess def, DataAccess use) {
			this.def = def;
			this.use = use;
		}
		
		public DataAccess getDef() {
			return def;
		}
		
		public DataAccess getUse() {
			return use;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((def == null) ? 0 : def.hashCode());
			result = prime * result + ((use == null) ? 0 : use.hashCode());
			return result;
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
			if(use == null) {
				if(other.use != null) return false;
			} else if(!use.equals(other.use)) return false;
			
			return true;
		}
		
		@Override
		public String toString() {
			return def + "-"+ use;
		}
	}
	
	public static String KEY = "du";
	public static String NAME = "Def-Use Pairs";

	private final Set<DefUse> duPairs;
	
	public CoverageDataFlow(Set<DefUse> duPairs) {
		this.duPairs = new LinkedHashSet<DefUse>(duPairs);
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(other instanceof CoverageDataFlow) {
			final CoverageDataFlow coverageDataFlow = (CoverageDataFlow) other;
			return duPairs.containsAll(coverageDataFlow.duPairs);
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
		StringBuilder sb = new StringBuilder();
		
		for(DefUse du : duPairs)
			sb.append(du).append("\n");
		
		return sb.toString();
	}

}
