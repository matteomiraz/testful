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


package testful.model;

import java.util.Set;

/**
 * Reset the repository (i.e. put all references to null)
 * 
 * @author matteo
 */
public final class ResetRepository extends Operation {

	private static final long serialVersionUID = -8362944709273921763L;

	public static final ResetRepository singleton = new ResetRepository();

	private ResetRepository() {
		super();
	}

	@Override
	public int hashCode() {
		return 31;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj || obj instanceof ResetRepository;
	}

	@Override
	public String toString() {
		return "init()";
	}

	@Override
	public Operation adapt(TestCluster cluster, ReferenceFactory refFactory) {
		return this;
	}

	@Override
	protected Set<Reference> calculateDefs() {
		throw new NullPointerException("Cannot calculate defs for ResetRepository!");
	}

	@Override
	protected Set<Reference> calculateUses() {
		return emptyRefsSet;
	}

	@Override
	public Operation clone() {
		return singleton;
	}
}
