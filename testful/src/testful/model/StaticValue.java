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
import java.lang.reflect.Modifier;

/**
 * StaticValues render constants declared in classes.
 *
 * @author matteo
 */
public class StaticValue implements Serializable, Comparable<StaticValue> {

	private static final long serialVersionUID = 4401280118747124819L;

	private final int id;

	/** type of the field (e.g. Class Foo { int field; } => int ) */
	private final Clazz type;
	/** the class that declares the field (e.g. Class Foo { int field; } => Foo ) */
	private final Clazz declaringClass;
	/** type name of the field (e.g. Class Foo { int field; } => field) */
	private final String name;

	StaticValue(int id, Clazz declaringClass, Clazz type, String name) {
		this.id = id;
		this.declaringClass = declaringClass ;
		this.type = type;
		this.name = name;
	}

	/**
	 * Returns the identification of the static value
	 * @return the identification of the static value
	 */
	public int getId() {
		return id;
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

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(StaticValue o) {
		return name.compareTo(o.name);
	}

	public static boolean skip(Field field) {

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

}
