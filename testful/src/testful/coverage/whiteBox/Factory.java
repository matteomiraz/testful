package testful.coverage.whiteBox;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import soot.ArrayType;
import soot.BooleanType;
import soot.CharType;
import soot.NullType;
import soot.PrimType;
import soot.RefType;
import soot.SootClass;
import soot.SootField;
import soot.Type;

public class Factory {

	private static final Logger logger = Logger.getLogger("testful.coverage.instrumenter.whitebox");

	public static final Factory singleton = new Factory();

	private final Map<SootClass, BlockClass> classes;
	private final Map<SootField, Data> fieldRepo;

	private final BitSet fieldsMask;

	private Factory() {
		classes = new HashMap<SootClass, BlockClass>();
		fieldRepo = new HashMap<SootField, Data>();
		fieldsMask = new BitSet();
	}

	public BitSet getFieldsMask() {
		return fieldsMask;
	}

	void updateFieldsMask(DataDef def) {
		fieldsMask.set(def.getId());
	}

	public Set<Data> get(Collection<SootField> fields) {
		Set<Data> ret = new HashSet<Data>(fields.size());

		for(SootField f : fields)
			ret.add(get(f));

		return ret;
	}

	public static Data getData(String field, Type type, boolean isParam) {
		if(type instanceof BooleanType)
			return Data.getPrimitiveData(field, Data.Type.Boolean, isParam);

		if(type instanceof CharType)
			return Data.getPrimitiveData(field, Data.Type.Character, isParam);

		if(type instanceof PrimType)
			return Data.getPrimitiveData(field, Data.Type.Number, isParam);

		if(type instanceof RefType) {
			if(((RefType) type).getClassName().equals("java.lang.String"))
				return Data.getPrimitiveData(field, Data.Type.String, isParam);

			return Data.getReferenceData(field, isParam);
		}

		// ((ArrayType) type).toString()
		if(type instanceof ArrayType)
			return Data.getArrayData(field, isParam);

		if(type instanceof NullType)
			return Data.getReferenceData(field, isParam);

		final NullPointerException exc = new NullPointerException("Unknown type: " + type + " (" + type.getClass().getCanonicalName() + ")");
		logger.log(Level.WARNING, exc.getMessage(), exc);
		throw exc;
	}

	public Data get(SootField field) {
		Data d = fieldRepo.get(field);
		if(d == null) {
			d = getData(field.getName(), field.getType(), false);
			fieldRepo.put(field, d);
		}
		return d;
	}

	public BlockClass get(SootClass sClass, Set<Data> fields) {
		BlockClass ret = classes.get(sClass);
		if(ret == null) {
			ret = new BlockClass(sClass.getName(), fields);
			classes.put(sClass, ret);
		}
		return ret;
	}

	public Collection<BlockClass> getClasses() {
		return classes.values();
	}

}
