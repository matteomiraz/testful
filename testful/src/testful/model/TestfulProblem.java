package testful.model;

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
import java.util.logging.Level;
import java.util.logging.Logger;

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
import testful.utils.CoverageWriter;
import testful.utils.ElementManager;
import testful.utils.Utils;

public class TestfulProblem implements Serializable {

	private static final Logger logger = Logger.getLogger("testful.evolutionary");

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

	private final CoverageWriter coverageWriter;
	private final OptimalTestCreator optimal;
	private final Tracker[] trackers = null;
	// new Tracker[] {
	//		new Tracker(BehaviorCoverage.KEY),
	//		new Tracker(BranchCoverage.KEY + "d"),
	//		new Tracker(DefUseCoverage.KEY + "d")
	// };

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

			refFactory = new ReferenceFactory(cluster, config.getNumVarCut(), config.getNumVar());

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

		if(logger.isLoggable(Level.FINER))
			optimal = new OptimalTestCreator();
		else
			optimal = null;

		if(logger.isLoggable(Level.FINE))
			coverageWriter = new CoverageWriter("testful.evolutionary.coverage");
		else
			coverageWriter = null;
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

	public void doneGeneration(int num, long time) {
		generationNumber = num;

		runnerCaching.updateCacheScore();

		if(optimal != null)
			optimal.log(num, invTot.get(), time);

		if (trackers != null) {
			for (Tracker tracker : trackers)
				tracker.write();
		}

		if(runnerCaching.isEnabled())
			logger.fine(runnerCaching.toString());
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

			if(coverageWriter != null)
				coverageWriter.write(gen, ops.size(), infos);

			if(optimal != null || trackers != null) {
				TestCoverage testCoverage = new TestCoverage(cluster, refFactory, ops.toArray(new Operation[ops.size()]), infos);

				if(optimal != null)
					optimal.update(testCoverage);

				if(trackers != null)
					for(Tracker tracker : trackers)
						tracker.update(testCoverage);

			}

		} else {
			logger.warning("Cannot retrieve test's coverage information");
		}

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
				logger.log(Level.WARNING, "Error valuating test: " + e.getMessage(), e);
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

	public TrackerDatum[] getData() {
		return data;
	}
}
