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

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;

/**
 * Measure execution performances
 * @author matteo
 */
public abstract class StopWatch {

	static final String PATTERN = "testful.timing";

	static final Logger logger = Logger.getLogger(PATTERN);
	static final boolean MONITOR;

	static {
		boolean monitor = TestFul.getProperty(TestFul.PROPERTY_MONITOR_PERFORMANCE, false);
		boolean loggable = logger.isLoggable(Level.FINE);

		if(monitor && !loggable) logger.warning("Cannot log performances: use at least a FINE level for logging");

		MONITOR = monitor && loggable;
	}

	public abstract void start(String name);
	public abstract void stop();
	public abstract void stop(String message);

	public static StopWatch getTimer() {
		if(MONITOR) return new Enabled();
		else return Disabled.singleton;
	}

	public static class Disabled extends StopWatch {

		public static Disabled singleton = new Disabled();

		/* (non-Javadoc)
		 * @see testful.utils.Timer#start(java.lang.String)
		 */
		@Override
		public void start(String name) { }

		/* (non-Javadoc)
		 * @see testful.utils.Timer#end()
		 */
		@Override
		public void stop() { }

		@Override
		public void stop(String message) { }
	}

	private static class Enabled extends StopWatch {
		private String name = null;
		private long begin = 0;


		/* (non-Javadoc)
		 * @see testful.utils.Timer#start(java.lang.String)
		 */
		@Override
		public void start(String name) {
			this.name = name;
			begin = System.nanoTime();
		}

		@Override
		public void stop() {
			stop(null);
		}

		@Override
		public void stop(String message) {
			if(name != null) {
				long end = System.nanoTime();
				logger.fine(name + " " + (end - begin)/1000000.0 + " ms" + (message!=null?" " + message : ""));
			}
		}
	}

	public static void main(String[] args) {
		for (String f : args) {
			try {
				LogFileReader fileReader = new LogFileReader(new File(f), PATTERN);

				Map<String, Float> times = new HashMap<String, Float>();

				for(String line : fileReader) {

					// 0 1                                      2   3  4  5 6
					//   execution.execution.assignConstant.cut 0.0 ms in 0 invocations
					String[] parts = line.split(" ");

					Float time = times.get(parts[1]);
					if(time == null) time = 0.0f;
					time += Float.parseFloat(parts[2]);
					times.put(parts[1], time);
				}

				for (Entry<String, Float> e : times.entrySet()) {
					System.out.println(e.getKey() + "," + e.getValue());
				}
			} catch (Exception e) {
			}
		}
	}

}
