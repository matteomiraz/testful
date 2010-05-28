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
 */
public class Test_getSSATestCase extends AutoTestCase {

	@Override
	protected List<Test> perform(Test test) throws Exception {
		List<Test> ret = new ArrayList<Test>(1);
		ret.add(test.simplify().getSSA());
		return ret;
	}

	public void testBehavior1() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.Simple");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 2, 2);

		Clazz cut = cluster.getCut();

		Reference c0 = refFactory.getReferences(cut)[0];
		Reference c1 = refFactory.getReferences(cut)[1];

		Constructorz cns = cut.getConstructors()[0];


		Methodz compare = null;
		for(Methodz m : cut.getMethods()) {
			if("compare".equals(m.getName())) compare = m;
		}

		Operation[] ops = new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, compare, new Reference[] { c1 }),
				new CreateObject(c1, cns, new Reference[] { })
		};

		Test t = new Test(cluster, refFactory, ops);

		System.out.println("Orig:");
		System.out.print("References: ");
		for (Reference r : t.getReferenceFactory().getReferences()) System.out.print(r + " ");
		System.out.println();
		System.out.println(t);
		System.out.println("----");

		t = t.simplify();

		System.out.println("Simplified:");
		System.out.print("References: ");
		for (Reference r : t.getReferenceFactory().getReferences()) System.out.print(r + " ");
		System.out.println();
		System.out.println(t);
		System.out.println("----");

		t = t.getSSA();

		System.out.println("SSA:");
		System.out.print("References: ");
		for (Reference r : t.getReferenceFactory().getReferences()) System.out.print(r + " ");
		System.out.println();
		System.out.println(t);
		System.out.println("----");
	}

}
