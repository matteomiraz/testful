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

package testful;

import java.io.File;
import java.util.List;

import org.kohsuke.args4j.Option;

/**
 * Stores information on the project being tested:
 * <ul>
 *   <li>The <b>directory</b> configuration
 *   </li>
 * <li>The <b>logging</b> configuration</li>
 * </ul>
 * @author matteo
 */
public interface IConfigProject extends IConfig {

	/**
	 * Returns the base directory (e.g. $HOME/workspace/project/ )
	 * @return the base directory
	 */
	public File getDirBase();

	/**
	 * Returns the source directory (e.g. $HOME/workspace/project/src/ )
	 * @return the source directory
	 */
	public File getDirSource();

	/**
	 * Returns the compiled directory (e.g. $HOME/workspace/project/bin/ )
	 * @return the compiled directory
	 */
	public File getDirCompiled();

	/**
	 * Returns the list of the libraries (both jar files and class directories).
	 * @return the list of the libraries (both jar files and class directories).
	 */
	public List<File> getLibraries();

	/**
	 * Returns the directory containing instrumented binaries (e.g. $HOME/workspace/project/instrumented/ )
	 * @return the directory containing instrumented binaries
	 */
	public File getDirInstrumented();

	/**
	 * Stores the configuration of the project being tested.
	 * This interface is usable with args4j.
	 * @author matteo
	 */
	public interface Args4j extends IConfigProject {

		/**
		 * Sets the project's base directory (e.g. $HOME/workspace/project/ )
		 * @param dirBase the project's base directory
		 */
		@Option(required = false, name = "-dir", usage = "Specify the project's base directory (default: the current directory)")
		public void setDirBase(File dirBase);

		/**
		 * Sets the directory containing source files.
		 * It can be both a path relative to the base directory (e.g., "bar" or "../foo/bar")
		 * or an absolute path (e.g., $HOME/workspace/foo/bar")
		 * @param dirSource the directory containing source files.
		 */
		@Option(required = false, name = "-dirSource", usage = "Specify the source directory (default: src)")
		public void setDirSource(File dirSource);

		/**
		 * Sets the directory containing compiled files.
		 * It can be both a path relative to the base directory (e.g., "bar" or "../foo/bar")
		 * or an absolute path (e.g., $HOME/workspace/foo/bar")
		 * @param dirCompiled the directory containing compiled files.
		 */
		@Option(required = false, name = "-dirCompiled", usage = "Specify the directory containing compiled files (default: bin)")
		public void setDirCompiled(File dirCompiled);

		/**
		 * Adds a library (either a jar file or a class directory)
		 * @param library the library to add (either a jar file or a class directory)
		 */
		@Option(required = false, multiValued=true, name = "-library", usage = "Adds a library (either a jar file or a class directory)")
		public void addLibrary(File library);

		/**
		 * Sets the directory containing instrumented files.
		 * It can be both a path relative to the base directory (e.g., "bar" or "../foo/bar")
		 * or an absolute path (e.g., $HOME/workspace/foo/bar")
		 * @param dirInstrumented the directory containing instrumented files.
		 */
		@Option(required = false, name = "-dirInstrumented", usage = "Specify the directory with instrumented files (default: instrumented)")
		public void setDirInstrumented(File dirInstrumented);
	}
}