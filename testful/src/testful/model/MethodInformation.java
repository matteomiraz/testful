package testful.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import testful.model.xml.XmlMethod;

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

	public static enum Kind {
		/** The method is static */
		STATIC,

		/** A Constructor */
		CONSTRUCTOR,

		/** The method is pure and to be used for writing assertions in tests. <br/>
		 * <ul>
		 * <li>Pure method: does not modify the state and returns an observation of the
		 * object's state (changing the return value, the state is not changed).</li>
		 * <li>Usable for creating assertions: it has no input parameters, and the value it returns
		 * can be used for regression testing.</li>
		 * </ul>
		 */
		OBSERVER,

		/** the method is pure: does not modify the state and returns an observation of the
		 * object's state (changing the return value, the state is not changed).*/
		PURE,

		/** the method is a mutator: does something, may mutate the object's state */
		MUTATOR;

		public static Kind convert (XmlMethod.Kind k) {
			switch(k) {
			case STATIC: return STATIC;
			case OBSERVER: return OBSERVER;
			case PURE: return PURE;
			default: return MUTATOR;
			}
		}
	}

	/**
	 * true if the method mutates the state of the object accepting the method
	 * invocation. Constructors by definition mutate (constructs) the state of the
	 * object
	 */
	private final Kind type;

	/** true if method returns part of the object's state */
	private final boolean returnsState;

	private final ParameterInformation[] parameters;

	public MethodInformation(Kind type, boolean returnsState, ParameterInformation[] parameters) {
		this.type = type;
		this.returnsState = returnsState;
		this.parameters = parameters;
	}

	public Kind getType() {
		return type;
	}

	public boolean isReturnsState() {
		return returnsState;
	}

	public ParameterInformation[] getParameters() {
		return parameters;
	}
}
