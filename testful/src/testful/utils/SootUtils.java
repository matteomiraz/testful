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
