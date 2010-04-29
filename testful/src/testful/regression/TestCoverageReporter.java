package testful.regression;

import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import org.kohsuke.args4j.Argument;

import testful.ConfigProject;
import testful.IConfigProject;
import testful.TestFul;
import testful.coverage.CoverageExecutionManager;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.model.OperationResult;
import testful.model.OperationStatus;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestReader;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.Context;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.utils.ElementManager;
import testful.utils.Utils;

/**
 * Executes one or more tests and monitor the coverage
 * @author matteo
 */
public class TestCoverageReporter extends TestReader {

	private static final Logger logger = Logger.getLogger("testful.regression");

	private static class Config extends ConfigProject implements IConfigProject.Args4j {

		@Argument
		private List<String> arguments = new ArrayList<String>();

	}

	private IRunner exec;
	private final ClassFinderCaching finder;
	private final IConfigProject config;

	public TestCoverageReporter(IConfigProject config) {
		try {
			this.config = config;

			exec = RunnerPool.getRunnerPool();
			finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));

		} catch(RemoteException e) {
			// never happens
			throw new RuntimeException("should never happen");
		}
	}

	public static void main(String[] args) {
		Config config = new Config();
		TestFul.parseCommandLine(config, args, TestCoverageReporter.class, "Test coverage reporter");

		if(config.isQuiet())
			testful.TestFul.printHeader("Test coverage reporter");

		TestFul.setupLogging(config);

		RunnerPool.getRunnerPool().startLocalWorkers();

		TestCoverageReporter coverage = new TestCoverageReporter(config);
		coverage.read(config.arguments);
	}

	@Override
	protected void read(String fileName, Test test) {
		try {
			OperationStatus.remove(test);
			OperationResult.remove(test);

			TrackerDatum[] data= Utils.readData(AnalysisWhiteBox.read(config.getDirInstrumented(), test.getCluster().getCut().getClassName()));

			Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(finder, test, data);
			Future<ElementManager<String, CoverageInformation>> future = exec.execute(ctx);
			ElementManager<String, CoverageInformation> coverage = future.get();

			if(test instanceof TestCoverage)
				for(CoverageInformation info : ((TestCoverage) test).getCoverage())
					coverage.put(info);

			Test t = new TestCoverage(test.getCluster(), test.getReferenceFactory(), test.getTest(), coverage);
			t.write(new GZIPOutputStream(new FileOutputStream(fileName + "-cov.ser.gz")));
		} catch(IOException e) {
			logger.log(Level.WARNING, "Cannot write the test " + fileName + ": " + e.getMessage(), e);
		} catch(Throwable e) {
			logger.log(Level.WARNING, "Cannot execute the test " + fileName + ": " + e.getMessage(), e);
		}
	}


	@Override
	public Logger getLogger() {
		return logger;
	}
}
