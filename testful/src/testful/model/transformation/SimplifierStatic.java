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

import java.util.ArrayList;
import java.util.List;

import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.PrimitiveClazz;
import testful.model.Reference;
import testful.model.ResetRepository;
import testful.model.Test;

/**
 * Statically analyze a test and remove invalid operations
 * @author matteo
 */
public class SimplifierStatic implements TestTransformation {
	public static final SimplifierStatic singleton = new SimplifierStatic();

	/**
	 * Returns a simplified copy of the test, removing invalid operations (statically analyzed)
	 * @return a simplified copy of the test
	 */
	@Override
	public Test perform(Test t) {
		Operation[] test = t.getTest();

		List<Operation> ops = new ArrayList<Operation>(test.length);
		Reference[] refs = t.getReferenceFactory().getReferences();

		/** initialized[i] true if the i-th reference is non-null */
		boolean[] initialized = new boolean[refs.length];

		/** initializedNull[i] true if has been emitted "ref_i = null" */
		boolean[] initializedNull = new boolean[refs.length];

		for(int i = 0; i < initialized.length; i++) initialized[i] = false;

		for(Operation op : test) {
			if(op instanceof AssignConstant) {
				AssignConstant ac = (AssignConstant) op;

				if(ac.getTarget() != null) {
					initialized[ac.getTarget().getId()] = (ac.getValue() != null);
					initializedNull[ac.getTarget().getId()] = (ac.getValue() == null);

					ops.add(op);
				}

			} else if(op instanceof AssignPrimitive) {
				AssignPrimitive ap = (AssignPrimitive) op;

				if(ap.getTarget() != null) {
					initialized[ap.getTarget().getId()] = ap.getValue() != null;
					initializedNull[ap.getTarget().getId()] = ap.getValue() == null;
					ops.add(op);
				}

			} else if(op instanceof CreateObject) {
				CreateObject co = (CreateObject) op;

				if(!checkInitializated(co.getConstructor().getParameterTypes(), co.getParams(), initialized)) continue;

				initializeToNull(ops, co.getParams(), initialized, initializedNull);

				if(co.getTarget() != null)
					initialized[co.getTarget().getId()] = true;

				ops.add(op);

			} else if(op instanceof Invoke) {
				Invoke in = (Invoke) op;

				if(!checkInitializated(in.getMethod().getParameterTypes(), in.getParams(), initialized)) continue;

				if(!in.getMethod().isStatic() && (in.getThis() == null || !initialized[in.getThis().getId()])) continue;

				if(in.getMethod().isStatic()) {
					op = new Invoke(in.getTarget(), null, in.getMethod(), in.getParams());
					op.addInfo(in);
				}

				initializeToNull(ops, in.getParams(), initialized, initializedNull);

				if(in.getTarget() != null)
					initialized[in.getTarget().getId()] = true;

				ops.add(op);

			} else if(op instanceof ResetRepository) {
				for(int i = 0; i < initialized.length; i++) initialized[i] = false;
				for(int i = 0; i < initializedNull.length; i++) initializedNull[i] = false;

				ops.add(op);
			}
		}

		return new Test(t.getCluster(), t.getReferenceFactory(), ops.toArray(new Operation[ops.size()]));
	}

	/** checks if all the parameters has been initialized */
	private static boolean checkInitializated(Clazz[] paramsType, Reference[] params, boolean[] initialized) {
		for(int i = 0; i < paramsType.length; i++)
			if(paramsType[i] instanceof PrimitiveClazz && // the parameter is primitive
					!((PrimitiveClazz) paramsType[i]).isClass() && // and the method uses the primitive version
					!initialized[params[i].getId()]) // but the reference used is null
				return false;

		return true;
	}

	/** if a reference is used as parameter, but it is not initialized, insert ref = null */
	private static void initializeToNull(List<Operation> ops, Reference[] params, boolean[] initialized, boolean[] nullInitialized) {
		for(int i = 0; i < params.length; i++) {
			if(!initialized[params[i].getId()] &&
					!nullInitialized[params[i].getId()]) {

				ops.add(new AssignConstant(params[i], null));
				nullInitialized[params[i].getId()] = true;
			}
		}
	}

}
