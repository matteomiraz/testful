/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011  Matteo Miraz
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

import testful.GenericTestCase;
import testful.coverage.behavior.AbstractorMethod;
import testful.coverage.behavior.AbstractorObjectState;
import testful.coverage.behavior.AbstractorRegistry;
import testful.coverage.behavior.BehaviorCoverage;
import testful.model.CreateObject;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.Test;
import testful.testCut.TestCoverageBehaviorCUT;
import testful.utils.ElementManager;

/**
 * Test for the behavioral coverage tracking functionality
 * @author matteo
 */
public class CoverageBehavioralTestCase extends GenericTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BehaviorCoverage.DOT = false;
		BehaviorCoverage.resetLabels();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		BehaviorCoverage.resetLabels();
	}

	public void testXmlDescription() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		AbstractorRegistry ar = cut.abstractorRegistry;

		AbstractorObjectState c = ar.getAbstractorClass("test.coverage.Behavior");
		assertNotNull(c);

		AbstractorMethod m = ar.getAbstractorMethod("test.coverage.Behavior.<init>()");
		assertNotNull(m);
	}


	public void testCns() throws Exception {
		TestCoverageBehaviorCUT cut = new TestCoverageBehaviorCUT();
		Test t = new Test(cut.cluster, cut.refFactory, new Operation[] {
				new CreateObject(cut.cuts[0], cut.cns, new Reference[] { }),
		});

		ElementManager<String, CoverageInformation> covs = getCoverage(t, null, cut.abstractorRegistry);
		BehaviorCoverage beh = (BehaviorCoverage) covs.get(BehaviorCoverage.KEY);

		assertNotNull(beh);
		assertEquals(1.0f, beh.getQuality());
		assertEquals("S0\n" +
				"  <init>() - -> S1\n" +
				"--- Legend ---\n" +
				"S0: Null Object\n" +
				"S1: test.coverage.Behavior (test.coverage.Behavior): {this.getN() = 0}\n", beh.toString());
	}
}