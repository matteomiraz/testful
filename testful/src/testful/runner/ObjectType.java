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

import testful.utils.SerializationUtils;

/**
 * Allows one to provide the executor with objects, which will be serialized, transferred, and cached automatically.
 * @author matteo
 */
public class ObjectType implements DataType {
	public static final String NAME = "object";

	public static final boolean COMPRESS = false;

	private final AtomicLong idGenerator = new AtomicLong();
	private final Map<String, byte[]> map = new HashMap<String, byte[]>();

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
		String id = obj.getISerializableIdentifier();
		if(id == null) {
			id = Long.toString(idGenerator.incrementAndGet(), Character.MAX_RADIX);
			obj.setISerializableIdentifier(id);
		}

		// if it is a new object, insert it in the map
		if(!map.containsKey(id))
			map.put(id, SerializationUtils.serialize(obj, COMPRESS));

		return id;
	}

	public static boolean contains(ObjectRegistry registry, ISerializable obj) {
		String id = obj.getISerializableIdentifier();
		if(id != null) {
			ISerializable o = registry.getObject(id);
			return o != null;
		}

		return false;
	}

	/* (non-Javadoc)
	 * @see testful.runner.DataType#getData(java.lang.String)
	 */
	@Override
	public byte[] getData(String identifier) throws RemoteException {
		return map.get(identifier);
	}
}
