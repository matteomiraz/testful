package testful.model;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import testful.ConfigCut;
import testful.GenericTestCase;
import testful.coverage.CoverageInformation;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;

/**
 * @author matteo
 *
 */
public class ExecutorTestCase extends GenericTestCase {

	public void testFraction() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.Fraction");
		TestCluster cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		ReferenceFactory refFactory = new ReferenceFactory(cluster, 4, 4);

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
		Reference f0 = refFactory.getReferences(cut)[0];
		Reference f1 = refFactory.getReferences(cut)[1];
		Reference f3 = refFactory.getReferences(cut)[3];

		Field THREE_FIFTHS = cut.toJavaClass().getField("THREE_FIFTHS");

		Methodz compareTo = null;
		Methodz divide = null;
		for(Methodz m : cut.getMethods()) {
			if("compareTo".equals(m.getName())) compareTo = m;
			if("divide".equals(m.getName())) divide = m;
		}


		Operation[] ops1 = new Operation[] {
				new AssignPrimitive(i0, 0),
				new AssignConstant(f1, new StaticValue(cluster, THREE_FIFTHS)),
				new Invoke(i0, f1, compareTo, new Reference[] { f3 }),
				new Invoke(f0, f1, divide, new Reference[] { i0 })
		};
		Test t1 = new Test(cluster, refFactory, ops1);
		ElementManager<String, CoverageInformation> cov1 = getCoverage(t1);

		Operation[] ops2 = new Operation[] {
				new AssignPrimitive(i0, 0),
				new AssignConstant(f1, new StaticValue(cluster, THREE_FIFTHS)),
				new Invoke(i0, f1, compareTo, new Reference[] { f3 }),
				new Invoke(f0, f1, divide, new Reference[] { i0 })
		};
		Test t2 = new Test(cluster, refFactory, ops2);

		ElementManager<String, CoverageInformation> cov2 = getCoverage(t2);

		List<ElementManager<String, CoverageInformation>> listCov2 = new ArrayList<ElementManager<String,CoverageInformation>>();
		listCov2.add(cov2);

		checkTestFailed(t1, cov1, Arrays.asList(t2), listCov2, cov2);
	}
}
