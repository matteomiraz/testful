package testful.mutation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.coverage.TrackerDatum;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.Test;
import testful.model.executor.ReflectionExecutor;
import testful.model.executor.TestExecutorInput;
import testful.runner.Job;
import testful.runner.DataFinder;
import testful.runner.Executor;
import testful.runner.RemoteClassLoader;

/**
 * This is a fake execution manager: it is able to manage "real" mutant execution managers,
 * giving to each of them a mutant to evaluate. Users must use this class to evaluate mutants.
 *
 * @author matteo
 *
 */
public class MutationTestExecutor extends Executor<TestExecutorInput, MutationCoverage> {

	private static Logger logger = Logger.getLogger("testful.mutation");

	public static Job<TestExecutorInput, MutationCoverage, MutationTestExecutor> getContext(DataFinder finder, Test test, TrackerDatum ... data) {
		return new Job<TestExecutorInput, MutationCoverage, MutationTestExecutor>(MutationTestExecutor.class, finder, new TestExecutorInput(test, true, data));
	}

	/** tracker data. trackerData[0] contains information about the mutation to run */
	private TrackerDatum[] trackerData;

	public MutationTestExecutor() {
		super();
	}

	/* (non-Javadoc)
	 * @see testful.runner.Executor#setInput(java.io.Serializable)
	 */
	@Override
	public void setInput(TestExecutorInput input) {
		super.setInput(input);

		TrackerDatum[] inputTrackerData = input.getTrackerData();

		int i = 1;
		trackerData = new TrackerDatum[inputTrackerData.length+1];
		for(TrackerDatum datum : inputTrackerData) trackerData[i++] = datum;
	}

	/* (non-Javadoc)
	 * @see testful.runner.Executor#execute()
	 */
	@Override
	public MutationCoverage execute() throws Exception {

		/** the configuration class */
		final Class<?> config;
		try {
			config = Class.forName(Utils.CONFIG_CLASS);
		} catch(ClassNotFoundException e) {
			Exception exc = new Exception("Cannot load the Configuration class " + Utils.CONFIG_CLASS + ": " + e.getMessage(), e);
			logger.log(Level.WARNING, e.getMessage(), e);
			throw exc;
		}

		final MutationCoverage coverage = new MutationCoverage();

		/** classes to mutate */
		final Set<String> classes = new HashSet<String>();
		for(Field f : config.getFields()) {
			if(f.getName().startsWith(Utils.CUR_MUTATION_PREFIX)) {
				String m = f.getName();
				String n = Utils.decodeClassName(m.substring(Utils.CUR_MUTATION_PREFIX.length(), m.length()-Utils.CUR_MUTATION_SUFFIX.length()));
				classes.add(n);
			}
		}

		for(String className : classes) {
			logger.fine("Applying mutation analysis on " + className);
			MutationCoverageSingle singleCov = executeMutantsOnSingleClass(className);

			if(singleCov != null)
				coverage.add(className, singleCov);
		}

		return coverage;
	}

	private MutationCoverageSingle executeMutantsOnSingleClass(String className) {
		try {
			Class<?> config = Class.forName(Utils.CONFIG_CLASS);

			Integer maxMutations = (Integer) config.getMethod(Utils.getMaxField(className)).invoke(null);
			Class<?> clazz = classLoader.loadClass(className);
			Field executedMutantsField = clazz.getField(Utils.EXECUTED_MUTANTS);

			MutationCoverageSingle coverage = null;
			Operation[] test = getInput().getTest().getTest();

			// run the original class (1st time)
			boolean hasFaults = execute(className, 0);
			logger.fine("Run test (" + test.length + ") on the original class " + className);

			if(!hasFaults) {
				// re-run the test, tracking operation status
				OperationResult.insert(test);
				execute(className, 0);
				logger.fine("Tracked operation status on " + className);

				// re-run the test, verifying operation status
				OperationResult.Verifier.insertOperationResultVerifier(test);
				hasFaults = execute(className, 0);
				logger.fine("Verified operation status on " + className);
			}

			if(hasFaults) {
				// the class contains some errors, revealed by the test
				logger.warning("The test reveals some errors in the class " + className + ": skipping mutation analysis!");
				return null;
			}

			// get the live mutant set
			execute(className, -1);

			BitSet executedMutants;
			try {
				executedMutants = (BitSet) executedMutantsField.get(null);
				logger.fine("Got executed mutants on " + className + ": " + executedMutants.cardinality() + "/" + maxMutations);
			} catch (Throwable e1) {
				executedMutants = null;
				logger.log(Level.FINE, "Cannot retrieve the list of executed mutants: " + e1, e1);
			}

			BitSet notExecutedMutants = new BitSet();
			if(executedMutants == null) notExecutedMutants.set(1, maxMutations + 1);
			else {
				notExecutedMutants.or(executedMutants);
				notExecutedMutants.flip(1, maxMutations + 1);
			}

			coverage = new MutationCoverageSingle(notExecutedMutants);

			//TODO: set the max execution time by modifying the test cluster!
			// final long maxExecutionTime = 5 * (originalExecutionTime) + 500;
			// logger.fine("Set maximum execution time to " + maxExecutionTime + " (" + originalExecutionTime + ")");

			for(int mutation = executedMutants.nextSetBit(0); mutation >= 0; mutation = executedMutants.nextSetBit(mutation + 1)) {
				try {
					hasFaults = execute(className, mutation);
					if(hasFaults) {
						coverage.setKilled(mutation);
						logger.fine("Killed mutant " + mutation);
					} else {
						coverage.setAlive(mutation);
						logger.fine("Alive mutant " + mutation);
					}

				} catch(Exception e) {
					logger.log(Level.WARNING, "Error executing mutant " + mutation + ": " + e.getMessage(), e);
				}

			}

			return coverage;

		} catch(Exception e) {
			logger.log(Level.WARNING, "Error executing the mutant: " + e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Executes the test with on a mutant, and returns if the mutant has been killed
	 * (i.e., the test detects an error)
	 * @param className the name of the class to mutate
	 * @param mutationID the id of the mutation to use
	 * @return true if the mutation has been killed, false otherwise
	 */
	private boolean execute(String className, int mutationID) {

		try {
			RemoteClassLoader loader = isReloadClasses() ? classLoader.getNew() : classLoader;

			Class<?> config = loader.loadClass(Utils.CONFIG_CLASS);
			Field mutationField = config.getField(Utils.getCurField(className));
			mutationField.set(null, mutationID);

			final int faults;
			if(isReloadClasses()) {
				Class<?> re = loader.loadClass(ReflectionExecutor.class.getName());
				Method execute = re.getMethod("execute", Test.class, Boolean.TYPE, Boolean.TYPE);
				faults = (Integer) execute.invoke(null, getInput().getTest(), false, true);
			} else {
				faults = ReflectionExecutor.execute(getInput().getTest(), false, true);
			}

			return faults > 0;
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error while executing the mutant " + className + "." + mutationID + ": " + e.getMessage(), e);
			return true;
		}
	}
}
