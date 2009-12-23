package testful.model;

import java.util.ArrayList;
import java.util.List;

import testful.GenericTestCase;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.utils.Utils;

/**
 * @author matteo
 *
 */
public class TestSplitter_splitAndMinimizeTestCase extends AutoTestCase {

	public void testErr() throws Exception {
		autoTest("statemachine.SimpleImpl", 952, -5704615417085461497l);
	}

	@Override
	protected List<Test> perform(Test test) throws Exception {
		IRunner exec = RunnerPool.createExecutor(null, false);
		
	TrackerDatum[] data = Utils.readData(AnalysisWhiteBox.read("cut/instrumented", test.getCluster().getCut().getClassName()));
		
		Test min = TestSplitter.splitAndMinimize(test, GenericTestCase.getFinder(), exec, data);

		List<Test> res = new ArrayList<Test>(1);
		res.add(min);
		return res;
	}
}
