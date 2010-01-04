package testful.model;

import java.util.HashSet;
import java.util.Set;

public final class PrimitiveClazz extends Clazz {

	private static final long serialVersionUID = -2125532247454284282L;

	public static enum PrimTypes {

		BooleanClass, BooleanType, ByteClass, ByteType, CharacterClass, CharacterType, DoubleClass, DoubleType, FloatClass, FloatType, IntegerClass, IntegerType, LongClass, LongType, ShortClass, ShortType;

		public static PrimTypes valueOf(Class<?> c) {
			if(Boolean.class == c) return BooleanClass;
			if(Boolean.TYPE == c) return BooleanType;

			if(Byte.class == c) return ByteClass;
			if(Byte.TYPE == c) return ByteType;

			if(Character.class == c) return CharacterClass;
			if(Character.TYPE == c) return CharacterType;

			if(Double.class == c) return DoubleClass;
			if(Double.TYPE == c) return DoubleType;

			if(Float.class == c) return FloatClass;
			if(Float.TYPE == c) return FloatType;

			if(Integer.class == c) return IntegerClass;
			if(Integer.TYPE == c) return IntegerType;

			if(Long.class == c) return LongClass;
			if(Long.TYPE == c) return LongType;

			if(Short.class == c) return ShortClass;
			if(Short.TYPE == c) return ShortType;

			System.err.println("ERROREE!!!");
			return null;
		}

		public Class<?> toClass() {
			switch(this) {
				case BooleanClass:
					return Boolean.class;
				case BooleanType:
					return Boolean.TYPE;

				case ByteClass:
					return Byte.class;
				case ByteType:
					return Byte.TYPE;

				case CharacterClass:
					return Character.class;
				case CharacterType:
					return Character.TYPE;

				case DoubleClass:
					return Double.class;
				case DoubleType:
					return Double.TYPE;

				case FloatClass:
					return Float.class;
				case FloatType:
					return Float.TYPE;

				case IntegerClass:
					return Integer.class;
				case IntegerType:
					return Integer.TYPE;

				case LongClass:
					return Long.class;
				case LongType:
					return Long.TYPE;

				case ShortClass:
					return Short.class;
				case ShortType:
					return Short.TYPE;

				default:
					System.err.println("ERROREE!!!");
					return null;
			}
		}

		boolean isClass() {
			return this == PrimTypes.BooleanClass || this == PrimTypes.ByteClass || this == PrimTypes.CharacterClass || this == PrimTypes.ShortClass || this == PrimTypes.IntegerClass
			|| this == PrimTypes.LongClass || this == PrimTypes.FloatClass || this == PrimTypes.DoubleClass;
		}

		PrimTypes other() {
			switch(this) {
				case BooleanClass:
					return BooleanType;
				case BooleanType:
					return BooleanClass;

				case ByteClass:
					return ByteType;
				case ByteType:
					return ByteClass;

				case CharacterClass:
					return CharacterType;
				case CharacterType:
					return CharacterClass;

				case DoubleClass:
					return DoubleType;
				case DoubleType:
					return DoubleClass;

				case FloatClass:
					return FloatType;
				case FloatType:
					return FloatClass;

				case IntegerClass:
					return IntegerType;
				case IntegerType:
					return IntegerClass;

				case LongClass:
					return LongType;
				case LongType:
					return LongClass;

				case ShortClass:
					return ShortType;
				case ShortType:
					return ShortClass;

				default:
					System.err.println("ERROREE!!!");
					return null;
			}
		}
	};

	private final PrimTypes realType;

	private final PrimitiveClazz clazzType;
	private final PrimitiveClazz clazzObject;

	/**
	 * Create a primitive Class
	 * 
	 * @param cluster : the test cluster
	 * @param type : the Class type (e.g., Integer)
	 */
	private PrimitiveClazz(TestCluster cluster, PrimTypes type, Clazz[] assignableTo) {
		super(cluster, type.toClass());
		realType = type;
		this.assignableTo = assignableTo;
		PrimitiveClazz other = new PrimitiveClazz(this);

		clazzObject = type.isClass() ? this : other;
		clazzType = type.isClass() ? other : this;
	}

	private PrimitiveClazz(PrimitiveClazz other) {
		super(other.cluster, other.realType.other().toClass());
		realType = other.realType.other();
		assignableTo = other.assignableTo;

		clazzObject = realType.isClass() ? this : other;
		clazzType = realType.isClass() ? other : this;
	}

	public PrimitiveClazz(TestCluster cluster, Class<?> type, Clazz[] assignableTo, PrimitiveClazz other) {
		super(cluster, type);
		realType = PrimTypes.valueOf(type);
		this.assignableTo = assignableTo;

		if(realType.isClass()) {
			clazzObject = this;
			clazzType = other;
		} else {
			clazzObject = other;
			clazzType = this;
		}
	}

	@Override
	public boolean isAbstract() {
		return false;
	}
	
	@Override
	public Class<?> toJavaClass() {
		return realType.toClass();
	}

	@Override
	public Clazz getReferenceClazz() {
		return clazzObject;
	}

	/**
	 * Returns the type
	 */
	public PrimTypes getType() {
		return realType;
	}

	/** true if using the class-type of the primitive */
	public boolean isClass() {
		return realType.isClass();
	}

	public String getCast() {
		return "(" + clazzType.getClassName() + ")";
	}

	static PrimitiveClazz[] createPrimitive(TestCluster cluster) {
		PrimitiveClazz[] ret = new PrimitiveClazz[16];

		PrimitiveClazz[] equiv = new PrimitiveClazz[2];
		PrimitiveClazz o = new PrimitiveClazz(cluster, PrimTypes.BooleanClass, equiv);
		ret[0] = equiv[0] = o.clazzObject;
		ret[1] = equiv[1] = o.clazzType;

		equiv = new PrimitiveClazz[2];
		PrimitiveClazz c = new PrimitiveClazz(cluster, PrimTypes.CharacterClass, equiv);
		ret[2] = equiv[0] = c.clazzObject;
		ret[3] = equiv[1] = c.clazzType;

		equiv = new PrimitiveClazz[12];
		PrimitiveClazz b = new PrimitiveClazz(cluster, PrimTypes.ByteClass, equiv);
		ret[4] = equiv[0] = b.clazzObject;
		ret[5] = equiv[1] = b.clazzType;

		PrimitiveClazz i = new PrimitiveClazz(cluster, PrimTypes.IntegerClass, equiv);;
		ret[6] = equiv[2] = i.clazzObject;
		ret[7] = equiv[3] = i.clazzType;

		PrimitiveClazz s = new PrimitiveClazz(cluster, PrimTypes.ShortClass, equiv);
		ret[8] = equiv[4] = s.clazzObject;
		ret[9] = equiv[5] = s.clazzType;

		PrimitiveClazz l = new PrimitiveClazz(cluster, PrimTypes.LongClass, equiv);
		ret[10] = equiv[6] = l.clazzObject;
		ret[11] = equiv[7] = l.clazzType;

		PrimitiveClazz f = new PrimitiveClazz(cluster, PrimTypes.FloatClass, equiv);
		ret[12] = equiv[8] = f.clazzObject;
		ret[13] = equiv[9] = f.clazzType;

		PrimitiveClazz d = new PrimitiveClazz(cluster, PrimTypes.DoubleClass, equiv);
		ret[14] = equiv[10] = d.clazzObject;
		ret[15] = equiv[11] = d.clazzType;

		return ret;
	}

	@Override
	void calculateAssignableTo() throws ClassNotFoundException {
		Set<Clazz> builder = new HashSet<Clazz>();

		// process primitive types (equivalent primitive types are stored in assignableTo)
		for(Clazz clazz : assignableTo)
			if(cluster.contains(clazz)) builder.add(clazz);

		// process superclasses and interfaces
		Set<Class<?>> todo = new HashSet<Class<?>>();

		Class<?> c = toJavaClass();
		if(c.isInterface()) todo.add(c);
		while(c != null) {
			Clazz clazz = cluster.getRegistry().getClazzIfExists(c);
			if(cluster.contains(clazz)) builder.add(clazz);

			for(Class<?> i : c.getInterfaces())
				insertInterfaceWithParents(todo, i);

			c = c.getSuperclass();
		}

		Set<Class<?>> done = new HashSet<Class<?>>();
		for(Class<?> i : todo)
			if(!done.contains(i)) {
				Clazz clazz = cluster.getRegistry().getClazzIfExists(i);
				if(cluster.contains(clazz)) {
					done.add(i);
					builder.add(clazz);
				}
			}

		assignableTo = builder.toArray(new Clazz[builder.size()]);
	}

	public static void refine(Set<Clazz> set) {
		Set<PrimitiveClazz> toRemove = new HashSet<PrimitiveClazz>();
		Set<PrimitiveClazz> toAdd = new HashSet<PrimitiveClazz>();

		for(Clazz c : set)
			if(c instanceof PrimitiveClazz) {
				toAdd.add(((PrimitiveClazz) c).clazzObject);
				toRemove.add(((PrimitiveClazz) c).clazzType);
			}

		for(Clazz c : toAdd)
			set.add(c);
		for(Clazz c : toRemove)
			set.remove(c);
	}

	public Object cast(Object object) {
		if(object instanceof Number) {
			Number num = (Number) object;
			switch(realType) {
				case ByteClass:
				case ByteType:
					return num.byteValue();
				case ShortClass:
				case ShortType:
					return num.shortValue();
				case IntegerClass:
				case IntegerType:
					return num.intValue();
				case LongClass:
				case LongType:
					return num.longValue();
				case FloatClass:
				case FloatType:
					return num.floatValue();
				case DoubleClass:
				case DoubleType:
					return num.doubleValue();
			}
		}

		if(object instanceof Character) return ((Character) object).charValue();

		if(object instanceof Boolean) return ((Boolean) object).booleanValue();

		System.err.println("ERR: cannot perform the conversion for type: " + object.getClass().getCanonicalName());
		return null;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + realType.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;
		if(!(obj instanceof PrimitiveClazz)) return false;

		return realType.equals(((PrimitiveClazz) obj).realType);
	}
}
