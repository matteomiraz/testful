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


package testful.coverage.bug;

import java.util.LinkedHashSet;
import java.util.Set;

import testful.coverage.CoverageInformation;

public class BugCoverage implements CoverageInformation {

	private static final long serialVersionUID = -7544654531090656602L;

	public static final String KEY = "bug";
	public static final String NAME = "bug discovered";

	public static BugCoverage getEmpty() {
		return new BugCoverage();
	}

	/** only BugTracker can access directly this field! */
	public final Set<Bug> bugs;

	private BugCoverage() {
		bugs = new LinkedHashSet<Bug>();
	}

	@Override
	public float getQuality() {
		return bugs.size();
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(!(other instanceof BugCoverage)) return false;

		return bugs.containsAll(((BugCoverage) other).bugs);
	}

	@Override
	public void merge(CoverageInformation other) {
		if(other instanceof BugCoverage) bugs.addAll(((BugCoverage) other).bugs);
	}

	@Override
	public CoverageInformation createEmpty() {
		return new BugCoverage();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for(Bug exc : bugs) {
			sb.append("--- Exception: ").append(exc.getFaultyExecutionException().getClass().getCanonicalName()).append(
					" -> " + exc.getFaultyExecutionException().getCause().getClass().getCanonicalName() + " ---\n");
			for(StackTraceElement ste : exc.getStackTrace())
				sb.append("  ").append(ste.toString()).append("\n");
		}

		return sb.toString();
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
	public BugCoverage clone() {
		BugCoverage ret = new BugCoverage();
		for(Bug b : bugs)
			ret.bugs.add(b);
		
		return ret;
	}
}
