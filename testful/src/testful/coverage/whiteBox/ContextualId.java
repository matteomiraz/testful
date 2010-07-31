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

import testful.TestFul;

/**
 * Extends traditional identification numbers with the Context ({@link Stack})
 * @author matteo
 */
public class ContextualId implements Serializable {
	private static final long serialVersionUID = -7933747383773429832L;

	private final int id;
	private final Stack context;
	private final int hashCode;

	public ContextualId(int id, Stack context) {
		if(context == null) {
			NullPointerException exc = new NullPointerException("The context must not be null");
			TestFul.debug(exc);
			throw exc;
		}

		this.id = id;
		this.context = context;

		hashCode = (31 + id) * 31 + context.hashCode();
	}

	public int getId() {
		return id;
	}

	public Stack getContext() {
		return context;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;

		if(!(obj instanceof ContextualId)) return false;
		ContextualId other = (ContextualId) obj;

		if(id != other.id) return false;
		if(!context.equals(other.context)) return false;

		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(id);
		sb.append(context.toString());

		return sb.toString();
	}
}

