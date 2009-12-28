package testful.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import testful.Configuration;
import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.coverage.RunnerCaching;
import testful.coverage.TestSizeInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.bug.BugCoverage;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageConditions;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.IRunner;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;
import testful.utils.TestfulLogger;
import testful.utils.Utils;
import testful.utils.TestfulLogger.CombinedCoverageWriter;
import testful.utils.TestfulLogger.CoverageWriter;

public class TestfulProblem implements Serializable {

	private static final long serialVersionUID = 6895567012306544198L;

	public static class TestfulConfig extends Configuration {
		public final Fitness fitness = new Fitness();
		public final Cluster cluster = new Cluster();

		public TestfulConfig() {
			super();
		}

		public TestfulConfig(String baseDir) {
			super(baseDir);
		}

		public static class Fitness {
			public boolean toMinimize;
			public boolean len;
			public boolean bug;
			public boolean bbd;
			public boolean bbn;
			public boolean brd;
			public boolean brn;
		}

		public static class Cluster {
			private String[] aux;
			private int repoSize;
			private int repoCutSize;

			public void setAux(String[] aux) {
				this.aux = aux;
			}

			public void setRepoSize(int repoSize) {
				this.repoSize = repoSize;
			}

			public void setRepoCutSize(int repoCutSize) {
				this.repoCutSize = repoCutSize;
			}

			public String[] getAux() {
				return aux;
			}

			public int getRepoSize() {
				return repoSize;
			}

			public int getRepoCutSize() {
				return repoCutSize;
			}
		}
	}

	private final int numObjs;

	private TestfulConfig config;

	private TestCluster cluster;
	private ReferenceFactory refFactory;

	private final AnalysisWhiteBox whiteAnalysis;

	private final RunnerCaching runnerCaching;

	/** cumulative number of invocations */
	private AtomicLong invTot = new AtomicLong(0);
	private final ClassFinderCaching finder;
	private final TrackerDatum[] data;
	private final boolean reloadClasses;

	private int generationNumber;

	private static CoverageWriter coverageWriter;
	private static CombinedCoverageWriter combinedWriter;
	private static OptimalTestCreator optimal = new OptimalTestCreator();
	private static Tracker[] trackers = new Tracker[] {
		//		new Tracker(BehaviorCoverage.KEY),
		//		new Tracker(BranchCoverage.KEY + "d"),
		//		new Tracker(DefUseCoverage.KEY + "d")
	};

	public TestfulProblem(IRunner runner, boolean enableCache, boolean reloadClasses, TestfulConfig config) throws TestfulException {
		this.config = config;
		runnerCaching = new RunnerCaching(runner, enableCache);
		this.reloadClasses = reloadClasses;

		try {
			finder = new ClassFinderCaching(new ClassFinderImpl(new File(config.getDirInstrumented()), new File(config.getDirJml()), new File(config.getDirVanilla())));
			TestfulClassLoader tcl = new TestfulClassLoader(finder);
			cluster = new TestCluster(tcl, config);
			cluster.clearCache();

			whiteAnalysis = AnalysisWhiteBox.read(config.getDirInstrumented(), config.getCut());
			data = Utils.readData(whiteAnalysis);

		} catch(ClassNotFoundException e) {
			throw new TestfulException("Class not found: " + e.toString());
		} catch(RemoteException e) {
			// never happens
			throw new TestfulException(e);
		}

		numObjs = (config.fitness.len ? 1 : 0) +
		(config.fitness.bug ? 1 : 0) +
		(config.fitness.bbd ? 1 : 0) + (config.fitness.bbd ? 1 : 0) +
		(config.fitness.brd ? 1 : 0) + (config.fitness.brn ? 1 : 0);

		refFactory = new ReferenceFactory(cluster, config.cluster.repoCutSize, config.cluster.repoSize);

		try {
			coverageWriter = TestfulLogger.singleton.getCoverageWriter("coverage.csv");
			combinedWriter = TestfulLogger.singleton.getCombinedCoverageWriter("combined.csv");
		} catch(FileNotFoundException e) {
			e.printStackTrace();
			throw new TestfulException("Cannot write stats to files!");
		}
	}

	public TestCluster getCluster() {
		return cluster;
	}

	public ReferenceFactory getRefFactory() {
		return refFactory;
	}

	public int getNumObjs() {
		return numObjs;
	}

	public RunnerCaching getRunnerCaching() {
		return runnerCaching;
	}

	public AnalysisWhiteBox getWhiteAnalysis() {
		return whiteAnalysis;
	}

	public void doneGeneration(int num) {
		generationNumber = num;

		runnerCaching.updateCacheScore();

		optimal.write();
		combinedWriter.write(num, invTot.get(), optimal.getCoverage(), optimal.getOptimal());
		for(Tracker tracker : trackers)
			tracker.write();
	}

	public int getGenerationNumber() {
		return generationNumber;
	}

	public float[] evaluate(long gen, List<Operation> ops, ElementManager<String, CoverageInformation> infos) {
		float[] ret = new float[numObjs];

		float covTot = 1;
		if(infos != null) {
			int i = config.fitness.len ? 1 : 0;

			if(config.fitness.bug) {
				CoverageInformation cov = infos.get(BugCoverage.KEY);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.fitness.toMinimize ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.fitness.bbd) {
				CoverageInformation cov = infos.get(CoverageBasicBlocks.KEY_CODE);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.fitness.toMinimize ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.fitness.bbn) {
				CoverageInformation cov = infos.get(CoverageBasicBlocks.KEY_CONTRACT);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.fitness.toMinimize ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.fitness.brd) {
				CoverageInformation cov = infos.get(CoverageConditions.KEY_CODE);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.fitness.toMinimize ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.fitness.brn) {
				CoverageInformation cov = infos.get(CoverageConditions.KEY_CONTRACT);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.fitness.toMinimize ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.fitness.len) {
				TestSizeInformation cov = (TestSizeInformation) infos.get(TestSizeInformation.KEY);
				if(cov != null) {
					cov.setOtherCovs(covTot);
					ret[0] = config.fitness.toMinimize ? -1.0f * cov.getQuality() : cov.getQuality();
				}
			}

			coverageWriter.write(gen, ops.size(), infos);
			TestCoverage testCoverage = new TestCoverage(cluster, refFactory, ops.toArray(new Operation[ops.size()]), infos);
			optimal.update(testCoverage);
			for(Tracker tracker : trackers)
				tracker.update(testCoverage);
		} else System.err.println("null infos");

		return ret;
	}

	public Future<ElementManager<String, CoverageInformation>> evaluate(List<Operation> ops) throws TestfulException {
		invTot.addAndGet(ops.size());

		try {
			Test test = createTest(ops);
			return runnerCaching.executeParts(finder, reloadClasses, test, data);
		} catch(Exception e) {
			throw new TestfulException(e);
		}
	}

	public Future<ElementManager<String, CoverageInformation>> evaluate(List<Operation> ops, TrackerDatum[] data) throws TestfulException {
		try {
			return runnerCaching.execute(finder, reloadClasses, createTest(ops), data);
		} catch(Exception e) {
			throw new TestfulException(e);
		}
	}

	public Future<ElementManager<String, CoverageInformation>> evaluate(Test test) throws TestfulException {
		try {
			return runnerCaching.executeParts(finder, reloadClasses, test, data);
		} catch(Exception e) {
			throw new TestfulException(e);
		}
	}

	public Collection<TestCoverage> evaluate(Collection<Test> tests) throws InterruptedException {
		Map<Test, Future<ElementManager<String, CoverageInformation>>> futures = new LinkedHashMap<Test, Future<ElementManager<String,CoverageInformation>>>();
		for(Test t : tests)
			futures.put(t, runnerCaching.executeParts(finder, reloadClasses, t, data));

		Collection<TestCoverage> ret = new ArrayList<TestCoverage>();
		for (Entry<Test, Future<ElementManager<String, CoverageInformation>>> f : futures.entrySet()) {
			try {
				ret.add(new TestCoverage(f.getKey(), f.getValue().get()));
			} catch (ExecutionException e) {
				System.err.println("Error valuating test: " + e);
			}
		}

		return ret;
	}


	public Test createTest(List<Operation> ops) {
		return new Test(cluster, refFactory, ops.toArray(new Operation[ops.size()]));
	}
}
