package testful.model;

import java.util.ArrayList;
import java.util.List;

import testful.ConfigCut;
import testful.GenericTestCase;
import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.runner.TestfulClassLoader;

/**
 * @author matteo
 *
 */
public class TestSimpifierTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		IRunner exec = RunnerPool.createExecutor("test", configRunner);

		TestSimplifier s = new TestSimplifier(exec, getFinder());

		List<Test> res = new ArrayList<Test>(1);
		res.add(s.analyze(test));
		return res;
	}

	public void testSimple1() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.Simple");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 4, 4);

		Clazz cut = cluster.getCut();

		Reference c0 = refFactory.getReferences(cut)[0];

		Constructorz cns = cut.getConstructors()[0];

		Methodz mInc = null;
		for(Methodz m : cut.getMethods()) {
			if("mInc".equals(m.getName())) mInc = m;
		}


		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, mInc, new Reference[] { })
		});

		Operation[][] expected = {
				{
					new CreateObject(c0, cns, new Reference[] { }),
					new Invoke(null, c0, mInc, new Reference[] { })
				}
		};

		check(test, expected);
	}
}
