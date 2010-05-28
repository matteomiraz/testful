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

import java.io.Serializable;
import java.util.Arrays;
import java.util.logging.Logger;

import testful.model.FaultyExecutionException;

public class Bug implements Serializable {

	private static final long serialVersionUID = 7235014552766544190L;

	private StackTraceElement[] stackTrace;
	private String msg;
	private FaultyExecutionException exc;

	public Bug(FaultyExecutionException exc, StackTraceElement base) {
		stackTrace = processStackTrace(exc.getCause(), base);
		msg = exc.getMessage();
		this.exc = exc;
	}

	private StackTraceElement[] processStackTrace(Throwable cause, StackTraceElement base) {
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

	public FaultyExecutionException getFaultyExecutionException() {
		return exc;
	}

	public StackTraceElement[] getStackTrace() {
		return stackTrace;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((msg == null) ? 0 : msg.hashCode());
		result = prime * result + Arrays.hashCode(stackTrace);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		Bug other = (Bug) obj;
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
