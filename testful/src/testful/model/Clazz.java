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

import testful.utils.ElementWithKey;

public class Clazz implements Serializable, Comparable<Clazz>, ElementWithKey<String> {

	private static final long serialVersionUID = 2529336968056188350L;

	private static final Clazz[] NO_CLAZZES = new Clazz[0];
	private static final StaticValue[] NO_CONSTANTS = new StaticValue[0];
	private static final Constructorz[] NO_CONSTRUCTORS = new Constructorz[0];
	private static final Methodz[] NO_METHODS = new Methodz[0];

	private final int id;

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

	Clazz(int id, String name, boolean isAbstract) {
		this.id = id;
		this.name = name;
		this.isAbstract = isAbstract;

		hashCode = name.hashCode();
	}

	/**
	 * @param methods the methods to set
	 */
	void setMethods(Methodz[] methods) {
		this.methods = methods;
	}

	/**
	 * @param constructors the constructors to set
	 */
	void setConstructors(Constructorz[] constructors) {
		this.constructors = constructors;
	}

	/**
	 * Returns the numeric id of the clazz
	 * @return the id of the clazz
	 */
	public int getId() {
		return id;
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
