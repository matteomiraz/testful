package testful.model;

import java.io.Serializable;

public class OperationPrimitiveResult extends OperationInformation {

	private static final long serialVersionUID = 772248461493438644L;
	
	public static final boolean VERBOSE = true;
	public static final String KEY = "PRESULT";

	private boolean set = false;
	private Serializable value = null;

	public OperationPrimitiveResult() {
		super(KEY);
	}

	public void setValue(Object val) {
		if(VERBOSE && set) System.err.println("WARNING: " + OperationPrimitiveResult.class.getCanonicalName() + " already set");
		if(val instanceof Boolean || val instanceof Byte || val instanceof Character || val instanceof String || val instanceof Short || val instanceof Integer || val instanceof Long
				|| val instanceof Float || val instanceof Double) {
			set = true;
			value = (Serializable) val;
		}
	}

	public boolean isSet() {
		return set;
	}

	public Serializable getValue() {
		return value;
	}

	public static void insert(Operation[] ops) {
		Test.ensureNoDuplicateOps(ops);

		for(Operation op : ops)
			if(op instanceof Invoke || op instanceof CreateObject) op.addInfo(new OperationPrimitiveResult());
	}

	public static void remove(Test t) {
		for(Operation op : t.getTest())
			op.removeInfo(OperationPrimitiveResult.KEY);
	}
}
