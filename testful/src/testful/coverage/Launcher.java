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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.Option;

import testful.ConfigProject;
import testful.IConfigProject;
import testful.TestFul;
import testful.coverage.soot.Instrumenter;
import testful.runner.ClassType;
import testful.runner.DataFinderCaching;
import testful.runner.DataFinderImpl;
import testful.runner.TestfulClassLoader;

/**
 * Modify the class under test, adding trackers and other stuff.
 *
 * @author matteo
 */
public class Launcher {

	public static class ConfigInstrumenter extends ConfigProject implements IConfigProject.Args4j {

		@Option(required = false, name = "-project", usage = "Instrument all the classes of the project")
		private boolean project;

		@Option(required = false, name = "-ignorePackage", multiValued=true, usage = "When -project is used, ignore these packages")
		private List<String> ignorePackage = new ArrayList<String>();

		@Option(required = false, name = "-ignoreClass", multiValued=true, usage = "When -project is used, ignore these classes")
		private List<String> ignoreClass = new ArrayList<String>();

		@Option(required = false, name = "-file", usage = "Instrument the classes listed in the file")
		private File file;

		@Argument(required = false, multiValued = true, usage = "Instrument these classes")
		private List<String> classes = new ArrayList<String>();

		@Option(required = false, name = "-stopperOnly", usage = "Only run the testStopper and do not insert any coverage tracking")
		private boolean stopperOnly;

		@Option(required = false, name = "-context", usage = "Use Contextual Analysis (used in Def-Use analysis)")
		private boolean context = false;
		public boolean isContext() {
			return context;
		}

		public static enum DataFlowCoverage {

			/** No Data Flow Coverage */
			DISABLED,

			/** all Def-Use pairs */
			DU,

			/** p-Use + all Def-Use pairs*/
			PUSE,

			/** Exposition of Definitions + p-Use + all Def-Use pairs*/
			EXPDEF;

			public boolean isPUse() {
				switch (this) {
				case PUSE:
				case EXPDEF:
					return true;
				default:
					return false;
				}
			}
		}

		@Option(required = false, name = "-dataFlowCoverage", usage = "The data-flow coverage criterion to use")
		private DataFlowCoverage dataFlowCoverage = DataFlowCoverage.DISABLED;
		public DataFlowCoverage getDataFlowCoverage() {
			return dataFlowCoverage;
		}

		/**
		 * @param project instrument the whole project
		 */
		public void setProject(boolean project) {
			this.project = project;
		}

		/**
		 * @return true if instruments the whole project
		 */
		public boolean isProject() {
			return project;
		}

		/**
		 * @return the classes to ignore
		 */
		public List<String> getIgnoreClass() {
			return ignoreClass;
		}

		/**
		 * @param c the class to ignore
		 */
		public void addIgnoreClass(String c) {
			ignoreClass.add(c);
		}

		/**
		 * @return the ignorePackage
		 */
		public List<String> getIgnorePackage() {
			return ignorePackage;
		}

		/**
		 * @param p the package to ignore
		 */
		public void setIgnorePackage(String p) {
			ignorePackage.add(p);
		}

		/**
		 * @param file sets the file that contains the list of the class to instrument
		 */
		public void setFile(File file) {
			this.file = file;
		}

		/**
		 * @return the file that contains the list of the class to instrument
		 */
		public File getFile() {
			return file;
		}

		/**
		 * @param className the name of the class to instrument
		 */
		public void addClasses(String className) {
			classes.add(className);
		}

		/**
		 * @return the list of the classes to instrument
		 */
		public List<String> getClasses() {
			return classes;
		}

		/* (non-Javadoc)
		 * @see testful.ConfigProject#validate()
		 */
		@Override
		public void validate() throws CmdLineException {
			super.validate();

			int n = 0;
			if(project) n++;
			if(file != null) n++;
			if(!classes.isEmpty()) n++;

			if(n != 1) throw new CmdLineException(null, "You must use -project, -file, or provide one (or more) classes as argument");

			if(!project && !(ignoreClass.isEmpty() || ignorePackage.isEmpty())) throw new CmdLineException(null, "You can use -ignore only with -project.");
		}
	}

	private static Logger logger = Logger.getLogger("testful.coverage.Instrumenter");

	public static void main(String[] args) {
		ConfigInstrumenter config  = new ConfigInstrumenter();

		TestFul.parseCommandLine(config, args, Launcher.class, "Instrumenter");

		if(!config.isQuiet())
			testful.TestFul.printHeader("Instrumenter");

		testful.TestFul.setupLogging(config);

		try {
			TestfulClassLoader tcl = new TestfulClassLoader(new DataFinderCaching(new DataFinderImpl(new ClassType(config))));

			final List<String> toInstrument;
			if(config.project) toInstrument = getProjectClasses(tcl, config);
			else if(config.file != null) toInstrument = readFile(config.file);
			else toInstrument = config.classes;

			Instrumenter.prepare(config, toInstrument);


			if(config.stopperOnly) {
				Instrumenter.run(config, toInstrument, testful.coverage.stopper.ExecutionStopperInstrumenter.singleton );
			} else {
				Instrumenter.run(config, toInstrument,
						new testful.coverage.whiteBox.WhiteInstrumenter(config),
						testful.coverage.stopper.ExecutionStopperInstrumenter.singleton
				);
			}

		} catch (Exception e) {
			logger.log(Level.WARNING, "Error during the instrumentation: " + e.getMessage(), e);
			e.printStackTrace();
			System.exit(1);
		}

		System.exit(0);
	}

	/**
	 * Builds the list of classes in the current project
	 * @param config the configuration of the current project
	 * @return the collection with all the names of the classes in the current project
	 */
	private static List<String> getProjectClasses(ClassLoader loader, ConfigInstrumenter config) {
		SortedSet<String> classes =  getProjectClasses(loader, new TreeSet<String>(), config.getDirCompiled(), config.getDirCompiled().getAbsolutePath(), config.getIgnorePackage(), config.getIgnoreClass());

		List<String> ret = new ArrayList<String>();
		for (String c : classes) ret.add(c);
		return ret;
	}

	/**
	 * Recursive method to get the list of classes in the current project
	 * @param ret the list of classes being built
	 * @param dir the directory to analyze
	 * @param base the base directory
	 * @param ignore list of classes and packages to ignore
	 */
	private static SortedSet<String> getProjectClasses(ClassLoader loader, SortedSet<String> ret, File dir, String base, List<String> ignorePackages, List<String> ignoreClasses) {
		for (File f : dir.listFiles()) {
			if(f.isDirectory()) getProjectClasses(loader, ret, f, base, ignorePackages, ignoreClasses);
			else if(f.isFile() && f.getName().endsWith(".class")) {
				final String fullName = f.getAbsolutePath();
				final String className = fullName.substring(base.length()+1, fullName.length() - 6).replace(File.separatorChar, '.');

				if(skip(ignorePackages, ignoreClasses, className)) continue;

				try {
					Class<?> c = loader.loadClass(className);

					if(c.isInterface()) continue;

					ret.add(className);

				} catch (Throwable e) {
					logger.warning("Cannot load class " + className + ": " + e);
				}
			}
		}

		return ret;
	}

	private static boolean skip(List<String> ignorePackages, List<String> ignoreClasses, final String className) {
		for (String i : ignoreClasses) {
			if(className.equals(i)) {
				logger.info("Ignoring " + className);
				return true;
			}
		}

		for (String i : ignorePackages) {
			if(className.startsWith(i)) {
				logger.info("Ignoring " + className + " (belongs to " + i + " package)");
				return true;
			}
		}

		return false;
	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static List<String> readFile(File file) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file));

		try {

			String line;
			List<String> ret = new ArrayList<String>();
			while((line = r.readLine()) != null)
				ret.add(line);

			return ret;

		} finally {
			r.close();
		}
	}
}
