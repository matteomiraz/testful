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

import java.lang.reflect.Method;

import testful.coverage.CoverageInformation;
import testful.coverage.Tracker;
import testful.model.OperationResult;
import testful.model.faults.FaultyExecutionException;
import testful.utils.ElementManager;

public class FaultTracker extends Tracker {

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

			// if it is a NullPointer Exception and a parameter was null, it is not a failure
			if(exc instanceof NullPointerException) {
				for (Object p : params) {
					if(p == null) return;
				}
			}

			// if the exception has been declared in the signature, it is not a failure
			final Class<? extends Throwable> excClass = exc.getClass();
			for (Class<?> d : declaredExceptions)
				if(d.isAssignableFrom(excClass))
					return;

			fault = new UnexpectedExceptionException(exc);
		}

		coverage.faults.add(new Fault(fault, targetClassName));

		if(opRes != null) opRes.setPostconditionError();

		throw (Throwable) fault;
	}

	/**
	 * Analyzes the exception thrown by a method, and determine if it is a fault or not.
	 * In the positive case, adds tracks the fault and terminate the execution throwing a {@link FaultyExecutionException}.
	 * @param exc the exception to analyze
	 * @param methodName the name of the invoked method
	 * @param methodParams the type of the parameters of the invoked method
	 * @param params the parameters given to the method
	 * @param opRes if there is a fault, invokes the preconditionError on this OperationResult (if any)
	 * @param targetClass the type of the object of the method being executed
	 * @throws Throwable if the exception is a fault, the method throws a {@link FaultyExecutionException}
	 */
	public void process(Throwable exc, String methodName, Class<?>[] methodParams, Object[] params, OperationResult opRes, Class<?> targetClass) throws Throwable {
		final FaultyExecutionException fault;

		if(exc instanceof FaultyExecutionException) {
			fault = (FaultyExecutionException) exc;

		} else {
			// if the exception is not an unchecked exception, it must be declared in the signature, and it is not a failure
			if((exc instanceof Exception) && !(exc instanceof RuntimeException)) return;

			// if it is a NullPointer Exception and a parameter was null, it is not a failure
			if(exc instanceof NullPointerException) {
				for (Object p : params) {
					if(p == null) return;
				}
			}

			// if the exception has been declared in the signature of the method of the class (or superclass / implemented interface), it is not a failure
			if(checkAllowedException(exc.getClass(), targetClass, methodName, methodParams))
				return;
			//			for (Class<?> d : declaredExceptions)
			//				if(d.isAssignableFrom(excClass))
			//					return;

			fault = new UnexpectedExceptionException(exc);
		}

		coverage.faults.add(new Fault(fault, targetClass.getName()));

		if(opRes != null) opRes.setPostconditionError();

		throw (Throwable) fault;
	}

	/**
	 * Check if an exception has been declared in a given method.
	 * It checks in the methodClass, and in all declarations of the same method in super classes and implemented interfaces.
	 * @param excClass the exception being thrown
	 * @param methodClass the class of the method being considered
	 * @param methodName the name of the method being considered
	 * @param methodParams the parameters of the method being considered
	 * @return true if the exception is allowed; false otherwise
	 */
	private boolean checkAllowedException(final Class<? extends Throwable> excClass, Class<?> methodClass, final String methodName, final Class<?>[] methodParams) {
		if(methodClass == null) return false;

		do {
			try {
				Method m = methodClass.getMethod(methodName, methodParams);
				for (Class<?> excType : m.getExceptionTypes()) {
					if(excType.isAssignableFrom(excClass)) {
						return true;
					}
				}
			} catch (NoSuchMethodException e) {
			}

			for(Class<?> interf : methodClass.getInterfaces()) {
				if(checkAllowedException(excClass, interf, methodName, methodParams)) {
					return true;
				}
			}

		} while((methodClass = methodClass.getSuperclass()) != null);

		return false;
	}


	/*
			if(baseObject != null) {
				long inizio = System.nanoTime();

				final String methodName = m.getName();
				final Class<?>[] paramTypes = m.getParameterTypes();


				SortedSet<Class<?>> excSet = new TreeSet<Class<?>>(new Comparator<Class<?>>() {
					@Override
					public int compare(Class<?> o1, Class<?> o2) {
						return o1.getName().compareTo(o2.getName());
					}
				});

				Class<?> baseClass = baseObject.getClass();
				do {
					System.err.println("u " + baseClass);
					try {
						Method m1 = baseClass.getMethod(methodName, paramTypes);
						if(m1 != null) {
							for (Class<?> excType : m1.getExceptionTypes()) {
								if(Exception.class.isAssignableFrom(excType) && !(Runtime.class.isAssignableFrom(excType))) {
									excSet.add(excType);
								}
							}
						}
					} catch (NoSuchMethodException e) {
					}
				} while((baseClass = baseClass.getSuperclass()) != null);

				long fine = System.nanoTime();
				System.err.println((fine-inizio)/1000000.0 + " " + excSet);
			}

	 */

}
