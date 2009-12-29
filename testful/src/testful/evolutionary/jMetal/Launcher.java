package testful.evolutionary.jMetal;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.crossover.OnePointCrossoverVarLen;
import jmetal.base.operator.localSearch.LocalSearch;
import jmetal.base.operator.selection.BinaryTournament2;
import jmetal.base.operator.selection.Selection;
import jmetal.util.JMException;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import testful.IUpdate;
import testful.TestfulException;
import testful.IUpdate.Callback;
import testful.model.Operation;
import testful.model.OptimalTestCreator;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestSplitter;
import testful.model.TestfulProblem;
import testful.regression.JUnitTestGenerator;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.utils.TestfulLogger;

public class Launcher {

	@Option(required = true, name = "-cut", usage = "The class to test", metaVar = "full.qualified.ClassName")
	private String cut;

	@Option(required = false, name = "-cutSize", usage = "Number of places in the repository for the CUT")
	private int cutSize = 4;

	@Option(required = false, name = "-auxSize", usage = "Number of places in the repository for auxiliary classes")
	private int auxSize = 4;

	@Option(required = false, name = "-testSize", usage = "Maximum test length (n° of invocations)")
	private int maxSize = 10000;

	@Option(required = false, name = "-reload", usage = "Reload classes before each run (reinitialize static fields)")
	private boolean reload = false;

	@Option(required = false, name = "-time", usage = "The maximum execution time (in seconds)")
	private int time = 600;

	@Option(required = false, name = "-localSearchPeriod", usage = "Period of the local search (default: every 20 generations; <= 0 to disable local search)")
	private int localSearchPeriod = 20;

	@Option(required = false, name = "-disableLength", usage = "Removes the length of test from the multi-objective fitness")
	private boolean disableLength = false;

	@Option(required = false, name = "-enableBug", usage = "Inserts the number of bug found in the multi-objective fitness")
	private boolean enableBug = false;

	@Option(required = false, name = "-disableBranch", usage = "Removes the branch coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
	private boolean disableBranch = false;

	@Option(required = false, name = "-disableBranchCode", usage = "Removes the branch coverage on the code from the multi-objective fitness")
	private boolean disableBranchCode = false;

	@Option(required = false, name = "-disableBranchContract", usage = "Removes the branch coverage on contracts from the multi-objective fitness")
	private boolean disableBranchContract = false;

	@Option(required = false, name = "-disableBasicBlock", usage = "Removes the basic block coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
	private boolean disableBasicBlock = false;

	@Option(required = false, name = "-disableBasicBlockCode", usage = "Removes the basic block coverage on the code from the multi-objective fitness")
	private boolean disableBasicBlockCode = false;

	@Option(required = false, name = "-disableBasicBlockContract", usage = "Removes the basic block coverage on contracts from the multi-objective fitness")
	private boolean disableBasicBlockContract = false;

	@Option(required = false, name = "-disableFitnessInheritance", usage = "Disable fitness inheritance, i.e., evaluate all individuals in each population")
	private boolean disableFitnessInheritance = false;

	@Option(required = false, name = "-fitnessInheritanceUniform", usage = "Use an uniform fitness inheritance")
	private boolean fitnessInheritanceUniform = false;

	@Option(required = false, name = "-remote", usage = "Use the specified remote evaluator")
	private String remote;

	@Option(required = false, name = "-noLocal", usage = "Do not use local evaluators")
	private boolean noLocal;

	@Option(required = false, name = "-enableCache", usage = "Enable evaluation cache. Notice that it can degrade performances")
	private boolean enableCache;

	@Option(required = false, name = "-baseDir", usage = "Specify the CUT's base directory")
	private String baseDir;

	@Option(required = false, name = "-popSize", usage = "The size of the population (# of individuals)")
	private int popSize = 512;

	private IRunner executor = null;

	public IRunner getExecutor() {
		if(executor == null)
			synchronized(this) {
				if(executor == null) {
					executor = RunnerPool.createExecutor("testful", noLocal);
					executor.addRemoteWorker(remote);
				}
			}

		return executor;
	}

	public void parseArgs(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);

		try {
			// parse the arguments.
			parser.parseArgument(args);

		} catch(CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java " + Launcher.class.getCanonicalName() + " [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("   Example: java " + Launcher.class.getCanonicalName() + parser.printExample(org.kohsuke.args4j.ExampleMode.REQUIRED));

			System.exit(1);
		}
	}

	public static void main(String[] args) throws TestfulException, SecurityException, IOException, InterruptedException {
		testful.TestFul.printHeader("Testful-nsgaII");
		String baseDir = TestfulLogger.singleton.getBaseDir();

		// Logger object and file to store log messages
		Logger logger_ = jmetal.base.Configuration.logger_;
		logger_.addHandler(new FileHandler(baseDir + File.separator + "NSGAII_main.log"));

		run(args, new IUpdate.Callback() {

			@Override
			public void update(long start, long current, long end, Map<String, Float> coverage) {

				StringBuilder sb = new StringBuilder();

				sb.append("Start: ").append(new Date(start)).append(" ").append(((current - start) / 1000) / 60).append(" minutes ").append(((current - start) / 1000) % 60).append(" seconds ago\n");
				sb.append("Now  : ").append(new Date()).append("\n");
				sb.append("End  : ").append(new Date(end)).append(" ").append(((end - current) / 1000) / 60).append(" minutes ").append(((end - current) / 1000) % 60).append(" seconds").append("\n");

				if(!coverage.isEmpty()) {
					sb.append("Coverage:\n");
					for(Entry<String, Float> cov : coverage.entrySet())
						sb.append("  ").append(cov.getKey()).append(": ").append(cov.getValue()).append("\n");
				}

				System.out.println(sb.toString());
			}
		});

		System.exit(0);
	}

	public static void run(String[] args, Callback callBack) throws TestfulException, InterruptedException {
		Launcher opt = new Launcher();
		opt.parseArgs(args);

		final TestfulProblem.TestfulConfig config;
		if (opt.baseDir != null) config = new TestfulProblem.TestfulConfig(opt.baseDir);
		else config = new TestfulProblem.TestfulConfig();

		config.setCut(opt.cut);
		config.cluster.setRepoSize(opt.auxSize);
		config.cluster.setRepoCutSize(opt.cutSize);

		config.fitness.len = !opt.disableLength;
		config.fitness.bug = opt.enableBug;
		config.fitness.bbd = !(opt.disableBasicBlock || opt.disableBasicBlockCode);
		config.fitness.bbn = !(opt.disableBasicBlock || opt.disableBasicBlockContract);
		config.fitness.brd = !(opt.disableBranch || opt.disableBranchCode);
		config.fitness.brn = !(opt.disableBranch || opt.disableBranchContract);

		JMProblem problem;
		try {
			problem = JMProblem.getProblem(opt.getExecutor(), opt.enableCache, opt.reload, config);
		} catch (JMException e) {
			throw new TestfulException(e);
		}

		NSGAII<Operation> algorithm = new NSGAII<Operation>(problem);
		algorithm.setPopulationSize(opt.popSize);
		algorithm.setMaxEvaluations(opt.time * 1000);
		algorithm.setInherit(!opt.disableFitnessInheritance);
		algorithm.setInheritUniform(opt.fitnessInheritanceUniform);

		// Mutation and Crossover for Real codification
		OnePointCrossoverVarLen<Operation> crossover = new OnePointCrossoverVarLen<Operation>();
		crossover.setProbability(0.50);
		crossover.setMaxLen(opt.maxSize);

		algorithm.setCrossover(crossover);

		TestfulMutation mutation = new TestfulMutation();
		mutation.setProbSimplify(0.05f);
		mutation.setProbability(0.01);
		mutation.setProbRemove(0.75f);
		algorithm.setMutation(mutation);

		/* Selection Operator */
		Selection<Operation,Solution<Operation>> selection = new BinaryTournament2<Operation>();
		algorithm.setSelection(selection);

		if(opt.localSearchPeriod > 0) {
			LocalSearch<Operation> localSearch = new LocalSearchBranch(problem.getProblem());
			algorithm.setImprovement(localSearch);
			algorithm.setLocalSearchPeriod(opt.localSearchPeriod);
		}

		algorithm.register(callBack);

		/* Execute the Algorithm */
		SolutionSet<Operation> population;
		try {
			population = algorithm.execute();
		} catch (JMException e) {
			throw new TestfulException(e);
		}

		/* convert tests to jUnit */

		/* split tests into smaller parts */
		Collection<Test> parts = new ArrayList<Test>();
		for(Solution<Operation> t : population)
			parts.addAll(TestSplitter.split(true, problem.getTest(t)));

		/* evaluate small tests */
		Collection<TestCoverage> testCoverage = problem.evaluate(parts);

		{
			int i = 0;
			JUnitTestGenerator gen = new JUnitTestGenerator(config);
			gen.executeTests();
			for(Test t : testCoverage)
				gen.read(File.separator + "TestBase" + i++, t);
		}

		/* select best small tests */
		OptimalTestCreator optimal = new OptimalTestCreator();
		for (TestCoverage tc : testCoverage)
			optimal.update(tc);

		/* write them to disk as JUnit */
		int i = 0;
		JUnitTestGenerator gen = new JUnitTestGenerator(config);
		gen.executeTests();
		for(Test t : optimal.get())
			gen.read(File.separator + "TestFul" + i++, t);

		gen.writeSuite(getPackageName(config.getCut()), "AllTests");
	}//main

	private static String getPackageName(String className) {
		StringBuilder pkgBuilder =  new StringBuilder();
		String[] parts = className.split("\\.");

		for(int i = 0; i < parts.length-1; i++) {
			if(i > 0) pkgBuilder.append('.');
			pkgBuilder.append(parts[i]);
		}
		return pkgBuilder.toString();
	}
} // NSGAII_main
