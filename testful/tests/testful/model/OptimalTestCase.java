package testful.model;

import testful.SimpleDummyTestCase;
import testful.coverage.whiteBox.CoverageBasicBlocks;

public class OptimalTestCase extends SimpleDummyTestCase {

	public void testOptimal1a() throws Exception {
		Test t1 = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(c0, cns, new Reference[] { })
		});
		TestCoverage tc1 = new TestCoverage(t1, getCoverage(t1));

		Test t2 = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, mInc, new Reference[] { })
		});
		TestCoverage tc2 = new TestCoverage(t2, getCoverage(t2));

		assertTrue("Invalid test configuration", tc1.getCoverage().get(CoverageBasicBlocks.KEY_CODE).getQuality() < tc2.getCoverage().get(CoverageBasicBlocks.KEY_CODE).getQuality());

		OptimalTestCreator opt = new OptimalTestCreator();
		assertEquals(0, opt.get().size());

		opt.update(tc1);

		StringBuilder msg = new StringBuilder();
		for (Test t : opt.get())
			printTest(msg, "", t, null);
		System.out.println(msg.toString());

		assertEquals(1, opt.get().size());
		assertEquals(tc1, opt.get().iterator().next());

		opt.update(tc2);
		assertEquals(1, opt.get().size());
		assertEquals(tc2, opt.get().iterator().next());
	}
}
