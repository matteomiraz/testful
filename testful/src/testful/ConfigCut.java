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

public class ConfigCut extends ConfigProject implements IConfigCut.Args4j, IConfigProject.Args4j {

	/** the class under test */
	private String cut;

	public ConfigCut() {
		super();
	}

	public ConfigCut(IConfigProject config) {
		super(config);
	}

	public ConfigCut(IConfigCut config) {
		super(config);
		cut = config.getCut();
	}

	/* (non-Javadoc)
	 * @see testful.IConfigCug#getCut()
	 */
	@Override
	public String getCut() {
		return cut;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigCut.Args4j#setCut(java.lang.String)
	 */
	@Override
	public void setCut(String cut) throws TestfulException {
		if(cut == null || cut.trim().isEmpty()) throw new TestfulException("You must specify the class name");
		this.cut = cut.trim();
	}

}