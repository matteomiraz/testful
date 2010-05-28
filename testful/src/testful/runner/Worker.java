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
import java.util.logging.Logger;

public class Worker extends Thread {

	private static Logger logger = Logger.getLogger("testful.executor.worker");

	private static int idGenerator = 0;

	private final WorkerManager workerManager;

	public Worker(WorkerManager manager) {
		super("Worker-" + ++idGenerator);
		workerManager = manager;
		setDaemon(true);
	}

	@Override
	public void run() {
		logger.fine("Created worker " + getName());

		try {
			while(true) {
				Context<?, ?> ctx = workerManager.getTest();

				TestfulClassLoader cl;
				try {
					cl = workerManager.getClassLoader(ctx);
				} catch(RemoteException e) {
					logger.warning("Cannot retrieve the class loader: " + e.getMessage());
					continue;
				}

				try {
					logger.finer("Evaluating " + ctx.id);
					ExecutionManager<?> execManager = ctx.getExecManager(cl);
					Serializable result = execManager.execute(ctx.stopOnBug);
					workerManager.putResult(ctx, result, cl);
				} catch(Exception e) {
					workerManager.putException(ctx, e, cl);
				}
			}
		} catch(RemoteException e) {
			logger.warning("Worker " + getName() + " interrupted: " + e.getMessage());
		}
	}
}
