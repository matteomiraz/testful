package testful.mutation;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import testful.TestfulException;
import testful.coverage.Tracker;
import testful.runner.ExecutionManager;
import testful.runner.Executor;

public class MutationExecutionManagerSingle extends ExecutionManager<Long> {
	private static Logger logger = Logger.getLogger("testful.mutation");

	static final long INTERRUPTED_EXECUTION = -1l;
	static final long FAULTY_EXECUTION = -2l;
	static final long ERROR_EXECUTION = -3l;

	public MutationExecutionManagerSingle(byte[] executorSerGz, byte[] trackerDataSerGz, boolean recycleClassLoader) throws TestfulException {
		super(executorSerGz, trackerDataSerGz, recycleClassLoader);
	}

	@Override
	protected Long getResult() {
		return executionTime;
	}

	@Override
	protected void warmUp() { }
	
	@Override
	protected void setup() throws ClassNotFoundException { }

	@Override
	protected void reallyExecute(boolean stopOnBug)  {
		try {
			Class<?> config = classLoader.loadClass(Utils.CONFIG_CLASS);
			
			MutationExecutionData datum = (MutationExecutionData) Tracker.getDatum(MutationExecutionData.KEY);
			if(datum == null) {
				executionTime = ERROR_EXECUTION;
				return;
			}

			Field mutationField = config.getField(Utils.getCurField(datum.className));
			mutationField.set(null, datum.mutation);
			TestStoppedException.stop = false;

			TestThread tthread = new TestThread(executor);
			tthread.start();
			executionTime = tthread.getResult(datum.maxExecutionTime);
		} catch(Exception e) {
			logger.warning(e.getMessage());
			executionTime = ERROR_EXECUTION;
		}
	}

	private static class TestThread extends Thread {

		private long executionTime;

		private final Executor executor;

		public TestThread(Executor executor) {
			super("TestThread");

			this.executor = executor;
		}

		public long getResult(long max) {
			try {
				this.join(max);

				if(!isAlive()) return executionTime;
			} catch(InterruptedException e) {
			}

			if(isAlive()) {
				try {
					interrupt();
					TestStoppedException.stop = true;
				} catch(Exception e) {
					e.printStackTrace();
				}

				try {
					if(isAlive()) join(2000);
				} catch(InterruptedException e) {
				}

				if(isAlive()) {
					System.err.println("\nthread still alive...");
					for(StackTraceElement ste : getStackTrace())
						System.err.println("  " + ste);

					System.err.println();
				}

				return INTERRUPTED_EXECUTION;
			}

			if(executionTime > max) System.err.println("LONG");

			return executionTime;
		}

		@Override
		public void run() {
			try {
				long start = System.currentTimeMillis();
				int nFaults = executor.execute(true);
				long stop = System.currentTimeMillis();

				if(nFaults == 0) executionTime = (stop - start);
				else executionTime = FAULTY_EXECUTION;
			} catch(ClassNotFoundException e) {
				executionTime = ERROR_EXECUTION;
			}
		}
	}

}
