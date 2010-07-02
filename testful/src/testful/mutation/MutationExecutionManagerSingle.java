package testful.mutation;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestfulException;
import testful.coverage.Tracker;
import testful.coverage.TrackerDatum;
import testful.runner.ExecutionManager;
import testful.runner.Executor;

public class MutationExecutionManagerSingle extends ExecutionManager<Long> {
	private static Logger logger = Logger.getLogger("testful.mutation");

	static final long FAULTY_EXECUTION = -2l;
	static final long ERROR_EXECUTION = -3l;

	public MutationExecutionManagerSingle(byte[] executorSerGz, byte[] trackerDataSerGz, boolean recycleClassLoader) throws TestfulException {
		super(executorSerGz, trackerDataSerGz, recycleClassLoader);
	}

	public MutationExecutionManagerSingle(Executor executor, TrackerDatum[] data) throws TestfulException {
		super(executor, data);
	}

	@Override
	protected Long getResult() {
		if(faults == null) return ERROR_EXECUTION;
		if(faults > 0) return FAULTY_EXECUTION;

		return executionTime;
	}

	@Override
	protected void warmUp() { }

	@Override
	protected void setup() throws ClassNotFoundException { }

	@Override
	protected void reallyExecute(boolean stopOnBug) throws ClassNotFoundException  {
		MutationExecutionData datum = (MutationExecutionData) Tracker.getDatum(MutationExecutionData.KEY);
		if(datum == null) {
			logger.finer("MutationExecution Data not found");
			return;
		}

		try {
			Class<?> config = classLoader.loadClass(Utils.CONFIG_CLASS);
			Field mutationField = config.getField(Utils.getCurField(datum.className));
			mutationField.set(null, datum.mutation);
		} catch (Throwable e) {
			logger.log(Level.FINE, "Cannot activate the mutation", e);
			return;
		}

		super.reallyExecute(stopOnBug);
	}
}
