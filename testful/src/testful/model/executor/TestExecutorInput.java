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

package testful.model.executor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import testful.TestFul;
import testful.coverage.TrackerDatum;
import testful.model.Test;
import testful.runner.ObjectRegistry;

/**
 * Provide a TestExecutor with all the inputs it requires.
 * @author matteo
 */
public class TestExecutorInput implements Externalizable {

	private static final long serialVersionUID = 5081223682955495656L;

	private boolean stopOnBug;

	private boolean discoverFaults = TestFul.getProperty(TestFul.PROPERTY_FAULT_DETECT, true);

	private TrackerDatum[] trackerData;

	private Test test;

	@Deprecated
	public TestExecutorInput() { }

	public TestExecutorInput(Test test, boolean stopOnBug, TrackerDatum... trackerData) {
		this.test = test;
		this.stopOnBug = stopOnBug;
		this.trackerData = trackerData;
	}

	/**
	 * Returns the test to execute
	 * @return the test test to execute
	 */
	public Test getTest() {
		return test;
	}

	public TrackerDatum[] getTrackerData() {
		return trackerData;
	}

	public boolean isDiscoverFaults() {
		return discoverFaults;
	}

	public boolean isStopOnBug() {
		return stopOnBug;
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(stopOnBug);

		out.writeBoolean(discoverFaults);

		out.writeShort(trackerData.length);
		for (TrackerDatum datum : trackerData)
			out.writeObject(datum);

		byte[] testSerialized = TestSerializer.serialize(test);
		out.writeInt(testSerialized.length);
		out.write(testSerialized);

	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		stopOnBug = in.readBoolean();

		discoverFaults = in.readBoolean();

		short trackerDataLen = in.readShort();
		trackerData = new TrackerDatum[trackerDataLen];
		for (int i = 0; i < trackerDataLen; i++)
			trackerData[i] = (TrackerDatum) in.readObject();

		int testSerializedLen = in.readInt();
		byte[] testSerialized = new byte[testSerializedLen];
		in.readFully(testSerialized);

		test = TestSerializer.deserialize(ObjectRegistry.singleton, testSerialized);
	}
}
