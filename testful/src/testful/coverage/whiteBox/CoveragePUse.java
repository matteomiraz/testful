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

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import testful.coverage.CoverageInformation;

/**
 * Contains the coverage of the P-Uses (Predicate Uses).
 * @author matteo
 */
public class CoveragePUse implements CoverageInformation {
	private static final long serialVersionUID = 344555259191283447L;

	public static class PUse implements Serializable {
		private static final long serialVersionUID = 883324275937140071L;

		/** the definition being used. If null, it is the default value (e.g., the auto-assigned 0 value for integers) */
		private final ContextualId def;

		private final int branchId;

		private final int hashCode;

		public PUse(int branchId, ContextualId def) {
			this.branchId = branchId;
			this.def = def;

			hashCode = 521 * ((def == null) ? 0 : def.hashCode()) + branchId;
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;

			if (obj == null) return false;

			if(!(obj instanceof PUse)) return false;

			PUse other = (PUse) obj;
			if (branchId != other.branchId) return false;
			if (def == null) {
				if (other.def != null)
					return false;
			} else if (!def.equals(other.def))
				return false;
			return true;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return branchId + "(" + (def==null?"default[]":def) + ")";
		}
	}

	public static String KEY = "pu";
	public static String NAME = "P-Uses";

	/** key: branchId, Value: definitions */
	private final Set<PUse> coverage;

	public CoveragePUse() {
		coverage = new LinkedHashSet<PUse>();
	}

	public CoveragePUse(Set<PUse> cov) {
		coverage = new LinkedHashSet<PUse>(cov);
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(other instanceof CoveragePUse) {
			final CoveragePUse coverageDataFlow = (CoveragePUse) other;

			if(!coverage.containsAll(coverageDataFlow.coverage)) return false;

			return true;
		}
		return false;
	}

	@Override
	public CoverageInformation createEmpty() {
		return new CoveragePUse();
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
		return coverage.size();
	}

	@Override
	public void merge(CoverageInformation other) {
		if(other instanceof CoveragePUse) {
			CoveragePUse coverageDataFlow = (CoveragePUse) other;

			coverage.addAll(coverageDataFlow.coverage);
		}
	}

	@Override
	public CoveragePUse clone() {
		return new CoveragePUse(coverage);
	}

	@Override
	public String toString() {
		String[] duStrings = new String[coverage.size()];

		int i = 0;
		for(PUse du : coverage)
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
