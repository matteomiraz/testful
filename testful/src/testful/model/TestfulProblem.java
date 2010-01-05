package testful.model;

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

import jmetal.util.PseudoRandom;
import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.coverage.RunnerCaching;
import testful.coverage.TestSizeInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.bug.BugCoverage;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageConditions;
import testful.evolutionary.IConfigEvolutionary;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;
import testful.utils.TestfulLogger;
import testful.utils.Utils;
import testful.utils.TestfulLogger.CombinedCoverageWriter;
import testful.utils.TestfulLogger.CoverageWriter;

public class TestfulProblem implements Serializable {

	private static final long serialVersionUID = 6895567012306544198L;

	private final int numObjs;

	private IConfigEvolutionary config;

	private final TestCluster cluster;
	private final ReferenceFactory refFactory;

	private final AnalysisWhiteBox whiteAnalysis;

	private final RunnerCaching runnerCaching;

	/** cumulative number of invocations */
	private AtomicLong invTot = new AtomicLong(0);
	private final ClassFinderCaching finder;
	private final TrackerDatum[] data;

	private int generationNumber;

	private static CoverageWriter coverageWriter;
	private static CombinedCoverageWriter combinedWriter;
	private static OptimalTestCreator optimal = new OptimalTestCreator();
	private static Tracker[] trackers = new Tracker[] {
		//		new Tracker(BehaviorCoverage.KEY),
		//		new Tracker(BranchCoverage.KEY + "d"),
		//		new Tracker(DefUseCoverage.KEY + "d")
	};

	public TestfulProblem(IConfigEvolutionary config) throws TestfulException {
		this.config = config;
		runnerCaching = new RunnerCaching(config.isCache());

		try {
			finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));
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

		numObjs = (config.isLength() ? 1 : 0) +
		(config.isBug() ? 1 : 0) +
		(config.isBbd() ? 1 : 0) + (config.isBbn() ? 1 : 0) +
		(config.isBrd() ? 1 : 0) + (config.isBrn() ? 1 : 0);

		refFactory = new ReferenceFactory(cluster, config.getNumVarCut(), config.getNumVar());

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

	public ClassFinderCaching getFinder() {
		return finder;
	}

	public int getNumObjs() {
		return numObjs;
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

		if(runnerCaching.isEnabled())
			System.out.println(runnerCaching.toString());
	}

	public int getGenerationNumber() {
		return generationNumber;
	}

	public float[] evaluate(long gen, List<Operation> ops, ElementManager<String, CoverageInformation> infos) {
		float[] ret = new float[numObjs];

		float covTot = 1;
		if(infos != null) {
			int i = config.isLength() ? 1 : 0;

			if(config.isBug()) {
				CoverageInformation cov = infos.get(BugCoverage.KEY);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.isToMinimize() ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.isBbd()) {
				CoverageInformation cov = infos.get(CoverageBasicBlocks.KEY_CODE);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.isToMinimize() ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.isBbn()) {
				CoverageInformation cov = infos.get(CoverageBasicBlocks.KEY_CONTRACT);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.isToMinimize() ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.isBrd()) {
				CoverageInformation cov = infos.get(CoverageConditions.KEY_CODE);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.isToMinimize() ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.isBrn()) {
				CoverageInformation cov = infos.get(CoverageConditions.KEY_CONTRACT);
				if(cov != null) {
					covTot += cov.getQuality();
					ret[i] = config.isToMinimize() ? -1 * cov.getQuality() : cov.getQuality();
				}
				i++;
			}

			if(config.isLength()) {
				TestSizeInformation cov = (TestSizeInformation) infos.get(TestSizeInformation.KEY);
				if(cov != null) {
					cov.setOtherCovs(covTot);
					ret[0] = config.isToMinimize() ? -1.0f * cov.getQuality() : cov.getQuality();
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
			return runnerCaching.executeParts(finder, config.isReload(), test, data);
		} catch(Exception e) {
			throw new TestfulException(e);
		}
	}

	public Future<ElementManager<String, CoverageInformation>> evaluate(List<Operation> ops, TrackerDatum[] data) throws TestfulException {
		try {
			return runnerCaching.execute(finder, config.isReload(), createTest(ops), data);
		} catch(Exception e) {
			throw new TestfulException(e);
		}
	}

	public Future<ElementManager<String, CoverageInformation>> evaluate(Test test) throws TestfulException {
		try {
			return runnerCaching.executeParts(finder, config.isReload(), test, data);
		} catch(Exception e) {
			throw new TestfulException(e);
		}
	}

	public Collection<TestCoverage> evaluate(Collection<Test> tests) throws InterruptedException {
		Map<Test, Future<ElementManager<String, CoverageInformation>>> futures = new LinkedHashMap<Test, Future<ElementManager<String,CoverageInformation>>>();
		for(Test t : tests)
			futures.put(t, runnerCaching.executeParts(finder, config.isReload(), t, data));

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

	private final TestSuite reserve = new TestSuite();

	/**
	 * Add tests to the reserve
	 * @param tests the tests to add
	 */
	public void addReserve(TestSuite tests){
		reserve.add(tests);
	}

	/**
	 * Add a test to the reserve
	 * @param test the test to add
	 */
	public void addReserve(TestCoverage test){
		reserve.add(test);
	}

	public List<Operation> generateTest() {
		final int size = 10;
		List<Operation> ret;

		Operation[] ops = reserve.getBestTest();
		if (ops!=null) {
			ops = Operation.adapt(ops, cluster, refFactory);
			ret = new ArrayList<Operation>(ops.length);
			for (Operation o : ops) ret.add(o);
			return ret;
		}

		ret = new ArrayList<Operation>(size);

		for (int i = 0; i < size; i++)
			ret.add(Operation.randomlyGenerate(cluster, refFactory, PseudoRandom.getMersenneTwisterFast()));

		return ret;
	}

}
