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

import java.util.ArrayList;
import java.util.List;

public class ConfigRunner implements IConfigRunner.Args4j {

	private List<String> remote = new ArrayList<String>();

	private boolean localEvaluation = true;

	/* (non-Javadoc)
	 * @see testful.IConfigRunner#getRemote()
	 */
	@Override
	public List<String> getRemote() {
		return remote;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigRunner.Args4j#addRemote(java.lang.String)
	 */
	@Override
	public void addRemote(String remote) {
		this.remote.add(remote);
	}

	/* (non-Javadoc)
	 * @see testful.IConfigRunner#isLocalEvaluation()
	 */
	@Override
	public boolean isLocalEvaluation() {
		return localEvaluation;
	}

	/* (non-Javadoc)
	 * @see testful.IConfigRunner#disableLocalEvaluation(boolean)
	 */
	@Override
	public void disableLocalEvaluation(boolean disableLocalEvaluation) {
		localEvaluation = !disableLocalEvaluation;
	}
}
