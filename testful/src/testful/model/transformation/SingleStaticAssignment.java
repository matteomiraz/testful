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
 * Modifies the test, using the Single Static Assignment (SSA) principle.<br>
 * It requires that the test does not contain any hidden behavior (use {@link SimplifierDynamic})
 * and requires that each operation uses parameters being initialized, including null parameters (use {@link SimplifierStatic}).
 * @author matteo
 */
public class SingleStaticAssignment implements TestTransformation {

	public static final SingleStaticAssignment singleton = new SingleStaticAssignment();

	/**
	 * Returns a Single Static Assignment of the test.<br>
	 * Before applying this transformation, the test must be transformed using both {@link SimplifierDynamic} and {@link SimplifierStatic}.
	 */
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
				AssignConstant ac = (AssignConstant) op;
				op = new AssignConstant(ssaCreate(newRefs, convert, ac.getTarget()), ac.getValue());
				ac.addInfo(op.getInfos());

			} else if(op instanceof AssignPrimitive) {
				AssignPrimitive ap = (AssignPrimitive) op;
				op = new AssignPrimitive(ssaCreate(newRefs, convert, ap.getTarget()), ap.getValue());
				op.addInfo(ap.getInfos());

			} else if(op instanceof CreateObject) {
				CreateObject co = (CreateObject) op;
				Reference[] params = ssaConvert(convert, co.getParams());
				Reference target = ssaCreate(newRefs, convert, co.getTarget());
				op = new CreateObject(target, co.getConstructor(), params);
				op.addInfo(co.getInfos());

			} else if(op instanceof Invoke) {
				Invoke in = (Invoke) op;
				Reference _this = ssaConvert(convert, in.getThis());
				Reference[] params = ssaConvert(convert, in.getParams());
				Reference target = ssaCreate(newRefs, convert, in.getTarget());
				op = new Invoke(target, _this, in.getMethod(), params);
				op.addInfo(in.getInfos());

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
		if(newRef == null) throw new NullPointerException("Running SSA on a test not valid (run SimplifierStatic) " + ref);
		return newRef;
	}

	private static Reference[] ssaConvert(Map<Reference, Reference> convert, Reference[] r) {
		Reference[] ret = new Reference[r.length];
		for(int i = 0; i < r.length; i++)
			ret[i] = ssaConvert(convert, r[i]);
		return ret;
	}

}
