package testful.coverage.whiteBox;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * Renders a variable or a field, able to store data
 * 
 * @author matteo
 */
public class Data implements Serializable {

	private static final long serialVersionUID = -4042773236943535616L;

	public enum Type {
		Boolean, Character, Number, String, Array, Reference
	}

	private final int id;
	private static int idGenerator = 0;

	private final Type type;
	private final String fieldName;

	private final BitSet mask;
	private final Set<DataDef> defs;
	private final Set<DataUse> uses;

	private boolean isField;
	private final boolean isParam;

	private Data(String fieldName, Type type, boolean param) {
		id = idGenerator++;

		this.type = type;

		isParam = param;
		this.fieldName = fieldName;
		isField = fieldName != null;

		mask = new BitSet();
		defs = new HashSet<DataDef>();
		uses = new HashSet<DataUse>();

	}

	public static Data getPrimitiveData(String fieldName, Type primType, boolean param) {
		return new Data(fieldName, primType, param);
	}

	public static Data getReferenceData(String fieldName, boolean param) {
		return new Data(fieldName, Type.Reference, param);
	}

	public static Data getArrayData(String fieldName, boolean param) {
		return new Data(fieldName, Type.Array, param);
	}

	public int getId() {
		return id;
	}

	public String getFieldName() {
		return fieldName;
	}

	void setField(boolean isField) {
		this.isField = isField;
	}

	public boolean isField() {
		return isField;
	}

	public boolean isParam() {
		return isParam;
	}

	public BitSet getMask() {
		return (BitSet) mask.clone();
	}

	public Type getType() {
		return type;
	}

	void addDef(DataDef def) {
		defs.add(def);
		mask.set(def.getId());
	}

	public Set<DataDef> getDefs() {
		return defs;
	}

	public Set<DataUse> getUses() {
		return uses;
	}

	void addUse(DataUse use) {
		uses.add(use);
	}

	@Override
	public String toString() {
		if(isParam) return "p" + id;
		if(isField) return "f" + id;
		return "d" + id;
	}
}
