/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010 Matteo Miraz
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

import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.runner.TestfulClassLoader;
import testful.utils.Timer;
import testful.utils.Timer.TimerCallBack;

/**
 * Ensure that third-party code terminates within a given threshold.
 *
 * @author matteo
 */
public final class Stopper implements TimerCallBack {

	/** If the kill switch is enabled and the classes' loading time is less then  this threshold, stops the execution of the controlled thread */
	private static final long LOADING_THRESHOLD = 10;

	private static final Logger logger = Logger.getLogger("testful.coverage.stopper");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);
	private static final boolean LOG_FINER = logger.isLoggable(Level.FINER);

	private static ThreadLocal<Timer> timers = new ThreadLocal<Timer>() {

		@Override
		protected Timer initialValue() {
			return new Timer(Thread.currentThread().getName() + "-timer");
		}
	};

	private final TestfulClassLoader loader;
	private final Timer timer;
	private final Thread controlledThread;

	public Stopper() {
		controlledThread = Thread.currentThread();

		ClassLoader cl = Stopper.class.getClassLoader();
		if(TestFul.DEBUG && !(cl instanceof TestfulClassLoader)) TestFul.debug("Stopper must be loaded by the Testful Class Loader");
		loader = (TestfulClassLoader) cl;

		timer = timers.get();
		timer.setCallBack(this);
	}

	/**
	 * Start the timer: in maxExecTime milliseconds kills the execution of the operation.
	 * @param maxExecTime the amount of time to wait before killing the execution of the operation
	 */
	public void start(int maxExecTime) {

		// reset the loading counter
		loader.getLoadingTime();

		if(LOG_FINER) logger.finer("Alarm " + timer + " set " + maxExecTime + " ms from now");
		timer.start(maxExecTime);
	}

	/**
	 * Stops the timer: the Stopper does not try to kill the execution of any operation.
	 */
	public void stop() {
		timer.stop();
		TestStoppedException.dontKill();

		// reset the loading counter
		loader.getLoadingTime();

		if(LOG_FINER) logger.finer("Alarm " + timer + " cleared");
	}

	/* (non-Javadoc)
	 * @see testful.utils.Timer.TimerCallBack#timerExpired()
	 */
	@Override
	public void timerExpired() {

		long loadingTime = loader.getLoadingTime();
		if(LOG_FINER) logger.finer("Alarm " + timer + ": " + loadingTime + " ms spent to load classes");
		if(loadingTime > LOADING_THRESHOLD) {
			timer.stop();
			timer.start(loadingTime);
			if(LOG_FINE) logger.fine("Alarm " + timer + " is delayed of " + loadingTime + "ms to compensate the classes loading time");
		} else {
			TestStoppedException.kill();
			controlledThread.interrupt();
			if(LOG_FINE) logger.fine("Alarm " + timer + " is ringing");
		}
	}

	/**
	 * Discards the timer: release the resources.
	 * Note: after invoking this method, the instance is no longer usable
	 */
	public void done() {
		timer.setCallBack(null);
		if(LOG_FINER) logger.finer("Alarm " + timer + " de-registered");
	}
}
