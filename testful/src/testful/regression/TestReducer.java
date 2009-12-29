package testful.regression;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import testful.Configuration;
import testful.model.Test;
import testful.model.TestReader;
import testful.model.TestSimplifier;
import testful.model.TestSplitter;
import testful.runner.ClassFinder;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.IRunner;
import testful.runner.RunnerPool;

public class TestReducer extends TestReader {

	@Option(required = false, name = "-simplify", usage = "Simplify the test")
	public boolean simplify;

	@Option(required = false, name = "-split", usage = "Split the test in smaller tests")
	public boolean split;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public static void main(String[] args) {
		testful.TestFul.printHeader("Test simplifier");

		TestReducer reducer = new TestReducer();
		CmdLineParser parser = new CmdLineParser(reducer);

		try {
			// parse the arguments.
			parser.parseArgument(args);

			reducer.init();

			reducer.read(reducer.arguments);
		} catch(CmdLineException e) {
			if(e.getMessage() != null && e.getMessage().trim().length() > 0) System.err.println(e.getMessage());

			System.err.println("java " + TestReducer.class.getCanonicalName() + " [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("   Example: java " + TestReducer.class.getCanonicalName() + parser.printExample(org.kohsuke.args4j.ExampleMode.REQUIRED));

			System.exit(1);
		}
	}

	private TestSimplifier simplifier = null;
	private Configuration config;

	private void init() {
		config = new Configuration("cut");

		if(simplify) try {
			IRunner executor = RunnerPool.createExecutor(null, false);
			ClassFinder finder = new ClassFinderCaching(new ClassFinderImpl(new File(config.getDirJml()), new File(config.getDirVanilla())));
			simplifier = new TestSimplifier(executor, finder);
		} catch(RemoteException e) {
			// never happens
		}
	}

	@Override
	protected void read(final String fileName, Test test) {
		System.out.println("Reducing test " + fileName + ": " + test.getTest().length);

		if(simplifier != null) test = simplifier.analyze(test);

		if(split) test = TestSplitter.splitAndMerge(test);

		String name = fileName + "-reduced.ser.gz";
		System.out.println("Writing test " + name + ": " + test.getTest().length);
		try {
			test.write(new GZIPOutputStream(new FileOutputStream(name)));
		} catch(IOException e) {
			System.err.println("Error occoured when writing test " + name + ": " + e);
		}


	}

}
