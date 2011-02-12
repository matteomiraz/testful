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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import testful.TestFul;

/**
 * Retrieve the bytecode of classes.
 *
 * @author matteo
 */
public class DataFinderImpl implements DataFinder {

	private static Logger logger = Logger.getLogger("testful.executor.classloader");

	private final String key;
	private final Map<String, DataType>types;

	public DataFinderImpl(DataType ... dataType) {
		key = UUID.randomUUID().toString();

		if(TestFul.getProperty(TestFul.PROPERTY_RUNNER_REMOTE, false)) {
			try {
				UnicastRemoteObject.exportObject(this, 0);
			} catch(Exception e) {
				logger.warning("Unable to use the classloader " + toString() + " in a remote context: " + e);
			}
		}

		types = new HashMap<String, DataType>((int) (dataType.length*1.5));
		for (DataType d : dataType) types.put(d.getName(), d);
	}

	@Override
	public String getKey() throws RemoteException {
		return key;
	}

	/* (non-Javadoc)
	 * @see testful.runner.DataFinder#getData(java.lang.String, java.lang.String)
	 */
	@Override
	public byte[] getData(String type, String id) throws RemoteException {
		DataType t = types.get(type);
		if(t == null) return null;

		return t.getData(id);
	}

}
