package testful.regression;

import java.io.FileOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import testful.ConfigProject;
import testful.ConfigRunner;
import testful.IConfigProject;
import testful.TestFul;
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

	private static class Config extends ConfigProject implements IConfigProject.Args4j {

		@Option(required = false, name = "-simplify", usage = "Simplify the test")
		public boolean simplify;

		@Option(required = false, name = "-split", usage = "Split the test in smaller tests")
		public boolean split;

		@Argument
		private List<String> arguments = new ArrayList<String>();

	}

	public static void main(String[] args) {
		testful.TestFul.printHeader("Test simplifier");

		Config config = new Config();
		TestFul.parseCommandLine(config, args, TestReducer.class);

		TestReducer reducer = new TestReducer(config, config.simplify, config.split);
		reducer.read(config.arguments);

		System.exit(0);
	}

	private final TestSimplifier simplifier;
	private final boolean split;

	private TestReducer(IConfigProject config, boolean simplify, boolean split) {
		this.split = split;

		if(simplify) {
			TestSimplifier simplifier = null;
			try {
				IRunner executor = RunnerPool.createExecutor("TestReducer", new ConfigRunner());
				ClassFinder finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirContracts(), config.getDirCompiled()));
				simplifier = new TestSimplifier(executor, finder);
			} catch(RemoteException e) {
				// never happens
			}
			this.simplifier = simplifier;
		} else {
			simplifier = null;
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
