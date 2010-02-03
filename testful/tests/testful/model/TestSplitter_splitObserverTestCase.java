package testful.model;

import java.util.List;

/**
 * @author matteo
 *
 */
public class TestSplitter_splitObserverTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		return TestSplitter.split(true, test);
	}
}
