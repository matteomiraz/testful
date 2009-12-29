package testful.regression;

import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import testful.Configuration;
import testful.model.Operation;
import testful.model.OperationStatus;
import testful.model.Test;
import testful.model.TestExecutionManager;
import testful.model.TestReader;
import testful.model.OperationStatus.Status;
import testful.runner.ClassFinder;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.IRunner;
import testful.runner.RunnerPool;

public class Replay extends TestReader {

	@Option(required = false, name = "-exitOnBug", usage = "Exit when a bug is found")
	public boolean EXIT_ON_BUG;

	@Option(required = false, name = "-verbose", usage = "Be more verbose")
	public boolean VERBOSE;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	private IRunner executor;
	private ClassFinder finder;

	public static void main(String[] args) {
		testful.TestFul.printHeader("Regression Testing");

		Replay replay = new Replay(new Configuration("cut"));
		CmdLineParser parser = new CmdLineParser(replay);

		try {
			// parse the arguments.
			parser.parseArgument(args);

			replay.read(replay.arguments);
		} catch(CmdLineException e) {
			if(e.getMessage() != null && e.getMessage().trim().length() > 0) System.err.println(e.getMessage());

			System.err.println("java " + Replay.class.getCanonicalName() + " [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("   Example: java " + Replay.class.getCanonicalName() + parser.printExample(org.kohsuke.args4j.ExampleMode.REQUIRED));

			System.exit(1);
		}

		System.exit(0);
	}

	public Replay(Configuration config) {
		executor = RunnerPool.createExecutor(null, false);
		try {
			finder = new ClassFinderCaching(new ClassFinderImpl(new File(config.getDirInstrumented()), new File(config.getDirJml()), new File(config.getDirVanilla())));
		} catch(RemoteException e) {
			// never happens!
		}
	}

	@Override
	protected void read(String fileName, Test t) {
		try {

			System.out.print("Replaying " + fileName);
			Test test = new Test(t.getCluster(), t.getReferenceFactory(), t.getTest());
			Future<Operation[]> future = executor.execute(TestExecutionManager.getContext(finder, test));
			System.out.print(" ... ");
			Operation[] operations = future.get();
			System.out.println("done");

			for(Operation op : operations) {
				OperationStatus info = (OperationStatus) op.getInfo(OperationStatus.KEY);

				if(info != null && info.getStatus() == Status.POSTCONDITION_ERROR) {
					dump(operations);
					if(EXIT_ON_BUG) System.exit(1);
				}
			}
		} catch(Exception e) {
			System.out.println("error:" + e);
		}
	}

	private void dump(Operation[] operations) {
		for(Operation op : operations) {
			System.out.println(op);
			OperationStatus info = (OperationStatus) op.getInfo(OperationStatus.KEY);
			if(info != null) System.out.println("  " + info);
		}
	}
}
