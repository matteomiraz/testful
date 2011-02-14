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

import java.io.Serializable;

/**
 * Marks serializable objects that can be uniquely identified (and cached).
 * @author matteo
 */
public interface ISerializable extends Serializable {

	/**
	 * Returns the (ISerializable) identifier of this object, or null if it has not been set yet.
	 * @return the (ISerializable) identifier of this object, or null if it has not been set yet.
	 */
	public String getISerializableIdentifier();

	/**
	 * When the identifier is required, the system sets it by invoking this method with the id of the object as argument.
	 * This must be stored in a <b>transient</b> field, and returned to subsequent invocations to method {@link ISerializable#getISerializableIdentifier()}.
	 * @param id the (ISerializable) identifier of this object.
	 */
	public void setISerializableIdentifier(String id);

}
