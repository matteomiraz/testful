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

import org.kohsuke.args4j.Option;

public interface IConfigCut extends IConfigProject {

	/**
	 * Returns the class under test
	 * @return the class under test
	 */
	public String getCut();

	public interface Args4j extends IConfigCut, IConfigProject.Args4j {

		/**
		 * Sets the class under test
		 * @param cut the class under test
		 * @throws TestfulException if the name is null or empty
		 */
		@Option(required = true, name = "-cut", usage = "The class to test", metaVar = "full.qualified.ClassName")
		public void setCut(String cut) throws TestfulException;
	}

}