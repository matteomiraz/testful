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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import testful.utils.Cloner;

/**
 * Allows one to provide the executor with objects, which will be serialized, transferred, and cached automatically.
 * @author matteo
 */
public class ObjectType implements DataType {
	public static final String NAME = "object";

	public static final boolean COMPRESS = false;

	private final AtomicLong idGenerator = new AtomicLong();
	private final Map<Long, byte[]> map = new HashMap<Long, byte[]>();

	public ObjectType() {
	}

	@Override
	public String getName() {
		return NAME;
	}

	/**
	 * Adds an object
	 * @param obj the object to add
	 * @return the identifier of the object
	 */
	public String addObject(ISerializable obj) {

		// get the id of the object
		Long id = obj.getISerializableIdentifier();
		if(id == null) {
			id = idGenerator.incrementAndGet();
			obj.setISerializableIdentifier(id);
		}

		// if it is a new object, insert it in the map
		if(!map.containsKey(id))
			map.put(id, Cloner.serialize(obj, COMPRESS));

		// return the String with the id
		return convertId(id);
	}

	public static String contains(DataFinder finder, ISerializable obj) {
		try {
			Long identifier = obj.getISerializableIdentifier();
			if(identifier != null) {

				String id = convertId(identifier);

				byte[] b = finder.getData(ObjectType.NAME, id);
				if(b != null) return id;
			}
		} catch (RemoteException e) {
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see testful.runner.DataType#getData(java.lang.String)
	 */
	@Override
	public byte[] getData(String identifier) throws RemoteException {

		return map.get(Long.parseLong(identifier, Character.MAX_RADIX));

	}

	/**
	 * Convert the identifier from long to string
	 * @param identifier the long identifier
	 * @return the string identifier
	 */
	static String convertId(long id) {
		return Long.toString(id, Character.MAX_RADIX);
	}

}
