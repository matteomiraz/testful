package testful.model.executor;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.jmlspecs.jmlexec.runtime.PostconditionException;

import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.CreateObject;
import testful.model.ExceptionRaisedException;
import testful.model.FaultyExecutionException;
import testful.model.InvariantViolationException;
import testful.model.Invoke;
import testful.model.Methodz;
import testful.model.Operation;
import testful.model.OperationPrimitiveResult;
import testful.model.OperationStatus;
import testful.model.PrimitiveClazz;
import testful.model.Reference;
import testful.model.ResetRepository;
import testful.model.StaticValue;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.OperationStatus.Status;
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

	private static final long serialVersionUID = -1696826206324780022L;

	private static final boolean DEBUG = false;

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
		this.cluster = test.getCluster();
		this.repositoryType = test.getReferenceFactory().getReferences();
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
		
		if(DEBUG) {
			System.out.println("Cluster: \n" + cluster + "\n---" );

			System.out.println("Executing:");
			for(Operation op : test) 
				System.out.println(op);
			System.out.println("---");
			
		}

		repository = new Object[repositoryType.length];
		faults = new HashMap<Operation, FaultyExecutionException>();

		for(Operation op : test)
			try {
				perform(op);
			} catch(FaultyExecutionException e) {
				faults.put(op, e);
				for(int i = 0; i < repository.length; i++)
					repository[i] = null;

				if(stopOnBug) break;
			}

			if(DEBUG)
				System.out.println(this + "\n"
						+ "=================================");

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
			System.err.println("Reflection error in get(Reference): " + e);

			if(DEBUG) {
				System.err.println("Ref: " + objRef.getClazz() + " :: " + objRef.getPos() + " (" + objRef.getId() + ")");

				System.err.println("Repository: ");
				for(Reference element : repositoryType)
					System.err.println("  " + element.getId() + " (" + element.getClazz() + ":" + element.getPos() + ") = " + repository[element.getId()]);

				e.printStackTrace();
			}

			return null;
		}
	}

	private void set(Operation op, Reference objRef, Object value) {
		OperationPrimitiveResult opPrimResult = (OperationPrimitiveResult) op.getInfo(OperationPrimitiveResult.KEY);
		if(opPrimResult != null) opPrimResult.setValue(value);

		try {
			repository[objRef.getId()] = value;
		} catch(Throwable e) {
			System.err.println("Reflection error in set(Reference): " + e);

			if(DEBUG) {
				System.err.println("Ref: " + objRef.getClazz() + " :: " + objRef.getPos() + " (" + objRef.getId() + ")");

				System.err.println("Repository: ");
				for(Reference element : repositoryType)
					System.err.println("  " + element.getId() + " (" + element.getClazz() + ":" + element.getPos() + ") = " + repository[element.getId()]);

				e.printStackTrace();
			}
			return;
		}
	}

	private boolean perform(Operation op) throws FaultyExecutionException {
		if(DEBUG) {
			System.out.println(this);
			System.out.println(op);
			System.out.println("----");
		}

		if(op instanceof AssignPrimitive) return perform((AssignPrimitive) op);
		else if(op instanceof AssignConstant) return perform((AssignConstant) op);
		else if(op instanceof CreateObject) return perform((CreateObject) op);
		else if(op instanceof Invoke) return perform((Invoke) op);
		else if(op instanceof ResetRepository) return perform((ResetRepository) op);
		else {
			System.err.println("Unknown operation: " + op.getClass().getCanonicalName() + " - " + op);
			return false;
		}
	}

	private boolean perform(ResetRepository op) {
		for(int i = 0; i < repository.length; i++)
			repository[i] = null;

		return true;
	}

	/** createObject */
	private boolean perform(CreateObject op) throws FaultyExecutionException {
		OperationStatus opStatus = (OperationStatus) op.getInfo(OperationStatus.KEY);

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
						if(opStatus != null) opStatus.setPreconditionError(); 
						return false;
					} else 
						initargs[i] = ((PrimitiveClazz) constructozParamsType[i]).cast(initargs[i]);
			}

		// perform the real invocation
		Object newObject = null;
		try {

			newObject = cons.newInstance(initargs);

		} catch(InvocationTargetException invocationException) {
			Throwable exc = invocationException.getTargetException();

			if(exc instanceof org.jmlspecs.jmlrac.runtime.JMLPreconditionError) {
				if(opStatus != null) opStatus.setPreconditionError();
				return false;
			} else if(exc instanceof FaultyExecutionException) {
				if(opStatus != null && (exc instanceof ExceptionRaisedException || exc instanceof InvariantViolationException || exc instanceof PostconditionException)) opStatus.setPostconditionError();
				throw (FaultyExecutionException) exc;
			} else // a valid exception is thrown
				if(opStatus != null) opStatus.setExceptional(exc);
		} catch(Throwable e) {
			if(DEBUG) {
				e.printStackTrace();
				System.err.println("Reflection error in perform(CreateObject): " + e);
			}
			
			throw new ExceptionRaisedException(e);
		}

		if(opStatus != null && opStatus.getStatus() != Status.EXCEPTIONAL) opStatus.setSuccessful();

		// save results
		if(targetPos != null) set(op, targetPos, newObject);

		return true;
	}

	private boolean perform(AssignPrimitive op) {
		Reference ref = op.getTarget();
		Serializable value = op.getValue();

		if(ref == null || value == null) return false;

		// perform the real invocation
		set(op, ref, value);
		return true;
	}

	private boolean perform(AssignConstant op) {
		Reference ref = op.getTarget();
		if(ref == null) return false;

		StaticValue value = op.getValue();

		// set to null
		if(value == null) try {
			set(op, ref, null);
			return true;
		} catch(Throwable e) {
			// something very strange happens!
			System.err.println("Reflection error in perform(AssignConstant)[1]: " + e);

			if(DEBUG) e.printStackTrace();

			return false;
		}

		// set to value
		try {
			Object newObject = value.toField().get(null);
			set(op, ref, newObject);
			return true;
		} catch(Throwable e) {
			// something very strange happens!
			if(DEBUG) {
				System.err.println("Reflection error in perform(AssignConstant)[2]: " + e);
				e.printStackTrace();
			}
			throw new ExceptionRaisedException(e);
		}
	}

	private boolean perform(Invoke op) throws FaultyExecutionException {
		Reference targetPos = op.getTarget();
		Reference sourcePos = op.getThis();
		Methodz method = op.getMethod();
		Reference[] params = op.getParams();
		Clazz[] paramsTypes = method.getParameterTypes();
		OperationStatus opStatus = (OperationStatus) op.getInfo(OperationStatus.KEY);

		Method m = method.toMethod();
		Object[] args = new Object[params.length];

		for(int i = 0; i < args.length; i++)
			if(params[i] == null) args[i] = null;
			else {
				args[i] = get(params[i]);

				if(paramsTypes[i] instanceof PrimitiveClazz) if(args[i] == null) {
					if(opStatus != null) opStatus.setPreconditionError();
					return false;
				} else args[i] = ((PrimitiveClazz) paramsTypes[i]).cast(args[i]);
			}

		Object baseObject = get(sourcePos);
		if(baseObject == null && !method.isStatic()) {
			if(opStatus != null) opStatus.setPreconditionError();
			return false;
		}

		Object newObject = null;
		try {
			newObject = m.invoke(baseObject, args);
		} catch(InvocationTargetException invocationException) {
			Throwable exc = invocationException.getTargetException();

			if(exc instanceof org.jmlspecs.jmlrac.runtime.JMLPreconditionError) {
				if(opStatus != null) opStatus.setPreconditionError();
				return false;
			} else if(exc instanceof FaultyExecutionException) {
				if(opStatus != null && (exc instanceof ExceptionRaisedException || exc instanceof InvariantViolationException || exc instanceof PostconditionException)) opStatus.setPostconditionError();
				throw (FaultyExecutionException) exc;
			} else // a valid exception is thrown
				if(opStatus != null) opStatus.setExceptional(exc);
		} catch(Throwable e) {
			if(DEBUG) {
				System.err.println("Reflection error in perform(Invoke): " + e);
				e.printStackTrace();
			}

			throw new ExceptionRaisedException(e);
		}

		if(opStatus != null && opStatus.getStatus() != Status.EXCEPTIONAL) opStatus.setSuccessful();

		if(targetPos != null) set(op, targetPos, newObject);

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
