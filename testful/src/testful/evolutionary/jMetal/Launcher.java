package testful.evolutionary.jMetal;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
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
import testful.model.TestSuite;
import testful.random.RandomTest;
import testful.random.RandomTestSplit;
import testful.regression.JUnitTestGenerator;
import testful.runner.RunnerPool;
import testful.utils.Utils;

public class Launcher {
	private static Logger logger = Logger.getLogger("testful.evolutionary");

	public static void main(String[] args) throws TestfulException {
		ConfigEvolutionary config = new ConfigEvolutionary();
		TestFul.parseCommandLine(config, args, Launcher.class, "Evolutionary test generator");

		if(!config.isQuiet())
			testful.TestFul.printHeader("Evolutionary test generator");

		TestFul.setupLogging(config);

		logger.config(TestFul.getProperties(config));

		run(config);

		System.exit(0);
	}

	public static void run(IConfigEvolutionary config, Callback ... callBacks) throws TestfulException {
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
		algorithm.setUseCpuTime(config.isUseCpuTime());

		try {
			problem.addReserve(genSmartPopulation(config, problem));
		} catch (Exception e) {
			logger.log(Level.WARNING, "Cannot create the initial population: " + e.getMessage(), e);
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

		JUnitTestGenerator gen = new JUnitTestGenerator(config, config.getDirGeneratedTests(), config.isReload(), true);
		for(Solution<Operation> t : population)
			gen.read("", problem.getTest(t));

		gen.writeSuite();

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
	private static TestSuite genSmartPopulation(IConfigEvolutionary config, JMProblem problem) throws TestfulException, RemoteException, ClassNotFoundException, FileNotFoundException{
		int smartTime = config.getSmartInitialPopulation();

		if(smartTime <= 0) return null;

		logger.info("Generating smart population");

		AnalysisWhiteBox whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), config.getCut());
		TrackerDatum[] data = Utils.readData(whiteAnalysis);

		RandomTest rt = new RandomTestSplit(config.isCache(), null, problem.getFinder() , problem.getCluster(), problem.getRefFactory(), data);

		rt.test(smartTime * 1000);

		return rt.getResults();
	} //genSmartPopulation
} // NSGAII_main
