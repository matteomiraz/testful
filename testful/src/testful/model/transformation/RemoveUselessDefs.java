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
import java.util.LinkedList;

import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.Reference;
import testful.model.ResetRepository;
import testful.model.Test;

/**
 * Returns an equivalent copy of the test, in which useless operations are modified,
 * removing useless assignments.<br>
 * For example, the operation <code>target = foo.bar()</code> is modified in
 * <code>foo.bar()</code> if <code>target</code> is never used in subsequent operations
 * @author matteo
 */
public class RemoveUselessDefs implements TestTransformation {

	public static final RemoveUselessDefs singleton = new RemoveUselessDefs();

	@Override
	public Test perform(Test t) {
		Operation[] test = t.getTest();

		Deque<Operation> ret = new LinkedList<Operation>();

		Clazz cut = t.getCluster().getCut();

		// usedReference[i] == true <==> exist a live use of i between the current point and the end of the test
		boolean[] usedReference = new boolean[t.getReferenceFactory().getReferences().length];
		for(int j = 0; j < usedReference.length; j++) usedReference[j] = false;

		for(int i = test.length - 1; i >= 0; i--) {
			Operation op = test[i];

			if(op instanceof ResetRepository) {
				for(int j = 0; j < usedReference.length; j++) usedReference[j] = false;
				ret.addFirst(op);
				continue;
			}

			if(op instanceof AssignConstant) {
				AssignConstant ac = (AssignConstant) op;
				Reference target = ac.getTarget();

				if(target == null) op = null;
				else if(target.getClazz() != cut && !usedReference[target.getId()])
					op = null;

			} else if(op instanceof AssignPrimitive) {
				AssignPrimitive ap = (AssignPrimitive) op;
				Reference target = ap.getTarget();

				if(target == null) op = null;
				else if(target.getClazz() != cut && !usedReference[target.getId()])
					op = null;

			} else if(op instanceof CreateObject) {
				CreateObject co = (CreateObject) op;
				Reference target = co.getTarget();

				if(target != null && target.getClazz() != cut && !usedReference[target.getId()])
					op = new CreateObject(null, co.getConstructor(), co.getParams());

			} else if(op instanceof Invoke) {
				Invoke in = (Invoke) op;
				Reference target = in.getTarget();

				if(target != null && target.getClazz() != cut && !usedReference[target.getId()])
					op = new Invoke(null, in.getThis(), in.getMethod(), in.getParams());

			}

			if(op != null) {
				ret.addFirst(op);

				for(Reference r : op.getUses())
					usedReference[r.getId()] = true;
			}
		}

		return new Test(t.getCluster(), t.getReferenceFactory(), ret.toArray(new Operation[ret.size()]));
	}

}
