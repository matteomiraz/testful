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

import testful.TestFul;
import testful.coverage.CoverageInformation;

/**
 * Contains the coverage of the du-pairs
 * @author matteo
 */
public class CoverageDataFlow implements CoverageInformation {

	private static final long serialVersionUID = 5341830687067491212L;

	public static class DefUse implements Serializable {
		private static final long serialVersionUID = 2768652445227605157L;

		/** the definition being used. If null, it is the default value (e.g., the auto-assigned 0 value for integers) */
		private final ContextualId def;
		/** the use. This must not be null */
		private final ContextualId use;

		private final int hashCode;

		public DefUse(ContextualId def, ContextualId use) {
			if(use == null) {
				NullPointerException e = new NullPointerException("The use cannot be null");
				TestFul.debug(e);
				throw e;
			}

			this.def = def;
			this.use = use;

			hashCode = 31 * ((def == null) ? 0 : def.hashCode()) + use.hashCode();
		}

		public ContextualId getDef() {
			return def;
		}

		public ContextualId getUse() {
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

	public CoverageDataFlow() {
		duPairs = new LinkedHashSet<DefUse>();
	}

	public CoverageDataFlow(Set<DefUse> duPairs) {
		this.duPairs = new LinkedHashSet<DefUse>(duPairs);
	}

	public Set<ContextualId> getDefsOfUse(ContextualId use) {
		Set<ContextualId> ret = new LinkedHashSet<ContextualId>();

		for (DefUse du : duPairs)
			if(du.getUse().equals(use))
				ret.add(du.getDef());

		return ret;
	}

	public Set<Integer> getDefsOfUse(int useId) {
		Set<Integer> ret = new LinkedHashSet<Integer>();

		for (DefUse du : duPairs)
			if(du.getUse().getId() == useId)
				ret.add(du.getDef() == null ? null : du.getDef().getId());

		return ret;
	}

	public Set<ContextualId> getUsesOfDef(ContextualId def) {
		Set<ContextualId> ret = new LinkedHashSet<ContextualId>();

		for (DefUse du : duPairs)
			if((def == null &&  du.getDef() == null) || (def != null && def.equals(du.getDef())))
				ret.add(du.getUse());

		return ret;
	}

	public Set<Integer> getUsesOfDef(Integer defId) {
		Set<Integer> ret = new LinkedHashSet<Integer>();

		for (DefUse du : duPairs)
			if((defId == null &&  du.getDef() == null) || (defId != null && defId == du.getDef().getId()))
				ret.add(du.getUse().getId());

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
		return new CoverageDataFlow();
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
