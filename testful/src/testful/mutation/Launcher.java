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
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestReader;
import testful.runner.ClassFinder;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.Context;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.utils.ElementManager;
import testful.utils.Instrumenter;

public class Launcher {

	private static Logger logger = Logger.getLogger("testful.mutation");

	public static void main(String[] args) throws Exception {
		ConfigMutation config = new ConfigMutation();
		TestFul.parseCommandLine(config, args, Launcher.class, "Mutation testing");

		TestFul.setupLogging(config);

		if(!config.isQuiet())
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

		Instrumenter.run(config, config.getGenMutant(), "mutant",
				ExecutionStopper.singleton,
				new MutatorFunctions(config),
				BugFinder.singleton
		);
	}

	public static void run(ConfigMutation config) {
		if(config.getArguments().isEmpty()) return;

		RunnerPool.getRunnerPool().config(config);

		try {
			IRunner exec = RunnerPool.getRunnerPool();

			ClassFinder finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));

			MutationRunner mutationRunner = new MutationRunner(exec, finder, config.isReload());
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

		private final boolean reload;

		private final BlockingQueue<MutJob> submitted;
		private final ClassFinder finder;
		private final IRunner exec;
		private boolean done = false;

		private final Thread futureWaiter;

		public MutationRunner(IRunner exec, ClassFinder finder, boolean reload) throws SecurityException {
			this.reload = reload;
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
				Context<MutationCoverage, MutationExecutionManager> ctx = MutationExecutionManager.getContext(finder, new Test(test.getCluster(), test.getReferenceFactory(), test.getTest()));
				if(reload) ctx.setRecycleClassLoader(false);
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
