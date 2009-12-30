package testful;

import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import testful.utils.TestfulLogger;

public class TestFul {

	public static final boolean DEBUG = false;

	public static void printHeader(String module) {
		System.out.println("Testful" + (module == null ? "" : (" - " + module)));
		System.out.println("Copyright 2009 - Matteo Miraz (miraz@elet.polimi.it)");
		System.out.println();
		System.out.println();

		String logFileName = TestfulLogger.singleton.getFile(module + ".xml").getAbsolutePath();
		System.out.println("Logging to " + logFileName);
		manageLogger(logFileName);
	}

	public static void manageLogger(String fileName) {
		// disable the console handler
		Logger.getLogger("testful").setUseParentHandlers(false);

		try {
			Handler fh = new FileHandler(fileName);
			fh.setLevel(Level.INFO);
			Logger.getLogger("testful").addHandler(fh);
		} catch(Exception e) {
			System.err.println("Cannot log: " + e);
		}
	}

	public static void parseCommandLine(Object config, String[] args, Class<?> launcher) {

		CmdLineParser parser = new CmdLineParser(config);
		try {
			// parse the arguments.
			parser.parseArgument(args);
		} catch(CmdLineException e) {

			System.err.println(e.getMessage());

			System.err.println();
			System.err.println("Usage: java " + launcher.getCanonicalName() + " [options...] arguments...");
			parser.setUsageWidth(120);
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("   Example: java " + launcher.getCanonicalName() + parser.printExample(org.kohsuke.args4j.ExampleMode.REQUIRED));

			System.exit(1);
		}
	}


}
