package testful.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import testful.model.xml.XmlClass;

public class Clazz implements Serializable {
	private static final long serialVersionUID = 5752705690732971069L;

	private static final boolean PRINT_VERBOSE = false;

	/** true if it is an abstract class or an interface */
	private final boolean isAbstract;
	
	private transient Class<?> javaClass;
	private final String name;
	private final boolean hasContracts;
	protected final TestCluster cluster;

	/** constructor of the clazz */
	private Constructorz[] constructors;

	/** methods of the clazz */
	private Methodz[] methods;

	/** constant assignable to the class */
	private StaticValue[] constants = new StaticValue[0];

	/**
	 * where can I store this class?<br>
	 * <i>Calculated by TestCluster.setup()</i>
	 */
	protected Clazz[] assignableTo = new Clazz[0];
	/**
	 * what can I use instead of this class?<br>
	 * <i>Calculated by TestCluster.setup()</i>
	 */
	private Clazz[] subClasses = new Clazz[0];

	private final int hashCode;
	
	Clazz(TestCluster cluster, Class<?> type) {
		javaClass = type;
		name = type.getCanonicalName();
		this.cluster = cluster;
		hasContracts = org.jmlspecs.jmlrac.runtime.JMLCheckable.class.isAssignableFrom(type);
		
		this.isAbstract = type.isInterface() || Modifier.isAbstract(type.getModifiers());
		
		this.hashCode = name.hashCode();
	}

	void setup(XmlClass xml) throws SecurityException, ClassNotFoundException {
		// calculate methodz
		Set<Methodz> list = new HashSet<Methodz>();
		for(Method meth : toJavaClass().getMethods())
			if(!Methodz.toSkip(meth)) // add the method to the set
				list.add(new Methodz(cluster, this, meth, xml != null ? xml.getMethod(meth) : null));
		methods = list.toArray(new Methodz[list.size()]);

		// calculate constructorz
		int i = 0;
		constructors = new Constructorz[toJavaClass().getConstructors().length];
		for(Constructor<?> c : toJavaClass().getConstructors())
			constructors[i++] = new Constructorz(cluster, c, xml != null ? xml.getConstructor(c) : null);
	}

	/**
	 * Check if the class is an abstract class or an interface
	 * @return true if it is an abstract class or an interface
	 */
	public boolean isAbstract() {
		return isAbstract;
	}
	
	public Class<?> toJavaClass() throws ClassNotFoundException {
		if(javaClass == null) javaClass = cluster.loadClass(name);

		return javaClass;
	}

	/**
	 * Clear the cache: discard the Class reference and forces constructors,
	 * methods, and staticValues to discard their cache
	 */
	public void clearCache() {
		javaClass = null;

		if(constructors != null) for(Constructorz cns : constructors)
			cns.clearCache();

		if(methods != null) for(Methodz m : methods)
			m.clearCache();

		if(constants != null) for(StaticValue c : constants)
			c.clearCache();
	}

	public Clazz getReferenceClazz() {
		return this;
	}

	@Override
	public String toString() {
		if(!PRINT_VERBOSE) return name;
		else {
			StringBuilder sb = new StringBuilder();
			sb.append(name).append("\n");

			if(constants != null) for(StaticValue sv : constants)
				sb.append("  sv : ").append(sv).append("\n");

			if(constructors != null) for(Constructorz cns : constructors)
				sb.append("  cns: ").append(cns).append("\n");

			if(methods != null) for(Methodz meth : methods)
				sb.append("  meth: ").append(meth).append("\n");

			return sb.toString();
		}
	}

	public boolean hasContracts() {
		return hasContracts;
	}

	public String getClassName() {
		return name;
	}

	void setConstants(StaticValue[] constants) {
		this.constants = constants;
	}

	public StaticValue[] getConstants() {
		return constants;
	}

	public Methodz[] getMethods() {
		return methods;
	}

	public Constructorz[] getConstructors() {
		return constructors;
	}

	void calculateAssignableTo() throws ClassNotFoundException {
		Set<Clazz> destinoBuilder = new HashSet<Clazz>();

		/** store all interfaces to process which the class implements */
		Set<Class<?>> todo = new HashSet<Class<?>>();

		Class<?> c = toJavaClass();
		if(c.isInterface()) todo.add(c);
		while(c != null) {
			Clazz clazz = cluster.getRegistry().getClazzIfExists(c);
			if(cluster.contains(clazz)) destinoBuilder.add(clazz);

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
					destinoBuilder.add(clazz);
				}
			}

		assignableTo = destinoBuilder.toArray(new Clazz[destinoBuilder.size()]);
	}

	/** Retrieve the clazz belonging to the cluster, able to store this type */
	public Clazz[] getAssignableTo() {
		return assignableTo;
	}

	void setSubClasses(Clazz[] subClasses) {
		this.subClasses = subClasses;
	}

	public Clazz[] getSubClasses() {
		return subClasses;
	}

	public static Class<?>[] convert(Clazz[] c) throws ClassNotFoundException {
		Class<?>[] ret = new Class<?>[c.length];
		for(int i = 0; i < c.length; i++)
			ret[i] = c[i].toJavaClass();
		return ret;
	}

	static void insertInterfaceWithParents(Set<Class<?>> set, Class<?> i) {
		if(i.getPackage() != null && i.getPackage().getName().startsWith("org.jmlspecs.")) return;

		if(set.add(i)) // if i is a new interface
			for(Class<?> ext : i.getInterfaces())
				insertInterfaceWithParents(set, ext);
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Clazz)) return false;

		return name.equals(((Clazz) obj).name);
	}
}
