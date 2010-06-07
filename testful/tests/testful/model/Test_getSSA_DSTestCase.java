package testful.model;

import testful.SimpleDummyTestCase;
import testful.coverage.CoverageInformation;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageBranch;
import testful.coverage.whiteBox.CoverageDataFlow;
import testful.coverage.whiteBox.CoverageDefExp;
import testful.utils.ElementManager;

public class Test_getSSA_DSTestCase extends SimpleDummyTestCase {

	public void test01() throws Exception {
		Test t1 = new Test(cluster, refFactory, new Operation[] {
				new AssignPrimitive(i3, -1828778093),
				new CreateObject(c3, cns, new Reference[] { }),
				new Invoke(i0, c3, wModulo, new Reference[] { i3 }),
				new CreateObject(i3, i_cns, new Reference[] { i0 }),
				new CreateObject(c2, cns, new Reference[] { }),
				new Invoke(null, c2, mInc, new Reference[] { }),
				new Invoke(i0, c2, wModulo, new Reference[] { i3 })
		});
		ElementManager<String, CoverageInformation> cov1 = getCoverage(t1);
		assertEquals(3.0f, cov1.get(CoverageBasicBlocks.KEY).getQuality());
		assertEquals(0.0f, cov1.get(CoverageBranch.KEY).getQuality());
		assertEquals(8.0f, cov1.get(CoverageDataFlow.KEY).getQuality());
		assertEquals(3.0f, cov1.get(CoverageDefExp.KEY).getQuality());

		Test t2 = new Test(cluster, refFactory, new Operation[] {
				new AssignPrimitive(i1, -1828778093),
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(i2, c0, wModulo, new Reference[] { i1 }),
				new CreateObject(i3, i_cns, new Reference[] { i2 }),
				new CreateObject(c1, cns, new Reference[] { }),
				new Invoke(null, c1, mInc, new Reference[] { }),
				new Invoke(i3, c1, wModulo, new Reference[] { i1 }) // the SSA uses i3 as input parameter! (it's wrong, since the first call to wModulo did not produce any result)
		});
		ElementManager<String, CoverageInformation> cov2 = getCoverage(t2);
		assertEquals(cov1.get(CoverageBasicBlocks.KEY).getQuality(), cov2.get(CoverageBasicBlocks.KEY).getQuality());
		assertEquals(cov1.get(CoverageBranch.KEY).getQuality(), cov2.get(CoverageBranch.KEY).getQuality());
		assertEquals(cov1.get(CoverageDataFlow.KEY).getQuality(), cov2.get(CoverageDataFlow.KEY).getQuality());
		assertEquals(cov1.get(CoverageDefExp.KEY).getQuality(), cov2.get(CoverageDefExp.KEY).getQuality());
	}
}
