package testful.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class MethodInformation implements Serializable {

	private static final long serialVersionUID = -4189345382981598729L;

	public static class ParameterInformation implements Serializable {

		private static final long serialVersionUID = 1197230168306757732L;

		/** the position number of the parameter (0 if first, 1 if second, ...) */
		private final int position;

		/**
		 * true if the parameter becomes part of the object accepting the method
		 * invocation
		 */
		private boolean captured;

		/**
		 * true if the parameter becomes part of the object returned by the method
		 * invocation
		 */
		private boolean capturedByReturn;

		/**
		 * true if the parameter is modified during the method invocation (the type
		 * of the parameter must be mutable)
		 */
		private boolean mutated;

		/**
		 * after the method invocation, parameters referenced by
		 * <code>captureStateOf</code> become part of the state of this parameter
		 */
		private Set<ParameterInformation> captureStateOf;

		public ParameterInformation(int pos) {
			position = pos;
			captureStateOf = new HashSet<ParameterInformation>();
		}

		public int getPosition() {
			return position;
		}

		public boolean isCaptured() {
			return captured;
		}

		public void setCaptured(boolean captured) {
			this.captured = captured;
		}

		public boolean isCapturedByReturn() {
			return capturedByReturn;
		}

		public void setCapturedByReturn(boolean capturedByReturn) {
			this.capturedByReturn = capturedByReturn;
		}

		public boolean isMutated() {
			return mutated;
		}

		public void setMutated(boolean mutated) {
			this.mutated = mutated;
		}

		public Set<ParameterInformation> getCaptureStateOf() {
			return captureStateOf;
		}

		public void addCaptureStateOf(ParameterInformation parameter) {
			captureStateOf.add(parameter);
		}
	}

	/**
	 * true if the method mutates the state of the object accepting the method
	 * invocation. Constructors by definition mutate (constructs) the state of the
	 * object
	 */
	private final boolean mutator;

	/** true if method returns part of the object's state */
	private final boolean returnsState;

	private final ParameterInformation[] parameters;

	public MethodInformation(boolean mutator, boolean returnsState, ParameterInformation[] parameters) {
		this.mutator = mutator;
		this.returnsState = returnsState;
		this.parameters = parameters;
	}

	public boolean isMutator() {
		return mutator;
	}

	public boolean isReturnsState() {
		return returnsState;
	}

	public ParameterInformation[] getParameters() {
		return parameters;
	}
}
