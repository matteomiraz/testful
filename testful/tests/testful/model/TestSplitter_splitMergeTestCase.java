package testful.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author matteo
 *
 */
public class TestSplitter_splitMergeTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		List<Test> ret = new ArrayList<Test>(1);
		ret.add(TestSplitter.splitAndMerge(test));
		return ret;
	}
}
