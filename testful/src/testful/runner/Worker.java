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

package testful.runner;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.utils.StopWatchNested;

/**
 * A thread executing something (i.e., working)
 * @author matteo
 */
public class Worker extends Thread {

	private static Logger logger = Logger.getLogger("testful.executor.worker");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);
	private static final boolean LOG_FINER = logger.isLoggable(Level.FINER);

	private static int idGenerator = 0;

	private final WorkerManager workerManager;

	public Worker(WorkerManager manager) {
		super("Worker-" + ++idGenerator);
		workerManager = manager;
		setDaemon(true);
	}

	StopWatchNested t_run = StopWatchNested.getRootTimer("wRun");
	StopWatchNested t_eval = t_run.getSubTimer("wRun.eval");
	StopWatchNested t_resOk = t_run.getSubTimer("wRun.putResult");
	StopWatchNested t_resExc = t_run.getSubTimer("wRun.putException");

	@Override
	public void run() {
		if(LOG_FINE) logger.fine("Created worker " + getName());

		try {
			while(true) {
				Context<?,?,?> ctx = workerManager.getTest();

				t_run.start();

				TestfulClassLoader cl;
				try {
					cl = workerManager.getClassLoader(ctx);
				} catch(RemoteException e) {
					logger.warning("Worker " + getName() + " cannot retrieve the class loader: " + e.getMessage());
					workerManager.putException(ctx, e, null);
					continue;
				}

				t_eval.start();
				try {
					if(LOG_FINER) logger.finer("Worker " + getName() + " is evaluating " + ctx.id);
					Serializable result = ctx.execute(cl);
					t_eval.stop();
					t_resOk.start();
					workerManager.putResult(ctx, result, cl);
					t_resOk.stop();
				} catch(Exception e) {
					t_eval.stop();
					t_resExc.start();
					workerManager.putException(ctx, e, cl);
					t_resExc.stop();
				}
				t_run.stop();
			}
		} catch(RemoteException e) {
			logger.warning("Worker " + getName() + " interrupted: " + e.getMessage());
		}
	}
}
