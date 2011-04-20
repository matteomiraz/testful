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

package testful.random;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.behavior.AbstractorRegistry;
import testful.coverage.behavior.BehaviorCoverage;
import testful.model.Operation;
import testful.model.OperationResultTestExecutor;
import testful.model.ReferenceFactory;
import testful.model.TestCluster;
import testful.model.TestClusterBuilder;
import testful.model.TestCoverage;
import testful.regression.JUnitTestGenerator;
import testful.regression.TestSuiteReducer;
import testful.runner.ClassType;
import testful.runner.DataFinder;
import testful.runner.DataFinderCaching;
import testful.runner.DataFinderImpl;
import testful.runner.ObjectType;
import testful.runner.RemoteClassLoader;
import testful.utils.ElementManager;

public class Launcher {
	protected static Logger logger = Logger.getLogger("testful.random");

	public static void main(String[] args) {

		IConfigRandom config = new ConfigRandom();
		TestFul.parseCommandLine(config, args, Launcher.class, "Random testing");

		testful.TestFul.printHeader("Random testing");

		logger.config(TestFul.printGetters(config));

		try {
			run(config);
		} catch (TestfulException e) {
			logger.log(Level.SEVERE, "Something went wrong: " + e.getMessage(), e);
		}

		System.exit(0);
	}

	public static void run(IConfigRandom config) throws TestfulException {
		DataFinder finder;
		TestCluster cluster;
		try {

			final ClassType classType = new ClassType(config);
			final ObjectType objectType = new ObjectType();
			finder = new DataFinderCaching(new DataFinderImpl(classType, objectType));

			TestClusterBuilder clusterBuilder = new TestClusterBuilder(new RemoteClassLoader(finder), config);
			cluster = clusterBuilder.getTestCluster();
			objectType.addObject(cluster);

			if(config.isBehavioral()) objectType.addObject(new AbstractorRegistry(cluster, clusterBuilder.getXmlRegistry()));

		} catch (RemoteException e) {
			// never happens
			throw new TestfulException("Cannot create the local classfinder");
		} catch (ClassNotFoundException e) {
			throw new TestfulException("Cannot find some classes: " + e);
		}

		ReferenceFactory refFactory = new ReferenceFactory(cluster, config.getNumVarCut(), config.getNumVar());

		RandomTest rt = null;

		logger.config("Using the " + config.getRandomType() + " algorithm");
		switch(config.getRandomType()) {
		case SIMPLE:
			rt = new RandomTestSimple(finder, config.isReloadClasses(), cluster, refFactory, config.getSeed());
			break;
		case SPLIT:
			rt = new RandomTestSplit(finder, config.isReloadClasses(), cluster, refFactory, config.getSeed());
			break;
		}

		Operation.GEN_NEW = config.getpGenNewObj();

		rt.test(config.getTime() * 1000);

		ElementManager<String, CoverageInformation> coverage = rt.getExecutionInformation();

		if(TestFul.logDir != null) {
			for(CoverageInformation info : coverage) {
				try {
					PrintWriter writer = new PrintWriter(TestFul.createFileWithBackup(TestFul.logDir, "coverage-" + info.getKey() + ".txt"));
					writer.println(info.getName() + ": " + info.getQuality());
					writer.println();
					writer.println(info);
					writer.close();
				} catch (IOException e) {
					logger.log(Level.WARNING, "Cannot write to file: " + e.getMessage(), e);
				}
			}
		}

		if(config.isBehavioral()) {

			BehaviorCoverage behavioralCoverage = (BehaviorCoverage) coverage.get(BehaviorCoverage.KEY);
			if(behavioralCoverage != null) {

				BehaviorCoverage.DOT = true;

				try {
					if(!config.getDirGeneratedTests().exists()) config.getDirGeneratedTests().mkdirs();
					final File outFile = new File(config.getDirGeneratedTests(), "behavior-" + config.getCut() + ".dot");
					PrintStream out = new PrintStream(outFile);
					out.println(behavioralCoverage.toString());
					out.close();
					logger.info("Behavioral model saved in " + outFile.getPath());
				} catch (Exception e) {
					logger.log(Level.WARNING, "Cannot save the behavioral model: " + e.getMessage(), e);
				}
			} else {
				logger.warning("The behavioral model is missing");
			}
		}

		/* simplify tests */
		final TestSuiteReducer reducer = new TestSuiteReducer(finder, config.isReloadClasses(), new TrackerDatum[0]);
		for (TestCoverage t : rt.getOptimalTests())
			reducer.process(t);

		/* convert tests to jUnit */
		try {
			Collection<TestCoverage> tests = OperationResultTestExecutor.execute(finder, reducer.getOutput(), config.isReloadClasses());

			JUnitTestGenerator gen = new JUnitTestGenerator(config.getDirGeneratedTests(), new RemoteClassLoader(finder), true);
			gen.read(tests);
			gen.writeSuite();

		} catch (RemoteException e) {
			logger.log(Level.WARNING, "Remote exception (should never happen): " + e.toString(), e);
		}
	}
}
