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

/**
 * A Simple goto.
 * 
 * @author matteo
 */
public class EdgeDirect extends Edge {

	private static final long serialVersionUID = 8996745596858488180L;

	public EdgeDirect(Block from) {
		super(from);
	}

	public static void create(Block from, Block to) {
		EdgeDirect e = new EdgeDirect(from);
		e.setTo(to);
	}

}
