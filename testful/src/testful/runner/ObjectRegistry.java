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

package testful.runner;

import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.utils.CachingMap;
import testful.utils.CachingMap.Cacheable;
import testful.utils.Cloner;

/**
 * ObjectRegistry allows one to retrieve objects (declared with {@link ObjectType}).
 * @author matteo
 */
public class ObjectRegistry {

	private static Logger logger = Logger.getLogger("testful.executor");

	/** When loaded by a TestfulClassLoader, this is the singleton to use */
	public static final ObjectRegistry singleton;

	static {
		ClassLoader loader = ObjectRegistry.class.getClassLoader();

		ObjectRegistry tmp = null;
		if(loader instanceof TestfulClassLoader) {
			tmp = new ObjectRegistry((TestfulClassLoader) loader);
		}
		singleton = tmp;
	}

	private final CachingMap<String, ISerializable> cache = new CachingMap<String, ISerializable>(50, 5 * 60 * 1000,  1 * 60 * 1000);

	private final DataFinder finder;
	private ObjectRegistry(TestfulClassLoader loader) {
		finder = loader.getFinder();
	}

	public ISerializable getObject(String identifier) {

		// check the cache of the objects
		CachingMap.Cacheable<ISerializable> tmp = cache.get(identifier);
		if(tmp != null)
			return tmp.getElement();

		try {

			byte[] b = finder.getData(ObjectType.NAME, identifier);
			if(b == null) return null;

			ISerializable object = (ISerializable) Cloner.deserialize(b, ObjectType.COMPRESS);
			object.setISerializableIdentifier(Long.parseLong(identifier, Character.MAX_RADIX));
			cache.put(identifier, new Cacheable<ISerializable>(object));
			return object;

		} catch (RemoteException exc) {
			logger.log(Level.WARNING, exc.getMessage(), exc);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ObjectRegistry of " + ObjectRegistry.class.getClassLoader();
	}
}