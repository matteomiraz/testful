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

/**
 * Ensure that third-party code terminates within a given threshold.
 *
 * @author matteo
 */
public final class Stopper {

	private static final Logger logger = Logger.getLogger("testful.coverage.stopper");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);
	private static final boolean LOG_FINER = logger.isLoggable(Level.FINER);

	/** Milliseconds between two kills */
	private static final int KILL_WAIT = 50;

	/** Standard waiting time if IDLE */
	private static final int IDLE_WAIT = 10000;

	/** wait and synchronize on this object */
	private final Object wait = new Object();

	/** if greater than zero, indicates when kill the worker (in mill-second, as reported by System.currentTimeMillis()) */
	private long endOfTheWorld = -1;

	/** If greater than zero, it is waiting until this milliseconds */
	private long waiting = -1;

	private boolean running = true;

	public Stopper() {
		final Thread controlled = Thread.currentThread();

		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while(running) {

						final long curr = System.currentTimeMillis();
						synchronized (wait) {

							final long end, delta;
							if(endOfTheWorld > 0) {
								end = endOfTheWorld;
								delta = end - curr;
							} else {
								end = curr + IDLE_WAIT;
								delta = IDLE_WAIT;
							}

							if(delta <= 0) { // KILL!
								if(LOG_FINE) logger.fine("Killing controlled thread");
								TestStoppedException.kill();
								controlled.interrupt();
								endOfTheWorld = curr + KILL_WAIT;

							} else { // Wait
								waiting = end;
								wait.wait(delta);
								waiting = -1;
							}
						}
					}
					if(LOG_FINER) logger.finer("Stopper thread has finished its job.");
				} catch (InterruptedException e) {
					logger.log(Level.FINER, "Stopper thread has been interrupted", e);
					running = false;
					TestStoppedException.kill();
					controlled.interrupt();
				}
			}
		});

		thread.setName(controlled.getName() + "-stopper");
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Start the timer: in maxExecTime milliseconds kills the execution of the operation.
	 * @param maxExecTime the amount of time to wait before killing the execution of the operation
	 */
	@SuppressWarnings("unused")
	public void start(int maxExecTime) {

		if(!running) throw new IllegalStateException("The Stopper is not running");

		if(TestFul.DEBUG && endOfTheWorld > 0)
			logger.log(Level.WARNING, "The Stopper is already running", new IllegalStateException("The end is scheduled for " + endOfTheWorld));

		final long _endOfTheWorld = System.currentTimeMillis() + maxExecTime;

		synchronized (wait) {
			endOfTheWorld = _endOfTheWorld;
			TestStoppedException.dontKill();

			if(waiting > _endOfTheWorld) wait.notify();
		}
	}

	/**
	 * Stops the timer: the Stopper does not try to kill the execution of any operation.
	 */
	public void stop() {
		synchronized (wait) {
			endOfTheWorld = -1;
			TestStoppedException.dontKill();
		}
	}

	/**
	 * Discards the timer: release the resources.
	 * Note: after invoking this method, the instance is no longer usable
	 */
	public void done() {
		synchronized (wait) {
			running = false;
			stop();

			wait.notify();
		}
	}
}
