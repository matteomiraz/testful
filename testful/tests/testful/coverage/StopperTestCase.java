/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010 Matteo Miraz
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

package testful.coverage;

import testful.SingleClassTestCase;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.model.Constructorz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Methodz;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.utils.ElementManager;

/**
 * TODO describe me!
 * @author matteo
 */
public class StopperTestCase extends SingleClassTestCase {

	/* (non-Javadoc)
	 * @see testful.SingleClassTestCase#getClassUnderTest()
	 */
	@Override
	protected String getClassUnderTest() {
		return "test.Stopped";
	}

	protected Constructorz cns;

	protected Methodz longMethod1;
	protected Methodz longMethod2;
	protected Methodz longMethod3;
	protected Methodz longMethod4;
	protected Methodz longMethod5;

	/* (non-Javadoc)
	 * @see testful.SingleClassTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		cns = cut.getConstructors()[0];

		longMethod1 = null;
		longMethod2 = null;
		longMethod3 = null;
		longMethod4 = null;
		longMethod5 = null;
		for(Methodz m : cut.getMethods()) {
			if(checkMethod(m, "longMethod1")) longMethod1 = m;
			if(checkMethod(m, "longMethod2")) longMethod2 = m;
			if(checkMethod(m, "longMethod3")) longMethod3 = m;
			if(checkMethod(m, "longMethod4")) longMethod4 = m;
			if(checkMethod(m, "longMethod5")) longMethod5 = m;
		}
		assertNotNull(longMethod1);
		assertNotNull(longMethod2);
		assertNotNull(longMethod3);
		assertNotNull(longMethod4);
		assertNotNull(longMethod5);
	}

	/* (non-Javadoc)
	 * @see testful.SingleClassTestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		cns = null;
		longMethod1 = null;
		longMethod2 = null;
		longMethod3 = null;
		longMethod4 = null;
		longMethod5 = null;
	}

	public void testLongMethod1() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod1, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		CoverageInformation bb = covs.get(CoverageBasicBlocks.KEY);
		assertEquals(5.0f, bb.getQuality());
	}

	public void testLongMethod2() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod2, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		CoverageInformation bb = covs.get(CoverageBasicBlocks.KEY);
		assertEquals(5.0f, bb.getQuality());
	}

	public void testLongMethod3() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod3, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		CoverageInformation bb = covs.get(CoverageBasicBlocks.KEY);
		assertEquals(5.0f, bb.getQuality());
	}

	public void testLongMethod4() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod4, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		CoverageInformation bb = covs.get(CoverageBasicBlocks.KEY);
		assertEquals(5.0f, bb.getQuality());
	}

	public void testLongMethod5() throws Exception {
		Test t = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cns, new Reference[] { }),
				new Invoke(null, cuts[0], longMethod5, new Reference[] { } )
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t);
		CoverageInformation bb = covs.get(CoverageBasicBlocks.KEY);
		assertEquals(7.0f, bb.getQuality());
	}
}
