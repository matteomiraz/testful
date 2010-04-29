package testful.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import testful.coverage.CoverageInformation;
import testful.coverage.TestSizeInformation;
import testful.utils.ElementManager;

public class OptimalTestCreator {

	private static final Logger logger = Logger.getLogger("testful.regression.OptimalTestCreator");

	/**
	 * stores the combined coverage obtained so far.
	 * Key: coverage criteria;
	 * value: combined coverage criteria
	 */
	private final ElementManager<String, CoverageInformation> combinedCoverage;

	/**
	 * stores the optimal solution found so far.
	 * Key: coverage criteria;
	 * value: solutions
	 */
	private final Set<TestCoverage> optimal;

	public OptimalTestCreator() {
		combinedCoverage = new ElementManager<String, CoverageInformation>();
		optimal = new HashSet<TestCoverage>();
	}

	/**
	 * Updates optimal solutions and combined coverage
	 * @param test the test with coverage
	 */
	public void update(TestCoverage test) {

		/** the test cover something more than tests found so far */
		boolean innovative = false;

		// update the combined coverage & check if the test is innovative
		for(CoverageInformation coverage : test.getCoverage()) {

			// skip the "Test Size": it is not a coverage information
			if(coverage instanceof TestSizeInformation) continue;

			// get or create the combined coverage
			CoverageInformation combined = combinedCoverage.get(coverage.getKey());
			if(combined == null) {
				combined = coverage.createEmpty();
				combinedCoverage.put(combined);
			}

			// update the combined coverage
			if(!innovative) {
				if(!combined.contains(coverage)) {
					combined.merge(coverage);
					innovative = true;
				}
			} else {
				combined.merge(coverage);
			}
		}

		// check if it is possible to remove some tests
		Set<TestCoverage> toRemove = new HashSet<TestCoverage>();
		for(TestCoverage other : optimal) {
			if(performsBetter(test, other))
				toRemove.add(other);
		}

		// if the test is not innovative, it should be shorter than to-be-removed tests
		if(!innovative) {
			int tot = 0;
			for(TestCoverage t : toRemove)
				tot += t.getTest().length;

			if(test.getTest().length < tot) toRemove.clear();
		}


		// if the test is innovative
		//    or if it is more efficient than the others
		if(innovative || !toRemove.isEmpty()) {
			for(TestCoverage t : toRemove)
				optimal.remove(t);

			optimal.add(test);
		}
	}

	/**
	 * Checks if the test can replace the other test
	 * (i.e., the other test does not cover any element not covered by the given test).
	 * @param test the reference test
	 * @param other the test that we want to remove
	 * @return true if the other test can be removed
	 */
	private boolean performsBetter(TestCoverage test, TestCoverage other) {
		for (CoverageInformation otherCov : other.getCoverage()) {
			if(otherCov instanceof TestSizeInformation) continue;

			CoverageInformation thisCov = test.getCoverage().get(otherCov.getKey());
			if(thisCov == null || !thisCov.contains(otherCov)) return false;
		}

		return true;
	}

	public Collection<TestCoverage> get() {
		return optimal;
	}

	public ElementManager<String, CoverageInformation> getCoverage() {
		return combinedCoverage;
	}

	public void log(Integer currentGeneration, long totInvocation, long time) {
		StringBuilder sb = new StringBuilder("combinedCoverage ");

		sb.append("inv=").append(totInvocation);

		sb.append(";").append("time=").append(time);

		if(currentGeneration != null) sb.append(";").append("gen=").append(currentGeneration);

		sb.append(";").append("tests-num").append("=").append(optimal.size());

		int tot = 0;
		for(Test t : optimal)
			tot += t.getTest().length;
		sb.append(";").append("tests-length").append("=").append(tot);

		for (CoverageInformation cov : combinedCoverage)
			sb.append(";").append(cov.getKey()).append("=").append(cov.getQuality());

		logger.finer(sb.toString());
	}
}
