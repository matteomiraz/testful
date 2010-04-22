package testful.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author matteo
 *
 */
public class Test_reorganizerSortSplitTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		List<Test> tmp = TestSplitter.split(true, test);
		List<Test> ret = new ArrayList<Test>(tmp.size());
		for(Test t : tmp)
			ret.add(t.reorganize().sortReferences());
		
		return ret;
	}
}
