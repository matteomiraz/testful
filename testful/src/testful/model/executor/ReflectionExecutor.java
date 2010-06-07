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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
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
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.faults.FaultyExecutionException;
import testful.model.faults.PreconditionViolationException;
import testful.model.faults.TestfulInternalException;
import testful.runner.Executor;
import testful.runner.TestfulClassLoader;

/**
 * This class is able to host a pool of objects, and execute operations on them.
 * It should be executed in another virtual machine than the caller, enabling
 * the managements of hard-fault (e.g., if the callee invokes
 * "System.exit(1);").
 *
 * @author matteo
 */

public class ReflectionExecutor implements Executor {

	private static final Logger logger = Logger.getLogger("testful.model.ReflectionExecutor");

	private static final long serialVersionUID = -1696826206324780022L;

	/** types of elements in the repository */
	private final Reference[] repositoryType;

	/** Test cluster */
	private final TestCluster cluster;

	/** the list of operations to perform */
	private final Operation[] test;

	/** The internal object repository */
	private transient Object[] repository;

	/** the set of faults */
	private transient Map<Operation, FaultyExecutionException> faults;

	public ReflectionExecutor(Test test) {
		cluster = test.getCluster();
		repositoryType = test.getReferenceFactory().getReferences();
		this.test = test.getTest();
	}

	@Override
	public int getTestLength() {
		return test.length;
	}

	public Operation[] getTest() {
		return test;
	}

	public TestCluster getCluster() {
		return cluster;
	}

	@Override
	public int execute(boolean stopOnBug) throws ClassNotFoundException, ClassCastException {
		final ClassLoader classLoader = this.getClass().getClassLoader();
		if(!(classLoader instanceof TestfulClassLoader))
			throw new ClassCastException("The executor must be loaded using the TestfulClassLoader!");

		cluster.clearCache();
		cluster.setClassLoader((TestfulClassLoader) classLoader);

		Clazz cut = cluster.getCut();
		cut.toJavaClass();

		if(logger.isLoggable(Level.FINER)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Cluster: \n").append(cluster.toString()).append("\n---\n");

			sb.append("Executing:\n");
			for(Operation op : test)
				sb.append(" ").append(op).append("\n");

			logger.finer(sb.toString());
		}

		repository = new Object[repositoryType.length];
		faults = new HashMap<Operation, FaultyExecutionException>();

		/** Number of invalid operations: precondition errors */
		int nPre = 0;
		/** Number of valid operations */
		int nValid = 0;
		/** Number of valid operations that reveal faults */
		int nFaulty = 0;

		for(Operation op : test) {
			try {
				if(logger.isLoggable(Level.FINEST)) {
					logger.finest(toString());
					logger.finest("Executing " + op);
				}

				perform(op);

				nValid++;

			} catch(Throwable e) {
				if (e instanceof TestfulInternalException) {
					// discard the exception and go ahead

				} else if(e instanceof PreconditionViolationException) {
					nPre++;

				} else if(e instanceof FaultyExecutionException) {
					nFaulty++;

					faults.put(op, (FaultyExecutionException) e);

					if(stopOnBug) break;

					// reset the repository
					for(int i = 0; i < repository.length; i++)
						repository[i] = null;
				}
			}
		}

		if(logger.isLoggable(Level.FINEST))
			logger.finest(toString());

		logger.fine(String.format("STATS ops invalid invalid%% valid valid%% faulty faulty%% -- %d %d %.2f %d %.2f %d %.2f",
				test.length,
				nPre, (nPre*100.0/test.length),
				nValid, (nValid*100.0/test.length),
				nFaulty, (nFaulty*100.0/test.length)));

		return faults.size();
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
	private Object get(Reference objRef) {
		try {
			if(repository == null) return null;

			return repository[objRef.getId()];
		} catch(Throwable e) {
			// something very strange happens!
			logger.log(Level.WARNING, "Reflection error in get(" + objRef + "): " + e.getMessage(), e);

			if(logger.isLoggable(Level.FINEST)) {

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

	private void set(Reference objRef, Object value) {
		try {
			repository[objRef.getId()] = value;
		} catch(Throwable e) {
			logger.log(Level.WARNING, "Reflection error in set(" + objRef + "=" + value + "): " + e.getMessage(), e);

			if(logger.isLoggable(Level.FINEST)) {
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

	private void perform(Operation op) throws Throwable {

		if(op instanceof AssignPrimitive) assignPrimitive((AssignPrimitive) op);
		else if(op instanceof AssignConstant) assignConstant((AssignConstant) op);
		else if(op instanceof CreateObject) createObject((CreateObject) op);
		else if(op instanceof Invoke) invoke((Invoke) op);
		else if(op instanceof ResetRepository) reset((ResetRepository) op);
		else logger.warning("Unknown operation: " + op.getClass().getCanonicalName() + " - " + op);
	}

	private void reset(ResetRepository op) {
		for(int i = 0; i < repository.length; i++)
			repository[i] = null;
	}

	/** createObject */
	private void createObject(CreateObject op) throws Throwable {
		OperationResult opRes = (OperationResult) op.getInfo(OperationResult.KEY);

		Reference targetPos = op.getTarget();
		Constructorz constructor = op.getConstructor();
		Reference[] params = op.getParams();

		Constructor<?> cons = constructor.toConstructor();

		// initialize input parameters
		Clazz[] constructozParamsType = constructor.getParameterTypes();
		Object[] initargs = new Object[params.length];

		for(int i = 0; i < initargs.length; i++)
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

		// perform the real invocation
		Object newObject = null;
		try {

			newObject = cons.newInstance(initargs);

			// save results
			if(targetPos != null) set(targetPos, newObject);

			if(opRes != null) opRes.setSuccessful(null, newObject, cluster);

		} catch(InvocationTargetException invocationException) {
			Throwable exc = invocationException.getTargetException();

			if(exc instanceof PreconditionViolationException) {
				if(opRes != null) opRes.setPreconditionError();
				throw exc;
			}

			if(exc instanceof FaultyExecutionException) {
				if(opRes != null) opRes.setPostconditionError();
				throw exc;
			}

			if(exc instanceof TestfulInternalException) {
				throw exc;
			}

			// a valid exception is thrown
			if(opRes != null) opRes.setExceptional(exc, null, cluster);

		} catch(Throwable e) {
			logger.log(Level.WARNING, "Reflection error in createObject(" + op + "): " + e.getMessage(), e);

			throw new TestfulInternalException.Impl(e);
		}
	}

	private void assignPrimitive(AssignPrimitive op) throws PreconditionViolationException.Impl, TestfulInternalException.Impl {
		Reference ref = op.getTarget();
		Serializable value = op.getValue();

		if(ref == null) throw new PreconditionViolationException.Impl("The reference is not set", null);
		if(value == null) throw new PreconditionViolationException.Impl("The primitive value has not been initialized", null);

		// perform the assignment
		set(ref, value);
	}

	private void assignConstant(AssignConstant op) throws PreconditionViolationException.Impl, TestfulInternalException.Impl {
		Reference ref = op.getTarget();

		if(ref == null) throw new PreconditionViolationException.Impl("The reference is not set", null);

		StaticValue value = op.getValue();

		// set to null
		if(value == null) {
			try {
				set(ref, null);
				return;
			} catch(Throwable e) {
				// something very strange happens!
				logger.log(Level.WARNING, "Reflection error in assignConstant(" + op + "): " + e.getMessage(), e);
				throw new TestfulInternalException.Impl(e);
			}
		}

		// set to value
		try {
			Object newObject = value.toField().get(null);
			set(ref, newObject);
		} catch(Throwable e) {
			// something very strange happens!
			logger.log(Level.WARNING, "Reflection error in assignConstant(" + op + "): " + e.getMessage(), e);
			throw new TestfulInternalException.Impl(e);
		}
	}

	private void invoke(Invoke op) throws Throwable {
		Reference targetPos = op.getTarget();
		Reference sourcePos = op.getThis();
		Methodz method = op.getMethod();
		Reference[] params = op.getParams();
		Clazz[] paramsTypes = method.getParameterTypes();
		OperationResult opRes = (OperationResult) op.getInfo(OperationResult.KEY);

		Method m = method.toMethod();
		Object[] args = new Object[params.length];

		Object baseObject = get(sourcePos);
		if(baseObject == null && !method.isStatic()) {
			if(opRes != null) opRes.setPreconditionError();
			throw new PreconditionViolationException.Impl("The object accepting the method call is null", null);
		}

		for(int i = 0; i < args.length; i++)
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

		Object result = null;
		try {

			result = m.invoke(baseObject, args);

			if(targetPos != null) set(targetPos, result);

			if(opRes != null) opRes.setSuccessful(baseObject, result, cluster);

		} catch(InvocationTargetException invocationException) {
			Throwable exc = invocationException.getTargetException();

			if(exc instanceof PreconditionViolationException) {
				if(opRes != null) opRes.setPreconditionError();
				throw exc;
			}

			if(exc instanceof FaultyExecutionException) {
				if(opRes != null) opRes.setPostconditionError();
				throw exc;
			}

			if(exc instanceof TestfulInternalException) {
				throw exc;
			}

			// a valid exception is thrown
			if(opRes != null) opRes.setExceptional(exc, null, cluster);

		} catch(Throwable e) {
			logger.log(Level.WARNING, "Reflection error in invoke(" + op + "): " + e.getMessage(), e);

			throw new TestfulInternalException.Impl(e);
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
