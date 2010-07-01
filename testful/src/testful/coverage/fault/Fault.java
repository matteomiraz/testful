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

	private final String message;
	private final String exceptionName;
	private final StackTraceElement[] stackTrace;

	private final String causeMessage;
	private final String causeExceptionName;

	private final int hashCode;

	/**
	 * Creates a fault from the {@link FaultyExecutionException}.
	 * @param exc The exception thrown
	 */
	public Fault(FaultyExecutionException exc) {
		message = exc.getMessage();
		exceptionName = exc.getClass().getCanonicalName();
		stackTrace = exc.getStackTrace();

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
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		Fault other = (Fault) obj;

		if(message == null) {
			if(other.message != null) return false;
		} else if(!message.equals(other.message)) return false;

		if(!Arrays.equals(stackTrace, other.stackTrace)) return false;

		return true;
	}
}
