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

import testful.ConfigCut;
import testful.GenericTestCase;
import testful.coverage.CoverageInformation;
import testful.coverage.bug.BugCoverage;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;

public class BugCoverageTestCase extends GenericTestCase {

	private TestCluster cluster;
	private ReferenceFactory refFactory;

	private Reference i0;
	private Reference i1;
	private Reference i2;
	private Reference f0;
	private Constructorz cns;

	private Methodz sum;

	@Override
	protected void setUp() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.WhiteSample");
		cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		refFactory = new ReferenceFactory(cluster, 1, 3);

		Clazz cut = cluster.getCut();

		Clazz iClazz = null;
		Clazz[] cl = cluster.getCluster();
		for(Clazz clazz : cl) {
			if("java.lang.Integer".equals(clazz.getClassName())) {
				iClazz = clazz;
				break;
			}
		}

		f0 = refFactory.getReferences(cut)[0];

		i0 = refFactory.getReferences(iClazz)[0];
		i1 = refFactory.getReferences(iClazz)[1];
		i2 = refFactory.getReferences(iClazz)[2];

		for(Methodz m : cut.getMethods()) {
			if("sum".equals(m.getName())) sum = m;
		}

		assertNotNull(sum);

		cns = cut.getConstructors()[0];
	}

	@Override
	protected void tearDown() throws Exception {
		cluster = null;
		refFactory = null;

		i0 = null;
		i1 = null;
		i2 = null;
		f0 = null;
		cns = null;
		sum = null;
	}

	public void testNormalExecution() throws Exception {
		System.err.println("BugCoverageTestCase.testNormalExecution()");

		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 1),
				new AssignPrimitive(i1, 2),
				new AssignPrimitive(i2, 3),
				new CreateObject(f0, cns, new Reference[] {}),
				new Invoke(null, f0, sum, new Reference[] { i0, i1, i2 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		assertNotNull("no bug coverage", cov.get(BugCoverage.KEY));
		assertEquals(0.0f, cov.get(BugCoverage.KEY).getQuality());

	}

	public void testPostconditionError() throws Exception {
		System.err.println("BugCoverageTestCase.testPostconditionError()");

		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 1),
				new AssignPrimitive(i1, 2),
				new AssignPrimitive(i2, 4),
				new CreateObject(f0, cns, new Reference[] {}),
				new Invoke(null, f0, sum, new Reference[] { i0, i1, i2 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		assertNotNull("no bug coverage", cov.get(BugCoverage.KEY));
		assertEquals(1.0f, cov.get(BugCoverage.KEY).getQuality());
	}

	/**
	 * The test invokes a method violating its precondition.
	 */
	public void testPreconditionError() throws Exception {
		System.err.println("BugCoverageTestCase.testPreconditionError()");

		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, -1),
				new AssignPrimitive(i1, -2),
				new AssignPrimitive(i2, 1),
				new CreateObject(f0, cns, new Reference[] {}),
				new Invoke(null, f0, sum, new Reference[] { i0, i1, i2 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		assertNotNull("no bug coverage", cov.get(BugCoverage.KEY));
		assertEquals(0.0f, cov.get(BugCoverage.KEY).getQuality());

	}

	/**
	 * A method of the class invokes another method violating its preconditions.
	 * The framework must report this as a bug!
	 */
	public void testInvalidCall() throws Exception {
		System.err.println("BugCoverageTestCase.testInvalidCall()");

		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 1),
				new AssignPrimitive(i1, 2),
				new AssignPrimitive(i2, -3),
				new CreateObject(f0, cns, new Reference[] {}),
				new Invoke(null, f0, sum, new Reference[] { i0, i1, i2 })
		};

		Test t = new Test(cluster, refFactory, ops);

		ElementManager<String, CoverageInformation> cov = getCoverage(t);

		assertNotNull("no bug coverage", cov.get(BugCoverage.KEY));
		assertEquals(1.0f, cov.get(BugCoverage.KEY).getQuality());

	}

}
