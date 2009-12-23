package testful.model;

import java.util.List;

import testful.model.TestfulProblem.TestfulConfig;
import testful.runner.TestfulClassLoader;

/**
 * @author matteo
 *
 */
public class TestSplitter_splitObserverTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		return TestSplitter.split(true, test);
	}
	
	public void testHardStatemachine() throws Exception {
		TestfulConfig config = new TestfulConfig();
		config.setCut("dummy.WhiteSample");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 1, 2);

		Clazz cut = cluster.getCut();

		Clazz cClazz = null;
		Clazz[] cl = cluster.getCluster();
		for(Clazz clazz : cl) {
			if("java.lang.Character".equals(clazz.getClassName())) {
				cClazz = clazz;
				break;
			}
		}

		Reference c0 = refFactory.getReferences(cClazz)[0];
		Reference c1 = refFactory.getReferences(cClazz)[1];
		Reference s0 = refFactory.getReferences(cut)[0];

		Methodz getState = null;
		Methodz nextCharV = null;
		Methodz nextCharC = null;
		for(Methodz m : cut.getMethods()) {
			if("getState".equals(m.getName())) getState = m;
			if("nextChar".equals(m.getName())) {
				if(m.getParameterTypes().length == 0) nextCharV = m;
				else nextCharC = m;
			}
		}
		
		Constructorz cns = cut.getConstructors()[0];

		Operation[] ops = new Operation[] {
				new CreateObject(s0, cns, new Reference[] { }),
				
				new Invoke(null, s0, getState, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),
				new Invoke(null, s0, getState, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				
				new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
				
				new Invoke(null, s0, getState, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),
				new Invoke(null, s0, getState, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),

				new Invoke(null, s0, nextCharC, new Reference[] { c1 }),

		};

		Test t = new Test(cluster, refFactory, ops);
		List<Test> tests = TestSplitter.split(true, t);
		
		System.out.println("original:");
		System.out.println(t);
		System.out.println("---");
		
		System.out.println("Modified: " + tests.size());
		for(Test t1 : tests) {
			System.out.println(t1);
			System.out.println("---");
		}
	}
}
