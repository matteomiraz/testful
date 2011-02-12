package testful.mutation;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.coverage.soot.Instrumenter;
import testful.coverage.stopper.ExecutionStopperInstrumenter;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestReader;
import testful.model.executor.TestExecutorInput;
import testful.runner.ClassType;
import testful.runner.Job;
import testful.runner.DataFinder;
import testful.runner.DataFinderCaching;
import testful.runner.DataFinderImpl;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.utils.ElementManager;

public class Launcher {

	private static Logger logger = Logger.getLogger("testful.mutation");

	public static void main(String[] args) throws Exception {
		ConfigMutation config = new ConfigMutation();
		TestFul.parseCommandLine(config, args, Launcher.class, "Mutation testing");

		testful.TestFul.printHeader("Mutation testing");

		generate(config);

		run(config);

		logger.info("\n\nDone\n");
		System.exit(0);
	}

	private static void generate(ConfigMutation config) {
		if(config.getGenMutant().isEmpty()) return;

		Instrumenter.prepare(config, config.getGenMutant());

		ConfigHandler.track = config.isTrack();

		Instrumenter.run(config, config.getGenMutant(),
				ExecutionStopperInstrumenter.singleton,
				new MutatorFunctions(config)
		);
	}

	public static void run(ConfigMutation config) {
		if(config.getArguments().isEmpty()) return;

		try {
			IRunner exec = RunnerPool.getRunnerPool();

			DataFinder finder = new DataFinderCaching(new DataFinderImpl(new ClassType(config)));

			MutationRunner mutationRunner = new MutationRunner(exec, finder, config.isReloadClasses());
			mutationRunner.read(config.getArguments());
			mutationRunner.join();
		} catch(Exception e) {
			logger.warning("Error while running tests: " + e);
		}
	}

	private static class MutJob {

		final String fileName;
		final Test test;
		final Future<MutationCoverage> future;

		public MutJob(String fileName, Test test, Future<MutationCoverage> future) {
			this.fileName = fileName;
			this.test = test;
			this.future = future;
		}
	}

	private static class MutationRunner extends TestReader {

		private final boolean reloadClasses;

		private final BlockingQueue<MutJob> submitted;
		private final DataFinder finder;
		private final IRunner exec;
		private boolean done = false;

		private final Thread futureWaiter;

		public MutationRunner(IRunner exec, DataFinder finder, boolean reloadClasses) throws SecurityException {
			this.reloadClasses = reloadClasses;
			this.exec = exec;
			submitted = new ArrayBlockingQueue<MutJob>(50);
			this.finder = finder;

			futureWaiter = new Thread(new Runnable() {

				@Override
				public void run() {
					while(!(done && submitted.isEmpty())) {
						try {
							MutJob job = submitted.take();
							MutationCoverage info = job.future.get();

							if(info == null) {
								logger.warning(job.fileName + " is not suitable for mutation testing: the test reveals an error in the class!");
							} else {
								logger.info(job.fileName + ":\t" + info.toString());

								ElementManager<String, CoverageInformation> coverage = new ElementManager<String, CoverageInformation>();
								coverage.put(info);
								if(job.test instanceof TestCoverage) {
									for(CoverageInformation cov : ((TestCoverage) job.test).getCoverage()) {
										coverage.put(cov);
									}
								}

								TestCoverage res = new TestCoverage(job.test.getCluster(), job.test.getReferenceFactory(), job.test.getTest(), coverage);
								res.write(new GZIPOutputStream(new FileOutputStream(job.fileName + "-mut.ser.gz")));
							}
						} catch(InterruptedException e) {
						} catch(ExecutionException e) {
							logger.log(Level.WARNING, "Error during the execution: " + e.getMessage(), e);
						} catch(IOException e) {
							logger.log(Level.WARNING, "Error while saving the result: " + e.getMessage(), e);
						}
					}
					logger.info("DONE");
				}

			});
			futureWaiter.start();
		}

		@Override
		protected void read(String fileName, Test test) {
			try {
				Job<TestExecutorInput, MutationCoverage, MutationTestExecutor> ctx = MutationTestExecutor.getContext(finder, new Test(test.getCluster(), test.getReferenceFactory(), test.getTest()));
				ctx.setReloadClasses(reloadClasses);
				MutJob mutJob = new MutJob(fileName, test, exec.execute(ctx));
				submitted.put(mutJob);
				logger.info("submitted: " + fileName);
			} catch(InterruptedException e) {
				logger.log(Level.WARNING, "Cannot submit the job: " + e.getMessage(), e);
			}
		}

		public void join() {
			try {
				done = true;
				futureWaiter.join();
			} catch(InterruptedException e) {
				logger.log(Level.WARNING, "Interrupted: " + e.getMessage(), e);
			}
		}

		@Override
		protected Logger getLogger() {
			return logger;
		}
	}
}
