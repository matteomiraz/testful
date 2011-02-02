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

package testful.utils;

import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;

/**
 * High-efficiency timer
 *
 * @author matteo
 */
public class Timer {

	private static final Logger logger = Logger.getLogger("testful.utils.timer");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);
	private static final boolean LOG_FINER = logger.isLoggable(Level.FINER);

	public interface TimerCallBack {
		void timerExpired();
	}

	/** Standard waiting time if IDLE */
	private static final int IDLE_WAIT = 10000;

	/** Amount of time (ms) between two  */
	private static final int SNOOZE = 20;

	/** wait and synchronize on this object */
	private final Object wait = new Object();

	/** this is the one to call back when the alarm rings */
	private TimerCallBack callBack;

	/** if greater than zero, indicates when notify the client (in milliseconds, as reported by System.currentTimeMillis()) */
	private long expiration = -1;

	/** If greater than zero, it is waiting until this milliseconds */
	private long waiting = -1;

	/** The name of this Timer */
	private final String name;

	public Timer(final String name) {
		this.name = name;

		final Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				while(true) {
					synchronized (wait) {

						final long curr = System.currentTimeMillis();

						// if the timer is not active, sleep for IDLE_WAIT
						final long end = expiration > 0 ? expiration : curr + IDLE_WAIT;

						if(curr >=	end) {
							// the timer is expired!

							if(callBack != null) callBack.timerExpired();
							else if(LOG_FINE) logger.fine("The alarm " + name + " is ringing, but no one cares about it!");

							expiration = curr + SNOOZE;

						} else {
							// Wait

							try {

								// when the timer has to wake up
								final long delta = end - curr;

								waiting = end;
								wait.wait(delta);

							} catch (InterruptedException e) {
								if(LOG_FINER) logger.log(Level.FINER, "Someone is killing the timer!", e);
							} finally {
								waiting = -1;
							}
						}
					}
				}
			}
		});

		thread.setName(name);
		thread.setDaemon(true);
		thread.start();
	}

	/**
	 * Starts the timer: in maxExecTime milliseconds calls the callBack.
	 * @param maxExecTime the amount of time to wait before calling the callBack.
	 */
	public void start(int maxExecTime) {

		synchronized (wait) {

			if(TestFul.DEBUG && expiration > 0)
				TestFul.debug(new IllegalStateException("The Timer is running: the alarm is scheduled for " + expiration));

			expiration = System.currentTimeMillis() + maxExecTime;

			if(waiting > expiration) wait.notify();
		}
	}

	/**
	 * Stops the timer and does not call the callBack anymore (unless another start is called).
	 */
	public void stop() {
		synchronized (wait) {
			expiration = -1;
		}
	}

	/**
	 * @param callBack the one to call back when the alarm rings
	 */
	public void setCallBack(TimerCallBack callBack) {
		this.callBack = callBack;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
}
