package testful.random;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import testful.TestFul;
import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.TestCluster;
import testful.runner.ClassFinder;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;
import testful.utils.TestfulLogger;
import testful.utils.Utils;

public class Launcher {

	public static void main(String[] args) {
		testful.TestFul.printHeader("Random testing");

		IConfigRandom config = new ConfigRandom();
		TestFul.parseCommandLine(config, args, Launcher.class);

		try {
			TestfulLogger.singleton.writeParameters(config.getSettings());
		} catch (IOException e) {
			System.err.println("Cannot write to file: " + e.getMessage());
		}

		try {
			run(config);
		} catch (TestfulException e) {
			System.err.println("Something went wrong: " + e.getMessage());
		}

		System.exit(0);
	}

	public static void run(IConfigRandom config) throws TestfulException {

		ClassFinder finder;
		TestCluster tc;
		try {
			finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));
			tc = new TestCluster(new TestfulClassLoader(finder), config);
		} catch (RemoteException e) {
			// never happens
			throw new TestfulException("Cannot create the local classfinder");
		} catch (ClassNotFoundException e) {
			throw new TestfulException("Cannot find some classes: " + e);
		}

		tc.clearCache();

		if(config.isVerbose()) System.out.println(tc.getRegistry().toString());

		ReferenceFactory refFactory = new ReferenceFactory(tc, config.getNumVarCut(), config.getNumVar());

		IRunner exec = RunnerPool.createExecutor("randomTesting", config);

		AnalysisWhiteBox whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), config.getCut());
		TrackerDatum[] data = Utils.readData(whiteAnalysis);

		RandomTest rt = null;

		switch(config.getRandomType()) {
		case SIMPLE:
			rt = new RandomTestSimple(exec, config.isCache(), finder, tc, refFactory, data);
			break;
		case SPLIT:
			rt = new RandomTestSplit(exec, config.isCache(), finder, tc, refFactory, data);
			break;
		}

		Operation.GEN_NEW = config.getpGenNewObj();

		rt.startNotificationThread(!config.isNoStats());

		rt.test(config.getTime() * 1000);

		ElementManager<String, CoverageInformation> coverage = rt.getExecutionInformation();
		if(!config.isNoStats()) for(CoverageInformation info : coverage)
			System.out.println(info.getName() + ": " + info.getQuality() + "\n" + info);

		for(CoverageInformation info : coverage) {
			try {
				PrintWriter writer = TestfulLogger.singleton.getWriter("coverage-" + info.getKey() + ".txt");
				writer.println(info.getName() + ": " + info.getQuality());
				writer.println();
				writer.println(info);
				writer.close();
			} catch (IOException e) {
				System.err.println("Canno write to file: " + e.getMessage());
			}
		}

	}
}
