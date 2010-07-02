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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

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
import testful.model.TestCluster;

/**
 * Sort references in a test
 * @author matteo
 */
public class ReferenceSorter implements TestTransformation {

	public static final ReferenceSorter singleton = new ReferenceSorter();

	@Override
	public Test perform(Test t) {
		Operation[] ops = t.getTest().clone();

		// for each class, stores unused references
		Map<Clazz, Queue<Reference>> freeRefs = sortReferencesGetFreeRefs(t.getCluster(), t.getReferenceFactory());

		int numRefs = t.getReferenceFactory().getReferences().length;

		// new references: ref will be replaced with refs[ref.getId()]
		Reference[] refs = new Reference[numRefs];

		for(int i = 0; i < ops.length; i++) {
			if(ops[i] instanceof AssignConstant) {
				AssignConstant o = (AssignConstant) ops[i];
				ops[i] = new AssignConstant(sortReferences(refs, freeRefs, o.getTarget()), o.getValue());
				ops[i].addInfo(o);

			} else if(ops[i] instanceof AssignPrimitive) {
				AssignPrimitive o = (AssignPrimitive) ops[i];
				ops[i] = new AssignPrimitive(sortReferences(refs, freeRefs, o.getTarget()), o.getValue());
				ops[i].addInfo(o);

			} else if(ops[i] instanceof CreateObject) {
				CreateObject o = (CreateObject) ops[i];
				ops[i] = new CreateObject(sortReferences(refs, freeRefs, o.getTarget()), o.getConstructor(), sortReferences(refs, freeRefs, o.getParams()));
				ops[i].addInfo(o);

			} else if(ops[i] instanceof Invoke) {
				Invoke o = (Invoke) ops[i];
				ops[i] = new Invoke(sortReferences(refs, freeRefs, o.getTarget()), sortReferences(refs, freeRefs, o.getThis()), o.getMethod(), sortReferences(refs, freeRefs, o.getParams()));
				ops[i].addInfo(o);

			} else if(ops[i] instanceof ResetRepository) {
				freeRefs = sortReferencesGetFreeRefs(t.getCluster(), t.getReferenceFactory());
				refs = new Reference[numRefs];

			}
		}

		return new Test(t.getCluster(), t.getReferenceFactory(), ops);
	}

	private static Map<Clazz, Queue<Reference>> sortReferencesGetFreeRefs(TestCluster cluster, ReferenceFactory refFactory) {
		Map<Clazz, Queue<Reference>> freeRefs = new HashMap<Clazz, Queue<Reference>>();
		for(Clazz c : cluster.getCluster()) {
			c = c.getReferenceClazz();

			final Reference[] refs = refFactory.getReferences(c);

			if(refs != null) {
				Queue<Reference> q = new LinkedList<Reference>();
				for(Reference r : refs)
					q.add(r);
				freeRefs.put(c, q);
			}
		}
		return freeRefs;
	}

	private static Reference[] sortReferences(Reference[] refs, Map<Clazz, Queue<Reference>> freeRefs, Reference[] r) {
		if(r == null) return null;

		Reference[] ret = new Reference[r.length];

		for(int i = 0; i < ret.length; i++) {
			ret[i] = sortReferences(refs, freeRefs, r[i]);
		}

		return ret;
	}

	private static Reference sortReferences(Reference[] refs, Map<Clazz, Queue<Reference>> freeRefs, Reference r) {
		if(r == null) return null;

		Reference ret = refs[r.getId()];

		if(ret == null) {
			ret = freeRefs.get(r.getClazz().getReferenceClazz()).remove();
			refs[r.getId()] = ret;
		}

		return ret;
	}
}
