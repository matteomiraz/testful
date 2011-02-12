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
import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;

/**
 * Stores the configuration of the project being tested.
 *
 * @author matteo
 */
public class ConfigProject implements IConfigProject.Args4j {

	/** the base directory (e.g. $HOME/workspace/project/ ) */
	private File dirBase;

	/** source directory */
	private File dirSource;

	/** directory with classes compiled by the user */
	private File dirCompiled;

	/** the list of the libraries (both jar files and class directories) */
	private List<File> libraries;

	/** directory with classes instrumented */
	private File dirInstrumented;

	public ConfigProject() {
		dirBase = new File(".");

		dirSource = new File("src");
		dirCompiled = new File("bin");
		libraries = new ArrayList<File>();
		dirInstrumented = new File("instrumented");
	}

	public ConfigProject(IConfigProject config) {
		dirBase = config.getDirBase();

		dirSource = config.getDirSource();
		dirCompiled = config.getDirCompiled();
		libraries = new ArrayList<File>();
		libraries.addAll(config.getLibraries());
		dirInstrumented = config.getDirInstrumented();
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#getDirBase()
	 */
	@Override
	public File getDirBase() {
		return dirBase;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#setDirBase(java.io.File)
	 */
	@Override
	public void setDirBase(File dirBase) {
		this.dirBase = dirBase;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.ProjectConfig#getDirSource()
	 */
	@Override
	public File getDirSource() {
		if(!dirSource.isAbsolute()) dirSource = new File(dirBase, dirSource.getPath()).getAbsoluteFile();
		return dirSource;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.ProjectConfig#setDirSource(java.io.File)
	 */
	@Override
	public void setDirSource(File dirSource) {
		this.dirSource = dirSource;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#getDirCompiled()
	 */
	@Override
	public File getDirCompiled() {
		if(!dirCompiled.isAbsolute()) dirCompiled = new File(dirBase, dirCompiled.getPath()).getAbsoluteFile();
		return dirCompiled;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#setDirCompiled(java.io.File)
	 */
	@Override
	public void setDirCompiled(File dirVanilla) {
		dirCompiled = dirVanilla;
	}

	/* (non-Javadoc)
	 * @see testful.IConfigProject#getDirLibraries()
	 */
	@Override
	public List<File> getLibraries() {
		return libraries;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigProject.Args4j#addDirLibrary(java.io.File)
	 */
	@Override
	public void addLibrary(File library) {
		if(!library.isAbsolute()) library = new File(dirBase, library.getPath()).getAbsoluteFile();
		libraries.add(library);
	}

	/**
	 * Sets the list of the libraries (both jar files and class directories)
	 * @param dirLibraries the list of the libraries (both jar files and class directories)
	 */
	public void setLibraries(List<File> dirLibraries) {
		libraries = dirLibraries;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#getDirInstrumented()
	 */
	@Override
	public File getDirInstrumented() {
		if(!dirInstrumented.isAbsolute()) dirInstrumented = new File(dirBase, dirInstrumented.getPath()).getAbsoluteFile();
		return dirInstrumented;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#setDirInstrumented(java.io.File)
	 */
	@Override
	public void setDirInstrumented(File dirInstrumented) {
		this.dirInstrumented = dirInstrumented;
	}


	/* (non-Javadoc)
	 * @see testful.IConfig#validate()
	 */
	@Override
	public void validate() throws CmdLineException {
		// everything is ok!
	}
}