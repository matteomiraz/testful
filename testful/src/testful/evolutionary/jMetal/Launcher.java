package testful.evolutionary.jMetal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
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
import testful.IUpdate;
import testful.TestFul;
import testful.TestfulException;
import testful.IUpdate.Callback;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.evolutionary.ConfigEvolutionary;
import testful.evolutionary.IConfigEvolutionary;
import testful.model.Operation;
import testful.model.OptimalTestCreator;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestCoverage;
import testful.model.TestSplitter;
import testful.model.TestsCollection;
import testful.random.RandomTest;
import testful.random.RandomTestSplit;
import testful.regression.JUnitTestGenerator;
import testful.utils.ElementManager;
import testful.utils.TestfulLogger;
import testful.utils.Utils;

public class Launcher {

	public static void main(String[] args) throws TestfulException, SecurityException, IOException, InterruptedException {
		testful.TestFul.printHeader("Testful-nsgaII");

		IConfigEvolutionary config = new ConfigEvolutionary();
		TestFul.parseCommandLine(config, args, Launcher.class);

		if(config.isVerbose()) {
			String baseDir = TestfulLogger.singleton.getBaseDir();

			// Logger object and file to store log messages
			Logger logger_ = jmetal.base.Configuration.logger_;
			logger_.addHandler(new FileHandler(baseDir + File.separator + "NSGAII_main.log"));
		}

		if(config.isQuiet()) {
			run(config, new IUpdate.Callback() {
				@Override
				public void update(long start, long current, long end, Map<String, Float> coverage) {
				}
			});
		} else {
			run(config, new IUpdate.Callback() {

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
		}

		System.exit(0);
	}

	public static void run(IConfigEvolutionary config, Callback callBack) throws TestfulException, InterruptedException {
		JMProblem problem;
		try {
			problem = new JMProblem(config);
		} catch (JMException e) {
			throw new TestfulException(e);
		}

		NSGAII<Operation> algorithm = new NSGAII<Operation>(problem);
		algorithm.setPopulationSize(config.getPopSize());
		algorithm.setMaxEvaluations(config.getTime() * 1000);
		algorithm.setInherit(config.getFitnessInheritance());

		if(config.isSmartInitialPopulation()) {
			try {
				problem.setInitPopulation(genSmartPopulation(config, problem));
			} catch (Exception e) {
				System.err.println("Cannot create the initial population: " + e.getMessage());
			}
		}

		// Mutation and Crossover for Real codification
		OnePointCrossoverVarLen<Operation> crossover = new OnePointCrossoverVarLen<Operation>();
		crossover.setProbability(0.50);
		crossover.setMaxLen(config.getMaxTestLen());

		algorithm.setCrossover(crossover);

		TestfulMutation mutation = new TestfulMutation(problem);
		mutation.setProbSimplify(0.05f);
		mutation.setProbability(0.01);
		mutation.setProbRemove(0.75f);
		algorithm.setMutation(mutation);

		/* Selection Operator */
		Selection<Operation,Solution<Operation>> selection = new BinaryTournament2<Operation>();
		algorithm.setSelection(selection);

		if(config.getLocalSearchPeriod() > 0) {
			LocalSearch<Operation> localSearch = new LocalSearchBranch(problem.getProblem());
			algorithm.setImprovement(localSearch);
			algorithm.setLocalSearchPeriod(config.getLocalSearchPeriod());
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

		/* select best small tests */
		OptimalTestCreator optimal = new OptimalTestCreator();
		for (TestCoverage tc : testCoverage)
			optimal.update(tc);

		/* write them to disk as JUnit */
		int i = 0;
		JUnitTestGenerator gen = new JUnitTestGenerator(config, problem.getProblem().getRunnerCaching(), false, config.getDirGeneratedTests());
		for(Test t : optimal.get()) {
			gen.read(File.separator + "Ful_" + getClassName(config.getCut()) + "_" + i++, t);
		}

		gen.writeSuite(getPackageName(config.getCut()), "AllTests_" + getClassName(config.getCut()));
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

	private static String getClassName(String className) {
		if(!className.contains(".")) return className;

		String[] parts = className.split("\\.");
		return parts[parts.length - 1];
	}

	/**
	 * This function uses random.Launcher to generate a smarter initial population
	 * @author Tudor
	 * @return
	 * @throws TestfulException
	 * @throws RemoteException
	 * @throws ClassNotFoundException
	 * @throws FileNotFoundException
	 */
	private static TestsCollection genSmartPopulation(IConfigEvolutionary config, JMProblem problem) throws TestfulException, RemoteException, ClassNotFoundException, FileNotFoundException{
		System.out.println("Generating smart population");

		TestCluster tc = problem.getProblem().getCluster();

		ReferenceFactory refFactory = new ReferenceFactory(tc, config.getNumVarCut(), config.getNumVar());

		AnalysisWhiteBox whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), config.getCut());
		TrackerDatum[] data = Utils.readData(whiteAnalysis);

		RandomTest rt = new RandomTestSplit(problem.getProblem().getRunnerCaching(), config.isCache(), problem.getProblem().getFinder() , tc, refFactory, data);

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
