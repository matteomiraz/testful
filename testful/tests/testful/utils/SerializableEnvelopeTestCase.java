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

import junit.framework.TestCase;

/**
 * Tests the {@link SerializableEnvelope}
 * @author matteo
 */
public class SerializableEnvelopeTestCase extends TestCase {

	private static class Ser implements Serializable {

		private static final long serialVersionUID = 1L;

		public int i;

	}

	private static class NotSerializable implements Externalizable {

		/* (non-Javadoc)
		 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
		 */
		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			fail("This is not supposed to be serialized");
		}

		/* (non-Javadoc)
		 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
		 */
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			fail("This is not supposed to be (de)serialized");
		}
	}

	public void testNoSerialization() {

		NotSerializable object = new NotSerializable();

		SerializableEnvelope<NotSerializable> foo = new SerializableEnvelope<NotSerializable>(object);

		NotSerializable deser = foo.getObject(SerializableEnvelope.class.getClassLoader());

		assertSame(object, deser);

	}

	public void testSerialization() throws Exception {
		Ser object = new Ser();
		object.i = 120;

		SerializableEnvelope<Ser> foo = new SerializableEnvelope<Ser>(object);
		assertSame(object, foo.getObject(SerializableEnvelope.class.getClassLoader()));

		// force serialization - de-serialization
		SerializableEnvelope<Ser> foo2 = SerializationUtils.copy(foo);
		Ser deser = foo2.getObject(SerializableEnvelope.class.getClassLoader());

		assertNotSame(object, deser);
		assertEquals(object.i, deser.i);

	}

}
