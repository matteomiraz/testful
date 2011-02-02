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

package testful.model.executor;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.coverage.fault.FaultTracker;
import testful.coverage.stopper.Stopper;
import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.ClassRegistry;
import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Methodz;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.PrimitiveClazz;
import testful.model.Reference;
import testful.model.ResetRepository;
import testful.model.StaticValue;
import testful.model.TestCluster;
import testful.model.faults.FaultyExecutionException;
import testful.model.faults.PreconditionViolationException;
import testful.model.faults.TestfulInternalException;
import testful.runner.Executor;
import testful.utils.StopWatchNested;

/**
 * This class is able to host a pool of objects, and execute operations on them.
 * It should be executed in another virtual machine than the caller, enabling
 * the managements of hard-fault (e.g., if the callee invokes
 * "System.exit(1);").
 *
 * @author matteo
 */
public class ReflectionExecutor extends Executor {

	private static final Logger logger = Logger.getLogger("testful.model.ReflectionExecutor");
	private static final boolean LOGGER_FINE   = logger.isLoggable(Level.FINE);
	private static final boolean LOGGER_FINER  = logger.isLoggable(Level.FINER);
	private static final boolean LOGGER_FINEST = logger.isLoggable(Level.FINEST);

	private static final long serialVersionUID = -1696826206324780022L;

	/** The internal object repository */
	private transient Object[] repository;

	public ReflectionExecutor(TestCluster testCluster, Reference[] testRefs, Operation[] test, boolean discoverFaults) {
		super(testCluster, testRefs, test, discoverFaults);

		if(TestFul.DEBUG && ClassRegistry.singleton == null)
			throw new ClassCastException("ClassRegistry not initialized");
	}

	private static final String TIMER_PREFIX = "exec";

	private static StopWatchNested timer = StopWatchNested.getRootTimer(TIMER_PREFIX + ".1");

	private static StopWatchNested timer_pre_stop = timer.getSubTimer(TIMER_PREFIX + ".2.preStop");

	private static StopWatchNested timer_stop = timer.getSubTimer(TIMER_PREFIX + ".3.stop");

	private static StopWatchNested timer_exec = timer.getSubTimer(TIMER_PREFIX + ".3.exec");

	private static StopWatchNested timer_assignPrimitive = timer_exec.getSubTimer(TIMER_PREFIX + ".4.assignPrimitive");
	private static StopWatchNested timer_createObject = timer_exec.getSubTimer(TIMER_PREFIX + ".4.createObject");
	private static StopWatchNested timer_createObjectCut = timer_createObject.getSubTimer(TIMER_PREFIX + ".4.createObject.cut");
	private static StopWatchNested timer_invoke = timer_exec.getSubTimer(TIMER_PREFIX + ".4.invoke");
	private static StopWatchNested timer_invokeCut = timer_invoke.getSubTimer(TIMER_PREFIX + ".4.invoke.cut");
	private static StopWatchNested timer_assignConstant = timer_exec.getSubTimer(TIMER_PREFIX + ".4.assignConstant");
	private static StopWatchNested timer_assignConstantCut = timer_assignConstant.getSubTimer(TIMER_PREFIX + ".4.assignConstant.cut");

	private static StopWatchNested timer_post_stop = timer.getSubTimer(TIMER_PREFIX + ".5.postStop");

	@Override
	public int execute(boolean stopOnBug) throws ClassNotFoundException, ClassCastException {

		timer.start();

		if(LOGGER_FINEST) {
			StringBuilder sb = new StringBuilder();

			sb.append("Executing:\n");
			for(Operation op : test)
				sb.append(" ").append(op).append("\n");

			logger.finest(sb.toString());
		}

		repository = new Object[repositoryType.length];

		/** Number of invalid operations: precondition errors */
		int nPre = 0;
		/** Number of valid operations */
		int nValid = 0;
		/** Number of valid operations that reveal faults */
		int nFaulty = 0;

		timer_pre_stop.start();
		final Stopper stopper = new Stopper();
		timer_pre_stop.stop();

		for(Operation op : test) {

			final Integer maxExecTime;
			if(op instanceof CreateObject) maxExecTime = ((CreateObject)op).getConstructor().getMaxExecutionTime();
			else if (op instanceof Invoke) maxExecTime = ((Invoke)op).getMethod().getMaxExecutionTime();
			else maxExecTime = null;

			try {
				if(LOGGER_FINEST) {
					logger.finest(toString());
					logger.finest("Executing " + op);
				}

				timer_stop.start();
				final long start;
				if(maxExecTime != null) {
					stopper.start(maxExecTime);
					if(LOGGER_FINER) start = System.nanoTime();
					else start = -1;
				} else {
					start = -1;
				}
				timer_stop.stop();

				timer_exec.start();
				if(op instanceof AssignPrimitive) assignPrimitive((AssignPrimitive) op);
				else if(op instanceof AssignConstant) assignConstant((AssignConstant) op);
				else if(op instanceof CreateObject) createObject((CreateObject) op);
				else if(op instanceof Invoke) invoke((Invoke) op);
				else if(op instanceof ResetRepository) reset();
				else logger.warning("Unknown operation: " + op.getClass().getName() + " - " + op);

				nValid++;

				if(LOGGER_FINER) {
					float length = (System.nanoTime() - start) / 1000000.0f;

					final String name;
					if(op instanceof CreateObject) name = ((CreateObject)op).getConstructor().toString();
					else if(op instanceof Invoke) name = ((Invoke)op).getMethod().toString();
					else name = "";

					logger.finer(String.format("OpExecution %.3f ms (%5.2f%%) %s", length, (100 * length / maxExecTime), name));
				}

			} catch(Throwable e) {

				if (e instanceof TestfulInternalException) {
					// clean the test execution
					nPre++;
					reset();

				} else if(e instanceof PreconditionViolationException) {
					nPre++;

				} else if(e instanceof FaultyExecutionException) {

					nFaulty++;
					if(stopOnBug) break;

					reset();
				}
			} finally {
				timer_exec.stop();

				if(maxExecTime != null) {
					stopper.stop();
					if(Thread.interrupted())
						logger.finest("Clean the thread interrupted status");
				}
			}
		}

		timer_post_stop.start();
		stopper.done();
		timer_post_stop.stop();

		if(LOGGER_FINEST) logger.finest(toString());
		if(LOGGER_FINE)   logger.fine(new StringBuilder("STATS").append(" ops:").append(test.length).append(" invalid:").append(nPre).append(" valid:").append(nValid).append(" faulty:").append(nFaulty).toString());

		timer.stop();

		return nFaulty;
	}

	/**
	 * Returns the object with the given type at the given position. Returns NULL
	 * if such element doesn't exist
	 *
	 * @param c the type of the desired element
	 * @param pos the position of the desired element
	 * @return the object
	 * @throws ClassNotFoundException
	 */
	private Object get(Reference objRef) throws TestfulInternalException.Impl {
		try {
			if(repository == null) return null;

			return repository[objRef.getId()];
		} catch(Throwable e) {
			// something very strange happens!
			logger.log(Level.WARNING, "Reflection error in get(" + objRef + "): " + e.getMessage(), e);

			if(LOGGER_FINEST) {
				StringBuilder sb = new StringBuilder();

				sb.append("Ref: ").append(objRef.getClazz()).append(" :: ").append(objRef.getPos()).append(" (").append(objRef.getId()).append(")\n");

				sb.append("Repository:\n");
				for(Reference element : repositoryType)
					sb.append("  ").append(element.getId()).append(" (").append(element.getClazz()).append(":").append(element.getPos()).append(") = ").append(repository[element.getId()]).append("\n");

				logger.finest(sb.toString());
			}

			throw new TestfulInternalException.Impl(e);
		}
	}

	private void set(Reference objRef, Object value) throws TestfulInternalException.Impl {
		try {
			repository[objRef.getId()] = value;
		} catch(Throwable e) {
			logger.log(Level.WARNING, "Reflection error in set(" + objRef + "=" + value + "): " + e.getMessage(), e);

			if(LOGGER_FINEST) {
				StringBuilder sb = new StringBuilder();

				sb.append("Ref: ").append(objRef.getClazz()).append(" :: ").append(objRef.getPos()).append(" (").append(objRef.getId()).append(")\n");

				sb.append("Repository:\n");
				for(Reference element : repositoryType)
					sb.append("  ").append(element.getId()).append(" (").append(element.getClazz()).append(":").append(element.getPos()).append(") = ").append(repository[element.getId()]).append("\n");

				logger.finest(sb.toString());
			}

			throw new TestfulInternalException.Impl(e);
		}
	}

	private void reset() {
		for(int i = 0; i < repository.length; i++)
			repository[i] = null;
	}

	/** createObject */
	private void createObject(CreateObject op) throws Throwable {
		try {
			timer_createObject.start();

			OperationResult opRes = (OperationResult) op.getInfo(OperationResult.KEY);

			Reference targetPos = op.getTarget();
			Constructorz constructor = op.getConstructor();
			Reference[] params = op.getParams();

			Constructor<?> cons;
			try {
				cons = ClassRegistry.singleton.getConstructor(constructor);
			} catch (Exception exc) {
				logger.log(Level.WARNING, exc.getMessage(), exc);
				throw new TestfulInternalException.Impl(exc);
			}

			// initialize input parameters
			Clazz[] constructozParamsType = constructor.getParameterTypes();
			Object[] initargs = new Object[params.length];

			for(int i = 0; i < initargs.length; i++) {
				if(params[i] == null) initargs[i] = null;
				else {
					initargs[i] = get(params[i]);

					if(constructozParamsType[i] instanceof PrimitiveClazz) {
						if(initargs[i] == null) {
							if(opRes != null) opRes.setPreconditionError();
							throw new PreconditionViolationException.Impl("The primitive value has not been initialized", null);
						} else {
							initargs[i] = ((PrimitiveClazz) constructozParamsType[i]).cast(initargs[i]);
						}
					}
				}
			}

			// perform the real invocation
			Object newObject = null;
			try {

				timer_createObjectCut.start();
				newObject = cons.newInstance(initargs);
				timer_createObjectCut.stop();

				// save results
				if(targetPos != null) set(targetPos, newObject);

				if(opRes != null) opRes.setSuccessful(null, newObject, cluster);

				return;

			} catch(InvocationTargetException invocationException) {
				timer_createObjectCut.stop();

				Throwable exc = invocationException.getTargetException();

				// check for nasty Errors (such as OutOfMemory errors)
				if(exc instanceof VirtualMachineError && !(exc instanceof StackOverflowError)) {
					reset(); // early free some memory
					logger.fine("VirtualMachine Error " + exc + " (" + exc.getClass().getCanonicalName() + ") while executing "  + op);
					throw new TestfulInternalException.Impl(exc);
				}

				// Internal error
				if(exc instanceof TestfulInternalException) throw exc;

				// precondition error
				if(exc instanceof PreconditionViolationException) {
					if(opRes != null) opRes.setPreconditionError();
					throw exc;
				}

				if(discoverFaults) {
					FaultTracker.singleton.process(exc, cons.getExceptionTypes(), initargs, opRes, cons.getDeclaringClass().getName());
				}

				// a valid exception is thrown
				if(opRes != null) opRes.setExceptional(exc, null, cluster);

			} catch(Throwable e) {
				timer_createObjectCut.stop();

				logger.log(Level.WARNING, "Reflection error in createObject(" + op + "): " + e.getMessage(), e);

				throw new TestfulInternalException.Impl(e);
			}
		} finally {
			timer_createObject.stop();
		}
	}

	private void assignPrimitive(AssignPrimitive op) throws PreconditionViolationException.Impl, TestfulInternalException.Impl {
		try {
			timer_assignPrimitive.start();

			Reference ref = op.getTarget();
			Serializable value = op.getValue();

			if(ref == null) throw new PreconditionViolationException.Impl("The reference is not set", null);

			// perform the assignment
			set(ref, value);
		} finally {
			timer_assignPrimitive.stop();
		}
	}

	private void assignConstant(AssignConstant op) throws PreconditionViolationException.Impl, TestfulInternalException.Impl {
		try {
			timer_assignConstant.start();

			Reference ref = op.getTarget();

			if(ref == null) throw new PreconditionViolationException.Impl("The reference is not set", null);

			StaticValue value = op.getValue();

			// set to null
			if(value == null) {
				try {
					try {
						timer_assignConstantCut.start();
						set(ref, null);
					} finally {
						timer_assignConstantCut.stop();
					}

					return;
				} catch(Throwable e) {
					// something very strange happens!
					logger.log(Level.WARNING, "Reflection error in assignConstant(" + op + "): " + e.getMessage(), e);
					throw new TestfulInternalException.Impl(e);
				}
			}

			// set to value
			try {
				try {
					timer_assignConstantCut.start();

					Field field = ClassRegistry.singleton.getField(value);
					Object newObject = field.get(null);
					set(ref, newObject);
				} finally {
					timer_assignConstantCut.stop();
				}
			} catch(Throwable e) {
				// something very strange happens!
				logger.log(Level.WARNING, "Reflection error in assignConstant(" + op + "): " + e.getMessage(), e);
				throw new TestfulInternalException.Impl(e);
			}

		} finally {
			timer_assignConstant.stop();
		}
	}

	private void invoke(Invoke op) throws Throwable {
		try {
			timer_invoke.start();

			final Reference targetPos = op.getTarget();
			final Reference sourcePos = op.getThis();
			final Methodz method = op.getMethod();
			final Reference[] params = op.getParams();
			final Clazz[] paramsTypes = method.getParameterTypes();
			final OperationResult opRes = (OperationResult) op.getInfo(OperationResult.KEY);

			final Method m = ClassRegistry.singleton.getMethod(method);
			final Object[] args = new Object[params.length];

			final Object baseObject;
			if(sourcePos != null) baseObject = get(sourcePos);
			else baseObject = null;

			if(baseObject == null && !method.isStatic()) {
				if(opRes != null) opRes.setPreconditionError();
				throw new PreconditionViolationException.Impl("The object accepting the method call is null", null);
			}

			for(int i = 0; i < args.length; i++) {
				if(params[i] == null) args[i] = null;
				else {
					args[i] = get(params[i]);

					if(paramsTypes[i] instanceof PrimitiveClazz) {
						if(args[i] == null) {
							if(opRes != null) opRes.setPreconditionError();
							throw new PreconditionViolationException.Impl("The primitive value has not been initialized", null);
						} else
							args[i] = ((PrimitiveClazz) paramsTypes[i]).cast(args[i]);
					}
				}
			}

			Object result = null;
			try {

				timer_invokeCut.start();
				result = m.invoke(baseObject, args);
				timer_invokeCut.stop();

				if(targetPos != null) set(targetPos, result);
				if(opRes != null) opRes.setSuccessful(baseObject, result, cluster);

				return;

			} catch(InvocationTargetException invocationException) {
				timer_invokeCut.stop();

				Throwable exc = invocationException.getTargetException();

				// check for nasty Errors (such as OutOfMemory errors)
				if(exc instanceof VirtualMachineError && !(exc instanceof StackOverflowError)) {
					reset(); // early free some memory
					logger.fine("VirtualMachine Error " + exc + " (" + exc.getClass().getCanonicalName() + ") while executing "  + op);
					throw new TestfulInternalException.Impl(exc);
				}

				// Internal error
				if(exc instanceof TestfulInternalException) throw exc;

				// precondition error
				if(exc instanceof PreconditionViolationException) {
					if(opRes != null) opRes.setPreconditionError();
					throw exc;
				}

				if(discoverFaults) {
					if(baseObject == null)
						FaultTracker.singleton.process(exc, m.getExceptionTypes(), args, opRes, m.getDeclaringClass().getName());
					else
						FaultTracker.singleton.process(exc, m.getName(), m.getParameterTypes(), args, opRes, baseObject.getClass());
				}

				// a valid exception is thrown
				if(opRes != null) opRes.setExceptional(exc, baseObject, cluster);

			} catch(Throwable e) {
				timer_invokeCut.stop();

				logger.log(Level.WARNING, "Reflection error in invoke(" + op + "): " + e.getMessage(), e);

				throw new TestfulInternalException.Impl(e);
			}
		} finally {
			timer_invoke.stop();
		}
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("Executor environment:\n");

		for(Reference element : repositoryType)
			ret.append("  ").append(String.format("%2d", element.getId())).append(" (").append(element.getClazz()).append(":").append(element.getPos()).append(") = ").append(repository[element.getId()]).append("\n");

		return ret.toString();
	}
}
