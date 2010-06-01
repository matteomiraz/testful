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

import org.kohsuke.args4j.Option;

public interface IConfigGeneration extends IConfigCut {

	/**
	 * Returns the class under test
	 * @return the class under test
	 */
	@Override
	public String getCut();

	/**
	 * Returns true if I have to reload classes before each run (reinitialize static fields)
	 * @return true if I have to reload classes before each run (reinitialize static fields)
	 */
	public boolean isReload();

	/**
	 * Returns the directory in which output tests will be put (e.g. $HOME/workspace/project/genTests/ )
	 * @return the directory in which output tests will be put
	 */
	public File getDirGeneratedTests();

	/**
	 * How much time do I have? (in seconds)
	 * @return the amount of time for generating the test
	 */
	public int getTime();

	/**
	 * Can I use caching mechanism to speed up the evaluation process?
	 * @return true if I can use caching mechanism to speed up the evaluation process
	 */
	public boolean isCache();

	/**
	 * How many variables there are for the each type?
	 * @return the number of variables for each type
	 */
	public int getNumVar();

	/**
	 * Returns how many variables there are for the CUT type
	 * @return the number of variables for the CUT type
	 */
	public int getNumVarCut();

	/**
	 * Returns the maximum length of a test
	 * @return the maximum length of a test
	 */
	public int getMaxTestLen();

	public interface Args4j extends IConfigGeneration, IConfigCut.Args4j {

		/**
		 * Set whether or not I have to reload classes before each run (reinitialize static fields)
		 * @param reload set if I have to reload classes before each run (reinitialize static fields)
		 */
		@Option(required = false, name = "-reload", usage = "Reload classes before each run (reinitialize static fields)")
		public void setReload(boolean reload);

		/**
		 * Sets the directory in which output tests will be put.
		 * It can be both a path relative to the base directory (e.g., "bar" or "../foo/bar")
		 * or an absolute path (e.g., $HOME/workspace/foo/bar")
		 * @param dirGeneratedTests the directory in which output tests will be put.
		 */
		@Option(required = false, name = "-dirTests", usage = "Specify the directory in which generated tests will be put")
		public void setDirGeneratedTests(File dirGeneratedTests);

		/**
		 * Sets the amount of time to generate the test
		 * @param time the amount of time (in seconds)
		 */
		@Option(required = false, name = "-time", usage = "The maximum execution time (in seconds)")
		public void setTime(int time);

		/**
		 * Sets if I can use caching mechanism to speed up the evaluation process
		 * @param cache true if I can use the cache
		 */
		@Option(required = false, name = "-enableCache", usage = "Enable evaluation cache. Notice that it can degrade performances")
		public void setCache(boolean cache);

		/**
		 * Sets how many variables there are for each type
		 * @param numVar the number of variable for each type
		 */
		@Option(required = false, name = "-auxSize", usage = "Number of places in the repository for auxiliary classes")
		public void setNumVar(int numVar);

		/**
		 * Sets the number of variables for the CUT type
		 * @param numVarCut the number of variables for the CUT type
		 */
		@Option(required = false, name = "-cutSize", usage = "Number of places in the repository for the CUT")
		public void setNumVarCut(int numVarCut);

		/**
		 * Sets the maximum length of a test
		 * @param maxTestLen the maximum length of a test
		 */
		@Option(required = false, name = "-testSize", usage = "Maximum test length (n° of invocations)")
		public void setMaxTestLen(int maxTestLen);
	}
}