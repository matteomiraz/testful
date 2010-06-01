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


package testful.coverage.whiteBox;

import java.io.Serializable;

public abstract class Condition implements Serializable {

	private static final long serialVersionUID = 5318886334221554655L;
	private final DataUse use1;
	private final DataUse use2;

	public Condition(DataUse use1, DataUse use2) {
		this.use1 = use1;
		this.use2 = use2;
	}

	public Condition(DataUse use) {
		this(use, null);
	}

	public DataUse getUse1() {
		return use1;
	}

	public DataUse getUse2() {
		return use2;
	}
}
