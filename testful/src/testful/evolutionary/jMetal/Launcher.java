package testful.evolutionary.jMetal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import jmetal.base.Configuration;
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
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.TestCluster;
import testful.model.TestfulProblem;
import testful.model.TestsCollection;
import testful.model.TestfulProblem.TestfulConfig;
import testful.random.RandomTest;
import testful.random.RandomTestSplit;
import testful.regression.JUnitTestGenerator;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.utils.ElementManager;
import testful.utils.TestfulLogger;
import testful.utils.Utils;

public class Launcher {

	public static Logger logger_; // Logger object
	public static FileHandler fileHandler_; // FileHandler object

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


	//Random integration ************************************************************************************
	@Option(required = false, name = "-smartAncestors", usage = "Use an enhanced initial population")
	private boolean smartInitialPopulation;

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

	public static void main(String[] args) throws TestfulException, JMException, SecurityException, IOException {
		testful.TestFul.printHeader("Testful-nsgaII");
		String baseDir = TestfulLogger.singleton.getBaseDir();

		Launcher opt = new Launcher();
		opt.parseArgs(args);

		// Logger object and file to store log messages
		logger_ = Configuration.logger_;
		fileHandler_ = new FileHandler(baseDir + File.separator + "NSGAII_main.log");
		logger_.addHandler(fileHandler_);

		//construct config
		final TestfulProblem.TestfulConfig config;
		if (opt.baseDir != null) config = new TestfulProblem.TestfulConfig(opt.baseDir);
		else config = new TestfulProblem.TestfulConfig();
		config.setCut(opt.cut);
		config.cluster.setRepoSize(opt.auxSize);
		config.cluster.setRepoCutSize(opt.cutSize);

		config.setCut(opt.cut);
		config.cluster.setRepoSize(opt.auxSize);
		config.cluster.setRepoCutSize(opt.cutSize);

		config.fitness.len = !opt.disableLength;
		config.fitness.bug = opt.enableBug;
		config.fitness.bbd = !(opt.disableBasicBlock || opt.disableBasicBlockCode);
		config.fitness.bbn = !(opt.disableBasicBlock || opt.disableBasicBlockContract);
		config.fitness.brd = !(opt.disableBranch || opt.disableBranchCode);
		config.fitness.brn = !(opt.disableBranch || opt.disableBranchContract);

		JMProblem problem = JMProblem.getProblem(opt.getExecutor(), opt.enableCache, opt.reload, config);
		if (opt.smartInitialPopulation) {
			try {
				problem.setInitPopulation(genSmartPopulation(opt, config, problem));
			} catch (Exception e) {
				logger_.warning("Cannot create the initial population: " + e.getMessage());
			}
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

		algorithm.register(new IUpdate.Callback() {

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

		/* Execute the Algorithm */
		long initTime = System.currentTimeMillis();
		SolutionSet<Operation> population = algorithm.execute();
		long estimatedTime = System.currentTimeMillis() - initTime;

		System.out.println("Total execution time: " + estimatedTime);
		System.out.println("Population size: " + population.size());


		/* convert tests to jUnit */
		int i = 0;
		JUnitTestGenerator gen = new JUnitTestGenerator(config);
		for(Solution<Operation> t : population)
			gen.read(File.separator + "TestFul" + i++, problem.getTest(t));

		gen.writeSuite();

		System.exit(0);
	}//main

	/**
	 * This function uses random.Launcher to generate a smarter initial population
	 * @author Tudor
	 * @return
	 * @throws TestfulException
	 * @throws RemoteException
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 */
	private static TestsCollection genSmartPopulation(Launcher opt, TestfulConfig config, JMProblem problem) throws TestfulException, RemoteException, ClassNotFoundException, FileNotFoundException{
		System.out.println("Generating smart population");

		TestCluster tc = problem.getProblem().getCluster();

		ReferenceFactory refFactory = new ReferenceFactory(tc, opt.cutSize, opt.auxSize);

		AnalysisWhiteBox whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), config.getCut());
		TrackerDatum[] data = Utils.readData(whiteAnalysis);

		RandomTest rt = new RandomTestSplit(opt.getExecutor(), opt.enableCache, problem.getProblem().getFinder() , tc, refFactory, data);

		rt.setKeepTests(true); //set whether to save tests or not

		rt.startNotificationThread(false);

		rt.test(30000);

		try {
			while(rt.getRunningJobs() > 0)
				Thread.sleep(1000);
		} catch(InterruptedException e) {}

		ElementManager<String, CoverageInformation> coverage = rt.getExecutionInformation();

		for(CoverageInformation info : coverage) {
			PrintWriter writer = TestfulLogger.singleton.getWriter("coverage-" + info.getKey() + ".txt");
			writer.println(info.getName() + ": " + info.getQuality());
			writer.println();
			writer.println(info);
			writer.close();
		}

		rt.stopNotificationThreads(); //Stop threads instead of stoping the entire system
		problem.getProblem().getCluster().clearCache(); //Leave it as you found it
		//throw results in case I'm being used by another program
		return rt.getResults();
	} //genSmartPopulation
} // NSGAII_main
