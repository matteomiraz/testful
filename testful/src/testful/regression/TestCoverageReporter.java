package testful.regression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

import testful.Configuration;
import testful.TestfulException;
import testful.coverage.CoverageExecutionManager;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.model.OperationPrimitiveResult;
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

public class TestCoverageReporter extends TestReader {

	private IRunner exec;
	private final ClassFinderCaching finder;
	private final Configuration config;

	public TestCoverageReporter(Configuration config) throws TestfulException {
		try {
			this.config = config;

			exec = RunnerPool.createExecutor(null, false);

			finder = new ClassFinderCaching(new ClassFinderImpl(new File(config.getDirInstrumented()), new File(config.getDirJml()), new File(config.getDirVanilla())));

		} catch(RemoteException e) {
			// never happens
			throw new TestfulException(e);
		}
	}

	public static void main(String[] args) {
		try {
			testful.TestFul.printHeader("Test coverage reporter");

			TestCoverageReporter coverage = new TestCoverageReporter(new Configuration("cut"));
			coverage.read(args);
		} catch(TestfulException e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	protected void read(String fileName, Test test) {
		try {
			OperationStatus.remove(test);
			OperationPrimitiveResult.remove(test);

			TrackerDatum[] data= Utils.readData(AnalysisWhiteBox.read(config.getDirInstrumented(), test.getCluster().getCut().getClassName()));

			Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(finder, test, data);
			Future<ElementManager<String, CoverageInformation>> future = exec.execute(ctx);
			ElementManager<String, CoverageInformation> coverage = future.get();

			if(test instanceof TestCoverage)
				for(CoverageInformation info : ((TestCoverage) test).getCoverage())
					coverage.put(info);

			Test t = new TestCoverage(test.getCluster(), test.getReferenceFactory(), test.getTest(), coverage);
			t.write(new GZIPOutputStream(new FileOutputStream(fileName + "-cov.ser.gz")));
			System.out.println("Done.\n\n");
		} catch(IOException e) {
			System.err.println("Cannot write the test: " + e.getMessage());
		} catch(Throwable e) {
			System.err.println("Cannot execute the test: " + e.getMessage());
		}
	}

}
