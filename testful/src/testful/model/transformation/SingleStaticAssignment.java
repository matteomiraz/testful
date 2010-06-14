/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010 Matteo Miraz
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

package testful.model.transformation;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.ReferenceFactory;
import testful.model.ResetRepository;
import testful.model.Test;

/**
 * Modifies the test, using the Single Static Assignment (SSA) principle.
 * @author matteo
 */
public class SingleStaticAssignment implements TestTransformation {

	public static final SingleStaticAssignment singleton = new SingleStaticAssignment();

	/** returns a Single Static Assignment of the test */
	@Override
	public Test perform(Test orig) {
		Operation[] test = orig.getTest();

		// for each clazz, counts the number of assignments (i.e. the number of required references)
		Map<Clazz, Integer> refs = new HashMap<Clazz, Integer>();
		for(Operation op : test) {

			Reference t = null;
			if(op instanceof AssignConstant)
				t = ((AssignConstant) op).getTarget();
			else if(op instanceof AssignPrimitive)
				t = ((AssignPrimitive) op).getTarget();
			else if(op instanceof CreateObject)
				t = ((CreateObject) op).getTarget();
			else if(op instanceof Invoke)
				t = ((Invoke) op).getTarget();

			if(t != null) {
				Integer num = refs.get(t.getClazz());
				if(num == null) num = 1;
				else num++;
				refs.put(t.getClazz(), num);
			}
		}

		/** the new reference factory */
		ReferenceFactory refFactory = new ReferenceFactory(refs);

		Map<Clazz, Deque<Reference>> newRefs = new HashMap<Clazz, Deque<Reference>>();
		for(Clazz c : refs.keySet()) {
			Deque<Reference> d = new LinkedList<Reference>();
			for(Reference r : refFactory.getReferences(c)) d.add(r);
			newRefs.put(c, d);
		}

		/** for each original reference (key) store the new reference to use (value) */
		Map<Reference, Reference> convert = new HashMap<Reference, Reference>();

		Operation[] newTest = new Operation[test.length];
		for(int i = 0; i < test.length; i++) {
			Operation op = test[i];

			if(op instanceof AssignConstant) {
				op = new AssignConstant(ssaCreate(newRefs, convert, ((AssignConstant) op).getTarget()), ((AssignConstant) op).getValue());

			} else if(op instanceof AssignPrimitive) {
				op = new AssignPrimitive(ssaCreate(newRefs, convert, ((AssignPrimitive) op).getTarget()), ((AssignPrimitive) op).getValue());

			} else if(op instanceof CreateObject) {
				CreateObject co = (CreateObject) op;
				Reference[] params = ssaConvert(convert, co.getParams());
				Reference target = ssaCreate(newRefs, convert, co.getTarget());
				op = new CreateObject(target, co.getConstructor(), params);

			} else if(op instanceof Invoke) {
				Invoke in = (Invoke) op;
				Reference _this = ssaConvert(convert, in.getThis());
				Reference[] params = ssaConvert(convert, in.getParams());
				Reference target = ssaCreate(newRefs, convert, in.getTarget());
				op = new Invoke(target, _this, in.getMethod(), params);
			} else if(op instanceof ResetRepository) {
				convert = new HashMap<Reference, Reference>();
			}

			newTest[i] = op;
		}

		return new Test(orig.getCluster(), refFactory, newTest);
	}

	private static Reference ssaCreate(Map<Clazz, Deque<Reference>> newRefs, Map<Reference, Reference> convert, Reference ref) {
		if(ref == null) return null;

		Reference newRef = newRefs.get(ref.getClazz()).remove();
		convert.put(ref, newRef);
		return newRef;
	}

	private static Reference ssaConvert(Map<Reference, Reference> convert, Reference ref) {
		if(ref == null) return null;

		Reference newRef = convert.get(ref);
		if(newRef == null) throw new NullPointerException("Running SSA on a test not valid (run simplify() )");
		return newRef;
	}

	private static Reference[] ssaConvert(Map<Reference, Reference> convert, Reference[] r) {
		Reference[] ret = new Reference[r.length];
		for(int i = 0; i < r.length; i++)
			ret[i] = ssaConvert(convert, r[i]);
		return ret;
	}

}
