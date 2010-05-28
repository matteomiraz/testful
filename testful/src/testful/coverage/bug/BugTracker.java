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

import org.jmlspecs.jmlrac.runtime.JMLAssertionError;
import org.jmlspecs.jmlrac.runtime.JMLEntryPreconditionError;
import org.jmlspecs.jmlrac.runtime.JMLInternalPreconditionError;
import org.jmlspecs.jmlrac.runtime.JMLInvariantError;
import org.jmlspecs.jmlrac.runtime.JMLPostconditionError;

import testful.coverage.CoverageInformation;
import testful.coverage.Tracker;
import testful.model.ExceptionRaisedException;
import testful.model.FaultyExecutionException;
import testful.model.InternalPreConditionViolationException;
import testful.model.InvariantViolationException;
import testful.model.PostConditionViolationException;
import testful.utils.ElementManager;

public class BugTracker extends Tracker {

	private static final BugTracker tracker = new BugTracker();

	public static BugTracker getTracker() {
		return tracker;
	}

	private final ElementManager<String, CoverageInformation> elemManager;
	private BugCoverage coverage;

	private BugTracker() {
		coverage = BugCoverage.getEmpty();
		elemManager = new ElementManager<String, CoverageInformation>(coverage);
	}

	@Override
	public void reset() {
		coverage = BugCoverage.getEmpty();
		elemManager.putAndReplace(coverage);
	}

	@Override
	public ElementManager<String, CoverageInformation> getCoverage() {
		return elemManager;
	}

	public void process(Throwable exc, boolean hasContracts) throws Throwable {

		// nested call..
		if(exc instanceof FaultyExecutionException) throw (FaultyExecutionException) exc;

		// it's a precondition violation: throwing the exception
		if(exc instanceof JMLEntryPreconditionError) throw exc;
		
		// it's a user-defined exception in a class with contracts: it's ok!
		if(hasContracts && !(exc instanceof JMLAssertionError)) throw exc;

		FaultyExecutionException fault;
		
		// a method under test called another method, violating its precondition 
		if(exc instanceof JMLInternalPreconditionError) 
			fault = new InternalPreConditionViolationException(exc.getMessage(), exc);

		// simply a postcondition violation
		else if(exc instanceof JMLPostconditionError) 
			fault = new PostConditionViolationException(exc.getMessage(), exc);
		
		// an invariant violation is a fault!
		else if(exc instanceof JMLInvariantError) 
			fault = new InvariantViolationException(exc.getMessage(), exc);
		
		// a generic jml assertion violation
		else if(exc instanceof JMLAssertionError) 
			fault = new ExceptionRaisedException(exc.getMessage(), exc);
		
		// if I reach this point, it means that the class has not contracts and throws an exception
		else 
			fault = new ExceptionRaisedException(exc);

		StackTraceElement base = Thread.currentThread().getStackTrace()[2];
		coverage.bugs.add(new Bug(fault, base));

		throw fault;
	}
}
