/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
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

package testful.runner;

import java.rmi.RemoteException;

/**
 * Represents a type of information, and allows to find its elements
 * @author matteo
 */
public interface DataType {

	/**
	 * Returns the name of this type of data
	 * @return the name of this type of data
	 */
	public String getName();

	/**
	 * Returns an extra info
	 * @param id the id of the information
	 * @return the payload containing the information
	 */
	public byte[] getData(String id) throws RemoteException;

}
