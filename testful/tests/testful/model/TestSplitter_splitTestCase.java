package testful.model;

import java.util.List;

import testful.model.TestfulProblem.TestfulConfig;
import testful.runner.TestfulClassLoader;

/**
 * @author matteo
 *
 */
public class TestSplitter_splitTestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		return TestSplitter.split(false, test);
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
				new Invoke(null, s0, nextCharV, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new Invoke(null, s0, getState, new Reference[] { }),
				
				new Invoke(null, s0, nextCharC, new Reference[] { c0 }),

				new Invoke(null, s0, getState, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new Invoke(null, s0, nextCharV, new Reference[] { }),
				new Invoke(null, s0, getState, new Reference[] { }),

				new Invoke(null, s0, nextCharC, new Reference[] { c0 }),

				new Invoke(null, s0, getState, new Reference[] { }),
				new Invoke(null, s0, nextCharV, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new Invoke(null, s0, getState, new Reference[] { }),

				new Invoke(null, s0, nextCharC, new Reference[] { c1 }),
				
				new Invoke(null, s0, getState, new Reference[] { }),
				new Invoke(null, s0, nextCharV, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new AssignPrimitive(c0, 'w'),
				new Invoke(null, s0, getState, new Reference[] { }),
				
				new Invoke(null, s0, nextCharC, new Reference[] { c1 }),

		};

		Test t = new Test(cluster, refFactory, ops);

		Operation[][] expected = {
				{
					new CreateObject(s0, cns, new Reference[] { }),
					
					new Invoke(null, s0, getState, new Reference[] { }),
					new Invoke(c0, s0, nextCharV, new Reference[] { }),
					
					new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
	
					new Invoke(null, s0, getState, new Reference[] { }),
					new Invoke(c0, s0, nextCharV, new Reference[] { }),
	
					new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
	
					new Invoke(null, s0, getState, new Reference[] { }),
					new Invoke(c0, s0, nextCharV, new Reference[] { }),
	
					new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
	
					new Invoke(null, s0, getState, new Reference[] { }),
					new Invoke(c1, s0, nextCharV, new Reference[] { }),
					
					new Invoke(null, s0, nextCharC, new Reference[] { c1 }),
				}
		};

		check(t, expected);
	}
	
	public void testHardStatemachine2() throws Exception {
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
					new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
				}
		};

		check(t, expected);
	}
	
	public void testHardStatemachine3() throws Exception {
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

		//  statemachine_Hard_0 = new statemachine.Hard();
		//  java_lang_Character_1 = (char) statemachine_Hard_0.nextChar();
		//  statemachine_Hard_0.nextChar(java_lang_Character_1);
		
		//  java_lang_Character_0 = (char)((int) 91 /* [ */ );
		//  statemachine_Hard_0 = new statemachine.Hard();
		//  statemachine_Hard_0.nextChar(java_lang_Character_0);
		
		//  java_lang_Character_0 = (char) statemachine_Hard_0.nextChar();
		//  statemachine_Hard_0 = new statemachine.Hard();
		//  java_lang_Character_1 = (char) statemachine_Hard_0.nextChar();
		//  java_lang_Character_0 = (char) statemachine_Hard_0.nextChar();
		Operation[] ops = new Operation[] {
				new CreateObject(s0, cns, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new Invoke(null, s0, nextCharC, new Reference[] { c1 }),

				new AssignPrimitive(c0, '['),
				new CreateObject(s0, cns, new Reference[] { }),
				new Invoke(null, s0, nextCharC, new Reference[] { c0 }),

				new Invoke(c0, s0, nextCharV, new Reference[] { }),
				new CreateObject(s0, cns, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),
		};

		Test t = new Test(cluster, refFactory, ops);

		Operation[][] expected = {
				{
						new CreateObject(s0, cns, new Reference[] { }),
						new Invoke(c1, s0, nextCharV, new Reference[] { }),
						new Invoke(null, s0, nextCharC, new Reference[] { c1 }),
				},
				{
						new AssignPrimitive(c0, '['),
						new CreateObject(s0, cns, new Reference[] { }),
						new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
						new Invoke(c0, s0, nextCharV, new Reference[] { }),
				}
		};

		check(t, expected);
	}

	public void testHardStatemachine4() throws Exception {
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

		//  statemachine_Hard_3 = new statemachine.Hard();
		//  java_lang_Character_1 = (char) statemachine_Hard_3.nextChar();
		//  statemachine_Hard_3.nextChar(java_lang_Character_1);

		//  java_lang_Character_0 = (char)((int) 91 /* [ */ );
		//  statemachine_Hard_3 = new statemachine.Hard();
		//  statemachine_Hard_3.nextChar(java_lang_Character_0);
		//  java_lang_Character_1 = (char) statemachine_Hard_3.nextChar();
		//  java_lang_Character_0 = (char) statemachine_Hard_3.nextChar();
		
		//  java_lang_Character_1 = (char)((int) 30006 /* 甶 */ );
		//  statemachine_Hard_3 = new statemachine.Hard();
		//  java_lang_Character_0 = (char) statemachine_Hard_3.nextChar();

		Operation[] ops = new Operation[] {
				new CreateObject(s0, cns, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new Invoke(null, s0, nextCharC, new Reference[] { c1 }),

				new AssignPrimitive(c0, '['),
				new CreateObject(s0, cns, new Reference[] { }),
				new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),

				new AssignPrimitive(c1, (char) 3006),
				new CreateObject(s0, cns, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),
		};

		Test t = new Test(cluster, refFactory, ops);

		Operation[][] expected = {
				{
						new CreateObject(s0, cns, new Reference[] { }),
						new Invoke(c1, s0, nextCharV, new Reference[] { }),
						new Invoke(null, s0, nextCharC, new Reference[] { c1 }),
				},
				{
						new AssignPrimitive(c0, '['),
						new CreateObject(s0, cns, new Reference[] { }),
						new Invoke(null, s0, nextCharC, new Reference[] { c0 }),
						new Invoke(c0, s0, nextCharV, new Reference[] { }),
				}, {
					new CreateObject(s0, cns, new Reference[] { }),
					new Invoke(c0, s0, nextCharV, new Reference[] { }),
				}
		};

		check(t, expected);
	}


	public void testHardStatemachine5() throws Exception {
		TestfulConfig config = new TestfulConfig();
		config.setCut("dummy.WhiteSample");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 2, 3);

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
		Reference c3 = refFactory.getReferences(cClazz)[2];
		Reference s0 = refFactory.getReferences(cut)[0];
		Reference s3 = refFactory.getReferences(cut)[1];

		Methodz nextCharV = null;
		Methodz nextCharC = null;
		for(Methodz m : cut.getMethods()) {
			if("nextChar".equals(m.getName())) {
				if(m.getParameterTypes().length == 0) nextCharV = m;
				else nextCharC = m;
			}
		}
		
		Constructorz sCns = cut.getConstructors()[0];

		Constructorz cCns = cClazz.getConstructors()[0];
		
	//  statemachine_Hard_3 = new statemachine.Hard();
	//  java_lang_Character_1 = (char) statemachine_Hard_3.nextChar();
	//  java_lang_Character_0 = (char) statemachine_Hard_3.nextChar();
	//  java_lang_Character_3 = (char) new java.lang.Character(java_lang_Character_0);
	//  statemachine_Hard_0 = new statemachine.Hard();
	//  statemachine_Hard_0.nextChar(java_lang_Character_3);

		Operation[] ops = new Operation[] {
				new CreateObject(s3, sCns, new Reference[] { }),
				new Invoke(c1, s0, nextCharV, new Reference[] { }),
				new Invoke(c0, s0, nextCharV, new Reference[] { }),

				new CreateObject(c3, cCns, new Reference[] { c0 }),

				new CreateObject(s0, sCns, new Reference[] { }),
				new Invoke(null, s0, nextCharC, new Reference[] { c3 }),
		};

		Test t = new Test(cluster, refFactory, ops);

		Operation[][] expected = {
				{
					new CreateObject(s0, sCns, new Reference[] { }),
				},
				{
					new CreateObject(s3, sCns, new Reference[] { }),
				}
		};

		check(t, expected);
	}

	public void testHardStatemachine6() throws Exception {
		TestfulConfig config = new TestfulConfig();
		config.setCut("dummy.WhiteSample");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 2, 3);

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
		Reference c2 = refFactory.getReferences(cClazz)[2];
		Reference s2 = refFactory.getReferences(cut)[0];
		Reference s3 = refFactory.getReferences(cut)[1];
		
		Methodz nextCharV = null;
		Methodz nextCharC = null;
		for(Methodz m : cut.getMethods()) {
			if("nextChar".equals(m.getName())) {
				if(m.getParameterTypes().length == 0) nextCharV = m;
				else nextCharC = m;
			}
		}
		
		Constructorz sCns = cut.getConstructors()[0];

		//  statemachine_Hard_3 = new statemachine.Hard();
		//  java_lang_Character_2 = (char) statemachine_Hard_3.nextChar();
		//  java_lang_Character_1 = (char) statemachine_Hard_3.nextChar();
		//  java_lang_Character_0 = (char) statemachine_Hard_3.nextChar();
		
		//  statemachine_Hard_2 = new statemachine.Hard();
		//  statemachine_Hard_2.nextChar(java_lang_Character_2);
		//  java_lang_Character_2 = (char) statemachine_Hard_2.nextChar();
		//  java_lang_Character_0 = (char) statemachine_Hard_2.nextChar();
		//  statemachine_Hard_3.nextChar(java_lang_Character_1);

		Operation[] ops = new Operation[] {
				/*  0 */	new CreateObject(s3, sCns, new Reference[] { }), 
				/*  1 */	new Invoke(c2, s3, nextCharV, new Reference[] { }),
				/*  2 */	new Invoke(c1, s3, nextCharV, new Reference[] { }),
				/*  3 */	new Invoke(c0, s3, nextCharV, new Reference[] { }),
				/*  4 */	new CreateObject(s2, sCns, new Reference[] { }),
				/*  5 */	new Invoke(null, s2, nextCharC, new Reference[] { c2 }),
				/*  6 */	new Invoke(c2, s2, nextCharV, new Reference[] { }),
				/*  7 */	new Invoke(c0, s2, nextCharV, new Reference[] { }),
				/*  8 */	new Invoke(null, s3, nextCharC, new Reference[] { c1 }),
		};

		Test t = new Test(cluster, refFactory, ops);

		//	statemachine_Hard_3 = new statemachine.Hard();
		//	java_lang_Character_2 = (char) statemachine_Hard_3.nextChar();
		//	java_lang_Character_1 = (char) statemachine_Hard_3.nextChar();
		
		//	statemachine_Hard_2 = new statemachine.Hard();
		//	statemachine_Hard_2.nextChar(java_lang_Character_2);
		//	java_lang_Character_2 = (char) statemachine_Hard_2.nextChar();
		//	statemachine_Hard_3.nextChar(java_lang_Character_1);

		Operation[][] expected = { 
				{
					/*  0 */	new CreateObject(s3, sCns, new Reference[] { }), 
					/*  1 */	new Invoke(c2, s3, nextCharV, new Reference[] { }),
					/*  4 */	new CreateObject(s2, sCns, new Reference[] { }),
					/*  5 */	new Invoke(null, s2, nextCharC, new Reference[] { c2 }),
					/*  6 */	new Invoke(c2, s2, nextCharV, new Reference[] { }),
				}, {
					/*  0 */	new CreateObject(s3, sCns, new Reference[] { }), 
					/*  2 */	new Invoke(c1, s3, nextCharV, new Reference[] { }),
					/*  8 */	new Invoke(null, s3, nextCharC, new Reference[] { c1 }),
				}
		};

		check(t, expected);
	}

	public void testCoffee1() throws Exception {
		TestfulConfig config = new TestfulConfig();
		config.setCut("dummy.WhiteSample");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 1, 3);

		Clazz cut = cluster.getCut();

		Clazz iClazz = null;
		Clazz[] cl = cluster.getCluster();
		for(Clazz clazz : cl) {
			if("java.lang.Integer".equals(clazz.getClassName())) {
				iClazz = clazz;
				break;
			}
		}

		Reference i0 = refFactory.getReferences(iClazz)[0];
		Reference i2 = refFactory.getReferences(iClazz)[1];
		Reference i3 = refFactory.getReferences(iClazz)[2];
		Reference s0 = refFactory.getReferences(cut)[0];

		Methodz getTarget = null;
		Methodz addCoin = null;
		for(Methodz m : cut.getMethods()) {
			if("getTarget".equals(m.getName())) getTarget = m;
			if("addCoin".equals(m.getName())) addCoin = m;
		}
		
		Constructorz sCns = cut.getConstructors()[0];

//	  prova_Coffee_0 = new prova.Coffee();
//	  java_lang_Integer_2 = (int) prova_Coffee_0.getTarget();
//	  prova_Coffee_0.addCoin(java_lang_Integer_2);
//	  java_lang_Integer_3 = (int) prova_Coffee_0.getTarget();
//	  java_lang_Integer_2 = (int) prova_Coffee_0.getTarget();
//	  java_lang_Integer_0 = (int) prova_Coffee_0.getTarget();
//	  java_lang_Integer_3 = (int)1;
//	  prova_Coffee_0.addCoin(java_lang_Integer_2);
		
		Operation[] ops = new Operation[] {
				new CreateObject(s0, sCns, new Reference[] { }),
				new Invoke(i2, s0, getTarget, new Reference[] { }),
				new Invoke(null, s0, addCoin, new Reference[] { i2 }),

				new Invoke(i3, s0, getTarget, new Reference[] { }),
				new Invoke(i2, s0, getTarget, new Reference[] { }),
				new Invoke(i0, s0, getTarget, new Reference[] { }),

				new AssignPrimitive(i3, 1),
				
				new Invoke(null, s0, addCoin, new Reference[] { i2 }),
		};

		//  prova_Coffee_0 = new prova.Coffee();
		//  java_lang_Integer_2 = (int) prova_Coffee_0.getTarget();
		//  prova_Coffee_0.addCoin(java_lang_Integer_2);
		//  java_lang_Integer_2 = (int) prova_Coffee_0.getTarget();
		//  prova_Coffee_0.addCoin(java_lang_Integer_2);
		Operation[][] expected = {
				{
					new CreateObject(s0, sCns, new Reference[] { }),
					new Invoke(i2, s0, getTarget, new Reference[] { }),
					new Invoke(null, s0, addCoin, new Reference[] { i2 }),

					new Invoke(i0, s0, getTarget, new Reference[] { }),
					
					new Invoke(null, s0, addCoin, new Reference[] { i0 }),
				},
		};

		check(new Test(cluster, refFactory, ops), expected);

	}
}
