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

import java.util.logging.Logger;

import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.coverage.Tracker;
import testful.model.faults.FaultyExecutionException;
import testful.utils.ElementManager;

public class FaultTracker extends Tracker {

	private static final FaultTracker tracker = new FaultTracker();

	private final ElementManager<String, CoverageInformation> elemManager;
	private FaultsCoverage coverage;

	private FaultTracker() {
		elemManager = new ElementManager<String, CoverageInformation>();
		reset();
	}

	@Override
	public void reset() {
		coverage = FaultsCoverage.getEmpty();
		elemManager.putAndReplace(coverage);
	}

	@Override
	public ElementManager<String, CoverageInformation> getCoverage() {
		return elemManager;
	}

	public static void processFaulty(FaultyExecutionException exc) {
		StackTraceElement base = Thread.currentThread().getStackTrace()[2];
		tracker.coverage.faults.add(new Fault(exc, base));
	}

	@SuppressWarnings("unused")
	public static void processException(Throwable exc) {
		if(TestFul.DEBUG && exc instanceof FaultyExecutionException)
			Logger.getLogger("testful.coverage.fault").warning(exc.getClass().getName() + " is instance of FaultyExecutionException: you should use the processFaulty method!");

		StackTraceElement base = Thread.currentThread().getStackTrace()[2];
		tracker.coverage.faults.add(new Fault(new UnexpectedExceptionException(exc), base));
	}
}
