package testful.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author matteo
 *
 */
public class Test_simplifyTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		List<Test> ret = new ArrayList<Test>(1);
		ret.add(test.simplify());
		return ret;
	}
}
