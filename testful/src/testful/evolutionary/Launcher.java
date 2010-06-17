/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.evolutionary;

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
import jmetal.util.PseudoRandom;
import testful.IUpdate.Callback;
import testful.TestFul;
import testful.TestfulException;
import testful.coverage.TrackerDatum;
import testful.model.Operation;
import testful.model.TestSuite;
import testful.random.RandomTest;
import testful.random.RandomTestSplit;
import testful.regression.JUnitTestGenerator;
import testful.regression.TestSuiteReducer;
import testful.runner.RunnerPool;

/**
 * Main TestFul class.
 * @author matteo
 */
public class Launcher {
	private static Logger logger = Logger.getLogger("testful.evolutionary");

	public static void main(String[] args) throws TestfulException {
		ConfigEvolutionary config = new ConfigEvolutionary();
		TestFul.parseCommandLine(config, args, Launcher.class, "Evolutionary test generator");

		if(!config.isQuiet())
			testful.TestFul.printHeader("Evolutionary test generator");

		TestFul.setupLogging(config);

		run(config);

		System.exit(0);
	}

	/**
	 * Starts TestFul.
	 * This method can be used if one wants to start Testful from a (java) program (and not from command line).
	 * @param config the configuration
	 * @param callBacks where publish updates on the run
	 * @throws TestfulException if something goes wrong
	 */
	public static void run(IConfigEvolutionary config, Callback ... callBacks) throws TestfulException {
		logger.config(TestFul.getProperties(config));

		PseudoRandom.setupMersenneTwisterFast(config.getSeed());

		RunnerPool.getRunnerPool().config(config);

		if(config.getLog() != null && logger.isLoggable(Level.FINE)) {
			try {
				final String logFile = new File(config.getLog(),  "NSGAII.log").getAbsolutePath();
				jmetal.base.Configuration.logger_.addHandler(new FileHandler(logFile));

				logger.info("Logging NSGAII to " + logFile);
			} catch (IOException e) {
				logger.warning("Cannot enable logging for NSGAII: " + e.getMessage());
			}
		}

		TestfulProblem testfulProblem;
		try {
			testfulProblem = new TestfulProblem(config);
		} catch (ClassNotFoundException e) {
			logger.log(Level.WARNING, "Cannot find some classes: " + e.getMessage(), e);
			throw new TestfulException(e);
		}

		JMProblem problem = new JMProblem(testfulProblem, config);

		NSGAII<Operation> algorithm = new NSGAII<Operation>(problem);
		algorithm.setPopulationSize(config.getPopSize());
		algorithm.setMaxEvaluations(config.getTime() * 1000);
		algorithm.setInherit(config.getFitnessInheritance());
		algorithm.setUseCpuTime(config.isUseCpuTime());

		try {
			testfulProblem.addReserve(genSmartPopulation(config, testfulProblem));
		} catch (Exception e) {
			logger.log(Level.WARNING, "Cannot create the initial population: " + e.getMessage(), e);
		}

		// Mutation and Crossover for Real codification
		OnePointCrossoverVarLen<Operation> crossover = new OnePointCrossoverVarLen<Operation>();
		crossover.setProbability(0.50);
		crossover.setMaxLen(config.getMaxTestLen());

		algorithm.setCrossover(crossover);

		TestfulMutation mutation = new TestfulMutation(testfulProblem);
		mutation.setProbability(0.05);
		mutation.setProbRemove(0.75f);
		algorithm.setMutation(mutation);

		/* Selection Operator */
		Selection<Operation,Solution<Operation>> selection = new BinaryTournament2<Operation>();
		algorithm.setSelection(selection);

		if(config.getLocalSearchPeriod() > 0) {
			LocalSearch<Operation> localSearch = new LocalSearchBranch(testfulProblem);
			algorithm.setImprovement(localSearch);
			algorithm.setLocalSearchPeriod(config.getLocalSearchPeriod());
			algorithm.setLocalSearchNum(config.getLocalSearchElements()/100.0f);
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

		/* simplify tests */
		final TestSuiteReducer reducer = new TestSuiteReducer(testfulProblem.getFinder(), testfulProblem.getData());
		for (Solution<Operation> sol : population)
			reducer.process(testfulProblem.getTest(sol.getDecisionVariables().variables_));

		/* convert tests to jUnit */
		JUnitTestGenerator gen = new JUnitTestGenerator(config.getDirGeneratedTests(), true);
		gen.read(reducer.getOutput());
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
	private static TestSuite genSmartPopulation(IConfigEvolutionary config, TestfulProblem testfulProblem) throws TestfulException, RemoteException, ClassNotFoundException, FileNotFoundException{
		int smartTime = config.getSmartInitialPopulation();

		if(smartTime <= 0) return null;

		logger.info("Generating smart population");

		TrackerDatum[] data = new TrackerDatum[] { };

		RandomTest rt = new RandomTestSplit(config.getLog(), testfulProblem.getFinder(), config.isReload(), testfulProblem.getCluster(), testfulProblem.getReferenceFactory(), config.getSeed(), data);

		rt.test(smartTime * 1000);

		return rt.getResults();
	} //genSmartPopulation
} // NSGAII_main
