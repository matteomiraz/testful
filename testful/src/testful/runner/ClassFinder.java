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

package testful.runner;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Allows one to retrieve the bytecode of a class.
 *
 * @author matteo
 */
public interface ClassFinder extends Remote {

	public String getKey() throws RemoteException;

	public byte[] getClass(String name) throws ClassNotFoundException, RemoteException;

	/**
	 * Returns the list of inner (nested) classes in a given class.
	 * @param fullClassName the name of the class (using the File convention: full.package.Name$InnerClass)
	 * @return the list of the name of the inner classes
	 */
	public List<String> getInnerClasses(final String fullClassName) throws ClassNotFoundException, RemoteException;
}
