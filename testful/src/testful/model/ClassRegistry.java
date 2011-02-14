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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import testful.utils.StopWatch;

/**
 * ClassRegistry is a Façade to the remote class-loader
 * and allows one to convert testful.model's element into java.lang.reflect ones.
 * @author matteo
 */
public class ClassRegistry implements IClassRegistry {

	private static final Logger logger = Logger.getLogger("testful.model.clazzRegistry");

	private static final StopWatch timerClass = StopWatch.Disabled.singleton; // Timer.getTimer();

	/** When loaded by a RemoteClassLoader, the singleton stores the ClassRegistry to use. */
	public static final IClassRegistry singleton;

	static {
		singleton = new ClassRegistry(ClassRegistry.class.getClassLoader());
	}

	private final ClassLoader loader;
	public ClassRegistry(ClassLoader loader) {
		this.loader = loader;
	}

	private final Map<Integer, Class<?>> clazzCache = new HashMap<Integer, Class<?>>();
	/* (non-Javadoc)
	 * @see testful.model.IClassRegistry#getClass(testful.model.Clazz)
	 */
	@Override
	public Class<?> getClass(Clazz clazz) throws ClassNotFoundException {

		timerClass.start("clazzRegistry.getClass");

		Class<?> cache = clazzCache.get(clazz.getId());
		if(cache != null) {
			timerClass.stop();
			return cache;
		}

		final Class<?> ret;
		if(clazz instanceof PrimitiveClazz) {
			switch(((PrimitiveClazz) clazz).getType()) {
			case BooleanClass:
				ret = Boolean.class;
				break;
			case BooleanType:
				ret = Boolean.TYPE;
				break;

			case ByteClass:
				ret = Byte.class;
				break;
			case ByteType:
				ret = Byte.TYPE;
				break;

			case CharacterClass:
				ret = Character.class;
				break;
			case CharacterType:
				ret = Character.TYPE;
				break;

			case DoubleClass:
				ret = Double.class;
				break;
			case DoubleType:
				ret = Double.TYPE;
				break;

			case FloatClass:
				ret = Float.class;
				break;
			case FloatType:
				ret = Float.TYPE;
				break;

			case IntegerClass:
				ret = Integer.class;
				break;
			case IntegerType:
				ret = Integer.TYPE;
				break;

			case LongClass:
				ret = Long.class;
				break;
			case LongType:
				ret = Long.TYPE;
				break;

			case ShortClass:
				ret = Short.class;
				break;
			case ShortType:
				ret = Short.TYPE;
				break;

			default:
				logger.warning("Primitive type not known: " + this);
				ret = null;
				break;
			}

		} else {
			ret = loader.loadClass(clazz.getClassName());
		}

		clazzCache.put(clazz.getId(), ret);
		timerClass.stop();

		return ret;
	}

	/* (non-Javadoc)
	 * @see testful.model.IClassRegistry#getClasses(testful.model.Clazz[])
	 */
	@Override
	public Class<?>[] getClasses(Clazz[] c) throws ClassNotFoundException {

		Class<?>[] ret = new Class<?>[c.length];
		for (int i = 0; i < ret.length; i++)
			ret[i] = getClass(c[i]);

		return ret;
	}

	private final Map<Integer, Field> fieldCache = new HashMap<Integer, Field>();
	/* (non-Javadoc)
	 * @see testful.model.IClassRegistry#getField(testful.model.StaticValue)
	 */
	@Override
	public Field getField(StaticValue value) throws ClassNotFoundException, SecurityException, NoSuchFieldException {

		Field cache = fieldCache.get(value.getId());
		if(cache != null) return cache;

		Class<?> declaringClass = getClass(value.getDeclaringClass());
		Field field = declaringClass.getField(value.getName());
		fieldCache.put(value.getId(), field);

		return field;
	}

	private final Map<Integer, Method> methodCache = new HashMap<Integer, Method>();
	/* (non-Javadoc)
	 * @see testful.model.IClassRegistry#getMethod(testful.model.Methodz)
	 */
	@Override
	public Method getMethod(Methodz m) throws ClassNotFoundException, SecurityException, NoSuchMethodException {

		Method cache = methodCache.get(m.getId());
		if(cache != null) return cache;

		Class<?> c = getClass(m.getClazz());
		Class<?>[] params = getClasses(m.getParameterTypes());
		Method method = c.getMethod(m.getName(), params);
		methodCache.put(m.getId(), method);

		return method;
	}

	private final Map<Integer, Constructor<?>> constructorCache = new HashMap<Integer, Constructor<?>>();
	/* (non-Javadoc)
	 * @see testful.model.IClassRegistry#getConstructor(testful.model.Constructorz)
	 */
	@Override
	public Constructor<?> getConstructor(Constructorz cns) throws ClassNotFoundException, SecurityException, NoSuchMethodException {

		Constructor<?> cache = constructorCache.get(cns.getId());
		if(cache != null) return cache;

		Class<?> c = getClass(cns.getClazz());
		Class<?>[] params = getClasses(cns.getParameterTypes());
		Constructor<?> constructor = c.getConstructor(params);
		constructorCache.put(cns.getId(), constructor);

		return constructor;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ClassRegistry of " + loader;
	}
}