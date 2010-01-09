package testful.model;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import testful.IConfigCut;
import testful.TestfulException;
import testful.model.xml.Parser;
import testful.model.xml.XmlAux;
import testful.model.xml.XmlClass;

public class TestCluster implements Serializable {

	private static final Logger logger = Logger.getLogger("testful.model");

	private static final long serialVersionUID = -3821344984330181680L;

	public class ClassRegistry {

		/** For each class retrieve the corresponding Clazz */
		private final Map<Class<?>, Clazz> registry;

		/** creates an empty registry (with primitive types loaded) */
		public ClassRegistry() {
			registry = new HashMap<Class<?>, Clazz>();

			for(PrimitiveClazz c : PrimitiveClazz.createPrimitive(TestCluster.this))
				registry.put(c.toJavaClass(), c);
		}

		public ClassRegistry(Clazz[] all) throws ClassNotFoundException {
			registry = new HashMap<Class<?>, Clazz>();

			for(Clazz c : all)
				registry.put(c.toJavaClass(), c);
		}

		public Clazz getClazz(Class<?> type) {
			Clazz ret = registry.get(type);
			if(ret == null) {
				ret = new Clazz(TestCluster.this, type);
				registry.put(type, ret);
			}
			return ret;
		}

		public Clazz getClazzIfExists(Class<?> type) {
			return registry.get(type);
		}

		/** to use only when the test cluster is restored from a serialized version */
		void addClazz(Clazz c) throws ClassNotFoundException {
			registry.put(c.toJavaClass(), c);
		}

		public Clazz[] convert(Class<?>[] c) {
			Clazz[] ret = new Clazz[c.length];
			for(int i = 0; i < c.length; i++)
				ret[i] = getClazz(c[i]);
			return ret;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();

			for(Clazz c : registry.values()) {
				sb.append(c.getClassName()).append("\n");

				sb.append("  Constants:");
				for(StaticValue v : c.getConstants())
					sb.append(v).append(" ");
				sb.append("\n");

				sb.append("  Assignable To:");
				for(Clazz d : c.getAssignableTo())
					sb.append(d.getClassName()).append(" ");
				sb.append("\n");

				sb.append("  SubClasses:");
				for(Clazz d : c.getSubClasses())
					sb.append(d.getClassName()).append(" ");
				sb.append("\n");
			}

			return sb.toString();
		}
	}

	/** Class Under Test */
	private final Clazz cut;

	/** All classes required for the test (CUT + AUX) */
	private final Clazz[] cluster;

	/** All known classes */
	private final Clazz[] all;

	private transient ClassRegistry registry;
	private transient ClassLoader classLoader;

	private transient Map<String, XmlClass> xml;

	public TestCluster(ClassLoader classLoader, IConfigCut config) throws ClassNotFoundException {
		Set<Clazz> clusterBuilder = new HashSet<Clazz>();
		Set<Clazz> toDo = new HashSet<Clazz>();
		registry = new ClassRegistry();
		this.classLoader = classLoader;

		cut = getRegistry().getClazz(classLoader.loadClass(config.getCut()));
		clusterBuilder.add(cut);
		addClazz(toDo, cut, config);

		// adding aux classes (as declared in xmls descriptors)
		while(!toDo.isEmpty()) {
			Clazz clazz = toDo.iterator().next();
			toDo.remove(clazz);

			if(!clusterBuilder.contains(clazz)) {
				clusterBuilder.add(clazz);
				addClazz(toDo, clazz, config);
			}

			if(clazz instanceof PrimitiveClazz) continue;

			XmlClass xmlClass = xml.get(clazz.getClassName());
			if(xmlClass != null && xmlClass.getAux() != null) {
				for(XmlAux aux : xmlClass.getAux())
					if(aux.getName() != null)
						toDo.add(getRegistry().getClazz(this.classLoader.loadClass(aux.getName())));
			}

		}

		PrimitiveClazz.refine(clusterBuilder);

		cluster = clusterBuilder.toArray(new Clazz[clusterBuilder.size()]);
		all = registry.registry.values().toArray(new Clazz[registry.registry.size()]);

		try {
			for(Clazz c : cluster)
				c.setup(xml.get(c.getClassName()));

			// for each known class
			for(Clazz c : all)
				c.calculateAssignableTo();

			calculateConstants();

			calculateSubClasses();
		} catch(ClassNotFoundException e) {
			// if happens, it is really weird
			logger.log(Level.WARNING, "Cannot find a class: " + e.getMessage(), e);
		}
	}

	private void addClazz(Set<Clazz> todo, Clazz clazz, IConfigCut config) throws ClassNotFoundException {
		todo.add(clazz);

		Class<?> javaClass = clazz.toJavaClass();

		if(xml == null) xml = new HashMap<String, XmlClass>();

		if(!xml.containsKey(clazz.getClassName())) {
			try {
				XmlClass xmlClass = Parser.singleton.parse(config, clazz.getClassName());
				xml.put(clazz.getClassName(), xmlClass);
			} catch(JAXBException e) {
				logger.log(Level.WARNING, "Cannot parse XML descriptor of class " + clazz.getClassName() + ": " + e.getMessage());
			}
		}

		// Inserting in the test cluster all input parameters of constructors of CUT
		for(Constructor<?> cns : javaClass.getConstructors())
			if(Modifier.isPublic(cns.getModifiers()))
				for(Class<?> param : cns.getParameterTypes())
					todo.add(getRegistry().getClazz(param));

		// Inserting in the test cluster all input parameters of methods of CUT
		for(Method meth : javaClass.getMethods())
			if(!Methodz.toSkip(meth))
				for(Class<?> param : meth.getParameterTypes())
					todo.add(getRegistry().getClazz(param));
	}

	/**
	 * Checks if it is possible to obtain objects
	 * (using constructors, constants, or values returned by methods)
	 * for each class it contains.<br/>
	 * If it is not the case, the method throws an exception,
	 * listing all the missing classes.
	 *
	 * @throws MissingClassException if some class is missing
	 */
	public void isValid() throws MissingClassException {
		Set<Clazz> missing = new HashSet<Clazz>();
		for (Clazz c : cluster)
			missing.add(c);

		for (Clazz c : cluster) {
			if(c.getConstructors().length > 0)
				for (Clazz assignable : c.getAssignableTo())
					missing.remove(assignable);

			if(missing.isEmpty()) return;

			for (Methodz m : c.getMethods()) {
				Clazz ret = m.getReturnType();
				if(ret != null)
					for (Clazz assignable : ret.getAssignableTo())
						missing.remove(assignable);
			}

			if(missing.isEmpty()) return;

			for (StaticValue sv : c.getConstants())
				for (Clazz assignable : sv.getType().getAssignableTo())
					missing.remove(assignable);

			if(missing.isEmpty()) return;

		}

		// just in case...
		if(missing.isEmpty()) return;

		throw new MissingClassException(missing, cut);

	}

	public static class MissingClassException extends TestfulException {
		private static final long serialVersionUID = -7271283808569139038L;

		/** Contains the missing classes */
		public final Set<String> missing;
		/** if true, it is critical */
		public final boolean fatal;

		public MissingClassException(Set<Clazz> missing, Clazz cut) {
			super("Some classes are missing");

			Set<String> tmp = new HashSet<String>();
			for (Clazz c : missing) tmp.add(c.getClassName());
			this.missing = Collections.unmodifiableSet(tmp);

			fatal = calculateFatal(missing, cut);
		}

		private static boolean calculateFatal(Set<Clazz> missing, Clazz cut) {
			if(missing.contains(cut)) return true;

			for (Constructorz cns : cut.getConstructors())
				for (Clazz p : cns.getParameterTypes())
					if(missing.contains(p)) return true;

			for (Methodz m : cut.getMethods())
				for (Clazz p : m.getParameterTypes())
					if(missing.contains(p)) return true;

			return false;
		}
	}

	public Collection<String> getClassesToInstrument() {
		Collection<String> ret = new TreeSet<String>();

		for(Clazz c : all) {
			XmlClass xmlClazz = xml.get(c.getClassName());
			if(xmlClazz != null && xmlClazz.isInstrument()) ret.add(c.getClassName());
		}

		return ret;
	}

	/**
	 * When the testCluster is deserialized, use this method to set the
	 * classLoader
	 */
	public void setClassLoader(ClassLoader classLoader) throws ClassNotFoundException {
		if(this.classLoader == classLoader) return;

		clearCache();
		this.classLoader = classLoader;
		registry = new ClassRegistry(all);
	}

	/**
	 * discards the cached
	 * <code>Class<code>es, making the testCluster like a deserialized one.
	 */
	public void clearCache() {
		registry = null;
		classLoader = null;
		for(Clazz c : all)
			c.clearCache();
	}

	public String[] getClasses() {
		String[] ret = new String[cluster.length];

		for(int i = 0; i < cluster.length; i++)
			ret[i] = cluster[i].getClassName();

		return ret;
	}

	public ClassRegistry getRegistry() {
		if(registry == null) // if loaded from a serialized version, fill the registry!
			try {
				registry = new ClassRegistry(all);
			} catch(ClassNotFoundException e) {
				logger.log(Level.SEVERE, "Cannot create the registry: " + e.getMessage(), e);

				registry = new ClassRegistry();
			}

			return registry;
	}

	public int getClusterSize() {
		return cluster.length;
	}

	public Clazz getCluster(int pos) {
		return cluster[pos];
	}

	public Clazz getCut() {
		return cut;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();

		ret.append("CUT: ").append(cut.getClassName()).append("\n");
		ret.append("Test Cluster: ");
		for(Clazz c : cluster)
			ret.append("\n  ").append(c.getClassName());

		ret.append("\nregistry:\n");
		for(Clazz c : registry.registry.values()) {
			ret.append(c.getClassName()).append(" -> ");
			for(Clazz to : c.getAssignableTo())
				ret.append(to.getClassName()).append(" ");

			ret.append("\n");
		}

		return ret.toString();
	}

	public Clazz[] getCluster() {
		return cluster;
	}

	Class<?> loadClass(String name) throws ClassNotFoundException {
		if(classLoader == null) throw new ClassNotFoundException("The classloader is not set");

		return classLoader.loadClass(name);
	}

	/** requires: destino is calculated */
	private void calculateConstants() throws SecurityException, ClassNotFoundException {
		Map<Clazz, Set<StaticValue>> fieldMap = new HashMap<Clazz, Set<StaticValue>>();

		for(Clazz cz : cluster) {
			for(Field field : cz.toJavaClass().getFields()) {
				Class<?> fieldType = field.getType();
				String fieldName = field.getName();

				if((field.getModifiers() & (Modifier.STATIC | Modifier.PUBLIC)) == 0) continue;

				if(fieldName.startsWith("__")) continue;
				if(cz.hasContracts() && fieldName.startsWith("rac$")) continue;

				Clazz fieldClazz = registry.getClazzIfExists(fieldType);
				if(fieldClazz == null || !contains(fieldClazz)) continue;

				for(Clazz d : fieldClazz.getAssignableTo()) {
					Set<StaticValue> fields = fieldMap.get(d);
					if(fields == null) {
						fields = new HashSet<StaticValue>();
						fieldMap.put(d, fields);
					}
					fields.add(new StaticValue(this, field));
				}
			}
			for(Clazz c : fieldMap.keySet()) {
				Set<StaticValue> fields = fieldMap.get(c);
				c.setConstants(fields.toArray(new StaticValue[fields.size()]));
			}
		}
	}

	private void calculateSubClasses() throws ClassNotFoundException {
		// Father => sons
		Map<Clazz, Set<Clazz>> sonMap = new HashMap<Clazz, Set<Clazz>>();

		for(Clazz cz : cluster) {
			Class<?> c = cz.toJavaClass();

			// all cz's parents
			Set<Class<?>> parents = new HashSet<Class<?>>();
			c = c.getSuperclass();
			while(c != null) {
				parents.add(c);
				for(Class<?> i : c.getInterfaces())
					Clazz.insertInterfaceWithParents(parents, i);

				c = c.getSuperclass();
			}

			for(Class<?> p : parents) {
				Clazz pz = registry.getClazzIfExists(p);
				if(contains(pz)) {
					Set<Clazz> sons = sonMap.get(pz);
					if(sons == null) {
						sons = new HashSet<Clazz>();
						sonMap.put(pz, sons);
					}
					sons.add(cz);
				}
			}
		}

		for(Clazz c : sonMap.keySet()) {
			Set<Clazz> sons = sonMap.get(c);
			c.setSubClasses(sons.toArray(new Clazz[sons.size()]));
		}
	}

	public boolean contains(Clazz clazz) {
		if(clazz == null) return false;

		for(Clazz c : cluster)
			if(c.equals(clazz)) return true;

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cut.hashCode();
		result = prime * result + Arrays.hashCode(cluster);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof TestCluster)) return false;

		TestCluster other = (TestCluster) obj;
		return cut.equals(other.cut) && Arrays.equals(cluster, other.cluster);
	}

	/**
	 * Adapts a clazz belonging to another test cluster to a clazz belonging to
	 * this test cluster
	 * 
	 * @param clazz the clazz
	 * @return a class belonging to this test cluster
	 */
	public Clazz adapt(Clazz clazz) {
		for(Clazz c : all)
			if(c.equals(clazz)) return c;

		logger.warning("Cannot adapt " + clazz);
		return null;
	}

	/**
	 * Adapts a method belonging to another test cluster to a method belonging to
	 * this test cluster
	 * 
	 * @param method the method
	 * @return a method belonging to this test cluster
	 */
	public Methodz adapt(Methodz method) {
		if(method == null) return null;

		Clazz thisClass = adapt(method.getClazz());
		if(thisClass == null) return null;

		for(Methodz m : thisClass.getMethods())
			if(m.equals(method)) return m;

		logger.warning("Cannot adapt " + method);
		return null;
	}

	/**
	 * Adapts a constructor belonging to another test cluster to a constructor
	 * belonging to this test cluster
	 * 
	 * @param cns the constructor
	 * @return a constructor belonging to this test cluster
	 */
	public Constructorz adapt(Constructorz cns) {
		if(cns == null) return null;

		Clazz thisClass = adapt(cns.getClazz());
		if(thisClass == null) return null;

		for(Constructorz c : thisClass.getConstructors())
			if(c.equals(cns)) return c;

		logger.warning("Cannot adapt " + cns);
		return null;
	}

	/**
	 * Adapts a constant belonging to another test cluster to a constant belonging
	 * to this test cluster
	 * 
	 * @param sv the constant
	 * @return a constant belonging to this test cluster
	 */
	public StaticValue adapt(StaticValue sv) {
		if(sv == null) return null;

		Clazz thisClass = adapt(sv.getDeclaringClass());
		if(thisClass == null) return null;

		for(StaticValue v : thisClass.getConstants())
			if(v.equals(sv)) return v;

		logger.warning("WARN: cannot adapt " + sv);
		return null;
	}
}
