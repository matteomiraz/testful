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

	private StackTraceElement[] stackTrace;
	private String msg;
	private FaultyExecutionException fault;

	/**
	 * Creates a fault from the {@link FaultyExecutionException} and the
	 * base element of the stack trace. The latter is the element of the stack trace
	 * that called the System Under Test.
	 * 
	 * @param exc The exception thrown
	 * @param base the element to consider as the base of stack trace
	 */
	public Fault(FaultyExecutionException exc, StackTraceElement base) {
		fault = exc;
		stackTrace = processStackTrace(exc, base);
		msg = exc.getMessage();
	}


	private StackTraceElement[] processStackTrace(FaultyExecutionException cause, StackTraceElement base) {
		StackTraceElement[] stackTrace = cause.getStackTrace();

		if(stackTrace.length == 0) {
			cause.fillInStackTrace(); // this seems to force the (sun) JVM to fill stack traces (in subsequent throws)!

			final Logger logger = Logger.getLogger("testful.coverage.bug");
			logger.warning("Empty StackTrace: using " + base);

			return new StackTraceElement[] { base };
		}

		int n = stackTrace.length;

		while(n > 0 && !stackTrace[n - 1].getClassName().equals(base.getClassName()))
			n--;

		StackTraceElement[] ret = new StackTraceElement[n];
		for(int i = 0; i < n; i++)
			ret[i] = stackTrace[i];

		return ret;
	}

	/**
	 * Returns the exception containing the fault
	 * @return the exception containing the fault
	 */
	public FaultyExecutionException getFault() {
		return fault;
	}

	/**
	 * Returns the stack trace
	 * @return the stack trace
	 */
	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}

	/**
	 * Calculate the hashCode of the fault, using the message of the exception and the (readapted) stack trace.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		result = prime * result + Arrays.hashCode(stackTrace);
		return result;
	}

	/**
	 * Compare two faults using the message of the exception and the (readapted) stack trace.
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		Fault other = (Fault) obj;
		if(msg == null) {
			if(other.msg != null) return false;
		} else if(!msg.equals(other.msg)) return false;
		if(!Arrays.equals(stackTrace, other.stackTrace)) return false;
		return true;
	}

	@Override
	public String toString() {
		return msg;
	}
}
