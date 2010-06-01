/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package testful.model;

import java.util.List;

import testful.ConfigCut;
import testful.GenericTestCase;
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
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.StateMachine");
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
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.StateMachine");
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
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.StateMachine");
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
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.StateMachine");
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
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.StateMachine");
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
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.StateMachine");
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
}
