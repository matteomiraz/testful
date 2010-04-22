package testful.random;

import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import testful.runner.RunnerPool;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;
import testful.utils.Utils;

public class Launcher {
	protected static Logger logger = Logger.getLogger("testful.random");

	public static void main(String[] args) {

		IConfigRandom config = new ConfigRandom();
		TestFul.parseCommandLine(config, args, Launcher.class, "Random testing");

		if(!config.isQuiet())
			testful.TestFul.printHeader("Random testing");

		TestFul.setupLogging(config);

		logger.config(TestFul.getProperties(config));

		try {
			run(config);
		} catch (TestfulException e) {
			logger.log(Level.SEVERE, "Something went wrong: " + e.getMessage(), e);
		}

		System.exit(0);
	}

	public static void run(IConfigRandom config) throws TestfulException {
		RunnerPool.getRunnerPool().config(config);

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

		ReferenceFactory refFactory = new ReferenceFactory(tc, config.getNumVarCut(), config.getNumVar());

		AnalysisWhiteBox whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), config.getCut());
		TrackerDatum[] data = Utils.readData(whiteAnalysis);

		RandomTest rt = null;

		logger.config("Using the " + config.getRandomType() + " algorithm");
		switch(config.getRandomType()) {
		case SIMPLE:
			rt = new RandomTestSimple(config.isCache(), config.getLog(), finder, tc, refFactory, data);
			break;
		case SPLIT:
			rt = new RandomTestSplit(config.isCache(), config.getLog(), finder, tc, refFactory, data);
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
