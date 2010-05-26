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
import testful.model.ExceptionRaisedException;
import testful.model.FaultyExecutionException;
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
import testful.runner.Executor;

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
	public int execute(boolean stopOnBug) throws ClassNotFoundException {

		cluster.clearCache();
		cluster.setClassLoader(this.getClass().getClassLoader());

		Clazz cut = cluster.getCut();
		cut.toJavaClass();

		if(logger.isLoggable(Level.FINEST)) {
			StringBuilder sb = new StringBuilder();

			sb.append("Cluster: \n").append(cluster).append("\n---\n");

			sb.append("Executing:\n");
			for(Operation op : test)
				sb.append(" ").append(op).append("\n");

			logger.finest(sb.toString());
		}

		repository = new Object[repositoryType.length];
		faults = new HashMap<Operation, FaultyExecutionException>();

		int nValid = 0;
		for(Operation op : test) {
			try {
				if(logger.isLoggable(Level.FINEST)) {
					logger.finest(toString());
					logger.finest("Executing " + op);
				}

				boolean valid = perform(op);

				if(valid) nValid++;
			} catch(FaultyExecutionException e) {
				faults.put(op, e);
				for(int i = 0; i < repository.length; i++)
					repository[i] = null;

				if(stopOnBug) break;
			}
		}

		if(logger.isLoggable(Level.FINEST))
			logger.finest(toString());

		logger.fine(String.format("STATS ops valid valid%% invalid invalid%% -- %d %d %.2f %d %.2f",
				test.length, nValid, (nValid*100.0/test.length), (test.length-nValid), ((test.length-nValid)*100.0/test.length)));

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

			return null;
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
			return;
		}
	}

	private boolean perform(Operation op) throws FaultyExecutionException {

		if(op instanceof AssignPrimitive) return assignPrimitive((AssignPrimitive) op);
		else if(op instanceof AssignConstant) return assignConstant((AssignConstant) op);
		else if(op instanceof CreateObject) return createObject((CreateObject) op);
		else if(op instanceof Invoke) return invoke((Invoke) op);
		else if(op instanceof ResetRepository) return reset((ResetRepository) op);
		else {
			logger.warning("Unknown operation: " + op.getClass().getCanonicalName() + " - " + op);
			return false;
		}
	}

	private boolean reset(ResetRepository op) {
		for(int i = 0; i < repository.length; i++)
			repository[i] = null;

		return true;
	}

	/** createObject */
	private boolean createObject(CreateObject op) throws FaultyExecutionException {
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

				if(constructozParamsType[i] instanceof PrimitiveClazz)
					if(initargs[i] == null) {
						if(opRes != null) opRes.setPreconditionError();
						return false;
					} else
						initargs[i] = ((PrimitiveClazz) constructozParamsType[i]).cast(initargs[i]);
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

			if(exc instanceof org.jmlspecs.jmlrac.runtime.JMLPreconditionError) {
				if(opRes != null) opRes.setPreconditionError();
				return false;
			} else if(exc instanceof FaultyExecutionException) {
				if(opRes != null) opRes.setPostconditionError();
				throw (FaultyExecutionException) exc;
			} else // a valid exception is thrown
				if(opRes != null) opRes.setExceptional(exc, null, cluster);
		} catch(Throwable e) {
			logger.log(Level.WARNING, "Reflection error in createObject(" + op + "): " + e.getMessage(), e);

			throw new ExceptionRaisedException(e);
		}

		return true;
	}

	private boolean assignPrimitive(AssignPrimitive op) {
		Reference ref = op.getTarget();
		Serializable value = op.getValue();

		if(ref == null || value == null) return false;

		// perform the real invocation
		set(ref, value);
		return true;
	}

	private boolean assignConstant(AssignConstant op) {
		Reference ref = op.getTarget();
		if(ref == null) return false;

		StaticValue value = op.getValue();

		// set to null
		if(value == null) try {
			set(ref, null);
			return true;
		} catch(Throwable e) {
			// something very strange happens!
			logger.log(Level.WARNING, "Reflection error in assignConstant(" + op + "): " + e.getMessage(), e);
			throw new ExceptionRaisedException(e);
		}

		// set to value
		try {
			Object newObject = value.toField().get(null);
			set(ref, newObject);
			return true;
		} catch(Throwable e) {
			// something very strange happens!
			logger.log(Level.WARNING, "Reflection error in assignConstant(" + op + "): " + e.getMessage(), e);
			throw new ExceptionRaisedException(e);
		}
	}

	private boolean invoke(Invoke op) throws FaultyExecutionException {
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
			return false;
		}

		for(int i = 0; i < args.length; i++)
			if(params[i] == null) args[i] = null;
			else {
				args[i] = get(params[i]);

				if(paramsTypes[i] instanceof PrimitiveClazz) {
					if(args[i] == null) {
						if(opRes != null) opRes.setPreconditionError();
						return false;
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

			if(exc instanceof org.jmlspecs.jmlrac.runtime.JMLPreconditionError) {
				if(opRes != null) opRes.setPreconditionError();
				return false;
			} else if(exc instanceof FaultyExecutionException) {
				if(opRes != null) opRes.setPostconditionError();
				throw (FaultyExecutionException) exc;
			} else // a valid exception is thrown
				if(opRes != null) opRes.setExceptional(exc, baseObject, cluster);
		} catch(Throwable e) {
			logger.log(Level.WARNING, "Reflection error in invoke(" + op + "): " + e.getMessage(), e);

			throw new ExceptionRaisedException(e);
		}

		return true;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder("Executor environment:\n");

		for(Reference element : repositoryType)
			ret.append("  ").append(String.format("%2d", element.getId())).append(" (").append(element.getClazz()).append(":").append(element.getPos()).append(") = ").append(repository[element.getId()]).append("\n");

		return ret.toString();
	}
}
