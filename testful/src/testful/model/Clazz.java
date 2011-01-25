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
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

import testful.model.TestCluster.Builder;
import testful.model.xml.XmlClass;
import testful.model.xml.XmlConstructor;
import testful.model.xml.XmlMethod;
import testful.utils.ElementWithKey;

public class Clazz implements Serializable, Comparable<Clazz>, ElementWithKey<String> {

	private static final long serialVersionUID = -2661635029405835812L;

	private static final Clazz[] NO_CLAZZES = new Clazz[0];
	private static final StaticValue[] NO_CONSTANTS = new StaticValue[0];
	private static final Constructorz[] NO_CONSTRUCTORS = new Constructorz[0];
	private static final Methodz[] NO_METHODS = new Methodz[0];

	/** true if it is an abstract class or an interface */
	private final boolean isAbstract;

	private final String name;

	/** constructor of the clazz */
	private Constructorz[] constructors = NO_CONSTRUCTORS;

	/** methods of the clazz */
	private Methodz[] methods = NO_METHODS;

	/** constant assignable to the class */
	private StaticValue[] constants = NO_CONSTANTS;

	/**
	 * where can I store this class?<br>
	 * <i>Calculated by TestCluster.setup()</i>
	 */
	protected Clazz[] assignableTo = NO_CLAZZES;
	/**
	 * what can I use instead of this class?<br>
	 * <i>Calculated by TestCluster.setup()</i>
	 */
	private Clazz[] subClasses = NO_CLAZZES;

	private final int hashCode;

	Clazz(String name, boolean isAbstract) {
		this.name = name;
		this.isAbstract = isAbstract;

		hashCode = name.hashCode();
	}

	void calculateMethods(Class<?> javaClass, XmlClass xml, Builder builder) throws SecurityException {

		// if the XML is null (i.e., it is a primitive class)
		// do not consider methods of the class
		if(xml == null) {
			return;
		}

		// calculate methodz
		Set<Methodz> mlist = new TreeSet<Methodz>();
		for(Method meth : javaClass.getMethods()) {
			final XmlMethod xmlMethod = xml.getMethod(meth);
			if(xmlMethod != null && !xmlMethod.isSkip()) {

				Clazz returnType;
				// ISSUE #1: if you need array support, vote here: http://code.google.com/p/testful/issues/detail?id=1
				if(meth.getReturnType() ==  Void.TYPE || meth.getReturnType().isArray() || meth.getReturnType().isEnum())
					returnType = null;
				else
					returnType = builder.get(meth.getReturnType());

				mlist.add(new Methodz(Modifier.isStatic(meth.getModifiers()), returnType, this, meth.getName(), builder.get(meth.getParameterTypes()), xmlMethod));
			}
		}
		methods = mlist.toArray(new Methodz[mlist.size()]);
		Arrays.sort(methods);

		// calculate constructorz
		if(isAbstract()) {
			constructors = new Constructorz[0];

		} else {
			Set<Constructorz> clist = new TreeSet<Constructorz>();
			for(Constructor<?> cns : javaClass.getConstructors()) {
				final XmlConstructor xmlCns = xml.getConstructor(cns);
				if(xmlCns != null && !xmlCns.isSkip())
					clist.add(new Constructorz(this, builder.get(cns.getParameterTypes()), xmlCns));
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

	@Override
	public String getKey() {
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

	void setAssignableTo(Clazz[] assignableTo) {
		this.assignableTo = assignableTo;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public Clazz clone() throws CloneNotSupportedException {
		return this;
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
