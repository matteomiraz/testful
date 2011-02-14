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

package testful.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * 
 * TODO describe me!
 * @author matteo
 */
public class SerializationUtils {

	private static final Logger logger = Logger.getLogger("testful.utils.SerializationUtils");

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T copy(T obj) {
		byte[] buff = serialize(obj, false);
		return (T) deserialize(buff, false);
	}

	// ------------------------- serialize --------------------------

	public static byte[] serialize(Serializable obj, boolean compress) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutput oo = new ObjectOutputStream(compress ? new GZIPOutputStream(baos) : baos);
			oo.writeObject(obj);
			oo.close();
			return baos.toByteArray();
		} catch(IOException e) {
			logger.log(Level.WARNING, e.getMessage() + " (SerializationUtils is loaded by " + SerializationUtils.class.getClassLoader() + ")", e);
			return new byte[0];
		}
	}

	// ----------------------- de-serialize -----------------------

	private static class CustomObjectInputStream extends ObjectInputStream {

		private final ClassLoader loader;

		public CustomObjectInputStream(InputStream in, ClassLoader loader) throws IOException {
			super(in);
			this.loader = loader;
		}

		/* (non-Javadoc)
		 * @see java.io.ObjectInputStream#resolveClass(java.io.ObjectStreamClass)
		 */
		@Override
		protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {

			String name = desc.getName();
			try {
				return Class.forName(name, false, loader);
			} catch (ClassNotFoundException ex) {
				return super.resolveClass(desc);
			}
		}

	}

	public static Serializable deserialize(byte[] buff, boolean compressed, ClassLoader loader) {
		ObjectInput oi = null;
		try {

			InputStream in = new ByteArrayInputStream(buff);
			if(compressed) in = new GZIPInputStream(in);

			if(loader == null) oi = new ObjectInputStream(in);
			else  oi = new CustomObjectInputStream(in, loader);

			return (Serializable) oi.readObject();

		} catch(Throwable e) {
			logger.log(Level.WARNING, "Error while reading the serialized object: " + e, e);
		} finally {
			if(oi != null) {
				try {
					oi.close();
				} catch(IOException e) {
					logger.log(Level.WARNING, "Error while closing the input stream: " +  e, e);
				}
			}
		}
		return null;
	}

	public static Serializable deserialize(byte[] buff, boolean compressed) {
		return deserialize(buff, compressed, null);
	}
}
