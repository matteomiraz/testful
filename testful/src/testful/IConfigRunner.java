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

import java.util.List;

import org.kohsuke.args4j.Option;

public interface IConfigRunner {

	/**
	 * Get the list of remote runners
	 * @return the list of remote runners
	 */
	public List<String> getRemote();

	/**
	 * Check if the local evaluation is enabled
	 * @return true if it is possible to execute tests locally
	 */
	public boolean isLocalEvaluation();

	public interface Args4j extends IConfigRunner {
		@Option(required = false, name = "-remote", multiValued = true, usage = "Use the specified remote evaluator")
		public void addRemote(String remote);

		@Option(required = false, name = "-noLocal", usage = "Do not use local evaluators")
		public void disableLocalEvaluation(boolean disableLocalEvaluation);
	}
}