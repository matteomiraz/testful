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


package testful.runner;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.utils.CachingMap;
import testful.utils.CachingMap.Cacheable;

public class ClassFinderCaching implements ClassFinder {

	private static Logger logger = Logger.getLogger("testful.executor.classloader");

	private final static int MAX_ELEMS = 1000;
	private final static long MIN_AGE = 15 * 60 * 1000; // 15 min
	private final static long MIN_UNUSED = 5 * 60 * 1000; //  5 min

	private final String key;
	private final CachingMap<String, byte[]> cache;
	private final ClassFinder finder;

	public ClassFinderCaching(ClassFinder finder) throws RemoteException {

		cache = new CachingMap<String, byte[]>(MAX_ELEMS, MIN_AGE, MIN_UNUSED);
		this.finder = finder;
		key = finder.getKey();

		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch(Exception e) {
			logger.log(Level.WARNING, "Unable to use the classloader " + toString() + " in a remote context: " + e.getMessage(), e);
		}

		logger.finest("Created classFinder with key: " + key);
	}

	@Override
	public byte[] getClass(String name) throws ClassNotFoundException, RemoteException {
		if(name == null) {
			new Exception("WARN: ClassFinderCaching.getClass(name=null)").printStackTrace();
			return new byte[0];
		}

		CachingMap.Cacheable<byte[]> tmp = cache.get(name);
		if(tmp != null) {
			logger.finest("(" + key + ") serving cached class " + name);
			return tmp.getElement();
		}

		try {
			byte[] buff = finder.getClass(name);
			cache.put(name, new Cacheable<byte[]>(buff));
			logger.finer("(" + key + ") serving retrieved class " + name);
			return buff;
		} catch(RemoteException e) {
			logger.log(Level.WARNING, "(" + key + ") cannot retrieve class " + name, e);
			throw new ClassNotFoundException("Cannot retrieve the class " + name, e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.getClass().getCanonicalName()).append("\n");
		for(Entry<String, Cacheable<byte[]>> entry : cache.entrySet())
			sb.append("  ").append(entry.getKey()).append(" (").append(entry.getValue().getElement().length).append(" byte)\n");

		return sb.toString();
	}

	@Override
	public String getKey() throws RemoteException {
		return key;
	}
}
