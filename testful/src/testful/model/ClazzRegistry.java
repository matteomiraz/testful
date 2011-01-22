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
import java.util.logging.Logger;

import testful.runner.TestfulClassLoader;

/**
 * TODO describe me!
 * @author matteo
 */
public class ClazzRegistry {

	private static final Logger logger = Logger.getLogger("testful.model.clazzRegistry");

	/** When loaded by a TestfulClassLoader, the singleton stores the ClazzRegistry to use. */
	public static final ClazzRegistry singleton;

	static {
		ClassLoader loader = ClazzRegistry.class.getClassLoader();

		ClazzRegistry tmp = null;
		if(loader instanceof TestfulClassLoader) {
			tmp = new ClazzRegistry((TestfulClassLoader) loader);
		}
		singleton = tmp;
	}

	private final TestfulClassLoader loader;
	public ClazzRegistry(TestfulClassLoader loader) {
		this.loader = loader;
	}

	public Class<?> getClass(Clazz clazz) throws ClassNotFoundException {

		if(clazz instanceof PrimitiveClazz) {
			switch(((PrimitiveClazz) clazz).getType()) {
			case BooleanClass:
				return Boolean.class;
			case BooleanType:
				return Boolean.TYPE;

			case ByteClass:
				return Byte.class;
			case ByteType:
				return Byte.TYPE;

			case CharacterClass:
				return Character.class;
			case CharacterType:
				return Character.TYPE;

			case DoubleClass:
				return Double.class;
			case DoubleType:
				return Double.TYPE;

			case FloatClass:
				return Float.class;
			case FloatType:
				return Float.TYPE;

			case IntegerClass:
				return Integer.class;
			case IntegerType:
				return Integer.TYPE;

			case LongClass:
				return Long.class;
			case LongType:
				return Long.TYPE;

			case ShortClass:
				return Short.class;
			case ShortType:
				return Short.TYPE;

			default:
				logger.warning("Primitive type not known: " + this);
				return null;
			}
		}

		return loader.loadClass(clazz.getClassName());
	}

	public Class<?>[] getClasses(Clazz[] c) throws ClassNotFoundException {
		Class<?>[] ret = new Class<?>[c.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = getClass(c[i]);

		return ret;
	}

	public Field getField(StaticValue value) throws ClassNotFoundException, SecurityException, NoSuchFieldException {
		Class<?> declaringClass = getClass(value.getDeclaringClass());
		Field field = declaringClass.getField(value.getName());
		return field;
	}

	public Method getMethod(Methodz m) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		Class<?> c = getClass(m.getClazz());
		Class<?>[] params = getClasses(m.getParameterTypes());
		return c.getMethod(m.getName(), params);
	}

	public Constructor<?> getConstructor(Constructorz cns) throws ClassNotFoundException, SecurityException, NoSuchMethodException {
		Class<?> c = getClass(cns.getClazz());
		Class<?>[] params = getClasses(cns.getParameterTypes());
		return c.getConstructor(params);
	}
}

//	public static class Impl extends ClazzRegistry {
//
//		private static final TestfulClassLoader testfulClassLoader;
//		private static final ClazzRegistry singleton;
//
//		static {
//			ClassLoader classLoader = ClazzRegistry.class.getClassLoader();
//			if(!(classLoader instanceof TestfulClassLoader)) throw new ClassCastException("ClazzRegistry.Impl must be loaded by using the TestfulClassLoader");
//
//			testfulClassLoader = (TestfulClassLoader) classLoader;
//
//			singleton = new Impl();
//		}
//
//		public static ClazzRegistry singleton() {
//			return singleton ;
//		}
//
//		private final Map<Integer, Clazz> classez = new HashMap<Integer, Clazz>();
//		private final Map<Integer, Class<?>> classes = new HashMap<Integer, Class<?>>();
//
//		public Clazz getClazz(int id) {
//			return classez.get(id);
//		}
//
//		public Class<?> getClass(int id) {
//			return classes.get(id);
//		}
//
//		private final Map<Integer, Methodz> methodz = new HashMap<Integer, Methodz>();
//		private final Map<Integer, Method> methods = new HashMap<Integer, Method>();
//
//		private final Map<Integer, Constructorz> constructorz = new HashMap<Integer, Constructorz>();
//		private final Map<Integer, Constructor<?>> constructor = new HashMap<Integer, Constructor<?>>();
//
//
//		public Method getMethod(Methodz methodz) {
//
//			Method method = methods.get(methodz.getId());
//			if(method == null) {
//				try {
//					class_ = getClass(methodz.getClazz());
//					method = class_.getMethod(methodz.getName(), getClass(methodz.getParameterTypes()));
//				} catch(Exception e) {
//					return null; // never happens
//				}
//			}
//			return method;
//		}
//
//
//
//	}
