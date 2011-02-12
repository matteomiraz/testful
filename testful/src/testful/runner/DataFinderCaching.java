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
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.utils.CachingMap;
import testful.utils.CachingMap.Cacheable;

public class DataFinderCaching implements DataFinder {

	private static Logger logger = Logger.getLogger("testful.executor.classloader");
	private static final boolean LOG_FINER = logger.isLoggable(Level.FINER);
	private static final boolean LOG_FINEST = logger.isLoggable(Level.FINEST);

	private final static int MAX_ELEMS = 3000;
	private final static long MIN_AGE = 15 * 60 * 1000; // 15 min
	private final static long MIN_UNUSED = 5 * 60 * 1000; //  5 min

	private final String key;
	private final CachingMap<String, byte[]> cache;
	private final Set<String> missing;

	private final DataFinder finder;

	public DataFinderCaching(DataFinder classFinder) throws RemoteException {

		cache = new CachingMap<String, byte[]>(MAX_ELEMS, MIN_AGE, MIN_UNUSED);
		missing = new LinkedHashSet<String>();

		finder = classFinder;
		key = classFinder.getKey();

		if(TestFul.getProperty(TestFul.PROPERTY_RUNNER_REMOTE, false)) {
			try {
				UnicastRemoteObject.exportObject(this, 0);
			} catch(Exception e) {
				logger.log(Level.WARNING, "Unable to use the classloader " + toString() + " in a remote context: " + e.getMessage(), e);
			}
		}

		logger.finest("Created classFinder with key: " + key);
	}

	@Override
	public String getKey() throws RemoteException {
		return key;
	}

	/* (non-Javadoc)
	 * @see testful.runner.DataFinder#getData(java.lang.String, java.lang.String)
	 */
	@Override
	public synchronized byte[] getData(String type, String id) throws RemoteException {
		if(type == null || id == null) {
			NoSuchElementException exc = new NoSuchElementException("Cannot find element " + type + " " + id);
			logger.log(Level.WARNING, exc.getMessage(), exc);
			return null;
		}

		final String name = type + "#" + id;

		CachingMap.Cacheable<byte[]> tmp = cache.get(name);
		if(tmp != null) {
			if(LOG_FINEST) logger.finest("(" + key + ") serving cached element " + name);
			return tmp.getElement();
		}

		if(missing.contains(name)) {
			if(LOG_FINEST) logger.log(Level.FINEST, "(" + key + ") cannot retrieve element " + name + " (cached)");
			return null;
		}

		try {
			byte[] buff = finder.getData(type, id);

			if(buff == null) {
				if(LOG_FINER) logger.finer("(" + key + ") serving retrieved element " + name + " (missing)");
				missing.add(name);
			} else {
				if(LOG_FINER) logger.finer("(" + key + ") serving retrieved element " + name);
				cache.put(name, new Cacheable<byte[]>(buff));
			}

			return buff;
		} catch(RemoteException e) {
			logger.log(Level.WARNING, "(" + key + ") cannot retrieve element " + name, e);
			return null;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.getClass().getName()).append("\n");
		for(Entry<String, Cacheable<byte[]>> entry : cache.entrySet())
			sb.append("  ").append(entry.getKey()).append(" (").append(entry.getValue().getElement().length).append(" byte)\n");

		return sb.toString();
	}
}
