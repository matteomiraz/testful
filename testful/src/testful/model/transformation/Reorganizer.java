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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.OperationResult.Status;
import testful.model.Reference;
import testful.model.ResetRepository;
import testful.model.StaticValue;
import testful.model.Test;

/**
 * Reorganize a test, sorting the operations in a unique way (if possible).
 * The test must either do not contain any faulty operation or have operations annotated with
 * the {@link OperationResult} information.
 * @author matteo
 */
public class Reorganizer implements TestTransformation {

	public static final Reorganizer singleton = new Reorganizer();

	/**
	 * Reorganize a test, sorting the operations in a unique way (if possible).
	 * The test must either do not contain any faulty operation or have operations annotated with
	 * the {@link OperationResult} information.
	 */
	@Override
	public Test perform(Test t) {
		// contains the reversed clustered version of the test
		LinkedList<ReorganizeOperationSet> builder = new LinkedList<ReorganizeOperationSet>();

		int n = 0;
		for(Operation op : t.getTest()) {
			final OperationResult result = (OperationResult) op.getInfo(OperationResult.KEY);
			if(result == null || result.getStatus() != Status.PRECONDITION_ERROR) {
				reorganizeAdd(builder, op);
				n++;
			}
		}

		int i = 0;
		Operation[] newOps = new Operation[n];
		Iterator<ReorganizeOperationSet> iter = builder.descendingIterator();
		while(iter.hasNext()) {
			Collection<Operation> next = iter.next();

			Operation[] array = next.toArray(new Operation[next.size()]);
			Arrays.sort(array, ReorganizeOperationComparator.singleton);

			for(Operation op : array)
				newOps[i++] = op;
		}

		return new Test(t.getCluster(), t.getReferenceFactory(), newOps);
	}

	private static void reorganizeAdd(List<ReorganizeOperationSet> builder, Operation op) {
		ListIterator<ReorganizeOperationSet> iter = builder.listIterator();

		while(iter.hasNext()) {
			ReorganizeOperationSet next = iter.next();

			if(!next.isSwappable(op)) {
				// Add op before next!

				// go before next
				iter.previous();

				if(iter.hasPrevious()) {
					iter.previous().add(op);
				} else {
					iter.add(new ReorganizeOperationSet(op));
				}

				return;
			}

			// reached the first element
			if(!iter.hasNext()) {
				next.add(op);
				return;
			}
		}

		// the list is empty: adding a new element!
		iter.add(new ReorganizeOperationSet(op));
	}

	private static final class ReorganizeOperationSet extends ArrayList<Operation> {

		private static final long serialVersionUID = -6567096691463221075L;

		private boolean containsResetRepository = false;
		private boolean containsFaulty = false;

		private final BitSet defs = new BitSet();
		private final BitSet uses = new BitSet();

		public ReorganizeOperationSet(Operation op) {
			super();
			add(op);
		}

		@Override
		public boolean add(Operation e) {
			boolean added = super.add(e);

			if(added) {

				if(e instanceof ResetRepository) {
					containsResetRepository = true;

				} else {
					final OperationResult opResult = (OperationResult) e.getInfo(OperationResult.KEY);
					containsFaulty = opResult != null && opResult.getStatus() == Status.POSTCONDITION_ERROR;

					defs.or(e.getDefsBitset());
					uses.or(e.getUsesBitset());
				}
			}

			return added;
		}

		public boolean isSwappable(Operation op) {
			if(containsResetRepository || op instanceof ResetRepository)
				return false;

			final OperationResult opResult = (OperationResult) op.getInfo(OperationResult.KEY);
			if(containsFaulty || (opResult != null && opResult.getStatus() == Status.POSTCONDITION_ERROR))
				return false;

			// check def-def or use-def
			BitSet tmp = (BitSet) defs.clone();
			tmp.or(uses);
			tmp.and(op.getDefsBitset());
			if(!tmp.isEmpty()) return false;

			// check def-use
			tmp = (BitSet) defs.clone();
			tmp.and(op.getUsesBitset());
			if(!tmp.isEmpty()) return false;

			return true;
		}
	}

	private static class ReorganizeOperationComparator implements Comparator<Operation> {
		static final ReorganizeOperationComparator singleton = new ReorganizeOperationComparator();

		@Override
		public int compare(Operation o1, Operation o2) {
			if(o1 == o2) return 0;

			String name1 = o1.getClass().getCanonicalName();
			String name2 = o2.getClass().getCanonicalName();
			int cmp = name1.compareTo(name2);
			if(cmp != 0) return cmp;

			if(o1 instanceof AssignConstant) {
				AssignConstant c1 = (AssignConstant) o1;
				AssignConstant c2 = (AssignConstant) o2;

				cmp = compare(c1.getValue(), c2.getValue());
				if(cmp != 0) return cmp;

				cmp = compareFull(c1.getTarget(), c2.getTarget());
				if(cmp != 0) return cmp;

				return 1;

			} else if(o1 instanceof AssignPrimitive) {
				AssignPrimitive p1 = (AssignPrimitive) o1;
				AssignPrimitive p2 = (AssignPrimitive) o2;

				cmp = compare(p1.getValue(), p2.getValue());
				if(cmp != 0) return cmp;

				cmp = compareFull(p1.getTarget(), p2.getTarget());
				if(cmp != 0) return cmp;

				return 1;

			} else if(o1 instanceof CreateObject) {
				CreateObject c1 = (CreateObject) o1;
				CreateObject c2 = (CreateObject) o2;

				name1 = c1.getConstructor().getFullConstructorName();
				name2 = c2.getConstructor().getFullConstructorName();
				cmp = name1.compareTo(name2);
				if(cmp != 0) return cmp;

				cmp = compare(c1.getParams(), c2.getParams());
				if(cmp != 0) return cmp;

				cmp = compareFull(c1.getTarget(), c2.getTarget());
				if(cmp != 0) return cmp;

				cmp = compareFull(c1.getParams(), c2.getParams());
				if(cmp != 0) return cmp;

				return 1;

			} else if(o1 instanceof Invoke) {
				Invoke i1 = (Invoke) o1;
				Invoke i2 = (Invoke) o2;

				cmp = compare(i1.getThis(), i2.getThis());

				name1 = i1.getMethod().getFullMethodName();
				name2 = i2.getMethod().getFullMethodName();
				cmp = name1.compareTo(name2);
				if(cmp != 0) return cmp;

				cmp = compare(i1.getTarget(), i2.getTarget());
				if(cmp != 0) return cmp;

				cmp = compare(i1.getParams(), i2.getParams());
				if(cmp != 0) return cmp;

				cmp = compareFull(i1.getThis(), i2.getThis());
				if(cmp != 0) return cmp;

				cmp = compareFull(i1.getTarget(), i2.getTarget());
				if(cmp != 0) return cmp;

				cmp = compareFull(i1.getParams(), i2.getParams());
				if(cmp != 0) return cmp;

				return 1;
			}

			cmp = o1.toString().compareTo(o2.toString());
			if(cmp != 0) return cmp;

			return 1;
		}

		private int compare(final Reference[] params1, final Reference[] params2) {
			int cmp;
			if(params1.length != params2.length) return params1.length - params2.length;
			for(int i = 0; i < params1.length; i++) {
				cmp = compare(params1[i], params2[i]);
				if(cmp != 0) return cmp;
			}
			return 0;
		}

		private int compareFull(final Reference[] params1, final Reference[] params2) {
			int cmp;
			if(params1.length != params2.length) return params1.length - params2.length;
			for(int i = 0; i < params1.length; i++) {
				cmp = compareFull(params1[i], params2[i]);
				if(cmp != 0) return cmp;
			}
			return 0;
		}

		private int compare(final Reference r1, final Reference r2) {
			if(r1 == null) {
				if(r2 == null) return 0;
				return -1;
			} else {
				if(r2 == null) return 1;

				return r1.getClazz().getClassName().compareTo(r2.getClazz().getClassName());
			}
		}

		private int compareFull(final Reference r1, final Reference r2) {
			if(r1 == null) {
				if(r2 == null) return 0;
				return -1;
			} else {
				if(r2 == null) return 1;

				int cmp = r1.getClazz().getClassName().compareTo(r2.getClazz().getClassName());
				if(cmp != 0) return cmp;

				return r1.getPos() - r2.getPos();
			}
		}

		private int compare(final StaticValue value1, final StaticValue value2) {
			if(value1 == null) {
				if(value2 == null)
					return 0;
				else
					return  1;
			}

			if(value2 == null) return -1;

			return (value1.getDeclaringClass().getClassName() + "." + value1.getName()).compareTo((value2.getDeclaringClass().getClassName() + "." + value2.getName()));
		}

		private int compare(final Serializable value1, final Serializable value2) {
			if(value1 == null) {
				if(value2 == null)
					return 0;
				else
					return  1;
			}

			if(value2 == null) return -1;

			return value1.toString().compareTo(value2.toString());
		}
	}
}
