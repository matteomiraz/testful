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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import testful.ConfigProject;
import testful.IConfigProject;
import testful.TestFul;
import testful.coverage.CoverageTestExecutor;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.model.OptimalTestCreator;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestReader;
import testful.runner.ClassType;
import testful.runner.DataFinderCaching;
import testful.runner.DataFinderImpl;
import testful.runner.RunnerPool;

/**
 * Executes one or more tests and monitor the coverage
 * @author matteo
 */
public class TestCoverageReporter extends TestReader {

	private static class Config extends ConfigProject implements IConfigProject.Args4j {

		@Option(required = false, name = "-writeTest", usage = "For each test read, write a TestCoverage to disk")
		private boolean writeTest = false;

		@Option(required = false, name = "-writeCoverage", usage = "Write the (combined) binary coverage in this file")
		private File writeCoverage;

		@Option(required = false, name = "-total", usage = "Print the total coverage")
		private boolean total = false;

		@Option(required = false, name = "-reload", usage = "Reload classes before each run (reinitialize static fields)")
		private boolean reload = false;

		@Argument
		private List<String> arguments = new ArrayList<String>();

	}

	private final static TrackerDatum[] TRACKER_DATA = new TrackerDatum[] {};

	private static final Logger logger = Logger.getLogger(TestCoverageReporter.class.getCanonicalName());
	@Override public Logger getLogger() { return logger; }

	private final Config config;
	private final DataFinderCaching finder;
	private long numInvocations = 0;
	private final OptimalTestCreator optimal;

	public TestCoverageReporter(Config conf) throws ClassNotFoundException {
		try {
			config = conf;
			finder = new DataFinderCaching(new DataFinderImpl(new ClassType(conf)));
			optimal = new OptimalTestCreator();
		} catch (RemoteException e) {
			// never happens
			logger.log(Level.WARNING, "Remote exception (should never happen): " + e.toString(), e);
			throw new ClassNotFoundException("Cannot contact the remote class loading facility", e);
		}
	}

	public static void main(String[] args) {
		Config config = new Config();
		TestFul.parseCommandLine(config, args, TestCoverageReporter.class, "Test coverage reporter");

		if(!config.isQuiet())
			testful.TestFul.printHeader("Test coverage reporter");

		TestFul.setupLogging(config);

		RunnerPool.getRunnerPool().startLocalWorkers();

		try {
			TestCoverageReporter covReporter = new TestCoverageReporter(config);

			long start = System.currentTimeMillis();
			covReporter.read(config.arguments);
			long end = System.currentTimeMillis();

			if(config.total)
				covReporter.report(end - start);

			if(config.writeCoverage != null)
				covReporter.writeCoverage(config.writeCoverage);

		} catch (ClassNotFoundException e) {
			System.exit(1);
		}
		System.exit(0);
	}

	@Override
	protected void read(String fileName, Test t) {
		try {

			logger.info("Executing a test with " + t.getTest().length + " operations");

			numInvocations += t.getTest().length;
			TestCoverage tCov = new TestCoverage(t,
					RunnerPool.getRunnerPool().execute(CoverageTestExecutor.getContext(finder, t, config.reload, TRACKER_DATA)).get());

			optimal.update(tCov);

			if(config.writeTest) {
				if(t instanceof TestCoverage)
					for(CoverageInformation info : ((TestCoverage) t).getCoverage())
						tCov.getCoverage().put(info);

				tCov.write(new GZIPOutputStream(new FileOutputStream(fileName + "-cov.ser.gz")));
			}

		} catch(IOException e) {
			logger.log(Level.WARNING, "Cannot write the test " + fileName + ": " + e.getMessage(), e);
		} catch(Throwable e) {
			logger.log(Level.WARNING, "Cannot execute the test " + fileName + ": " + e.getMessage(), e);
		}
	}

	public void report(long time) {
		logger.info("coverage report: "  + optimal.createLogMessage(-1, numInvocations, time));
	}

	private void writeCoverage(File writeCoverage) {
		try {
			optimal.getCoverage().write(writeCoverage);
		} catch (IOException e) {
			logger.log(Level.WARNING, "Cannot write the combined coverage to " + writeCoverage + ": " + e.getMessage(), e);
		}
	}

}
