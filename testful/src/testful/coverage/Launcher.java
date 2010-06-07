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
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
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

		@Option(required = false, name = "-file", usage = "Instrument the classes listed in the file")
		private File file;

		@Argument(required = false, multiValued = true, usage = "Instrument these classes")
		private List<String> classes = new ArrayList<String>();

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
			ClassFinderCaching finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirCompiled()));
			TestfulClassLoader tcl = new TestfulClassLoader(finder);

			final List<String> toInstrument;
			if(config.project) toInstrument = getProjectClasses(tcl, config);
			else if(config.file != null) toInstrument = readFile(config.file);
			else toInstrument = config.classes;

			Instrumenter.prepare(config, toInstrument);

			Instrumenter.run(config, toInstrument,
					testful.coverage.fault.FaultInstrumenter.singleton,
					testful.coverage.whiteBox.WhiteInstrumenter.singleton
			);
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
	private static List<String> getProjectClasses(ClassLoader loader, IConfigProject config) {
		SortedSet<String> classes =  getProjectClasses(loader, new TreeSet<String>(), config.getDirCompiled(), config.getDirCompiled().getAbsolutePath());

		List<String> ret = new ArrayList<String>();
		for (String c : classes) ret.add(c);
		return ret;
	}

	/**
	 * Recursive method to get the list of classes in the current project
	 * @param ret the list of classes being built
	 * @param dir the directory to analyze
	 * @param base the base directory
	 */
	private static SortedSet<String> getProjectClasses(ClassLoader loader, SortedSet<String> ret, File dir, String base) {
		for (File f : dir.listFiles()) {
			if(f.isDirectory()) getProjectClasses(loader, ret, f, base);
			else if(f.isFile() && f.getName().endsWith(".class")) {
				final String fullName = f.getAbsolutePath();
				final String className = fullName.substring(base.length()+1, fullName.length() - 6).replace(File.separatorChar, '.');

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
