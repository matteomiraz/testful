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
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import testful.model.xml.XmlClass;
import testful.model.xml.XmlConstructor;
import testful.model.xml.XmlMethod;

public class Clazz implements Serializable, Comparable<Clazz> {
	private static final long serialVersionUID = 5752705690732971069L;

	/** true if it is an abstract class or an interface */
	private final boolean isAbstract;

	private final String name;

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

	Clazz(String name, boolean isAbstract) {
		this.name = name;
		this.isAbstract = isAbstract;

		hashCode = name.hashCode();
	}

	void calculateMethods(TestCluster cluster, XmlClass xml, ClazzRegistry registry) throws SecurityException, ClassNotFoundException {

		// if the XML is null (i.e., it is a primitive class)
		// do not consider methods of the class
		if(xml == null) {
			methods = new Methodz[0];
			constructors = new Constructorz[0];
			return;
		}

		// calculate methodz
		Set<Methodz> mlist = new TreeSet<Methodz>();
		for(Method meth : registry.getClass(this).getMethods()) {
			final XmlMethod xmlMethod = xml.getMethod(meth);
			if(xmlMethod != null && !xmlMethod.isSkip())
				mlist.add(new Methodz(cluster, this, meth, xmlMethod));
		}
		methods = mlist.toArray(new Methodz[mlist.size()]);
		Arrays.sort(methods);

		// calculate constructorz
		if(isAbstract()) {
			constructors = new Constructorz[0];

		} else {
			Set<Constructorz> clist = new TreeSet<Constructorz>();
			for(Constructor<?> cns : registry.getClass(this).getConstructors()) {
				final XmlConstructor xmlCns = xml.getConstructor(cns);
				if(xmlCns != null && !xmlCns.isSkip())
					clist.add(new Constructorz(cluster, cns, xmlCns));
			}
			constructors = clist.toArray(new Constructorz[clist.size()]);
			Arrays.sort(constructors);
		}
	}

	/**
	 * Check if the class is an abstract class or an interface
	 * @return true if it is an abstract class or an interface
	 */
	public boolean isAbstract() {
		return isAbstract;
	}

	public Clazz getReferenceClazz() {
		return this;
	}

	@Override
	public String toString() {
		return name;
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

	void calculateAssignableTo(TestCluster cluster, ClazzRegistry registry) throws ClassNotFoundException {
		Set<Clazz> destinoBuilder = new TreeSet<Clazz>();

		/** store all interfaces to process which the class implements */
		Set<Class<?>> todo = new HashSet<Class<?>>();

		Class<?> c = registry.getClass(this);
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

	static void insertInterfaceWithParents(Set<Class<?>> set, Class<?> i) {
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

	@Override
	public int compareTo(Clazz o) {
		return name.compareTo(o.name);
	}
}
