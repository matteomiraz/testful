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

import java.util.ArrayList;
import java.util.List;

import testful.ConfigCut;
import testful.GenericTestCase;
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

	/**
	 * Found a bug in dummy.Simple: the test
	 * <ul>
	 * <li>1) java_lang_Integer_1 = (int) dummy_Simple_3.oStatus();</li>
	 * <li>2) dummy_Simple_3 = new dummy.Simple();</li>
	 * <li>3) java_lang_Integer_0 = (int) dummy_Simple_3.oAbs();</li>
	 * <li>4) java_lang_Object_3 = new java.lang.Object();</li>
	 * <li>5) dummy_Simple_2 = new dummy.Simple();</li>
	 * <li>6) dummy_Simple_2.compare(java_lang_Object_3);</li>
	 * <li>7) java_lang_Integer_2 = (int) dummy_Simple_2.oAbs();</li>
	 * </ul>
	 * became (note the istruction #1):
	 * <ul>
	 * <li>5) dummy_Simple_2 = new dummy.Simple();</li>
	 * <li>4) java_lang_Object_3 = new java.lang.Object();</li>
	 * <li>1) java_lang_Integer_1 = (int) dummy_Simple_3.oStatus();</li>
	 * <li>2) dummy_Simple_3 = new dummy.Simple();</li>
	 * <li>3) java_lang_Integer_2 = (int) dummy_Simple_2.oAbs();</li>
	 * <li>6) dummy_Simple_2.compare(java_lang_Object_3);</li>
	 * <li>7) java_lang_Integer_0 = (int) dummy_Simple_3.oAbs();</li>
	 * </ul>
	 */
	public void testSimple() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.Simple");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 4, 4);

		Clazz cut = cluster.getCut();
		Clazz iClazz = null;
		Clazz oClazz = null;
		for(Clazz clazz : cluster.getCluster()) {
			if("java.lang.Integer".equals(clazz.getClassName())) iClazz = clazz;
			if("java.lang.Object".equals(clazz.getClassName())) oClazz = clazz;
		}
		assertNotNull("Cannot find java.lang.Integer", iClazz);
		assertNotNull("Cannot find java.lang.Object", oClazz);

		Reference s2 = refFactory.getReferences(cut)[2];
		Reference s3 = refFactory.getReferences(cut)[3];
		Reference i0 = refFactory.getReferences(iClazz)[0];
		Reference i1 = refFactory.getReferences(iClazz)[1];
		Reference i2 = refFactory.getReferences(iClazz)[2];
		Reference o3 = refFactory.getReferences(oClazz)[3];

		Methodz oStatus = null;
		Methodz oAbs = null;
		Methodz compare = null;
		for(Methodz m : cut.getMethods()) {
			if("oStatus".equals(m.getName())) oStatus = m;
			if("oAbs".equals(m.getName())) oAbs = m;
			if("compare".equals(m.getName())) compare = m;
		}
		assertNotNull("Cannot find a method", oAbs);
		assertNotNull("Cannot find a method", oStatus);
		assertNotNull("Cannot find a method", compare);

		Constructorz sCns = cut.getConstructors()[0];
		Constructorz oCns = oClazz.getConstructors()[0];

		Test orig = new Test(cluster, refFactory, new Operation[] {
				new Invoke(i1, s3, oStatus, new Reference[]{ }),
				new CreateObject(s3, sCns, new Reference[] { }),
				new Invoke(i0, s3, oAbs, new Reference[]{ }),
				new CreateObject(o3, oCns, new Reference[]{ }),
				new CreateObject(s2, sCns, new Reference[]{ }),
				new Invoke(null, s2, compare, new Reference[] { o3 }),
				new Invoke(i2, s2, oAbs, new Reference[] { })
		});

		autoTest(orig);
	}
}
