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
