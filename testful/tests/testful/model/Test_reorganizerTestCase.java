package testful.model;

import java.util.ArrayList;
import java.util.List;

import testful.model.TestfulProblem.TestfulConfig;
import testful.runner.TestfulClassLoader;

/**
 * @author matteo
 *
 */
public class Test_reorganizerTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		List<Test> ret = new ArrayList<Test>(2);
		ret.add(test.reorganize());
		return ret;
	}
	
	public void testHardStatemachine1() throws Exception {
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

		Methodz nextCharV = null;
		Methodz nextCharC = null;
		for(Methodz m : cut.getMethods()) {
			if("nextChar".equals(m.getName())) {
				if(m.getParameterTypes().length == 0) nextCharV = m;
				else nextCharC = m;
			}
		}
		
		Constructorz cns = cut.getConstructors()[0];

		//statemachine_Hard_1 = new statemachine.Hard();
	  //java_lang_Character_2 = (char) statemachine_Hard_1.nextChar();
	  //java_lang_Character_2 = (char)((int) 44 /* , */ );
	  //java_lang_Character_0 = (char) statemachine_Hard_1.nextChar();
	  //statemachine_Hard_1.nextChar(java_lang_Character_0);
		Operation[] ops = new Operation[] {
				new CreateObject(s0, cns, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new AssignPrimitive(c1, 'w'),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),
				new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
		};

		Test t = new Test(cluster, refFactory, ops);

		Operation[][] expected = {
				{
					new CreateObject(s0, cns, new Reference[] { }),
					new Invoke(c0, s0, nextCharV, new Reference[] { }),
					new Invoke(c1, s0, nextCharV, new Reference[] { }),
					new AssignPrimitive(c1, 'w'),
					new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
				}
		};

		check(t, expected);
	}

}
