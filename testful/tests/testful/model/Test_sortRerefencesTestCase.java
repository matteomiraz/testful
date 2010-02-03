package testful.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author matteo
 *
 */
public class Test_sortRerefencesTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		List<Test> ret = new ArrayList<Test>(2);
		ret.add(test.sortReferences());
		return ret;
	}
}
