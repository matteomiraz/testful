/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
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

/**
 * Converts testful.model's elements into java.lang.reflect ones.
 * @author matteo
 */
public interface IClassRegistry {

	public abstract Class<?> getClass(Clazz clazz) throws ClassNotFoundException;

	public abstract Class<?>[] getClasses(Clazz[] c) throws ClassNotFoundException;

	public abstract Field getField(StaticValue value) throws ClassNotFoundException, SecurityException, NoSuchFieldException;

	public abstract Method getMethod(Methodz m) throws ClassNotFoundException, SecurityException, NoSuchMethodException;

	public abstract Constructor<?> getConstructor(Constructorz cns) throws ClassNotFoundException, SecurityException, NoSuchMethodException;

}