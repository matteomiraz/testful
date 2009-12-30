package testful.mutation;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import soot.PackManager;
import soot.Transform;
import soot.jimple.toolkits.scalar.NopEliminator;
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
import testful.utils.ActiveBodyTransformer;
import testful.utils.ElementManager;
import testful.utils.JimpleWriter;
import testful.utils.SootMain;

public class Launcher {

	public static void main(String[] args) throws Exception {
		testful.TestFul.printHeader("Mutation testing");

		ConfigMutation config = new ConfigMutation();
		TestFul.parseCommandLine(config, args, Launcher.class);

		generate(config);

		run(config);

		System.out.println("\n\nDone\n");
		System.exit(0);
	}

	private static final boolean preWriter = false;
	private static final boolean mutator = true;
	private static final boolean bugFinder = true;
	private static final boolean stopper = true;
	private static final boolean postWriter = false;
	private static final boolean nopEliminator = true;

	private static final String[] SOOT_CONF = new String[] { "--keep-line-number", "--xml-attributes", "-validate", "-f", "c" };

	private static void generate(ConfigMutation config) {
		if(config.getGenMutant().isEmpty()) return;

		List<String> sootClassPath = new ArrayList<String>();
		sootClassPath.add(config.getDirContracts().getAbsolutePath());
		sootClassPath.add(config.getDirCompiled().getAbsolutePath());
		sootClassPath.add(System.getProperty("java.class.path"));

		String bootClassPath = System.getProperty("sun.boot.class.path");
		if(bootClassPath != null) sootClassPath.add(bootClassPath); // vaild for sun and ibm jvm
		else {
			System.err.println("Unknown Java Vendor: " + System.getProperty("java.vm.vendor"));
			System.getProperties().list(System.out);
			System.exit(1);
		}

		String params[] = new String[SOOT_CONF.length + 2 + 2 + config.getGenMutant().size()];

		params[0] = "-cp";
		for(String cp : sootClassPath) {
			if(params[1] == null) params[1] = "";
			else params[1] += File.pathSeparator;
			params[1] += cp;
		}

		int i = 2;
		for(String s : SOOT_CONF)
			params[i++] = s;

		params[i++] = "-output-dir";
		params[i++] = config.getDirOutput().getAbsolutePath();

		for(String className : config.getGenMutant())
			params[i++] = className;

		SootMain.singleton.processCmdLine(params);

		ConfigHandler.track = config.isTrack();

		for(String className : config.getGenMutant())
			ConfigHandler.singleton.manage(className);

		String last = null;
		if(preWriter) {
			String newPhase = "jtp.preWriter";
			PackManager.v().getPack("jtp").add(new Transform(newPhase, JimpleWriter.singleton));
			last = newPhase;
			System.out.println("Enabled phase: " + last);
		}

		if(mutator) {
			String newPhase = "jtp.mutator";
			System.out.println("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, new MutatorFunctions(config)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, new MutatorFunctions(config)), last);
			last = newPhase;
		}

		if(bugFinder) {
			String newPhase = "jtp.bugFinder";
			System.out.println("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(BugFinder.singleton)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(BugFinder.singleton)), last);
			last = newPhase;
		}

		if(stopper) {
			String newPhase = "jtp.stopper";
			System.out.println("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(ExecutionStopper.singleton)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(ExecutionStopper.singleton)), last);
			last = newPhase;
		}

		if(postWriter) {
			String newPhase = "jtp.postWriter";
			System.out.println("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)), last);
			last = newPhase;
		}

		if(nopEliminator) {
			String newPhase = "jtp.nopEliminator";
			System.out.println("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(NopEliminator.v())));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(NopEliminator.v())), last);
			last = newPhase;
		}

		System.out.println();

		SootMain.singleton.run();

		try {
			ConfigHandler.singleton.done(config.getDirOutput());
		} catch(IOException e) {
			System.err.println("Cannot write config: " + e);
		}

		for(String className : config.getGenMutant()) {
			try {
				PrintStream statFile = new PrintStream(className + ".txt");
				ConfigHandler.singleton.writeStats(statFile);
				statFile.close();
			} catch(IOException e) {
				System.err.println("Cannot write stats: " + e);
			}
		}
	}

	public static void run(ConfigMutation config) {
		try {
			IRunner exec = RunnerPool.createExecutor("mutation", config);

			ClassFinder finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirOutput()));

			MutationRunner mutationRunner = new MutationRunner(exec, finder, config.isReload());
			mutationRunner.read(config.getArguments());
			mutationRunner.join();
		} catch(Exception e) {
			System.err.println("Error while running tests: " + e);
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
					while(!(done && submitted.isEmpty()))
						try {
							MutJob job = submitted.take();
							MutationCoverage info = job.future.get();

							if(info == null) System.out.println("Warning: " + job.fileName + " is not suitable for mutation testing: the test reveals an error in the class!");
							else {
								System.out.println(job.fileName + ":\t" + info.toString());

								ElementManager<String, CoverageInformation> coverage = new ElementManager<String, CoverageInformation>();
								coverage.put(info);
								if(job.test instanceof TestCoverage) for(CoverageInformation cov : ((TestCoverage) job.test).getCoverage())
									coverage.put(cov);

								TestCoverage res = new TestCoverage(job.test.getCluster(), job.test.getReferenceFactory(), job.test.getTest(), coverage);
								res.write(new GZIPOutputStream(new FileOutputStream(job.fileName + "-mut.ser.gz")));
							}
						} catch(InterruptedException e) {
						} catch(ExecutionException e) {
							System.out.println("Error during the execution: " + e);
							e.printStackTrace();
						} catch(IOException e) {
							System.out.println("Error while saving the result: " + e);
							e.printStackTrace();
						}
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
				System.out.println("submitted: " + fileName);
			} catch(InterruptedException e) {
				System.err.println("Cannot submit the job: " + e);
			}
		}

		public void join() {
			try {
				done = true;
				futureWaiter.join();
			} catch(InterruptedException e) {
				System.err.println("Interrotto! " + e.getMessage());
			}
		}
	}
}
