package testful.model;

import java.util.ArrayList;
import java.util.List;

import testful.GenericTestCase;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.utils.Utils;

/**
 * @author matteo
 *
 */
public class TestSplitter_splitAndMinimizeTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		TrackerDatum[] data = Utils.readData(AnalysisWhiteBox.read(config.getDirInstrumented(), test.getCluster().getCut().getClassName()));

		Test min = TestSplitter.splitAndMinimize(test, GenericTestCase.getFinder(), data);

		List<Test> res = new ArrayList<Test>(1);
		res.add(min);
		return res;
	}
}
