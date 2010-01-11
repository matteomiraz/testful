package testful.model;

import testful.ConfigCut;
import testful.GenericTestCase;
import testful.model.TestCluster.MissingClassException;
import testful.runner.TestfulClassLoader;

public class TestClusterTestCase extends GenericTestCase {

	public void testIsValid() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("testcluster.Point");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
			fail("The cluster is not valid: Point implementations are missing!");
		} catch (MissingClassException e) {
			assertTrue(e.fatal);
			assertEquals(1, e.missing.size());
			assertTrue(e.missing.contains("testcluster.Point"));
		}
	}

	public void testIsValid2() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("testcluster.Triangle");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
			fail("The cluster is not valid: Point implementations are missing!");
		} catch (MissingClassException e) {
			assertTrue(e.fatal);
			assertEquals(1, e.missing.size());
			assertTrue(e.missing.contains("testcluster.Point"));
		}
	}

	public void testIsValid3() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("testcluster.Triangle2");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}

	public void testIsValid4() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.config);
		config.setCut("testcluster.Triangle3");

		TestCluster tc = new TestCluster(new TestfulClassLoader(getFinder()), config);

		try {
			tc.isValid();
		} catch (MissingClassException e) {
			fail(e.getMessage());
		}
	}
}


