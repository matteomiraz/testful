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

package testful.regression;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import testful.ConfigProject;
import testful.IConfigProject;
import testful.TestFul;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.OperationResult.Status;
import testful.model.OperationResultExecutionManager;
import testful.model.Test;
import testful.model.TestReader;
import testful.runner.ClassType;
import testful.runner.DataFinder;
import testful.runner.DataFinderCaching;
import testful.runner.DataFinderImpl;
import testful.runner.RunnerPool;

/**
 * Executes one or more tests on a given class.
 * @author matteo
 */
public class Replay extends TestReader {

	private static final Logger logger = Logger.getLogger("testful.regression");

	private static class Config extends ConfigProject implements IConfigProject.Args4j {

		@Option(required = false, name = "-exitOnBug", usage = "Exit when a bug is found")
		public boolean exitOnBug;

		/** should I reload all classes every new test? */
		@Option(required = false, name = "-reload", usage = "Reload classes before each run (reinitialize static fields)")
		private boolean reloadClasses = false;

		@Argument
		private List<String> tests = new ArrayList<String>();
	}

	private final boolean exitOnBug;
	private final boolean reloadClasses;

	private DataFinder finder;

	public static void main(String[] args) {

		Config config = new Config();
		TestFul.parseCommandLine(config, args, Replay.class, "Replay");

		if(!config.isQuiet())
			TestFul.printHeader("Regression Testing");

		TestFul.setupLogging(config);

		RunnerPool.getRunnerPool().startLocalWorkers();

		try {
			Replay replay = new Replay(config, config.exitOnBug, config.reloadClasses);
			replay.read(config.tests);
		} catch (ClassNotFoundException e) {
			System.exit(1);
		}

		System.exit(0);
	}

	public Replay(IConfigProject config, boolean exitOnBug, boolean reloadClasses) throws ClassNotFoundException {

		this.reloadClasses = reloadClasses;
		this.exitOnBug = exitOnBug;

		try {
			finder = new DataFinderCaching(new DataFinderImpl(new ClassType(config)));
		} catch (RemoteException e) {
			// never happens
			logger.log(Level.WARNING, "Remote exception (should never happen): " + e.toString(), e);
			throw new ClassNotFoundException("Cannot contact the remote class loading facility", e);
		}
	}

	@Override
	protected void read(String fileName, Test test) {
		try {
			logger.info("Replaying " + fileName);
			OperationResult.insert(test.getTest());
			OperationResultExecutionManager.execute(finder, test, reloadClasses);
			Operation[] operations = test.getTest();

			for(Operation op : operations) {
				OperationResult info = (OperationResult) op.getInfo(OperationResult.KEY);

				if(info != null && info.getStatus() == Status.POSTCONDITION_ERROR) {
					dump(operations);
					if(exitOnBug) System.exit(1);
				}
			}
		} catch(Exception e) {
			logger.log(Level.WARNING, "Cannot execute the test " + fileName + ": " + e.getMessage(), e);
		}
	}

	private void dump(Operation[] operations) {
		StringBuilder sb = new StringBuilder();

		sb.append("I found an error:\n");

		for(Operation op : operations) {
			sb.append(op).append("\n");
			OperationResult info = (OperationResult) op.getInfo(OperationResult.KEY);
			if(info != null) sb.append("  ").append(info);
		}

		logger.info(sb.toString());
	}

	@Override
	public Logger getLogger() {
		return logger;
	}
}
