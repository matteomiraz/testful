package testful.model;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Queue;

public class Test implements Serializable {

	private static final long serialVersionUID = 1591209932563881988L;

	/** the test cluster */
	private final TestCluster cluster;
	/** the reference factory */
	private final ReferenceFactory refFactory;
	/** the test as sequence of operation */
	private final Operation[] test;

	private final int hashCode;

	public Test(TestCluster cluster, ReferenceFactory refFactory, Operation[] test) {
		this.cluster = cluster;
		this.refFactory = refFactory;
		this.test = test;
		hashCode = Arrays.hashCode(test);
	}

	public Test(Test ... parts) throws Exception {
		if(parts == null) throw new Exception("Cannot join tests: null");
		if(parts.length == 0) throw new Exception("Cannot join tests: 0 test provided!");

		cluster = parts[0].cluster;

		// TBD: it is likely that different tests have different reference factory!
		refFactory = parts[0].refFactory;

		int len = 0;
		for(Test other : parts) {
			if(!cluster.equals(other.cluster) || !refFactory.equals(other.refFactory)) throw new Exception("Incompatible tests");

			len += 1 + other.test.length;
		}

		int i = 0;
		test = new Operation[len - 1];
		for(Test other : parts) {
			if(i > 0) test[i++] = ResetRepository.singleton;
			for(Operation op : other.test)
				test[i++] = op.adapt(cluster, refFactory);
		}
		hashCode = Arrays.hashCode(test);
	}

	public TestCluster getCluster() {
		return cluster;
	}

	public Operation[] getTest() {
		return test;
	}

	public ReferenceFactory getReferenceFactory() {
		return refFactory;
	}

	public Test join(Test ... others) throws Exception {
		Test[] tmp = new Test[others.length + 1];

		int i = 0;
		tmp[i++] = this;
		for(Test t : others)
			tmp[i++] = t;

		return new Test(tmp);
	}

	public void write(OutputStream outputStream) throws IOException {
		ObjectOutput write = null;
		try {
			write = new ObjectOutputStream(outputStream);
			write.writeObject(this);
		} finally {
			if(write != null) write.close();
			else outputStream.close();
		}
	}

	/**
	 * Ensures that there are no duplicate operations.<br>
	 * after this method holds: (\forall int i; 0 <= i && i < test.length;
	 * (\forall int j; i < j && j < test.length; test[i] != test[j] ))
	 */
	public void ensureNoDuplicateOps() {
		ensureNoDuplicateOps(test);
	}


	/**
	 * Ensures that there are no duplicate operations.<br>
	 * after this method holds: (\forall int i; 0 <= i && i < test.length;
	 * (\forall int j; i < j && j < test.length; test[i] != test[j] ))
	 */
	public static void ensureNoDuplicateOps(Operation[] test) {
		IdentityHashMap<Operation, Operation> map = new IdentityHashMap<Operation, Operation>();

		for(int i = 0; i < test.length; i++) {
			if(map.containsKey(test[i])) test[i] = test[i].clone();

			map.put(test[i], test[i]);
		}
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Test)) return false;

		return Arrays.equals(test, ((Test) obj).test);
	}

	public static final Comparator<Test> sizeComparator = new SizeComparator();

	private static class SizeComparator implements Comparator<Test>, Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public int compare(Test o1, Test o2) {
			int ret = o1.getTest().length - o2.getTest().length;
			if(ret == 0 && o1 != o2) return 1;
			return ret;
		}
	}

	/**
	 * Split the test into its independent parts (identified by ResetRepository
	 * operation)
	 * 
	 * @return the collection of independent tests
	 */
	public Collection<Test> split() {
		Collection<Test> ret = new ArrayList<Test>();

		List<Operation> ops = new ArrayList<Operation>();
		for(Operation op : test)
			if(op instanceof ResetRepository) {
				if(!ops.isEmpty()) ret.add(new Test(cluster, refFactory, ops.toArray(new Operation[ops.size()])));
			} else ops.add(op);

		if(!ops.isEmpty()) ret.add(new Test(cluster, refFactory, ops.toArray(new Operation[ops.size()])));

		return ret;
	}

	/**
	 * Returns an equivalent copy of the test, in which useless operations are modified,
	 * removing useless assignments.<br>
	 * For example, the operation <code>target = foo.bar()</code> is modified in
	 * <code>foo.bar()</code> if <code>target</code> is never used in subsequent operations
	 * @return a modified copy of the test
	 */
	public Test removeUselessDefs() {
		Deque<Operation> ret = new LinkedList<Operation>();

		Clazz cut = cluster.getCut();

		// usedReference[i] == true <==> exist a live use of i between the current point and the end of the test
		boolean[] usedReference = new boolean[refFactory.getReferences().length];
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

		return new Test(cluster, refFactory, ret.toArray(new Operation[ret.size()]));
	}

	/**
	 * Returns a simplified copy of the test, removing invalid operations (statically analyzed)
	 * @return a simplified copy of the test
	 */
	public Test simplify() {
		List<Operation> ops = new ArrayList<Operation>(test.length);
		Reference[] refs = getReferenceFactory().getReferences();

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

				if(!simplifyIsInvokable(co.getConstructor().getParameterTypes(), co.getParams(), initialized)) continue;

				simplifyAddNullRef(ops, co.getParams(), initialized, initializedNull);

				if(co.getTarget() != null)
					initialized[co.getTarget().getId()] = true;

				ops.add(op);

			} else if(op instanceof Invoke) {
				Invoke in = (Invoke) op;

				if(!simplifyIsInvokable(in.getMethod().getParameterTypes(), in.getParams(), initialized)) continue;

				if(in.getThis() != null && !initialized[in.getThis().getId()]) continue;

				simplifyAddNullRef(ops, in.getParams(), initialized, initializedNull);

				if(in.getTarget() != null)
					initialized[in.getTarget().getId()] = true;

				ops.add(op);

			} else if(op instanceof ResetRepository) {
				for(int i = 0; i < initialized.length; i++) initialized[i] = false;
				for(int i = 0; i < initializedNull.length; i++) initializedNull[i] = false;

				ops.add(op);
			}
		}

		return new Test(getCluster(), getReferenceFactory(), ops.toArray(new Operation[ops.size()]));
	}

	/** checks if all the parameters has been initialized */
	private boolean simplifyIsInvokable(Clazz[] paramsType, Reference[] params, boolean[] initialized) {
		for(int i = 0; i < paramsType.length; i++)
			if(paramsType[i] instanceof PrimitiveClazz && // the parameter is primitive
					!((PrimitiveClazz) paramsType[i]).isClass() && // and the method uses the primitive version
					!initialized[params[i].getId()]) // but the reference used is null
				return false;

		return true;
	}

	/** if a reference is used as parameter, but it is not initialized, insert ref = null */
	private void simplifyAddNullRef(List<Operation> ops, Reference[] params, boolean[] initialized, boolean[] nullInitialized) {
		for(int i = 0; i < params.length; i++) {
			if(!initialized[params[i].getId()] &&
					!nullInitialized[params[i].getId()]) {

				ops.add(new AssignConstant(params[i], null));
				nullInitialized[params[i].getId()] = true;
			}
		}
	}

	/** returns a Single Static Assignment of the test */
	public Test getSSA() {
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

		return new Test(getCluster(), refFactory, newTest);
	}

	private Reference ssaCreate(Map<Clazz, Deque<Reference>> newRefs, Map<Reference, Reference> convert, Reference ref) {
		if(ref == null) return null;

		Reference newRef = newRefs.get(ref.getClazz()).remove();
		convert.put(ref, newRef);
		return newRef;
	}

	private Reference ssaConvert(Map<Reference, Reference> convert, Reference ref) {
		if(ref == null) return null;

		Reference newRef = convert.get(ref);
		if(newRef == null) throw new NullPointerException("Running SSA on a test not valid (run simplify() )");
		return newRef;
	}

	private Reference[] ssaConvert(Map<Reference, Reference> convert, Reference[] r) {
		Reference[] ret = new Reference[r.length];
		for(int i = 0; i < r.length; i++)
			ret[i] = ssaConvert(convert, r[i]);
		return ret;
	}

	/** sort references */
	public Test sortReferences() {
		Operation[] ops = getTest().clone();

		// for each class, stores unused references
		Map<Clazz, Queue<Reference>> freeRefs = sortReferencesGetFreeRefs();

		int numRefs = getReferenceFactory().getReferences().length;

		// new references: ref will be replaced with refs[ref.getId()]
		Reference[] refs = new Reference[numRefs];

		for(int i = 0; i < ops.length; i++) {
			if(ops[i] instanceof AssignConstant) {
				AssignConstant o = (AssignConstant) ops[i];
				ops[i] = new AssignConstant(sortReferences(refs, freeRefs, o.getTarget()), o.getValue());

			} else if(ops[i] instanceof AssignPrimitive) {
				AssignPrimitive o = (AssignPrimitive) ops[i];
				ops[i] = new AssignPrimitive(sortReferences(refs, freeRefs, o.getTarget()), o.getValue());

			} else if(ops[i] instanceof CreateObject) {
				CreateObject o = (CreateObject) ops[i];
				ops[i] = new CreateObject(sortReferences(refs, freeRefs, o.getTarget()), o.getConstructor(), sortReferences(refs, freeRefs, o.getParams()));

			} else if(ops[i] instanceof Invoke) {
				Invoke o = (Invoke) ops[i];
				ops[i] = new Invoke(sortReferences(refs, freeRefs, o.getTarget()), sortReferences(refs, freeRefs, o.getThis()), o.getMethod(), sortReferences(refs, freeRefs, o.getParams()));

			} else if(ops[i] instanceof ResetRepository) {
				freeRefs = sortReferencesGetFreeRefs();
				refs = new Reference[numRefs];

			}
		}

		return new Test(getCluster(), getReferenceFactory(), ops);
	}

	private Map<Clazz, Queue<Reference>> sortReferencesGetFreeRefs() {
		Map<Clazz, Queue<Reference>> freeRefs = new HashMap<Clazz, Queue<Reference>>();
		for(Clazz c : getCluster().getCluster()) {
			c = c.getReferenceClazz();

			final Reference[] refs = getReferenceFactory().getReferences(c);

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

	public Test reorganize() {
		// contains the reversed clustered version of the test
		LinkedList<ReorganizeOperationSet> builder = new LinkedList<ReorganizeOperationSet>();

		for(Operation op : test)
			reorganizeAdd(builder, op);

		int i = 0;
		Operation[] newOps = new Operation[test.length];
		Iterator<ReorganizeOperationSet> iter = builder.descendingIterator();
		while(iter.hasNext()) {
			Collection<Operation> next = iter.next();

			Operation[] array = next.toArray(new Operation[next.size()]);
			Arrays.sort(array, ReorganizeOperationComparator.singleton);

			for(Operation op : array)
				newOps[i++] = op;
		}

		return new Test(cluster, refFactory, newOps);
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

				if(e instanceof ResetRepository)
					containsResetRepository = true;

				else {
					defs.or(e.getDefsBitset());
					uses.or(e.getUsesBitset());
				}
			}

			return added;
		}

		public boolean isSwappable(Operation op) {
			if(containsResetRepository || op instanceof ResetRepository)
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
				return cmp;

			} else if(o1 instanceof AssignPrimitive) {
				AssignPrimitive p1 = (AssignPrimitive) o1;
				AssignPrimitive p2 = (AssignPrimitive) o2;

				cmp = compare(p1.getValue(), p2.getValue());
				if(cmp != 0) return cmp;

				cmp = compareFull(p1.getTarget(), p2.getTarget());
				return cmp;

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
				return cmp;

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
				return cmp;
			}

			return o1.toString().compareTo(o2.toString());
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Test for class ").append(cluster.getCut().getClassName()).append("\n");

		for(Operation op : test) {

			Iterator<OperationInformation> infos = op.getInfos();
			if(infos.hasNext()) sb.append("\n");
			while(infos.hasNext())
				sb.append("  //").append(infos.next().toString()).append("\n");

			sb.append("  ").append(op.toString()).append(";\n");
		}

		return sb.toString();
	}
}
