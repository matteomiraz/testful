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
import testful.coverage.fault.FaultsCoverage;
import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Methodz;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.utils.ElementManager;

/**
 * Test for the fault detection functionality
 * @author matteo
 */
public class FaultTestCase extends SingleClassTestCase {

	private Reference[] objects;
	private Constructorz oCns;
	private Constructorz cCns;

	private Methodz a, a1, a2;
	private Methodz b, b1;
	private Methodz c, c1, c2;
	private Methodz d;
	private Methodz e, e1;

	@Override
	protected String getClassUnderTest() {
		return "test.coverage.Fault";
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		Clazz oClazz = null;
		for(Clazz clazz : cluster.getCluster()) {
			if("java.lang.Object".equals(clazz.getClassName())) oClazz = clazz;
		}
		assertNotNull(oClazz);

		objects = refFactory.getReferences(oClazz);

		cCns = cut.getConstructors()[0];
		assertNotNull(cCns);

		oCns = null;
		for(Constructorz c : cut.getConstructors()) {
			if(checkParameters(c.getParameterTypes(), new Clazz[] { })) oCns = c;
		}
		assertNotNull(oCns);

		a = null;
		a1 = null;
		a2 = null;
		b = null;
		b1 = null;
		c = null;
		c1 = null;
		c2 = null;
		d = null;
		e = null;
		e1 = null;
		for(Methodz m : cut.getMethods()) {
			if(checkMethod(m, "a" , oClazz)) a  = m;
			if(checkMethod(m, "a1", oClazz)) a1 = m;
			if(checkMethod(m, "a2", oClazz)) a2 = m;
			if(checkMethod(m, "b"         )) b  = m;
			if(checkMethod(m, "b1"        )) b1 = m;
			if(checkMethod(m, "c"         )) c  = m;
			if(checkMethod(m, "c1"        )) c1 = m;
			if(checkMethod(m, "c2"        )) c2 = m;
			if(checkMethod(m, "d"         )) d  = m;
			if(checkMethod(m, "e"         )) e  = m;
			if(checkMethod(m, "e1"        )) e1 = m;
		}
		assertNotNull(a);
		assertNotNull(a1);
		assertNotNull(a2);
		assertNotNull(b );
		assertNotNull(b1);
		assertNotNull(c );
		assertNotNull(c1);
		assertNotNull(c2);
		assertNotNull(d );
		assertNotNull(e );
		assertNotNull(e1);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		objects = null;
		cCns = null;
		oCns = null;

		a = null;
		a1 = null;
		a2 = null;
		b = null;
		b1 = null;
		c = null;
		c1 = null;
		c2 = null;
		d = null;
		e = null;
		e1 = null;
	}

	public void testA() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[0], oCns, new Reference[] { }),
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testANull() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testA1() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[0], oCns, new Reference[] { }),
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a1, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());
	}

	public void testA1Null() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a1, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testA2() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(objects[0], oCns, new Reference[] { }),
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a2, new Reference[] { objects[0] } )
		}));


		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testA2Null() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], a2, new Reference[] { objects[0] } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testB() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], b , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());
	}

	public void testB1() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], b1, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testC() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], c , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());
	}

	public void testC1() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], c1, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testC2() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], c2, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testD() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], d , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}

	public void testE() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], e , new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertNotNull(fCov);
		assertEquals(1.0f, fCov.getQuality());
	}

	public void testE1() throws Exception {
		ElementManager<String, CoverageInformation> covs = getCoverage(new Test(cluster, refFactory, new Operation[] {
				new CreateObject(cuts[0], cCns, new Reference[] { }),
				new Invoke(null, cuts[0], e1, new Reference[] { } )
		}));

		FaultsCoverage fCov = (FaultsCoverage) covs.get(FaultsCoverage.KEY);
		assertTrue(fCov == null || fCov.getQuality() == 0);
	}
}
