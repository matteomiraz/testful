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

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.TestCluster;
import testful.runner.ClassType;
import testful.runner.DataFinder;
import testful.runner.DataFinderCaching;
import testful.runner.DataFinderImpl;
import testful.runner.RunnerPool;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;

public class Launcher {
	protected static Logger logger = Logger.getLogger("testful.random");

	public static void main(String[] args) {

		IConfigRandom config = new ConfigRandom();
		TestFul.parseCommandLine(config, args, Launcher.class, "Random testing");

		if(!config.isQuiet())
			testful.TestFul.printHeader("Random testing");

		TestFul.setupLogging(config);

		logger.config(TestFul.printGetters(config));

		try {
			run(config);
		} catch (TestfulException e) {
			logger.log(Level.SEVERE, "Something went wrong: " + e.getMessage(), e);
		}

		System.exit(0);
	}

	public static void run(IConfigRandom config) throws TestfulException {
		RunnerPool.getRunnerPool().config(config);

		DataFinder finder;
		TestCluster tc;
		try {
			finder = new DataFinderCaching(new DataFinderImpl(new ClassType(config)));
			tc = new TestCluster(new TestfulClassLoader(finder), config);
		} catch (RemoteException e) {
			// never happens
			throw new TestfulException("Cannot create the local classfinder");
		} catch (ClassNotFoundException e) {
			throw new TestfulException("Cannot find some classes: " + e);
		}

		ReferenceFactory refFactory = new ReferenceFactory(tc, config.getNumVarCut(), config.getNumVar());

		RandomTest rt = null;

		logger.config("Using the " + config.getRandomType() + " algorithm");
		switch(config.getRandomType()) {
		case SIMPLE:
			rt = new RandomTestSimple(config.getLog(), finder, config.isReloadClasses(), tc, refFactory, config.getSeed());
			break;
		case SPLIT:
			rt = new RandomTestSplit(config.getLog(), finder, config.isReloadClasses(), tc, refFactory, config.getSeed());
			break;
		}

		Operation.GEN_NEW = config.getpGenNewObj();

		rt.test(config.getTime() * 1000);

		if(config.getLog() != null) {
			ElementManager<String, CoverageInformation> coverage = rt.getExecutionInformation();
			for(CoverageInformation info : coverage) {
				try {
					PrintWriter writer = new PrintWriter(TestFul.createFileWithBackup(config.getLog(), "coverage-" + info.getKey() + ".txt"));
					writer.println(info.getName() + ": " + info.getQuality());
					writer.println();
					writer.println(info);
					writer.close();
				} catch (IOException e) {
					logger.log(Level.WARNING, "Cannot write to file: " + e.getMessage(), e);
				}
			}
		}
	}
}
