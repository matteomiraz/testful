package testful.regression;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import testful.ConfigProject;
import testful.ConfigRunner;
import testful.IConfigProject;
import testful.TestFul;
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

	private static class Config extends ConfigProject implements IConfigProject.Args4j {

		@Option(required = false, name = "-exitOnBug", usage = "Exit when a bug is found")
		public boolean exitOnBug;

		@Argument
		private List<String> arguments = new ArrayList<String>();
	}

	private final boolean exitOnBug;

	private IRunner executor;
	private ClassFinder finder;

	public static void main(String[] args) {
		TestFul.printHeader("Regression Testing");

		Config config = new Config();
		TestFul.parseCommandLine(config, args, Replay.class);

		Replay replay = new Replay(config, config.exitOnBug);
		replay.read(config.arguments);


		System.exit(0);
	}

	public Replay(IConfigProject config, boolean exitOnBug) {
		this.exitOnBug = exitOnBug;

		executor = RunnerPool.createExecutor("Replay", new ConfigRunner());
		try {
			finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));
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
					if(exitOnBug) System.exit(1);
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
