package testful.random;

import java.io.File;
import java.util.concurrent.Future;
import java.util.logging.Level;

import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestSplitter;
import testful.model.TestSplitter.Listener;
import testful.runner.ClassFinder;
import testful.utils.ElementManager;
import testful.utils.SimpleEntry;

public class RandomTestSplit extends RandomTest {

	private final TestSplitter simplifier;

	public RandomTestSplit(boolean enableCache, File logDirectory, ClassFinder finder, TestCluster cluster, ReferenceFactory refFactory, TrackerDatum ... data) {
		super(enableCache, logDirectory, finder, cluster, refFactory, data);

		simplifier = new TestSplitter(true, cluster, refFactory);
	}

	@Override
	public void work(long duration) {
		start = System.currentTimeMillis();
		stop = start + duration;

		simplifier.register(new Listener() {

			@Override
			public void notify(TestCluster cluster, ReferenceFactory refFactory, Operation[] ops) {
				try {
					Future<ElementManager<String, CoverageInformation>> fut = runner.execute(finder, true, new Test(cluster, refFactory, ops), data);
					tests.put(new SimpleEntry<Operation[], Future<ElementManager<String, CoverageInformation>>>(ops, fut));
				} catch(InterruptedException e) {
					logger.log(Level.WARNING, "Interrupted: " + e.getMessage(), e);
				}
			}
		});

		while(System.currentTimeMillis() < stop) {
			Operation op = Operation.randomlyGenerate(cluster, refFactory, random);
			simplifier.analyze(op);
		}

		simplifier.flush();
	}
}
