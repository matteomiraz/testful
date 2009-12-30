package testful;

import testful.model.Clazz;
import testful.model.Constructorz;
import testful.model.Methodz;
import testful.model.Reference;
import testful.model.ReferenceFactory;
import testful.model.TestCluster;
import testful.runner.TestfulClassLoader;

public class SimpleDummyTestCase extends GenericTestCase {
	protected TestCluster cluster;
	protected ReferenceFactory refFactory;

	protected Reference c0, c1, c2, c3;
	protected Reference i0, i1, i2, i3;

	protected Constructorz cns;

	protected Methodz mInc;
	protected Methodz mDec;
	protected Methodz oStatus;
	protected Methodz wModulo;
	protected Methodz oAbs;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("dummy.Simple");
		cluster = new TestCluster(new TestfulClassLoader(getFinder()), config);
		refFactory = new ReferenceFactory(cluster, 4, 4);

		Clazz cut = cluster.getCut();

		Clazz iClazz = null;
		Clazz[] cl = cluster.getCluster();
		for(Clazz clazz : cl) {
			if("java.lang.Integer".equals(clazz.getClassName())) {
				iClazz = clazz;
				break;
			}
		}

		i0 = refFactory.getReferences(iClazz)[0];
		i1 = refFactory.getReferences(iClazz)[1];
		i2 = refFactory.getReferences(iClazz)[2];
		i3 = refFactory.getReferences(iClazz)[3];

		c0 = refFactory.getReferences(cut)[0];
		c1 = refFactory.getReferences(cut)[1];
		c2 = refFactory.getReferences(cut)[2];
		c3 = refFactory.getReferences(cut)[3];

		cns = cut.getConstructors()[0];

		mInc = null;
		mDec = null;
		oStatus = null;
		wModulo = null;
		oAbs = null;
		for(Methodz m : cut.getMethods()) {
			if("mInc".equals(m.getName())) mInc = m;
			if("mDec".equals(m.getName())) mDec = m;
			if("oStatus".equals(m.getName())) oStatus = m;
			if("wModulo".equals(m.getName())) wModulo = m;
			if("oAbs".equals(m.getName())) oAbs = m;
		}
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		cluster = null;
		refFactory = null;

		cns      = null;
		mInc     = null;
		mDec     = null;
		oStatus  = null;
		wModulo  = null;
		oAbs     = null;

		i0 = null;
		i1 = null;
		i2 = null;
		i3 = null;

		c0 = null;
		c1 = null;
		c2 = null;
		c3 = null;
	}


}
