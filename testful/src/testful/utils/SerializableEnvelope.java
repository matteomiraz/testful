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

package testful.utils;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Envelope for smart serialization of objects.
 * It supports caching both
 * of objects (so to allow efficient local evaluations)
 * and of serialized objects (so to avoid useless deserializations).
 * @param <T> the type of the object being transferred
 * @author matteo
 */
public class SerializableEnvelope<T extends Serializable> implements Externalizable {

	private static final Logger logger = Logger.getLogger("testful.utils.SerializableEnvelope");

	private transient T object;
	private transient byte[] serialized;

	@Deprecated
	public SerializableEnvelope() {
		object = null;
		serialized = null;
	}

	public SerializableEnvelope(T object) {
		this.object = object;
		this.serialized = null;
	}

	@SuppressWarnings("unchecked")
	public T getObject(ClassLoader classLoader) {

		if(object == null) {

			try {
				object = (T) SerializationUtils.deserialize(serialized, false, classLoader);
			} catch (Exception e) {
				logger.log(Level.WARNING, "Cannot deserialize the object: " + e, e);
			}
		}

		return object;
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if(serialized == null)
			serialized = SerializationUtils.serialize(object, false);

		out.writeInt(serialized.length);
		out.write(serialized);
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {

		int len = in.readInt();
		serialized = new byte[len];

		in.readFully(serialized);
	}

}
