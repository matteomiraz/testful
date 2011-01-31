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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.model.TestCluster;

/**
 * Efficiently serializes and de-serializes Executors
 * @author matteo
 */
public class ExecutorSerializer {

	private static final Logger logger = Logger.getLogger("testful.runner");

	// checks the System property of the current JVM
	private static final boolean DISCOVER_FAULTS = TestFul.getProperty(TestFul.PROPERTY_FAULT_DETECT, true);

	public static <T extends IExecutor> byte[] serialize(DataFinder finder, Class<T> executor, Test test) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ObjectOutput oo = new ObjectOutputStream(baos);

			oo.writeObject(executor.getName());
			oo.writeBoolean(DISCOVER_FAULTS);

			String clusterID = ObjectType.contains(finder, test.getCluster());

			if(clusterID != null) {
				oo.writeBoolean(true);

				oo.writeObject(clusterID);
				oo.writeObject(test.getReferenceFactory().getReferences());
				oo.writeObject(test.getTest());

			} else {
				oo.writeBoolean(false);

				oo.writeObject(test.getCluster());
				oo.writeObject(test.getReferenceFactory().getReferences());
				oo.writeObject(test.getTest());
			}

			oo.close();
			return baos.toByteArray();
		} catch (IOException e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			return new byte[0];
		}
	}

	public static IExecutor deserialize(byte[] serialized) {

		ObjectInput oi = null;
		try {

			if(TestFul.DEBUG && !(ExecutorSerializer.class.getClassLoader() instanceof TestfulClassLoader))
				throw new ClassCastException("ExecutorSerializer must be loaded by the Testful Class Loader, when executing method deserialize");

			ByteArrayInputStream bais = new ByteArrayInputStream(serialized);
			oi = new ObjectInputStream(bais);

			String execClassName = (String) oi.readObject();
			boolean discoverFaults = oi.readBoolean();

			boolean optimized = oi.readBoolean();

			final TestCluster testCluster;
			final Reference[] testRefs;
			final Operation[] testOps;

			if(optimized) {
				String clusterID = (String) oi.readObject();
				testCluster = (TestCluster) ObjectRegistry.singleton.getObject(clusterID);
				testRefs = (Reference[]) oi.readObject();
				testOps = (Operation[]) oi.readObject();

			} else {
				testCluster = (TestCluster) oi.readObject();
				testRefs = (Reference[]) oi.readObject();
				testOps = (Operation[]) oi.readObject();
			}

			@SuppressWarnings("unchecked")
			Class<? extends IExecutor> execClass = (Class<? extends IExecutor>) Class.forName(execClassName);
			Constructor<? extends IExecutor> execCns = execClass.getConstructor(TestCluster.class, Reference[].class, Operation[].class, Boolean.TYPE);

			IExecutor exec = execCns.newInstance(testCluster, testRefs, testOps, discoverFaults);

			return exec;
		} catch(Exception exc) {
			logger.log(Level.WARNING, exc.getMessage(), exc);

		} finally {
			if(oi != null) {
				try {
					oi.close();
				} catch(IOException e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
			}
		}
		return null;

	}
}
