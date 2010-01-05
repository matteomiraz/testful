package testful.evolutionary.jMetal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.crossover.OnePointCrossoverVarLen;
import jmetal.base.operator.localSearch.LocalSearch;
import jmetal.base.operator.selection.BinaryTournament2;
import jmetal.base.operator.selection.Selection;
import jmetal.util.JMException;
import testful.TestFul;
import testful.TestfulException;
import testful.IUpdate.Callback;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.evolutionary.ConfigEvolutionary;
import testful.evolutionary.IConfigEvolutionary;
import testful.model.Operation;
import testful.model.OptimalTestCreator;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestSplitter;
import testful.model.TestSuite;
import testful.random.RandomTest;
import testful.random.RandomTestSplit;
import testful.regression.JUnitTestGenerator;
import testful.runner.RunnerPool;
import testful.utils.Utils;

public class Launcher {
	private static Logger logger = Logger.getLogger("testful.evolutionary");

	public static void main(String[] args) throws TestfulException, InterruptedException {
		ConfigEvolutionary config = new ConfigEvolutionary();
		TestFul.parseCommandLine(config, args, Launcher.class, "Evolutionary test generator");

		if(!config.isQuiet())
			testful.TestFul.printHeader("Evolutionary test generator");

		TestFul.setupLogging(config);

		logger.config(TestFul.getProperties(config));

		run(config);

		System.exit(0);
	}

	public static void run(IConfigEvolutionary config, Callback ... callBacks) throws TestfulException, InterruptedException {
		RunnerPool.getRunnerPool().config(config);

		if(config.getLog() != null && config.getLogLevel().getLoggingLevel().intValue() > Level.FINE.intValue()) {
			try {
				final String logFile = config.getLog().getAbsolutePath() + File.separator + "NSGAII_main.log";
				jmetal.base.Configuration.logger_.addHandler(new FileHandler(logFile));

				logger.info("Logging NSGAII to " + logFile);
			} catch (IOException e) {
				logger.warning("Cannot enable logging for NSGAII: " + e.getMessage());
			}
		}


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
				problem.addReserve(genSmartPopulation(config, problem));
			} catch (Exception e) {
				logger.log(Level.WARNING, "Cannot create the initial population: " + e.getMessage(), e);
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
			LocalSearch<Operation> localSearch = problem.getLocalSearch();
			algorithm.setImprovement(localSearch);
			algorithm.setLocalSearchPeriod(config.getLocalSearchPeriod());
		}

		for (Callback callBack : callBacks)
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
		JUnitTestGenerator gen = new JUnitTestGenerator(config, false, config.getDirGeneratedTests(), true);
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
	private static TestSuite genSmartPopulation(IConfigEvolutionary config, JMProblem problem) throws TestfulException, RemoteException, ClassNotFoundException, FileNotFoundException{
		logger.info("Generating smart population");

		AnalysisWhiteBox whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), config.getCut());
		TrackerDatum[] data = Utils.readData(whiteAnalysis);

		RandomTest rt = new RandomTestSplit(config.isCache(), null, problem.getFinder() , problem.getCluster(), problem.getRefFactory(), data);

		rt.test(30000);

		return rt.getResults();
	} //genSmartPopulation
} // NSGAII_main
