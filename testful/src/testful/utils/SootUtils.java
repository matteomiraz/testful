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


package testful.utils;

import java.util.HashSet;
import java.util.Set;

import soot.ArrayType;
import soot.RefType;
import soot.SootClass;
import soot.Type;

public class SootUtils {

	/**
	 * returns true if the value can be assigned to the type (thus, type is equals
	 * or a supertype of value)
	 * 
	 * @param type the type that should host the value
	 * @param value the value to assign to the type
	 * @return true if it is possible to assign the value to the type
	 */
	public static boolean isAssignable(SootClass type, SootClass value) {
		Set<SootClass> toConsider = new HashSet<SootClass>();
		Set<SootClass> all = new HashSet<SootClass>();

		all.add(value);
		toConsider.add(value);

		while(!toConsider.isEmpty()) {
			SootClass elem = toConsider.iterator().next();

			if(elem == type) return true;

			toConsider.remove(elem);
			if(elem.hasSuperclass()) {
				SootClass superElem = elem.getSuperclass();
				if(all.add(superElem)) toConsider.add(superElem);
			}

			for(SootClass interf : elem.getInterfaces())
				if(all.add(interf)) toConsider.add(interf);
		}

		return false;
	}
	
	public static boolean isReference(Type t) {
		while(t instanceof ArrayType)
			t = ((ArrayType)t).getElementType();

		return t instanceof RefType;
	}


}
