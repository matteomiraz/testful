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

package testful.runner;

import java.io.File;
import java.net.URL;

import testful.ConfigCut;
import testful.ConfigProject;
import testful.GenericTestCase;
import testful.model.TestCluster;

/**
 * Tests the class finder
 * @author matteo
 */
public class ClassFinderTestCase extends GenericTestCase {

	public void testBase() throws Exception {
		ConfigProject config = new ConfigProject(GenericTestCase.getConfig());

		ClassType classType = new ClassType(config);
		DataFinderImpl finder = new DataFinderImpl(classType);
		classType.addClassData(new ClassData() {
			@Override
			public void load(String className, URL classFile) {
				if("JDOMAbout".equals(className))
					fail("JDOMAbout loaded from " + classFile.toString());
			}
		});

		byte[] b = finder.getData(ClassType.NAME, "JDOMAbout");
		assertNull("JDOMAbout should not be found", b);
	}

	public void testWrongJar() throws Exception {
		ConfigProject config = new ConfigProject(GenericTestCase.getConfig());
		config.addLibrary(new File("lib/jmetal.jar").getAbsoluteFile());

		ClassType classType = new ClassType(config);
		DataFinderImpl finder = new DataFinderImpl(classType);
		classType.addClassData(new ClassData() {
			@Override
			public void load(String className, URL classFile) {
				if("JDOMAbout".equals(className))
					fail("JDOMAbout loaded from " + classFile.toString());
			}
		});

		byte[] b = finder.getData(ClassType.NAME, "JDOMAbout");
		assertNull("JDOMAbout should not be found", b);
	}

	public void testFirstJar() throws Exception {
		ConfigProject config = new ConfigProject(GenericTestCase.getConfig());
		final File lib = new File("lib/jdom.jar").getAbsoluteFile();
		config.addLibrary(lib);

		ClassType classType = new ClassType(config);
		DataFinderImpl finder = new DataFinderImpl(classType);
		classType.addClassData(new ClassData() {
			@Override
			public void load(String className, URL classFile) {
				if("JDOMAbout".equals(className))
					assertEquals("jar:file:" + lib.getAbsolutePath() + "!/JDOMAbout.class", classFile.toString());
			}
		});

		byte[] c = finder.getData(ClassType.NAME, "JDOMAbout");
		assertNotNull(c);
	}

	public void testSecondJar() throws Exception {
		ConfigProject config = new ConfigProject(GenericTestCase.getConfig());
		config.addLibrary(new File("lib/jmetal.jar").getAbsoluteFile());
		final File lib = new File("lib/jdom.jar").getAbsoluteFile();
		config.addLibrary(lib);

		ClassType classType = new ClassType(config);
		DataFinderImpl finder = new DataFinderImpl(classType);
		classType.addClassData(new ClassData() {
			@Override
			public void load(String className, URL classFile) {
				if("JDOMAbout".equals(className))
					assertEquals("jar:file:" + lib.getAbsolutePath() + "!/JDOMAbout.class", classFile.toString());
			}
		});

		byte[] c = finder.getData(ClassType.NAME, "JDOMAbout");
		assertNotNull(c);
	}

	public void testInnerClass() throws Exception {
		ConfigCut config = new ConfigCut(GenericTestCase.getConfig());
		config.setCut("test.model.cluster.testInnerClass.Container");

		TestfulClassLoader classLoader = new TestfulClassLoader(getFinder());
		TestCluster tc = new TestCluster(classLoader, config);

		assertEquals(2, tc.getCluster().length);
		assertEquals("test.model.cluster.testInnerClass.Container", tc.getCluster()[0].getClassName());
		assertEquals("test.model.cluster.testInnerClass.Container$Contained", tc.getCluster()[1].getClassName());
	}
}
