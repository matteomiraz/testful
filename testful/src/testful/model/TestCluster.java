/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.model;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.IConfigCut;
import testful.IConfigProject;
import testful.TestfulException;
import testful.model.xml.XmlClass;
import testful.model.xml.XmlConstructor;
import testful.model.xml.XmlMethod;
import testful.runner.ISerializable;
import testful.utils.ClassComparator;
import testful.utils.ElementManager;

public class TestCluster implements ISerializable {

	private static final long serialVersionUID = 6041896902165032348L;

	private static final Logger logger = Logger.getLogger("testful.model");

	private static class Builder {

		private final IConfigProject config;
		private final ClassLoader classLoader;

		/** all clazzes of the test cluster, including ALL primitive types, String, and all methods' return types */
		private final ElementManager<String, Clazz> all;

		/** the clazzes involved in the test cluster, that are those used as input parameters (for primitive types, only the Class is considered) */
		private final ElementManager<String, Clazz> cluster;

		private final AtomicInteger clazzIdGenerator = new AtomicInteger();
		private final AtomicInteger methodzIdGenerator = new AtomicInteger();
		private final AtomicInteger constructorzIdGenerator = new AtomicInteger();
		private final AtomicInteger staticValueIdGenerator = new AtomicInteger();

		public Builder(ClassLoader classLoader, IConfigCut config) throws ClassNotFoundException {
			this.classLoader = classLoader;
			this.config = config;

			cluster = new ElementManager<String, Clazz>();

			all = new ElementManager<String, Clazz>();
			all.put(new Clazz(clazzIdGenerator.incrementAndGet(), "java.lang.String", false));
			for (Clazz c : PrimitiveClazz.createPrimitive(clazzIdGenerator))
				all.put(c);

			//TODO: remove xmls!
			// calculate the test cluster and the set of types involved in the test (all)
			Map<String,XmlClass> xml = calculateCluster(config.getCut());

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

					XmlClass xmlClass = xml.get(c.getClassName());
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

		private Map<String, XmlClass> calculateCluster(String cutClass) throws ClassNotFoundException {
			Map<String, XmlClass> xml = new HashMap<String, XmlClass>();
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

						clazz = new Clazz(clazzIdGenerator.incrementAndGet(), className, javaClass.isInterface() || Modifier.isAbstract(javaClass.getModifiers()));
						all.put(clazz);
						cluster.put(clazz);

						XmlClass xmlClass = xml.get(className);
						if(xmlClass == null) {
							xmlClass = XmlClass.get(config, javaClass);
							assert(xmlClass != null);
							xml.put(className, xmlClass);
						}

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

			return xml;
		}

		private void calculateMethods(Clazz _class, XmlClass xmlClass, Class<?> javaClass) {

			// calculate methodz
			Set<Methodz> mlist = new TreeSet<Methodz>();
			for(Method meth : javaClass.getMethods()) {
				final XmlMethod xmlMethod = xmlClass.getMethod(meth);
				if(xmlMethod != null && !xmlMethod.isSkip()) {

					Clazz returnType;
					// ISSUE #1: if you need array support, vote here: http://code.google.com/p/testful/issues/detail?id=1
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

		public Clazz getClusterClazz(String name) {
			return cluster.get(name);
		}

		Clazz get(Class<?> c) {
			return all.get(c.getName());
		}

		Clazz[] get(Class<?>[] c) {
			Clazz[] ret = new Clazz[c.length];
			for (int i = 0; i < ret.length; i++)
				ret[i] = get(c[i]);
			return ret;
		}

		public Clazz[] getCluster() {
			Clazz[] ret = cluster.toArray(new Clazz[cluster.size()]);
			Arrays.sort(ret);
			return ret;
		}

		public Clazz[] getAll() {
			Clazz[] ret = all.toArray(new Clazz[all.size()]);
			Arrays.sort(ret);
			return ret;
		}
	}

	/** Class Under Test */
	private final Clazz cut;

	/** All classes required for the test (CUT + AUX). It is sorted according to the name of the classes */
	private final Clazz[] cluster;

	/** The whole set of clazzes involved in the test. It includes the cluster, primitive types, and all return types. */
	private final Clazz[] all;

	public TestCluster(ClassLoader classLoader, IConfigCut config) throws ClassNotFoundException {
		Builder builder = new Builder(classLoader, config);

		all = builder.getAll();
		cluster = builder.getCluster();
		cut = builder.getClusterClazz(config.getCut());

		if(logger.isLoggable(Level.CONFIG)) {
			StringBuilder sb = new StringBuilder("Test Cluster:");

			for (Clazz c : cluster) {
				sb.append("\nclass " + c.getClassName());
				for (Constructorz cns : c.getConstructors()) print(sb, cns.toString(), cns.getParameterTypes(), cns.getMethodInformation());
				for (Methodz m : c.getMethods()) print(sb, m.toString(), m.getParameterTypes(), m.getMethodInformation());
			}

			logger.config(sb.toString());
		}
	}

	private void print(StringBuilder sb, final String name, final Clazz[] params, final MethodInformation info) {

		sb.append("\n * [" + info.getType() + "] "  + name);

		for (int i = 0; i < info.getParameters().length; i++) {
			sb.append(String.format("\n     p%2d %s ", i, params[i].getClassName()));
			if(info.getParameters()[i].isMutated()) sb.append(" mutated");
			if(info.getParameters()[i].isCaptured()) sb.append(" captured");
			if(info.getParameters()[i].isCapturedByReturn()) sb.append(" capturedByReturn");
			if(info.getParameters()[i].getCaptureStateOf() != null && !info.getParameters()[i].getCaptureStateOf().isEmpty())
				sb.append(info.getParameters()[i].getCaptureStateOf());
		}
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
			if(!c.isAbstract())
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

		public MissingClassException(Set<Clazz> missing, Clazz cut) {
			super("Some classes are missing:" + missing);

			Set<String> tmp = new HashSet<String>();
			for (Clazz c : missing) tmp.add(c.getClassName());
			this.missing = Collections.unmodifiableSet(tmp);
		}
	}

	public String[] getClasses() {
		String[] ret = new String[cluster.length];

		for(int i = 0; i < cluster.length; i++)
			ret[i] = cluster[i].getClassName();

		return ret;
	}

	public Clazz getClazz(String name) {
		for (Clazz c : cluster) {
			if(c.getClassName().equals(name))
				return c;
		}
		return null;
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

	private transient Map<Integer, Clazz> clazzCache;
	private transient Map<Integer, Constructorz> constructorzCache;
	private transient Map<Integer, Methodz> methodzCache;
	private transient Map<Integer, StaticValue> staticValueCache;

	private void updateCaches() {
		clazzCache = new HashMap<Integer, Clazz>();
		constructorzCache = new HashMap<Integer, Constructorz>();
		methodzCache = new HashMap<Integer, Methodz>();
		staticValueCache = new HashMap<Integer, StaticValue>();

		for (Clazz c : all) {
			clazzCache.put(c.getId(), c);

			for (Constructorz cns : c.getConstructors())
				constructorzCache.put(cns.getId(), cns);

			for (Methodz meth : c.getMethods())
				methodzCache.put(meth.getId(), meth);

			for (StaticValue sv : c.getConstants())
				staticValueCache.put(sv.getId(), sv);
		}
	}

	public Clazz getClazzById(int id) {
		if(clazzCache == null)
			updateCaches();

		return clazzCache.get(id);
	}

	public Constructorz getConstructorById(int id) {
		if(constructorzCache == null)
			updateCaches();

		return constructorzCache.get(id);
	}

	public Methodz getMethodById(int id) {
		if(methodzCache == null)
			updateCaches();

		return methodzCache.get(id);
	}

	public StaticValue getStaticValueById(int id) {
		if(staticValueCache == null)
			updateCaches();

		return staticValueCache.get(id);
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();

		ret.append("CUT: ").append(cut.getClassName()).append("\n");

		ret.append("Test Cluster: ");
		for(Clazz c : cluster)
			ret.append("\n  ").append(c.getClassName());

		return ret.toString();
	}

	public Clazz[] getCluster() {
		return cluster;
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
		final int idx = Arrays.binarySearch(all, clazz);
		if(idx >= 0) return all[idx];

		logger.warning("Cannot adapt class " + clazz + " " + Arrays.toString(cluster));
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

		final Clazz thisClass = adapt(method.getClazz());
		if(thisClass == null) return null;
		final Methodz[] methods = thisClass.getMethods();

		final int idx = Arrays.binarySearch(methods, method);
		if(idx >= 0) return methods[idx];

		logger.warning("Cannot adapt Method " + method + " " + Arrays.toString(methods));
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

		final Clazz thisClass = adapt(cns.getClazz());
		if(thisClass == null) return null;
		final Constructorz[] constructors = thisClass.getConstructors();

		final int idx = Arrays.binarySearch(constructors, cns);
		if(idx >= 0) return constructors[idx];

		logger.warning("Cannot adapt Constructor " + cns + " " + Arrays.toString(constructors));
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

		final Clazz thisClass = adapt(sv.getType());
		if(thisClass == null) return null;
		final StaticValue[] constants = thisClass.getConstants();

		final int idx = Arrays.binarySearch(constants, sv);
		if(idx >= 0) return constants[idx];

		logger.warning("cannot adapt StaticValue " + sv + " " + Arrays.toString(constants));
		return null;
	}

	private transient long iSerializableIdentifier;

	@Override
	public void setISerializableIdentifier(long id) {
		iSerializableIdentifier = id;
	}

	@Override
	public Long getISerializableIdentifier() {
		return iSerializableIdentifier;
	};
}