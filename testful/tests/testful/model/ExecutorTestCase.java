package testful.model;

import java.lang.reflect.Field;

import testful.GenericTestCase;
import testful.model.TestfulProblem.TestfulConfig;
import testful.runner.TestfulClassLoader;

/**
 * @author matteo
 *
 */
public class ExecutorTestCase extends GenericTestCase {

	public void testBehavior1() throws Exception {
		TestfulConfig config = new TestfulConfig();
		config.setCut("dummy.WhiteSample");
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

		Operation[] ops = new Operation[] {
				new AssignConstant(f1, new StaticValue(cluster, THREE_FIFTHS)),
				new Invoke(i0, f1, compareTo, new Reference[] { f3 }),
				new Invoke(f0, f1, divide, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		getCoverage(t);
	}

	public void testBehavior2() throws Exception {
		TestfulConfig config = new TestfulConfig();
		config.setCut("dummy.WhiteSample");
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

		Operation[] ops = new Operation[] {
				new AssignPrimitive(i0, 0),
				new AssignConstant(f1, new StaticValue(cluster, THREE_FIFTHS)),
				new Invoke(i0, f1, compareTo, new Reference[] { f3 }),
				new Invoke(f0, f1, divide, new Reference[] { i0 })
		};

		Test t = new Test(cluster, refFactory, ops);

		getCoverage(t);
	}
}
