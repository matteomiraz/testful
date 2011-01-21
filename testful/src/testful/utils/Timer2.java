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

import java.util.Collection;
import java.util.LinkedList;

import testful.TestFul;

/**
 * Measure execution performances of nested parts
 * @author matteo
 */
public abstract class Timer2 {

	public abstract void start();
	public abstract void stop();

	public abstract Timer2 getSubTimer(String name);

	public static Timer2 getRootTimer(String name) {
		if(Timer.MONITOR) return new Timer2Impl(name);
		else return TimerDisabled.singleton;
	}

	private static class TimerDisabled extends Timer2 {

		public static TimerDisabled singleton = new TimerDisabled();

		private TimerDisabled() { }

		@Override
		public void start() { }

		@Override
		public void stop() { }

		@Override
		public Timer2 getSubTimer(String name) {
			return this;
		}
	}

	private static class Timer2Impl extends Timer2 {
		private final String name;
		private final Timer2Impl parent;
		public Timer2Impl(String name) {
			parent = null;
			this.name = name;
		}

		private Timer2Impl(Timer2Impl parent, String name) {
			this.parent = parent;
			this.name = name;
		}

		private Collection<Timer2Impl> subtimers = new LinkedList<Timer2Impl>();
		@Override
		public Timer2 getSubTimer(String name) {
			Timer2Impl ret = new Timer2Impl(this, name);
			subtimers.add(ret);
			return ret;
		}

		private int n;
		private long duration = 0;
		private long start = 0;

		private boolean paused = false;

		/* (non-Javadoc)
		 * @see testful.utils.Timer2#start()
		 */
		@Override
		public void start() {
			if(TestFul.DEBUG && start != 0)
				new IllegalStateException("The timer " + name + " is still running").printStackTrace();

			if(parent != null)
				parent.pause();

			n++;
			start = System.nanoTime();
		}

		public void pause() {
			if(TestFul.DEBUG && paused)
				new IllegalStateException("The timer " + name + " is paused").printStackTrace();

			paused = true;
			duration += (System.nanoTime() - start);
		}

		public void resume() {
			if(TestFul.DEBUG && !paused)
				new IllegalStateException("The timer " + name + " is not paused").printStackTrace();

			paused = false;
			start = System.nanoTime();
		}

		/* (non-Javadoc)
		 * @see testful.utils.Timer2#stop()
		 */
		@Override
		public void stop() {
			long end = System.nanoTime();
			duration += end - start;

			start = 0;

			if(parent != null) parent.resume();
			else log();
		}

		public void log() {
			Timer.logger.fine(name + " " + duration/1000000.0 + " ms in " + n + " invocations");
			duration = 0;
			n = 0;

			for (Timer2Impl subTimer : subtimers)
				subTimer.log();
		}

	}
}
