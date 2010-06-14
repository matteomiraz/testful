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

package testful.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import testful.model.xml.XmlMethod;

/**
 * Contains information on methods:
 * <ul>
 *  <li>the {@link testful.model.MethodInformation.Kind} of the method (static, observer, pure, or mutator).</li>
 *  <li>if the method returns part of the object's state</li>
 *  <li>the information on parameters {@link testful.model.MethodInformation.ParameterInformation}</li>
 * </ul>
 * @author matteo
 */
public class MethodInformation implements Serializable {

	private static final long serialVersionUID = -4189345382981598729L;

	/**
	 * Contains information on parameters of a method:
	 * <ul>
	 *  <li>The position of the method</li>
	 *  <li>if state of the parameter is captured and it becomes part of the object's state (i.e., modifying the parameter, the object change, and vice versa)</li>
	 *  <li>if state of the parameter becomes part of the returned value (i.e., modifying the parameter, the state of the returned value, and vice versa)</li>
	 *  <li>if state of the parameter becomes part of another parameter (i.e., modifying a parameter, the other changes)</li>
	 *  <li>if the state of the parameter is mutated</li>
	 * </ul>
	 * @author matteo
	 */
	public static class ParameterInformation implements Serializable {

		private static final long serialVersionUID = 1197230168306757732L;

		/** the position number of the parameter (0 if first, 1 if second, ...) */
		private final int position;

		/** true if the parameter becomes part of the object accepting the method invocation */
		private boolean captured;

		/** true if the parameter becomes part of the object returned by the method invocation */
		private boolean capturedByReturn;

		/** true if the parameter is modified during the method invocation (the type of the parameter must be mutable) */
		private boolean mutated;

		/** after the method invocation, parameters referenced by <code>captureStateOf</code> become part of the state of this parameter */
		private Set<ParameterInformation> captureStateOf;

		/**
		 * Creates a default parameter information
		 * @param pos the ordinal position of the parameter (0 is the first)
		 */
		public ParameterInformation(int pos) {
			position = pos;
			captureStateOf = new HashSet<ParameterInformation>();
		}

		/**
		 * Returns the position number of the parameter (0 if first, 1 if second, ...)
		 * @return the position number of the parameter (0 if first, 1 if second, ...)
		 */
		public int getPosition() {
			return position;
		}

		/**
		 * Indicates if the parameter becomes part of the object accepting the method invocation
		 * @return true if the parameter becomes part of the object accepting the method invocation
		 */
		public boolean isCaptured() {
			return captured;
		}

		/**
		 * Indicates if the parameter becomes part of the object accepting the method invocation
		 * @param captured true if the parameter becomes part of the object accepting the method invocation
		 */
		public void setCaptured(boolean captured) {
			this.captured = captured;
		}

		/**
		 * Indicates if the parameter becomes part of the object returned by the method invocation
		 * @return true if the parameter becomes part of the object returned by the method invocation
		 */
		public boolean isCapturedByReturn() {
			return capturedByReturn;
		}

		/**
		 * Indicates if the parameter becomes part of the object returned by the method invocation
		 * @param capturedByReturn true if the parameter becomes part of the object returned by the method invocation
		 */
		public void setCapturedByReturn(boolean capturedByReturn) {
			this.capturedByReturn = capturedByReturn;
		}

		/**
		 * Indicates if the parameter is modified during the method invocation (the type of the parameter must be mutable)
		 * @return true if the parameter is modified during the method invocation (the type of the parameter must be mutable)
		 */
		public boolean isMutated() {
			return mutated;
		}

		/**
		 * Indicates if the parameter is modified during the method invocation (the type of the parameter must be mutable)
		 * @param mutated true if the parameter is modified during the method invocation (the type of the parameter must be mutable)
		 */
		public void setMutated(boolean mutated) {
			this.mutated = mutated;
		}

		/**
		 * Returns the parameters that the current parameter exchanges the state with
		 * @return the parameters that the current parameter exchanges the state with
		 */
		public Set<ParameterInformation> getCaptureStateOf() {
			return captureStateOf;
		}

		/**
		 * Add a parameter that the current parameter exchanges the state with
		 * @param parameter the parameter that the current parameter exchanges the state with
		 */
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
