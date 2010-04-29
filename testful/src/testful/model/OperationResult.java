package testful.model;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.utils.Cloner;

/**
 * Stores the result of an operation (Invoke or CreateObject).
 * 
 * It records the status of the operation (<b>invalid</b>, <b>faulty</b>,
 * <b>normal</b>, or <b>exceptional</b>).<br>
 * 
 * Moreover,
 * <ul>
 *   <li>In case of a <b>normal method</b> invocation, stores the value of the result.</li>
 *   <li>In case of a <b>normal constructor</b> invocation, stores the value of the result (the object created).</li>
 *   <li>In case of a <b>normal</b> or an <b>exceptional method</b> invocation, stores the state of the object accepting the invocation.</li>
 *   <li>In case of an <b>exception</b>, stores the thrown exception.</li>
 * </ul>
 * 
 * The inner class OperationResult.Verifier allows the user to perform regression testing.
 * 
 * @author matteo
 */
public class OperationResult extends OperationInformation {

	private static final long serialVersionUID = 772248461493438644L;

	public static final String KEY = "OP_RESULT";

	public static enum Status {
		/** The operation has not been executed */
		NOT_EXECUTED,

		/** the operation is invalid */
		PRECONDITION_ERROR,

		/** the operation is faulty */
		POSTCONDITION_ERROR,

		/** the operation is OK: it terminates without throwing any exception */
		SUCCESSFUL,

		/** the operation is OK: it terminates throwing an exception */
		EXCEPTIONAL
	}

	protected Status status = Status.NOT_EXECUTED;
	protected Value result = null;
	protected Value object = null;
	protected Throwable exc = null;

	public OperationResult() {
		super(KEY);
	}

	private OperationResult(OperationResult other)  {
		super(KEY);

		status = other.status;
		object = other.object;
		result = other.result;
		exc = other.exc;
	}

	public void setPreconditionError() {
		status = Status.PRECONDITION_ERROR;
	}

	public void setPostconditionError() {
		status = Status.POSTCONDITION_ERROR;
	}

	@SuppressWarnings("unused")
	public void setSuccessful(Object object, Object result, TestCluster cluster) throws FaultyExecutionException {

		if(TestFul.DEBUG && status != Status.NOT_EXECUTED)
			Logger.getLogger("testful.model").warning(OperationResult.class.getCanonicalName() + " already set");

		status = Status.SUCCESSFUL;
		this.object = new Value(object, cluster);
		this.result = new Value(result, cluster);
	}

	@SuppressWarnings("unused")
	public void setExceptional(Throwable exc, Object object, TestCluster cluster) {

		if(TestFul.DEBUG && status != Status.NOT_EXECUTED)
			Logger.getLogger("testful.model").warning(OperationResult.class.getCanonicalName() + " already set");

		status = Status.EXCEPTIONAL;
		this.exc = exc;
		this.object = new Value(object, cluster);
	}

	public Status getStatus() {
		return status;
	}

	public Value getObject() {
		return object;
	}

	public Value getResult() {
		return result;
	}

	public Throwable getException() {
		return exc;
	}

	public static void insert(Operation[] ops) {
		Test.ensureNoDuplicateOps(ops);

		for(Operation op : ops)
			if(op instanceof Invoke || op instanceof CreateObject)
				op.addInfo(new OperationResult());
	}

	public static void remove(Test t) {
		for(Operation op : t.getTest())
			op.removeInfo(OperationResult.KEY);
	}

	@Override
	public OperationInformation clone() {
		return new OperationResult(this);
	}

	@Override
	public String toString() {
		if(status == Status.NOT_EXECUTED) return "Not Executed";

		String ret = status.toString();
		if(status == Status.EXCEPTIONAL) ret += " " + exc.getClass().getCanonicalName() + ": " + exc.getMessage() + ";";
		if(object != null) ret += " object: " + object + ";";
		if(result != null) ret += " result: " + result + ";";
		return ret;
	}

	public static final class Value implements Serializable {
		private static final long serialVersionUID = -100047631209369725L;

		private final boolean isNull;
		private final String type;
		private final Serializable object;
		private final Map<String, Serializable> observers;

		public Value(Object o, TestCluster cluster) throws FaultyExecutionException {
			if(o == null) {
				isNull = true;
				type = null;
				object = null;
				observers = null;

			} else {
				isNull = false;
				type = o.getClass().getCanonicalName();
				object = saveObject(o);

				observers = new HashMap<String, Serializable>();
				Clazz clazz = cluster.getClass(o.getClass());
				if(clazz != null) {
					for (Methodz m : clazz.getMethods()) {
						if(m.getParameterTypes().length == 0 && m.getMethodInformation().getType() == MethodInformation.Kind.OBSERVER) {
							Method method = m.toMethod();

							try {
								Object res = method.invoke(o);
								Serializable res1 = saveObject(res);
								if(res1 != null)
									observers.put(m.getName(), res1);

							} catch (Throwable e) {
								Logger.getLogger("testful.model").log(Level.FINEST, "OperationResult: error while inspecting " + m.getName() + " (" + type + "): " + e, e);
							}
						}
					}
				}
			}
		}

		private static Serializable saveObject(Object o) {
			if(o == null) return null;

			Class<?> c = o.getClass();
			if(c == Boolean.TYPE || c == Boolean.class ||
					c == Byte.TYPE || c == Byte.class ||
					c == Character.TYPE || c == Character.class || c == String.class ||
					c == Short.TYPE || c == Short.class ||
					c == Integer.TYPE || c == Integer.class ||
					c == Long.TYPE || c == Long.class ||
					c == Float.TYPE || c == Float.class ||
					c == Double.TYPE || c == Double.class)
				return (Serializable) o;

			while(c.isArray()) c = c.getComponentType();

			if(c == Boolean.TYPE || c == Boolean.class ||
					c == Byte.TYPE || c == Byte.class ||
					c == Character.TYPE || c == Character.class || c == String.class ||
					c == Short.TYPE || c == Short.class ||
					c == Integer.TYPE || c == Integer.class ||
					c == Long.TYPE || c == Long.class ||
					c == Float.TYPE || c == Float.class ||
					c == Double.TYPE || c == Double.class)
				return new ArrayObject((Serializable) o);

			return null;
		}

		public static class ArrayObject implements Serializable {
			private static final long serialVersionUID = 8139913445774263584L;

			final byte[] serialized;
			final int hash;

			public ArrayObject(Serializable o) {
				serialized = Cloner.serialize(o, false);
				hash = Arrays.hashCode(serialized);
			}

			public Serializable getObject() {
				return Cloner.deserialize(serialized, false);
			}

			@Override
			public int hashCode() {
				return hash;
			}

			@Override
			public boolean equals(Object obj) {
				if(this == obj) return true;
				if(obj == null) return false;

				if(!(obj instanceof ArrayObject)) return false;
				ArrayObject other = (ArrayObject) obj;

				if (hash != other.hash) return false;
				if (!Arrays.equals(serialized, other.serialized)) return false;

				return true;
			}

			@Override
			public String toString() {
				return "{Array}";
			}
		}

		public boolean isNull() {
			return isNull;
		}

		public Serializable getObject() {

			if(object instanceof ArrayObject)
				return ((ArrayObject) object).getObject();

			else
				return object;
		}

		public String getType() {
			return type;
		}

		public Set<String> getObservers() {
			return observers.keySet();
		}

		public Serializable getObserver(String key) {
			Serializable ret = observers.get(key);
			if(ret instanceof ArrayObject) return ((ArrayObject) ret).getObject();
			return ret;
		}

		@Override
		public String toString() {
			if(isNull) return "null";

			StringBuilder sb = new StringBuilder();
			if(type != null) sb.append("type:").append(type);
			if(object != null) sb.append(" object:").append(object);
			for (Entry<String, Serializable> o : observers.entrySet())
				sb.append(" obj.").append(o.getKey()).append(":").append(o.getValue());
			return sb.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (isNull ? 1231 : 1237);
			result = prime * result + ((object == null) ? 0 : object.hashCode());
			result = prime * result + ((observers == null) ? 0 : observers.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;

			if (!(obj instanceof Value))
				return false;
			Value other = (Value) obj;

			if (isNull != other.isNull) return false;
			if (type != null && !type.equals(other.type)) return false;
			if (object != null && !object.equals(other.object)) return false;

			if (observers != null) {
				if(!observers.equals(other.observers)) return false;
			}

			return true;
		}

		public void check(Value other) {
			if(other == null) throw new OperationResultVerifierException("", this, other);


			if (isNull != other.isNull) throw new OperationResultVerifierException("NULL", isNull, other.isNull);
			if (type != null && !type.equals(other.type)) throw new OperationResultVerifierException("type", type, other.type);
			if (object != null && !object.equals(other.object)) throw new OperationResultVerifierException("object", object, other.object);

			if (observers != null) {
				if(other.observers == null) throw new OperationResultVerifierException("no observers");
				for (Entry<String, Serializable> e : observers.entrySet()) {
					Serializable o = other.observers.get(e.getKey());
					if(o != null && e.getValue() != null && !e.getValue().equals(o))
						throw new OperationResultVerifierException("observer " + e.getKey(), e.getValue(), o);
				}
			}


		}
	}


	public static class Verifier extends OperationResult {
		private static final long serialVersionUID = -1087900671239338703L;

		public Verifier(OperationResult op) {
			object = op.object;
			result = op.result;
		}

		@Override
		public void setPreconditionError() {
			if(status != Status.PRECONDITION_ERROR) throw new OperationVerifierException(status, Status.PRECONDITION_ERROR);
		}

		@Override
		public void setPostconditionError() {
			if(status != Status.POSTCONDITION_ERROR) throw new OperationVerifierException(status, Status.POSTCONDITION_ERROR);
		}

		@Override
		public void setSuccessful(Object object, Object result, TestCluster cluster) throws FaultyExecutionException {
			if(status != Status.SUCCESSFUL) throw new OperationVerifierException(status, Status.SUCCESSFUL);

			this.object.check(new Value(object, cluster));
			this.result.check(new Value(result, cluster));
		}

		@Override
		public void setExceptional(Throwable exc, Object object, TestCluster cluster) {
			if(status != Status.EXCEPTIONAL) throw new OperationVerifierException(status, Status.EXCEPTIONAL);

			Throwable thisExc = this.exc;
			if(!thisExc.getClass().equals(exc.getClass()) || !thisExc.getMessage().equals(exc.getMessage())) throw new OperationVerifierException(thisExc, exc);

			this.object.check(new Value(object, cluster));
		}

		public static void insertOperationResultVerifier(Operation[] ops) {
			Test.ensureNoDuplicateOps(ops);

			for(Operation op : ops) {
				OperationResult res = (OperationResult) op.removeInfo(OperationResult.KEY);
				if(res != null) op.addInfo(new Verifier(res));
			}
		}

		@Override
		public OperationInformation clone() {
			return new Verifier(this);
		}
	}

	public static class OperationVerifierException extends FaultyExecutionException {

		private static final long serialVersionUID = -5320352798948137983L;

		private OperationVerifierException(String msg) {
			super(msg, null);
		}

		public OperationVerifierException(Status expected, Status actual) {
			this("Operation Verifier: expected " + expected + ", actual: " + actual);
		}

		public OperationVerifierException(Throwable expected, Throwable actual) {
			this("Operation Verifier: operation termiated with a wrong exception. Expected " + expected + ", actual: " + actual);
		}
	}


	public static class OperationResultVerifierException extends FaultyExecutionException {
		private static final long serialVersionUID = -9113533247815125403L;

		private final Serializable expected;
		private final Serializable actual;
		private final String method;

		public OperationResultVerifierException(String msg) {
			super("Operation result verifier: " + msg, null);
			method = null;
			expected = null;
			actual = null;
		}

		public OperationResultVerifierException(String method, Serializable expected, Serializable actual) {
			super("Operation result verifier: " + method + " expected: " + expected + " actual: " + actual, null);
			this.method = method;
			this.expected = expected;
			this.actual = actual;
		}

		public Serializable getExpected() {
			return expected;
		}

		public Serializable getActual() {
			return actual;
		}

		public String getMethod() {
			return method;
		}
	}
}
