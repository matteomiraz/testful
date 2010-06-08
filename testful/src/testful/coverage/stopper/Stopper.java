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


/**
 * Ensure that third-party code terminates within a given threshold.
 *
 * @author matteo
 */
public final class Stopper {

	/** When idle, wait on this object */
	private final Object idle = new Object();

	/** When counting the time before killing the execution of an operation, wait on this object */
	private final Object wait = new Object();

	/** if greater than zero, indicates when kill the worker (in mill-second, as reported by System.currentTimeMillis()) */
	private long endOfTheWorld = -1;

	/** The stopper is waiting (on wait) until this timestamp */
	private long waiting = -1;

	private boolean running;

	public Stopper() {
		running = true;
		final Thread controlled = Thread.currentThread();

		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while(running) {
						long end;

						// wait for an operation
						if((end = endOfTheWorld) < 0) {
							synchronized (idle) {
								if((end = endOfTheWorld) < 0) {
									idle.wait(10000);
									end = endOfTheWorld;
								}
							}
						}

						if(end > 0) {
							long delta = end - System.currentTimeMillis();

							if(delta <= 0) { // KILL!
								TestStoppedException.stop();
								controlled.interrupt();
								endOfTheWorld = System.currentTimeMillis() + 50;

							} else { // Wait
								waiting = end;
								synchronized (wait) {
									wait.wait(delta);
								}
								waiting = -1;
							}
						}

					}
				} catch (InterruptedException e) {
					running = false;
					TestStoppedException.stop();
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
	public void start(int maxExecTime) {

		if(!running) throw new IllegalStateException("The Stopper is not running");

		endOfTheWorld = System.currentTimeMillis() + maxExecTime;
		TestStoppedException.start();

		synchronized (idle) {
			idle.notify();
		}

		if(waiting > endOfTheWorld) {
			synchronized (wait) {
				wait.notify();
			}
		}
	}

	/**
	 * Stops the timer: the Stopper does not try to kill the execution of any operation.
	 */
	public void stop() {
		endOfTheWorld = -1;
	}

	/**
	 * Discards the timer: release the resources.
	 * Note: after invoking this method, the instance is no longer usable
	 */
	public void done() {
		endOfTheWorld = -1;
		running = false;

		synchronized (idle) {
			idle.notify();
		}

		synchronized (wait) {
			wait.notify();
		}
	}
}
