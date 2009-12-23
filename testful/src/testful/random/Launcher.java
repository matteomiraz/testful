package testful.random;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.TestCluster;
import testful.model.TestfulProblem.TestfulConfig;
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

	@Option(required = true, name = "-cut", usage = "The class to test", metaVar = "full.qualified.ClassName")
	private String cut;

	@Option(required = false, name = "-cutSize", usage = "Number of places in the repository for the CUT")
	private int cutSize = 8;

	@Option(required = false, name = "-auxSize", usage = "Number of places in the repository for auxiliary classes")
	private int auxSize = 8;

	@Option(required = false, name = "-pGenNewObj", usage = "Probability to create new objects")
	private float pGenNewObj = 0.35f;

	@Option(required = false, name = "-noStats", usage = "Do not print randomTest stats")
	private boolean noStats = false;

	@Option(required = false, name = "-verbose")
	private boolean verbose = false;

	@Option(required = false, name = "-simple", usage = "Use simple RT instead of split RT")
	private boolean simple = false;

	@Option(required = false, name = "-time", usage = "Duration of randomTest (in seconds)")
	private int time = 60;

	@Option(required = false, name = "-remote", usage = "Use the specified remote evaluator")
	private String remote;

	@Option(required = false, name = "-noLocal", usage = "Do not use local evaluators")
	private boolean noLocal;

	@Option(required = false, name = "-enableCache", usage = "Enable evaluation cache. Notice that it can degrade performances")
	private boolean enableCache;

	@Option(required = false, name = "-baseDir", usage = "Specify the base directory.")
	private String cutBase = "cut";

	public static void main(String[] args) throws ClassNotFoundException {
		testful.TestFul.printHeader("Random testing");

		Launcher rt = new Launcher();
		CmdLineParser parser = new CmdLineParser(rt);

		try {
			// parse the arguments.
			parser.parseArgument(args);

			TestfulLogger.singleton.writeParameters(rt.getSettings());

			rt.run();

		} catch(CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java " + Launcher.class.getCanonicalName() + " [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("   Example: java " + Launcher.class.getCanonicalName() + parser.printExample(org.kohsuke.args4j.ExampleMode.REQUIRED));

			System.exit(1);
		} catch(IOException e) {
			System.err.println("Error writing to file: " + e.getMessage());
			System.exit(1);
		} catch(TestfulException e) {
			System.err.println("Generic error: " + e.getMessage());
			System.exit(1);
		}
	}

	private Set<String> getSettings() {
		Set<String> ret = new HashSet<String>();

		ret.add("cut=" + cut);
		ret.add("cutSize=" + cutSize);
		ret.add("auxSize=" + auxSize);
		ret.add("pGenNewObj=" + pGenNewObj);
		ret.add("stats=" + !noStats);
		ret.add("time=" + time);

		return ret;
	}

	private void run() throws ClassNotFoundException, IOException, TestfulException {
		
		TestfulConfig config = new TestfulConfig(cutBase);
		config.setCut(cut);
		
		ClassFinder finder = new ClassFinderCaching(new ClassFinderImpl(new File(config.getDirInstrumented()), new File(config.getDirJml()), new File(config.getDirVanilla())));

		TestCluster tc = new TestCluster(new TestfulClassLoader(finder), config);
		tc.clearCache();

		if(verbose) System.out.println(tc.getRegistry().toString());

		ReferenceFactory refFactory = new ReferenceFactory(tc, cutSize, auxSize);

		IRunner exec = RunnerPool.createExecutor("randomTesting", noLocal);
		exec.addRemoteWorker(remote);

		AnalysisWhiteBox whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), config.getCut()); 
		TrackerDatum[] data = Utils.readData(whiteAnalysis);

		RandomTest rt;
		if(simple) rt = new RandomTestSimple(exec, enableCache, finder, tc, refFactory, data);
		else rt = new RandomTestSplit(exec, enableCache, finder, tc, refFactory, data);

		Operation.GEN_NEW = pGenNewObj;

		rt.startNotificationThread(!noStats);

		rt.test(time * 1000);

		try {
			while(rt.getRunningJobs() > 0)
				Thread.sleep(1000);
		} catch(InterruptedException e) {
		}

		ElementManager<String, CoverageInformation> coverage = rt.getExecutionInformation();
		if(!noStats) for(CoverageInformation info : coverage)
			System.out.println(info.getName() + ": " + info.getQuality() + "\n" + info);

		for(CoverageInformation info : coverage) {
			PrintWriter writer = TestfulLogger.singleton.getWriter("coverage-" + info.getKey() + ".txt");
			writer.println(info.getName() + ": " + info.getQuality());
			writer.println();
			writer.println(info);
			writer.close();
		}

		System.exit(0);
	}
}
