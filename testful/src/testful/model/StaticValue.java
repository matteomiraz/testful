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
import java.lang.reflect.Field;

/**
 * StaticValues render constants declared in classes.
 * 
 * @author matteo
 */
public class StaticValue implements Serializable {

	private static final long serialVersionUID = 2915220460472722096L;
	
	/** type of the field (e.g. Class Foo { int field; } => int ) */
	private final Clazz type;
	/** the class that declares the field (e.g. Class Foo { int field; } => Foo ) */
	private final Clazz declaringClass;
	/** type name of the field (e.g. Class Foo { int field; } => field) */
	private final String name;

	private transient Field field;

	StaticValue(TestCluster cluster, Field f) {
		type = cluster.getRegistry().getClazz(f.getType());
		declaringClass = cluster.getRegistry().getClazz(f.getDeclaringClass());
		name = f.getName();
		field = f;
	}

	/**
	 * Returns the type of the field (e.g., Class Foo { int field; } => int ).
	 * 
	 * @return the type of the field
	 */
	public Clazz getType() {
		return type;
	}

	/**
	 * Returns the class declaring the field (e.g. Class Foo { int field; } => Foo
	 * )
	 * 
	 * @return the class declaring the field
	 */
	public Clazz getDeclaringClass() {
		return declaringClass;
	}

	/**
	 * Returns the name of the field (e.g. Class Foo { int field; } => "field" )
	 * 
	 * @return the name of the field
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the field using the java reflection API
	 * 
	 * @return the "Field" object
	 */
	public Field toField() {
		if(field == null) try {
			field = declaringClass.toJavaClass().getField(name);
		} catch(Exception e) {
			// this should never happens!
			e.printStackTrace();
		}

		return field;
	}

	/** Clear the cache: discard the Field reference */
	public void clearCache() {
		field = null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + declaringClass.hashCode();
		result = prime * result + name.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof StaticValue)) return false;

		StaticValue other = (StaticValue) obj;
		return name.equals(other.name) && declaringClass.equals(other.declaringClass);
	}

	@Override
	public String toString() {
		return declaringClass.getClassName() + "." + name;
	}
}
