package testful.regression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
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
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.model.Operation;
import testful.model.OptimalTestCreator;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestExecutionManager;
import testful.model.TestReader;
import testful.model.TestSplitter;
import testful.runner.ClassFinder;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.Context;
import testful.runner.RunnerPool;
import testful.utils.ElementManager;
import testful.utils.Utils;

/**
 * Given a test suite for a class, tries to reduce it.
 * 
 * @author matteo
 */
public class TestSuiteReducer {
	private static final Logger logger = Logger.getLogger("testful.regression");

	private static class Config extends ConfigCut implements IConfigCut.Args4j, IConfigRunner.Args4j {

		@Option(required = true, name = "-dirOut", usage = "Specify the output directory")
		private File out;

		@Argument
		private List<String> tests = new ArrayList<String>();

		private List<String> remote = new ArrayList<String>();

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
	}

	public static void main(String[] args) {
		Config config = new Config();
		TestFul.parseCommandLine(config, args, TestSuiteReducer.class, "Test suite reducer");

		if(config.isQuiet())
			testful.TestFul.printHeader("Test suite reducer");

		TestFul.setupLogging(config);

		RunnerPool.getRunnerPool().config(config);

		ClassFinderCaching finder = null;
		try {
			finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));
		} catch (RemoteException e) {
			// never happens
		}

		AnalysisWhiteBox whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), config.getCut());
		TrackerDatum[] data = Utils.readData(whiteAnalysis);

		final TestSuiteReducer reducer = new TestSuiteReducer(finder, data);
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

	private static final TestSimplifier simplifier = new TestSimplifier();

	private final OptimalTestCreator optimal = new OptimalTestCreator();
	private final ClassFinder finder;
	private final TrackerDatum[] data;

	public TestSuiteReducer(ClassFinder finder, TrackerDatum[] data) {
		this.finder = finder;
		this.data = data;
	}

	public void process(Test test) {
		logger.fine("Reducing test: initial length " + test.getTest().length);
		if(logger.isLoggable(Level.FINER))
			logger.finer("Original test:\n"+test);

		try {
			Operation[] ops = TestExecutionManager.getOpStatus(finder, test);
			ops = simplifier.process(ops);
			test = new Test(test.getCluster(), test.getReferenceFactory(), ops);
			if(logger.isLoggable(Level.FINER))
				logger.finer("Simplified in:\n"+test);

			try {
				test = test.removeUselessDefs().simplify().getSSA();
			} catch (Exception e) {
				logger.log(Level.WARNING, "Unexpected exception: " + e,  e);
			}

			final List<Test> parts = TestSplitter.split(false, test);
			logger.fine("Identified " + parts.size() + "parts");

			for (Test part : parts) {
				if(logger.isLoggable(Level.FINER)) logger.finer("Part:\n" + part);

				part = part.removeUselessDefs();
				if(logger.isLoggable(Level.FINER)) logger.finer("Part - without useless defs:\n" + part);

				part = part.simplify();
				if(logger.isLoggable(Level.FINER)) logger.finer("Part - statically simplified:\n" + part);

				part = part.getSSA();
				if(logger.isLoggable(Level.FINER)) logger.finer("Part - SSA:\n" + part);

				part = part.removeUselessDefs();
				if(logger.isLoggable(Level.FINER)) logger.finer("Part - without useless defs:\n" + part);

				part = part.reorganize();
				if(logger.isLoggable(Level.FINER)) logger.finer("Part - reorganized:\n" + part);

				part = part.sortReferences();
				if(logger.isLoggable(Level.FINER)) logger.finer("Part - sorted:\n" + part);

				// calculate the coverage for the test
				final TestCoverage coverage = getCoverage(part);
				if(logger.isLoggable(Level.FINER)) logger.finer("Part - coverage:\n" + coverage);
				optimal.update(coverage);
			}

		} catch (InterruptedException e) {
			logger.log(Level.WARNING, "Error while reducing the test: " + e, e);
		} catch (ExecutionException e) {
			logger.log(Level.WARNING, "Error while reducing the test: " + e, e);
		}
	}

	private TestCoverage getCoverage(Test t) throws InterruptedException, ExecutionException {

		Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(finder, t, data);
		ctx.setRecycleClassLoader(false);
		Future<ElementManager<String, CoverageInformation>> f = RunnerPool.getRunnerPool().execute(ctx);

		ElementManager<String, CoverageInformation> cov = f.get();

		return new TestCoverage(t, cov);
	}

	public List<TestCoverage> getOutput() {
		return optimal.get();
	}
}
