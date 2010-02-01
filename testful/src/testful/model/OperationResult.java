package testful.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;

public class OperationResult extends OperationInformation {

	private static final long serialVersionUID = 772248461493438644L;

	public static final String KEY = "OP_RESULT";

	protected Value result = null;
	protected Value object = null;

	public OperationResult() {
		super(KEY);
	}

	@SuppressWarnings("unused")
	public void setValue(Object object, Object result, TestCluster cluster) throws FaultyExecutionException {

		if(TestFul.DEBUG && (object != null || result != null))
			Logger.getLogger("testful.model").warning(OperationResult.class.getCanonicalName() + " already set");

		this.object = new Value(object, cluster);
		this.result = new Value(result, cluster);
	}

	public Value getObject() {
		return object;
	}

	public Value getResult() {
		return result;
	}

	public static void insert(Operation[] ops) {
		Test.ensureNoDuplicateOps(ops);

		for(Operation op : ops)
			if(op instanceof Invoke || op instanceof CreateObject) op.addInfo(new OperationResult());
	}

	public static void remove(Test t) {
		for(Operation op : t.getTest())
			op.removeInfo(OperationResult.KEY);
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
				if(o instanceof Serializable) object = (Serializable) o;
				else object = null;

				observers = new HashMap<String, Serializable>();
				Clazz clazz = cluster.getClass(o.getClass());
				if(clazz != null) {
					for (Methodz m : clazz.getMethods()) {
						if(m.getParameterTypes().length == 0 && !m.getMethodInformation().isMutator()) {
							try {
								Object res = m.toMethod().invoke(o);
								if(res instanceof Serializable)
									observers.put(m.getName(), (Serializable) res);
							} catch (Throwable e) {
								Logger.getLogger("testful.model").log(Level.FINEST, "OperationResult: error while inspecting " + o + " (" + type + ") via " + m.getName() + ": " + e, e);
							}
						}
					}
				}
			}
		}

		public boolean isNull() {
			return isNull;
		}

		public Serializable getObject() {
			return object;
		}

		public String getType() {
			return type;
		}

		public Map<String, Serializable> getObservers() {
			return observers;
		}

		@Override
		public String toString() {
			if(isNull) return "null";

			StringBuilder sb = new StringBuilder();
			sb.append("type:").append(type);
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

			if (!(obj instanceof Value)) return false;
			Value other = (Value) obj;

			if (isNull != other.isNull) return false;
			if (object != null && !object.equals(other.object)) return false;
			if (observers != null && !observers.equals(other.observers)) return false;
			if (type != null && !type.equals(other.type)) return false;

			return true;
		}



	}
}
