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
import testful.model.OperationResult;
import testful.model.faults.FaultyExecutionException;
import testful.utils.ElementManager;

public class FaultTracker extends Tracker {

	private static final Logger logger = Logger.getLogger("testful.coverage.fault");

	public static final FaultTracker singleton = new FaultTracker();

	private final ElementManager<String, CoverageInformation> elemManager = new ElementManager<String, CoverageInformation>();
	private FaultsCoverage coverage;

	private FaultTracker() {
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

	/**
	 * Analyzes the exception thrown by a method, and determine if it is a fault or not.
	 * In the positive case, adds tracks the fault and terminate the execution throwing a {@link FaultyExecutionException}.
	 * @param exc the exception to analyze
	 * @param declaredExceptions the exceptions that a method declare to throw
	 * @param params the parameters given to the method
	 * @param opRes if there is a fault, invokes the preconditionError on this OperationResult (if any)
	 * @param targetClassName the name of the class of the method being executed
	 * @throws Throwable if the exception is a fault, the method throws a {@link FaultyExecutionException}
	 */
	public void process(Throwable exc, Class<?>[] declaredExceptions, Object[] params, OperationResult opRes, String targetClassName) throws Throwable {
		final FaultyExecutionException fault;

		if(exc instanceof FaultyExecutionException) {
			fault = (FaultyExecutionException) exc;

		} else {
			// if the exception is not an unchecked exception, it must be declared in the signature, and it is not a failure
			if((exc instanceof Exception) && !(exc instanceof RuntimeException)) return;

			// if the exception has been declared in the signature, it is not a failure
			final Class<? extends Throwable> excClass = exc.getClass();
			for (Class<?> d : declaredExceptions)
				if(d.isAssignableFrom(excClass))
					return;

			// if it is a NullPointer Exception and a parameter was null, it is not a failure
			if(exc instanceof NullPointerException) {
				for (Object p : params) {
					if(p == null) return;
				}
			}

			fault = new UnexpectedExceptionException(exc);
		}

		processStackTrace(fault, targetClassName);

		coverage.faults.add(new Fault(fault));

		if(opRes != null) opRes.setPostconditionError();

		throw (Throwable) fault;
	}

	private static void processStackTrace(FaultyExecutionException fault, String baseClassName) {
		StackTraceElement[] stackTrace = fault.getStackTrace();

		if(stackTrace.length == 0) {
			if(TestFul.DEBUG) logger.warning("Empty StackTrace");
			else logger.fine("Empty StackTrace");

			return ;
		}

		int n = stackTrace.length;
		int base = 0;

		// remove initial elements in the stack
		if(baseClassName != null) {
			while(--n >= 0 && !baseClassName.equals(stackTrace[n].getClassName()));

			if(n >= 0) n++;
			else n = stackTrace.length;
		}

		// remove the last element in the stack
		while(base < n && stackTrace[base].getClassName().startsWith("testful.")) base++;

		StackTraceElement[] pruned = new StackTraceElement[n - base];
		for(int i = base; i < n; i++)
			pruned[i-base] = stackTrace[i];

		fault.setStackTrace(pruned);
	}
}
