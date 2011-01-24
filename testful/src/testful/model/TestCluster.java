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

import java.io.Serializable;
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
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.IConfigCut;
import testful.TestfulException;
import testful.model.xml.XmlClass;
import testful.model.xml.XmlConstructor;
import testful.model.xml.XmlMethod;
import testful.runner.TestfulClassLoader;
import testful.utils.ClassComparator;

public class TestCluster implements Serializable {

	private static final Logger logger = Logger.getLogger("testful.model");

	private static final long serialVersionUID = -3821344984330181680L;

	public static class ClassRegistry {

		/** For each class retrieve the corresponding Clazz */
		private final Map<Class<?>, Clazz> registry;

		/** creates an empty registry (with primitive types loaded) */
		public ClassRegistry(ClazzRegistry clazzRegistry) {
			registry = new HashMap<Class<?>, Clazz>();

			try {
				for(PrimitiveClazz c : PrimitiveClazz.createPrimitive())
					registry.put(clazzRegistry.getClass(c), c);
			} catch (ClassNotFoundException e) {
				// never happens
			}
		}

		public Clazz getClazz(Class<?> type) {
			Clazz ret = registry.get(type);
			if(ret == null) {
				ret = new Clazz(type.getName(), type.isInterface() || Modifier.isAbstract(type.getModifiers()));
				registry.put(type, ret);
			}
			return ret;
		}

		public Clazz getClazzIfExists(Class<?> type) {
			return registry.get(type);
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

	/** All classes required for the test (CUT + AUX). It is sorted according to the name of the classes */
	private final Clazz[] cluster;

	/** All known classes */
	private final Clazz[] all;

	public TestCluster(TestfulClassLoader classLoader, IConfigCut config) throws ClassNotFoundException {
		SortedSet<Clazz> clusterBuilder = new TreeSet<Clazz>();
		Set<Clazz> toDo = new HashSet<Clazz>();
		Map<String,XmlClass> xml = new HashMap<String, XmlClass>();

		ClazzRegistry clazzRegistry = new ClazzRegistry(classLoader);
		ClassRegistry classRegistry = new ClassRegistry(clazzRegistry);

		cut = classRegistry.getClazz(classLoader.loadClass(config.getCut()));
		clusterBuilder.add(cut);
		addClazz(toDo, cut, config, xml, classRegistry, clazzRegistry);

		// adding aux classes (as declared in xmls descriptors)
		while(!toDo.isEmpty()) {
			Clazz clazz = toDo.iterator().next();
			toDo.remove(clazz);

			if(clazz instanceof PrimitiveClazz || clazz.getClassName().equals("java.lang.String")) {
				clusterBuilder.add(clazz);
				continue;
			}

			if(!clusterBuilder.contains(clazz)) {
				clusterBuilder.add(clazz);
				addClazz(toDo, clazz, config, xml, classRegistry, clazzRegistry);
			}

			XmlClass xmlClass = xml.get(clazz.getClassName());
			if(xmlClass != null) {
				for(String aux : xmlClass.getCluster()) {
					final Clazz clusterClazz = classRegistry.getClazz(classLoader.loadClass(aux));
					if(!clusterBuilder.contains(clusterClazz))
						toDo.add(clusterClazz);
				}
			}
		}

		PrimitiveClazz.refine(clusterBuilder);

		cluster = clusterBuilder.toArray(new Clazz[clusterBuilder.size()]);

		for(Clazz c : cluster)
			c.calculateMethods(this, xml.get(c.getClassName()), clazzRegistry, classRegistry);

		// for each known class
		all = classRegistry.registry.values().toArray(new Clazz[classRegistry.registry.size()]);
		Arrays.sort(all);

		for(Clazz c : all)
			c.calculateAssignableTo(this, clazzRegistry, classRegistry);

		calculateConstants(classRegistry, clazzRegistry);

		calculateSubClasses(classRegistry, clazzRegistry);

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

	private void addClazz(Set<Clazz> todo, Clazz clazz, IConfigCut config, Map<String, XmlClass> xml, ClassRegistry registry, ClazzRegistry clazzRegistry) throws ClassNotFoundException {
		todo.add(clazz);

		Class<?> javaClass = clazzRegistry.getClass(clazz);

		XmlClass xmlClass = xml.get(clazz.getClassName());
		if(xmlClass == null) {
			xmlClass = XmlClass.get(config, javaClass);
			if(xmlClass == null) return; // if null, ignore it!
			xml.put(clazz.getClassName(), xmlClass);
		}

		// Include types used in public fields
		for(Field f : javaClass.getFields()) {
			if(!skipField(f))
				registry.getClazz(f.getType());
		}

		// Consider constructors
		for(Constructor<?> cns : javaClass.getConstructors()) {
			XmlConstructor xmlCns = xmlClass.getConstructor(cns);
			if(xmlCns != null && !xmlCns.isSkip())
				for(Class<?> param : cns.getParameterTypes())
					todo.add(registry.getClazz(param));
		}

		// Consider methods
		for(Method meth : javaClass.getMethods()) {
			if(meth.getName().startsWith("__testful")) continue;

			XmlMethod xmlMeth = xmlClass.getMethod(meth);
			if(xmlMeth != null && !xmlMeth.isSkip()) {

				// add input parameters to the test cluster
				for(Class<?> param : meth.getParameterTypes())
					todo.add(registry.getClazz(param));
			}
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

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();

		ret.append("CUT: ").append(cut.getClassName()).append("\n");

		ret.append("Test Cluster: ");
		for(Clazz c : cluster)
			ret.append("\n  ").append(c.getClassName());

		if (logger.isLoggable(Level.FINEST)) {
			ret.append("\nALL:");
			for (Clazz c : all)
				ret.append("\n  ").append(c.getClassName());
		}

		return ret.toString();
	}

	public Clazz[] getCluster() {
		return cluster;
	}

	/** requires: destino is calculated */
	private void calculateConstants(ClassRegistry classRegistry, ClazzRegistry clazzRegistry) throws SecurityException, ClassNotFoundException {
		Map<Clazz, Set<StaticValue>> fieldMap = new HashMap<Clazz, Set<StaticValue>>();

		for(Clazz cz : cluster) {
			for(Field field : clazzRegistry.getClass(cz).getFields()) {
				Class<?> fieldType = field.getType();

				if(skipField(field)) continue;

				Clazz fieldClazz = classRegistry.getClazzIfExists(fieldType);
				if(fieldClazz == null) continue;

				for(Clazz d : fieldClazz.getAssignableTo()) {
					if(contains(d)) {
						Set<StaticValue> fields = fieldMap.get(d);
						if(fields == null) {
							fields = new TreeSet<StaticValue>();
							fieldMap.put(d, fields);
						}
						fields.add(new StaticValue(this, field, classRegistry));
					}
				}
			}

			for(Clazz c : fieldMap.keySet()) {
				Set<StaticValue> fields = fieldMap.get(c);
				c.setConstants(fields.toArray(new StaticValue[fields.size()]));
			}
		}
	}

	private boolean skipField(Field field) {

		final int modifiers = field.getModifiers();
		if(!Modifier.isPublic(modifiers)) return true;
		if(!Modifier.isStatic(modifiers)) return true;

		// ISSUE #1: if you need array support, vote here: http://code.google.com/p/testful/issues/detail?id=1
		if(field.getType().isArray()) return true;
		if(field.getType().isEnum()) return true;

		// testful's related fields start with a double underscore
		if(field.getName().startsWith("__")) return true;

		if(field.getType().getName().startsWith("testful.")) return true;
		if(field.getDeclaringClass().getName().startsWith("testful.")) return true;

		return false;
	}

	private void calculateSubClasses(ClassRegistry registry, ClazzRegistry clazzRegistry) throws ClassNotFoundException {
		// Father => sons
		Map<Clazz, Set<Clazz>> sonMap = new HashMap<Clazz, Set<Clazz>>();

		for(Clazz cz : cluster) {
			Class<?> c = clazzRegistry.getClass(cz);

			// all cz's parents
			Set<Class<?>> parents = new TreeSet<Class<?>>(ClassComparator.singleton);
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

		logger.warning("Cannot adapt class " + clazz + " " + Arrays.toString(all));
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
}