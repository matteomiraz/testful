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

import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Logger;

import testful.TestFul;
import testful.model.faults.FaultyExecutionException;

/**
 * Represents a fault of the system.
 * It is automatically derivable from the {@link FaultyExecutionException}.
 * Faults with same message and same source (the stack trace) are assumed to be the same (they are equal).
 *
 * @author matteo
 */
public class Fault implements Serializable {

	private static final long serialVersionUID = 7235014552766544190L;

	/** Maximum length of recursion */
	private static final int MAX_STEP = 25;

	/** (recursion) Minimum number of iterations */
	private static final int MIN_ITER = 5;

	private static final Logger logger = Logger.getLogger("testful.coverage.fault");

	private final String message;
	private final String exceptionName;
	private final StackTraceElement[] stackTrace;

	private final String causeMessage;
	private final String causeExceptionName;

	private final int hashCode;

	/**
	 * Creates a fault from the {@link FaultyExecutionException}.
	 * @param exc The exception thrown
	 * @param targetClassName the name of the class of the method being executed
	 */
	public Fault(FaultyExecutionException exc, String targetClassName) {
		message = exc.getMessage();
		exceptionName = exc.getClass().getCanonicalName();
		stackTrace = processStackTrace(exc, targetClassName);

		Throwable cause = exc.getCause();

		if(cause == null) {
			causeMessage = null;
			causeExceptionName = null;
		} else {
			causeMessage= cause.getMessage();
			causeExceptionName = cause.getClass().getCanonicalName();
		}

		hashCode =
			31*31*31*31*exceptionName.hashCode() +
			31*31*31*((message == null) ? 0 : message.hashCode()) +
			31*31*Arrays.hashCode(stackTrace) +
			31*((causeExceptionName == null) ? 0 : causeExceptionName.hashCode()) +
			((causeMessage == null) ? 0 : causeMessage.hashCode());
	}

	private static StackTraceElement[] processStackTrace(FaultyExecutionException fault, String baseClassName) {
		final StackTraceElement[] stackTrace = fault.getStackTrace();
		if(stackTrace.length == 0) {
			if(TestFul.DEBUG) logger.warning("Empty StackTrace");
			else logger.fine("Empty StackTrace");

			return new StackTraceElement[0];
		}

		int n = stackTrace.length;
		int base = 0;

		// remove initial elements in the stack
		if(baseClassName != null) {
			while(--n >= 0 && !baseClassName.equals(stackTrace[n].getClassName()));

			if(n >= 0) n++;
			else n = stackTrace.length;
		}

		// remove last elements in the stack (if they belongs to testful)
		while(base < n && stackTrace[base].getClassName().startsWith("testful.")) base++;

		StackTraceElement[] pruned = new StackTraceElement[n - base];
		for(int i = base; i < n; i++)
			pruned[i-base] = stackTrace[i];

		// if there could be a loop in the stack trace
		if ( fault instanceof testful.coverage.stopper.TestStoppedException ||
				(fault instanceof testful.coverage.fault.UnexpectedExceptionException && fault.getCause() instanceof java.lang.StackOverflowError)) {

			// for each initial point
			final int len = pruned.length-1;
			for (int initial = len; initial > 0 ; initial--) {

				// for each valid step
				for(int step = 1; step < MAX_STEP && (initial + 1 - MIN_ITER*step) >= 0; step++) {

					if(checkRecursion(pruned, initial, step)) {

						StackTraceElement[] recursion = new StackTraceElement[len-initial + step + 2];

						recursion[0] = new StackTraceElement(" --  recursion", "end  -- ", "", -1);
						for(int i = 0; i < step; i++)
							recursion[i+1] = pruned[initial-step+i+1];
						recursion[step+1] = new StackTraceElement(" -- recursion", "start -- ", "", -1);

						for(int i = 0; i < len-initial; i++)
							recursion[step+2+i] = pruned[initial + 1 + i];

						return recursion;

					}
				}
			}
		}

		return pruned;
	}

	private static boolean checkRecursion(StackTraceElement[] pruned, int initial, int step) {
		int j = -1;
		for(int i = initial - step; i >= 0; i--) {
			if(++j % step == 0) j = 0;

			if(!pruned[i].equals( pruned[initial - j])) return false;
		}

		return true;
	}

	/**
	 * @return the exceptionName
	 */
	public String getExceptionName() {
		return exceptionName;
	}

	/**
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return the causeExceptionName
	 */
	public String getCauseExceptionName() {
		return causeExceptionName;
	}

	/**
	 * @return the causeMessage
	 */
	public String getCauseMessage() {
		return causeMessage;
	}

	/**
	 * @return the stackTrace
	 */
	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}

	/**
	 * Calculate the hashCode of the fault, using the message of the exception and the (readapted) stack trace.
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	/**
	 * Compare two faults using the message of the exception and the (readapted) stack trace.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;

		if (!(obj instanceof Fault)) return false;
		Fault other = (Fault) obj;

		if (!exceptionName.equals(other.exceptionName)) return false;

		if (message == null) {
			if (other.message != null) return false;
		} else if (!message.equals(other.message)) return false;

		if (!Arrays.equals(stackTrace, other.stackTrace)) return false;

		if (causeExceptionName == null) {
			if (other.causeExceptionName != null) return false;
		} else if (!causeExceptionName.equals(other.causeExceptionName)) return false;

		if (causeMessage == null) {
			if (other.causeMessage != null) return false;
		} else if (!causeMessage.equals(other.causeMessage)) return false;

		return true;
	}
}
