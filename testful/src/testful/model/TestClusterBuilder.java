package testful.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;

import testful.IConfigCut;
import testful.model.xml.XmlClass;
import testful.model.xml.XmlConstructor;
import testful.model.xml.XmlMethod;
import testful.model.xml.XmlRegistry;
import testful.utils.ClassComparator;
import testful.utils.ElementManager;

public class TestClusterBuilder {

	private final String cut;

	private final ClassLoader classLoader;
	private final XmlRegistry xmlRegistry;

	/** all clazzes of the test cluster, including ALL primitive types, String, and all methods' return types */
	private final ElementManager<String, Clazz> all;

	/** the clazzes involved in the test cluster, that are those used as input parameters (for primitive types, only the Class is considered) */
	private final ElementManager<String, Clazz> cluster;

	private final AtomicInteger clazzIdGenerator = new AtomicInteger();
	private final AtomicInteger methodzIdGenerator = new AtomicInteger();
	private final AtomicInteger constructorzIdGenerator = new AtomicInteger();
	private final AtomicInteger staticValueIdGenerator = new AtomicInteger();

	public TestClusterBuilder(ClassLoader classLoader, IConfigCut config) throws ClassNotFoundException {

		cut = config.getCut();

		this.classLoader = classLoader;
		xmlRegistry = new XmlRegistry(classLoader, config);

		cluster = new ElementManager<String, Clazz>();

		all = new ElementManager<String, Clazz>();
		all.put(new Clazz(clazzIdGenerator.incrementAndGet(), "java.lang.String", false));
		for (Clazz c : PrimitiveClazz.createPrimitive(clazzIdGenerator))
			all.put(c);

		// calculate the test cluster and the set of types involved in the test (all)
		calculateCluster(cut);

		// calculate constructors, methods, and assignableTo
		for(Clazz c : all) {

			Class<?> javaClass;
			Set<Clazz> assignableToBuilder = new TreeSet<Clazz>();

			if(c instanceof PrimitiveClazz) {

				javaClass = ((PrimitiveClazz) c).isClass() ? classLoader.loadClass(c.getClassName()) : null;

				// primitive types have a pre-defined compatible types
				for (Clazz comPrime : c.getAssignableTo())
					if(cluster.get(comPrime.getClassName()) != null)
						assignableToBuilder.add(comPrime);

			} else {
				javaClass = classLoader.loadClass(c.getClassName());

				XmlClass xmlClass = xmlRegistry.getXmlClass(c);
				if(xmlClass != null) // java.lang.String usually does not have any XML description
					calculateMethods(c, xmlClass, javaClass);
			}

			while(javaClass != null) {
				Clazz clazz = cluster.get(javaClass.getName());
				if(clazz != null) assignableToBuilder.add(clazz);

				for(Class<?> i : getAllImplementedInterfaces(javaClass)) {
					Clazz iClazz = cluster.get(i.getName());
					if(iClazz != null) assignableToBuilder.add(iClazz);
				}

				javaClass = javaClass.getSuperclass();
			}

			c.setAssignableTo(assignableToBuilder.toArray(new Clazz[assignableToBuilder.size()]));
		}

		calculateConstants(classLoader);

		calculateSubClasses(classLoader);
	}

	private void calculateCluster(String cutClass) throws ClassNotFoundException {
		Set<String> toDo = new HashSet<String>();
		toDo.add(cutClass);

		while(!toDo.isEmpty()) {
			String className = toDo.iterator().next();
			toDo.remove(className);

			if(cluster.get(className) == null) {

				Clazz clazz = all.get(className);
				if(clazz != null) {
					cluster.put(clazz.getReferenceClazz());

				} else {
					Class<?> javaClass = classLoader.loadClass(className);
					XmlClass xmlClass = xmlRegistry.getXmlClass(className);

					clazz = new Clazz(clazzIdGenerator.incrementAndGet(), className, javaClass.isInterface() || Modifier.isAbstract(javaClass.getModifiers()));
					all.put(clazz);
					cluster.put(clazz);

					// Include types used in public fields
					for(Field f : javaClass.getFields()) {
						if(!StaticValue.skip(f))
							toDo.add(f.getType().getName());
					}

					// Consider constructors
					for(Constructor<?> cns : javaClass.getConstructors()) {
						XmlConstructor xmlCns = xmlClass.getConstructor(cns);
						if(xmlCns != null && !xmlCns.isSkip())
							for(Class<?> param : cns.getParameterTypes())
								toDo.add(param.getName());
					}

					// Consider methods
					for(Method meth : javaClass.getMethods()) {
						if(meth.getName().startsWith("__testful")) continue;

						XmlMethod xmlMeth = xmlClass.getMethod(meth);
						if(xmlMeth != null && !xmlMeth.isSkip()) {

							// add input parameters to the test cluster
							for(Class<?> param : meth.getParameterTypes())
								toDo.add(param.getName());

							// toDo.add(meth.getReturnType().getName());
						}
					}

					for(String aux : xmlClass.getCluster()) {
						if(cluster.get(aux) == null) toDo.add(aux);
					}
				}
			}
		}
	}

	private void calculateMethods(Clazz _class, XmlClass xmlClass, Class<?> javaClass) {

		// calculate methodz
		Set<Methodz> mlist = new TreeSet<Methodz>();
		for(Method meth : javaClass.getMethods()) {
			final XmlMethod xmlMethod = xmlClass.getMethod(meth);
			if(xmlMethod != null && !xmlMethod.isSkip()) {

				Clazz returnType;
				// ISSUE #1: if you need enum support, vote here: http://code.google.com/p/testful/issues/detail?id=1
				if(meth.getReturnType() ==  Void.TYPE || meth.getReturnType().isArray() || meth.getReturnType().isEnum())
					returnType = null;
				else
					returnType = get(meth.getReturnType());

				mlist.add(new Methodz(methodzIdGenerator.incrementAndGet(), Modifier.isStatic(meth.getModifiers()), returnType, _class, meth.getName(), get(meth.getParameterTypes()), xmlMethod));
			}
		}
		Methodz[] methods = mlist.toArray(new Methodz[mlist.size()]);
		Arrays.sort(methods);
		_class.setMethods(methods);

		// calculate constructorz
		if(!_class.isAbstract()) {
			Set<Constructorz> clist = new TreeSet<Constructorz>();
			for(Constructor<?> cns : javaClass.getConstructors()) {
				final XmlConstructor xmlCns = xmlClass.getConstructor(cns);
				if(xmlCns != null && !xmlCns.isSkip())
					clist.add(new Constructorz(constructorzIdGenerator.incrementAndGet(), _class, get(cns.getParameterTypes()), xmlCns));
			}
			Constructorz[] constructors = clist.toArray(new Constructorz[clist.size()]);
			Arrays.sort(constructors);
			_class.setConstructors(constructors);
		}
	}

	/** requires: assignableTo calculated */
	private void calculateConstants(ClassLoader classLoader) throws SecurityException, ClassNotFoundException {
		Map<Clazz, Set<StaticValue>> fieldMap = new HashMap<Clazz, Set<StaticValue>>();

		for(Clazz cz : cluster) {

			for(Field field : classLoader.loadClass(cz.getClassName()).getFields()) {
				Class<?> fieldType = field.getType();

				if(StaticValue.skip(field)) continue;

				Clazz fieldTypez = get(fieldType);
				if(fieldTypez == null) continue;

				StaticValue sv = new StaticValue(staticValueIdGenerator.incrementAndGet(), cz, fieldTypez, field.getName());
				for(Clazz d : fieldTypez.getAssignableTo()) {
					if(cluster.get(d.getClassName()) != null) {
						Set<StaticValue> fields = fieldMap.get(d);
						if(fields == null) {
							fields = new TreeSet<StaticValue>();
							fieldMap.put(d, fields);
						}
						fields.add(sv);
					}
				}
			}
		}

		for(Clazz c : fieldMap.keySet()) {
			Set<StaticValue> fields = fieldMap.get(c);
			c.setConstants(fields.toArray(new StaticValue[fields.size()]));
		}
	}

	private void calculateSubClasses(ClassLoader classLoader) throws ClassNotFoundException {
		// Father => sons
		Map<Clazz, Set<Clazz>> sonMap = new HashMap<Clazz, Set<Clazz>>();

		for(Clazz cz : cluster) {
			Class<?> c = classLoader.loadClass(cz.getClassName());

			// all cz's parents
			Set<Class<?>> parents = new TreeSet<Class<?>>(ClassComparator.singleton);
			c = c.getSuperclass();
			while(c != null) {
				parents.add(c);
				for(Class<?> i : c.getInterfaces())
					insertInterfaceWithParents(parents, i);

				c = c.getSuperclass();
			}

			for(Class<?> p : parents) {
				Clazz pz = cluster.get(p.getName());
				if(pz != null) {
					Set<Clazz> sons = sonMap.get(pz);
					if(sons == null) {
						sons = new TreeSet<Clazz>();
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

	static Set<Class<?>> getAllImplementedInterfaces(Class<?> javaClass) {
		Set<Class<?>> ret = new HashSet<Class<?>>();

		for (Class<?> i : javaClass.getInterfaces())
			insertInterfaceWithParents(ret, i);

		return ret;
	}

	private static void insertInterfaceWithParents(Set<Class<?>> set, Class<?> i) {
		if(set.add(i)) // if i is a new interface
			for(Class<?> ext : i.getInterfaces())
				insertInterfaceWithParents(set, ext);
	}

	private Clazz get(Class<?> c) {
		return all.get(c.getName());
	}

	private Clazz[] get(Class<?>[] c) {
		Clazz[] ret = new Clazz[c.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = get(c[i]);
		return ret;
	}

	public TestCluster getTestCluster() {

		Clazz[] _cluster = cluster.toArray(new Clazz[cluster.size()]);
		Arrays.sort(_cluster);

		Clazz[] _all = all.toArray(new Clazz[all.size()]);
		Arrays.sort(_all);

		return new TestCluster(cluster.get(cut), _cluster, _all);
	}

	/**
	 * @return the xmlRegistry
	 */
	public XmlRegistry getXmlRegistry() {
		return xmlRegistry;
	}
}
