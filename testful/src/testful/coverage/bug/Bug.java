package testful.coverage.bug;

import java.io.Serializable;
import java.util.Arrays;

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
			System.err.println("Empty StackTrace: using " + base);
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
