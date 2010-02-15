package testful.coverage;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.ConfigCut;
import testful.IConfigCut;
import testful.TestFul;
import testful.model.TestCluster;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.TestfulClassLoader;
import testful.utils.Instrumenter;

/**
 * Modify the class under test, adding trackers and other stuff.
 * 
 * @author matteo
 */
public class Launcher {

	private static Logger logger = Logger.getLogger("testful.coverage.Instrumenter");

	public static void main(String[] args) {
		IConfigCut config  = new ConfigCut();

		TestFul.parseCommandLine(config, args, Launcher.class, "Instrumenter");

		if(!config.isQuiet())
			testful.TestFul.printHeader("Instrumenter");

		testful.TestFul.setupLogging(config);

		try {
			ClassFinderCaching finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirContracts(), config.getDirCompiled()));
			TestfulClassLoader tcl = new TestfulClassLoader(finder);
			TestCluster tc = new TestCluster(tcl, config);

			Collection<String> toInstrument = tc.getClassesToInstrument();

			Instrumenter.prepare(config, toInstrument);

			Instrumenter.run(config, toInstrument, config.getCut(),
					//			testful.coverage.behavior.BehaviorInstrumenter.singleton,
					//			testful.coverage.bug.BugInstrumenter.singleton,
					testful.coverage.whiteBox.WhiteInstrumenter.singleton
			);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error during the instrumentation: " + e.getMessage(), e);
		}

		System.exit(0);
	}
}
