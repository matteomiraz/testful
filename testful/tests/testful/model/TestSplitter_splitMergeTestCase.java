package testful.model;

import java.util.ArrayList;
import java.util.List;

import ec.util.MersenneTwisterFast;

/**
 * @author matteo
 *
 */
public class TestSplitter_splitMergeTestCase extends AutoTestCase {

	public void testFrac() throws Exception {
		MersenneTwisterFast r = new MersenneTwisterFast(17);
		for(int n = 1; n < 1000; n++) {
			System.out.printf("%5.1f%% ", n/10.0);
			autoTest("apache.Fraction", 200, r.nextLong());
		}

	}

	@Override
	protected List<Test> perform(Test test) throws Exception {
		List<Test> ret = new ArrayList<Test>(1);
		ret.add(TestSplitter.splitAndMerge(test));
		return ret;
	}
}
