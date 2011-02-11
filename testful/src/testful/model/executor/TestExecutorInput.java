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

import java.io.Serializable;

import testful.TestFul;
import testful.coverage.TrackerDatum;
import testful.model.Test;
import testful.runner.ObjectRegistry;

/**
 * Provide a TestExecutor with all the inputs it requires.
 * @author matteo
 */
public class TestExecutorInput implements Serializable {

	private static final long serialVersionUID = 5081223682955495656L;

	private final TestSerializer testSerializer;
	private final TrackerDatum[] trackerData;
	private final boolean stopOnBug;

	private final boolean discoverFaults = TestFul.getProperty(TestFul.PROPERTY_FAULT_DETECT, true);

	public TestExecutorInput(Test test, boolean stopOnBug, TrackerDatum... trackerData) {
		testSerializer = new TestSerializer(test);
		this.stopOnBug = stopOnBug;
		this.trackerData = trackerData;
	}

	/**
	 * Returns the test to execute
	 * @return the test test to execute
	 */
	public Test getTest() {
		testSerializer.setObjectRegistry(ObjectRegistry.singleton);
		return testSerializer.getTest();
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
}
