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

package testful.coverage.fault;

import java.util.LinkedHashSet;
import java.util.Set;

import testful.coverage.CoverageInformation;

public class FaultsCoverage implements CoverageInformation {

	private static final long serialVersionUID = -7544654531090656602L;

	public static final String KEY = "faults";
	public static final String NAME = "fault discovered";

	public static FaultsCoverage getEmpty() {
		return new FaultsCoverage();
	}

	/** only FaultTracker can access directly this field! */
	public final Set<Fault> faults;

	private FaultsCoverage() {
		faults = new LinkedHashSet<Fault>();
	}

	@Override
	public float getQuality() {
		return faults.size();
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(!(other instanceof FaultsCoverage)) return false;

		return faults.containsAll(((FaultsCoverage) other).faults);
	}

	@Override
	public void merge(CoverageInformation other) {
		if(other instanceof FaultsCoverage) faults.addAll(((FaultsCoverage) other).faults);
	}

	@Override
	public CoverageInformation createEmpty() {
		return new FaultsCoverage();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for(Fault exc : faults) {
			sb.append(exc.getExceptionName()).append(": ").append(exc.getMessage()).append("\n");
			for(StackTraceElement ste : exc.getStackTrace())
				sb.append("  ").append(ste.toString()).append("\n");
			sb.append("---\n");
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
	public FaultsCoverage clone() {
		FaultsCoverage ret = new FaultsCoverage();
		for(Fault b : faults)
			ret.faults.add(b);

		return ret;
	}
}
