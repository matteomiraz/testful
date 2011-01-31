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
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import testful.ConfigCut;
import testful.IConfigCut;
import testful.IConfigRunner;
import testful.TestFul;
import testful.coverage.CoverageExecutionManager;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.model.OptimalTestCreator;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestReader;
import testful.model.transformation.RemoveUselessDefs;
import testful.model.transformation.SimplifierDynamic;
import testful.model.transformation.SimplifierStatic;
import testful.model.transformation.Splitter;
import testful.model.transformation.TestTransformation;
import testful.model.transformation.TestTransformationPipeline;
import testful.runner.ClassType;
import testful.runner.Context;
import testful.runner.DataFinder;
import testful.runner.DataFinderCaching;
import testful.runner.DataFinderImpl;
import testful.runner.RunnerPool;
import testful.utils.ElementManager;

/**
 * Given a (large) test suite for a class, retrieves the minimum test suite for the class.
 * @author matteo
 */
public class TestSuiteReducer {
	private static final Logger logger = Logger.getLogger("testful.regression.simplifier");

	private static final TestTransformation transform = new TestTransformationPipeline(
			RemoveUselessDefs.singleton,
			SimplifierStatic.singleton
	);

	private final OptimalTestCreator optimal = new OptimalTestCreator();
	private final boolean reloadClasses;
	private final DataFinder finder;
	private final TrackerDatum[] data;

	public TestSuiteReducer(DataFinder finder, boolean reloadClasses, TrackerDatum[] data) {
		this.reloadClasses = reloadClasses;
		this.finder = finder;
		this.data = data;
	}

	public void process(Test test) {
		try {

			test = SimplifierDynamic.singleton.perform(finder, test, reloadClasses);

			test = transform.perform(test);

			final List<Test> parts = Splitter.split(false, test);

			for (Test part : parts) {

				part = transform.perform(part);

				// calculate the coverage for the test and pass it to the optimal test selector
				optimal.update(getCoverage(part));
			}

		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "Error while reducing the test: " + e, e);
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "Error while reducing the test: " + e, e);
		}
	}

	private TestCoverage getCoverage(Test t) throws InterruptedException, ExecutionException {

		Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(finder, t, reloadClasses, data);
		ctx.setReloadClasses(reloadClasses);
		Future<ElementManager<String, CoverageInformation>> f = RunnerPool.getRunnerPool().execute(ctx);

		ElementManager<String, CoverageInformation> cov = f.get();

		return new TestCoverage(t, cov);
	}

	public Collection<TestCoverage> getOutput() {
		return optimal.get();
	}

	// -------------------- static version ------------

	public static Collection<TestCoverage> reduce(DataFinder finder, boolean reloadClasses, List<String> tests) {
		final TestSuiteReducer reducer = new TestSuiteReducer(finder, reloadClasses, new TrackerDatum[0]);
		new TestReader() {

			@Override
			protected Logger getLogger() {
				return logger;
			}

			@Override
			protected void read(String fileName, Test test) {
				reducer.process(test);
			}

		}.read(tests);

		return reducer.getOutput();
	}

	// -------------------- main ----------------------

	private static class Config extends ConfigCut implements IConfigCut.Args4j, IConfigRunner.Args4j {

		@Option(required = true, name = "-dirOut", usage = "Specify the output directory")
		private File out;

		@Argument
		private List<String> tests = new ArrayList<String>();

		private List<String> remote = new ArrayList<String>();

		/** should I reload all classes every new test? */
		@Option(required = false, name = "-reload", usage = "Reload classes before each run (reinitialize static fields)")
		private boolean reloadClasses = false;

		private boolean localEvaluation = true;

		@Override
		public List<String> getRemote() {
			return remote;
		}

		@Override
		public void addRemote(String remote) {
			this.remote.add(remote);
		}

		@Override
		public boolean isLocalEvaluation() {
			return localEvaluation;
		}

		@Override
		public void disableLocalEvaluation(boolean disableLocalEvaluation) {
			localEvaluation = !disableLocalEvaluation;
		}

		public boolean isReloadClasses() {
			return reloadClasses;
		}
	}

	public static void main(String[] args) {
		Config config = new Config();
		TestFul.parseCommandLine(config, args, TestSuiteReducer.class, "Test suite reducer");

		if(config.isQuiet())
			testful.TestFul.printHeader("Test suite reducer");

		TestFul.setupLogging(config);

		RunnerPool.getRunnerPool().config(config);

		DataFinderCaching finder = null;
		try {
			finder = new DataFinderCaching(new DataFinderImpl(new ClassType(config)));
		} catch (RemoteException e) {
			// never happens
			logger.log(Level.WARNING, "Remote exception (should never happen): " + e.toString(), e);
			System.exit(1);
		}

		TrackerDatum[] data = new TrackerDatum[] { };

		final TestSuiteReducer reducer = new TestSuiteReducer(finder, config.isReloadClasses(), data);
		new TestReader() {

			@Override
			protected Logger getLogger() {
				return logger;
			}

			@Override
			protected void read(String fileName, Test test) {
				reducer.process(test);
			}

		}.read(config.tests);


		int i = 0;
		for (TestCoverage t : reducer.getOutput()) {
			try {
				t.write(new GZIPOutputStream(new FileOutputStream(new File(config.out, "Test-" + (i++) + ".ser.gz"))));
			} catch (IOException e) {
				logger.log(Level.WARNING, "Cannot write a test: " + e.getLocalizedMessage(), e);
			}
		}

		System.exit(0);
	}
}
