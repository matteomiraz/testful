package testful.regression;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

import testful.ConfigProject;
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

/**
 * Executes one or more tests on a given class.
 * @author matteo
 */
public class Replay extends TestReader {

	private static final Logger logger = Logger.getLogger("testful.regression");

	private static class Config extends ConfigProject implements IConfigProject.Args4j {

		@Option(required = false, name = "-exitOnBug", usage = "Exit when a bug is found")
		public boolean exitOnBug;

		@Argument
		private List<String> tests = new ArrayList<String>();
	}

	private final boolean exitOnBug;

	private IRunner executor;
	private ClassFinder finder;

	public static void main(String[] args) {

		Config config = new Config();
		TestFul.parseCommandLine(config, args, Replay.class, "Regression Testing");

		if(config.isQuiet())
			TestFul.printHeader("Regression Testing");

		TestFul.setupLogging(config);

		RunnerPool.getRunnerPool().startLocalWorkers();

		Replay replay = new Replay(config, config.exitOnBug);
		replay.read(config.tests);


		System.exit(0);
	}

	public Replay(IConfigProject config, boolean exitOnBug) {
		this.exitOnBug = exitOnBug;

		executor = RunnerPool.getRunnerPool();

		try {
			finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));
		} catch(RemoteException e) {
			// never happens!
		}
	}

	@Override
	protected void read(String fileName, Test t) {
		try {

			logger.info("Replaying " + fileName);
			Test test = new Test(t.getCluster(), t.getReferenceFactory(), t.getTest());
			Future<Operation[]> future = executor.execute(TestExecutionManager.getContext(finder, test));
			Operation[] operations = future.get();

			for(Operation op : operations) {
				OperationStatus info = (OperationStatus) op.getInfo(OperationStatus.KEY);

				if(info != null && info.getStatus() == Status.POSTCONDITION_ERROR) {
					dump(operations);
					if(exitOnBug) System.exit(1);
				}
			}
		} catch(Exception e) {
			logger.log(Level.WARNING, "Cannot execute the test " + fileName + ": " + e.getMessage(), e);
		}
	}

	private void dump(Operation[] operations) {
		StringBuilder sb = new StringBuilder();

		sb.append("I found an error:\n");

		for(Operation op : operations) {
			sb.append(op).append("\n");
			OperationStatus info = (OperationStatus) op.getInfo(OperationStatus.KEY);
			if(info != null) sb.append("  ").append(info);
		}

		logger.info(sb.toString());
	}

	@Override
	public Logger getLogger() {
		return logger;
	}
}
