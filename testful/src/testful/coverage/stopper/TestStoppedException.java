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

package testful.coverage.stopper;

import java.util.logging.Logger;

import testful.model.faults.FaultyExecutionException;

/**
 * Used for interrupting the execution of the test.
 *
 * @author matteo
 */
public class TestStoppedException extends RuntimeException implements FaultyExecutionException  {

	private static final long serialVersionUID = -7939628914571439861L;

	/** if set to true, stops the execution */
	private static volatile boolean kill = false;

	/** If true (and if kill is true) interrupts the static initialization of classes */
	private static final boolean STOP_CLINIT = false;

	/** the exception that is thrown */
	private static TestStoppedException singleton = null;

	public static void check() {
		if (kill) {
			if (singleton == null)
				singleton = new TestStoppedException();

			if(!STOP_CLINIT) {
				for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
					if(ste.getMethodName().equals("<clinit>")) {
						Logger.getLogger("testful.coverage.stopper").fine("Not killing the thread: executing the static initialization of " + ste.getClassName());
						return;
					}
				}
			}

			throw singleton;
		}
	}

	public static void kill() {
		TestStoppedException.kill = true;
	}

	public static void dontKill() {
		TestStoppedException.kill = false;
		singleton = null;
	}

	private TestStoppedException() {
		super("Test stopped", null);
	}
}