/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.ConfigCut;
import testful.IConfigCut;
import testful.TestFul;
import testful.model.TestCluster;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.TestfulClassLoader;
import testful.utils.Instrumenter;

/**
 * Modify the class under test, adding trackers and other stuff.
 * 
 * @author matteo
 */
public class Launcher {

	private static Logger logger = Logger.getLogger("testful.coverage.Instrumenter");

	public static void main(String[] args) {
		IConfigCut config  = new ConfigCut();

		TestFul.parseCommandLine(config, args, Launcher.class, "Instrumenter");

		if(!config.isQuiet())
			testful.TestFul.printHeader("Instrumenter");

		testful.TestFul.setupLogging(config);

		try {
			ClassFinderCaching finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirContracts(), config.getDirCompiled()));
			TestfulClassLoader tcl = new TestfulClassLoader(finder);
			TestCluster tc = new TestCluster(tcl, config);

			Collection<String> toInstrument = tc.getClassesToInstrument();

			Instrumenter.prepare(config, toInstrument);

			Instrumenter.run(config, toInstrument, config.getCut(),
					//			testful.coverage.behavior.BehaviorInstrumenter.singleton,
					//			testful.coverage.bug.BugInstrumenter.singleton,
					testful.coverage.whiteBox.WhiteInstrumenter.singleton
			);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error during the instrumentation: " + e.getMessage(), e);
		}

		System.exit(0);
	}
}
