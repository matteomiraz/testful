package testful.model;

import testful.coverage.CoverageInformation;
import testful.utils.ElementManager;

public class TestCoverage extends Test {

	private static final long serialVersionUID = 1420569092643662448L;
	
	/** the coverage */
	private final ElementManager<String, CoverageInformation> coverage;

	public TestCoverage(TestCluster cluster, ReferenceFactory refFactory, Operation[] test, ElementManager<String, CoverageInformation> coverage) {
		super(cluster, refFactory, test);
		this.coverage = coverage;
	}

	public TestCoverage(Test test, ElementManager<String, CoverageInformation> coverage) {
		super(test.getCluster(), test.getReferenceFactory(), test.getTest());
		this.coverage = coverage;
	}

	public ElementManager<String, CoverageInformation> getCoverage() {
		return coverage;
	}
}
