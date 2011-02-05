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

import testful.TestFul;

/**
 * Measure execution performances
 * @author matteo
 */
public abstract class StopWatchFast {

	public abstract void start();
	public abstract void stop();
	public abstract void log();

	public static StopWatchFast getTimer(String name) {
		if(StopWatch.MONITOR) return new Enabled(name);
		else return Disabled.singleton;
	}

	public static class Disabled extends StopWatchFast {

		public static Disabled singleton = new Disabled();

		/*
		 * (non-Javadoc)
		 * @see testful.utils.StopWatchFast#start()
		 */
		@Override
		public void start() { }

		/* (non-Javadoc)
		 * @see testful.utils.Timer#end()
		 */
		@Override
		public void stop() { }

		/* (non-Javadoc)
		 * @see testful.utils.StopWatchFast#log()
		 */
		@Override
		public void log() { }
	}

	private static class Enabled extends StopWatchFast {
		private final String name;
		private long duration = 0;
		private long start = 0;
		private int n;

		public Enabled(String name) {
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * @see testful.utils.StopWatchFast#start()
		 */
		@Override
		public void start() {
			if(TestFul.DEBUG && start != 0)
				TestFul.debug(new IllegalStateException("The timer " + name + " is still running"));

			n++;
			start = System.nanoTime();
		}

		/*
		 * (non-Javadoc)
		 * @see testful.utils.StopWatchFast#stop()
		 */
		@Override
		public void stop() {
			final long end = System.nanoTime();

			if(TestFul.DEBUG && start == 0)
				TestFul.debug(new IllegalStateException("The timer " + name + " is not running"));

			duration += end - start;
			start = 0;
		}

		/* (non-Javadoc)
		 * @see testful.utils.StopWatchFast#log()
		 */
		@Override
		public void log() {

			if(TestFul.DEBUG && start != 0)
				TestFul.debug(new IllegalStateException("The timer " + name + " is still running"));

			if(n > 0) {
				StopWatch.logger.fine(name + " " + duration/1000000.0 + " ms in " + n + " invocations");
				duration = 0;
				n = 0;
			}
		}
	}
}
