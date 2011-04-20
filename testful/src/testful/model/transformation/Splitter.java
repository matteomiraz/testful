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

package testful.model.transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.coverage.CoverageTestExecutor;
import testful.coverage.TrackerDatum;
import testful.model.AssignConstant;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.MethodInformation;
import testful.model.MethodInformation.Kind;
import testful.model.MethodInformation.ParameterInformation;
import testful.model.Operation;
import testful.model.OperationPosition;
import testful.model.OptimalTestCreator;
import testful.model.PrimitiveClazz;
import testful.model.Reference;
import testful.model.ReferenceFactory;
import testful.model.ResetRepository;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestCoverage;
import testful.runner.DataFinder;
import testful.runner.RunnerPool;
import testful.utils.ElementManager;

/**
 * Splits a test into smaller tests.
 *
 * @author matteo
 */
@SuppressWarnings("unchecked")
public class Splitter {

	private static Logger logger = Logger.getLogger("testful.model");

	public static interface Listener {
		public void notify(TestCluster cluster, ReferenceFactory refFactory, Operation[] test);
	}

	private final Collection<Listener> listener = new ArrayList<Listener>();

	public void register(Listener l) {
		listener.add(l);
	}

	/**
	 * Splits a test into smaller, independent, parts
	 * @param splitObservers if true, also splits observers
	 * @param test the test to analyze
	 * @return the list of independent parts of the test
	 */
	public static List<Test> split(boolean splitObservers, final Test test) {

		final List<Test> res = new ArrayList<Test>();
		Splitter splitter = new Splitter(splitObservers, test.getCluster(), test.getReferenceFactory(), new Listener() {
			@Override
			public void notify(TestCluster cluster, ReferenceFactory refFactory, Operation[] ops) {
				Test t;
				t = new Test(cluster, refFactory, ops);
				t = RemoveUselessDefs.singleton.perform(t);
				res.add(t);
			}
		});

		test.ensureNoDuplicateOps();
		for (Operation op : test.getTest())
			splitter.analyze(op);
		splitter.flush();

		simplify(res);

		return res;
	}

	public static Test splitAndMerge(final Test test) {
		try {
			final SortedSet<Operation> set = new TreeSet<Operation>(OperationPosition.orderComparator);
			Splitter splitter = new Splitter(true, test.getCluster(), test.getReferenceFactory(), new Listener() {
				@Override
				public void notify(TestCluster cluster, ReferenceFactory refFactory, Operation[] ops) {
					for (Operation op : ops)
						set.add(op);
				}
			});

			test.ensureNoDuplicateOps();
			for (Operation op : test.getTest())
				splitter.analyze(op);
			splitter.flush();

			Test ret;
			ret = new Test(test.getCluster(), test.getReferenceFactory(), set.toArray(new Operation[set.size()]));
			ret = RemoveUselessDefs.singleton.perform(ret);

			return ret;
		} catch (Throwable e) {
			TestFul.debug(e);
			logger.log(Level.FINE, "Error during split and merge: " + e, e);
			return test;
		}
	}

	public static Test splitAndMinimize(final Test orig, DataFinder finder, boolean reloadClasses, TrackerDatum ... data) {

		final List<Test> res = new ArrayList<Test>();
		Splitter splitter = new Splitter(true, orig.getCluster(), orig.getReferenceFactory(), new Listener() {
			@Override
			public void notify(TestCluster cluster, ReferenceFactory refFactory, Operation[] ops) {
				res.add(new Test(cluster, refFactory, ops));
			}
		});

		orig.ensureNoDuplicateOps();
		for (Operation op : orig.getTest())
			splitter.analyze(op);
		splitter.flush();

		simplify(res);

		Map<Test, Future<ElementManager<String, CoverageInformation>>> futures = new HashMap<Test, Future<ElementManager<String,CoverageInformation>>>();
		for(Test t : res)
			futures.put(t, RunnerPool.getRunnerPool().execute(CoverageTestExecutor.getContext(finder, t, reloadClasses, data)));

		SortedSet<TestCoverage> results = new TreeSet<TestCoverage>(new Comparator<TestCoverage>() {
			@Override
			public int compare(TestCoverage  o1, TestCoverage o2) {
				if(o1 == o2) return 0;

				int v = o1.getTest().length - o2.getTest().length;
				if(v != 0) return v;

				float c1 = 0;
				for(CoverageInformation cov : o1.getCoverage()) c1 += cov.getQuality();

				float c2 = 0;
				for(CoverageInformation cov : o2.getCoverage()) c2 += cov.getQuality();

				if(c1 < c2) return -1;
				if(c1 > c2) return 1;

				return 1; // never return 0: they are not the same test!
			}
		});

		for(Entry<Test, Future<ElementManager<String, CoverageInformation>>> entry : futures.entrySet()) {
			try {
				results.add(new TestCoverage(entry.getKey(), entry.getValue().get()));
			} catch(Exception e) {
				TestFul.debug(e);
				logger.log(Level.FINE, e.getMessage(), e);
			}
		}

		OptimalTestCreator opt = new OptimalTestCreator();
		for(TestCoverage c : results) opt.update(c);

		SortedSet<Operation> set = new TreeSet<Operation>(OperationPosition.orderComparator);
		for(Test t : opt.get())
			for(Operation op : t.getTest())
				set.add(op);

		Test ret;
		ret = new Test(orig.getCluster(), orig.getReferenceFactory(), set.toArray(new Operation[set.size()]));
		ret = RemoveUselessDefs.singleton.perform(ret);

		return ret;
	}

	private static void simplify(final List<Test> res) {
		// removes tests contained in other tests
		for(int i = 0; i < res.size(); i++) {
			Operation[] ops_i = res.get(i).getTest();

			int j = -1;
			Iterator<Test> iter = res.iterator();
			while(iter.hasNext()) {
				j++;
				Operation[] ops_j = iter.next().getTest();
				if(ops_i != ops_j && contains(ops_i, ops_j)) {
					iter.remove();
					if(j < i) {
						i--;
						j--;
					}
				}
			}
		}
	}

	/**
	 * Checks if a contains b
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean contains(Operation[] a, Operation[] b) {
		if(a.length < b.length) return false;

		for(int i = 0; i < b.length; i++)
			if(!a[i].equals(b[i]))
				return false;

		return true;
	}

	private int position = 0;

	/** if true, split observers (i.e. find longest invocation sequences leading to new object states) */
	private boolean splitObservers;

	/** the test cluster */
	private final TestCluster cluster;

	/** the reference factory */
	private final ReferenceFactory refFactory;

	/** the ordered set of references (refs[x].getId() == x) */
	private final Reference[] refs;

	/**
	 * for each object in the repository, keeps trace of the operation that
	 * modifies its state.
	 */
	protected final TreeSet<Operation>[] operations;

	/** if isACopy[i] is true, operations[i] contains a copy of operations[j] (it may not be exactly the same, due to some aliasing) */
	private final boolean[] isACopy;

	/** if aliases[i] != null, stores the reference to use instead of the i-th reference */
	private final Reference[] aliases;

	/**
	 * if aliases[i] != null, aliasesOrigOp[i] stores the aliasing operation:
	 * <ol>
	 * 	<li><code>i0 = s0.observer()</code></li>
	 *	<li><code>i1 = s0.observer()</code></li>
	 * </ol>
	 * <code>aliasesOp[1]</code> contains <code>i0 = s0.observer()</code>
	 */
	private final Operation[] aliasesOrigOp;

	/**
	 * if aliases[i] != null, aliasesOp[i] stores the aliased operation:
	 * <ol>
	 * 	<li><code>i0 = s0.observer()</code></li>
	 *	<li><code>i1 = s0.observer()</code></li>
	 * </ol>
	 * <code>aliasesOp[1]</code> contains <code>i1 = s0.observer()</code>
	 */
	private final Operation[] aliasesOp;

	public Splitter(boolean splitObservers, TestCluster cluster, ReferenceFactory refFactory) {
		this.splitObservers = splitObservers;
		this.cluster = cluster;
		this.refFactory = refFactory;

		refs = refFactory.getReferences();
		Arrays.sort(refs, new Comparator<Reference>() {
			@Override
			public int compare(Reference o1, Reference o2) { return o1.getId() - o2.getId(); }
		});

		int numRefs = refs.length;

		operations = new TreeSet[numRefs];
		for(int i = 0; i < operations.length; i++)
			operations[i] = new TreeSet<Operation>(OperationPosition.orderComparator);

		if(!splitObservers) {
			isACopy = new boolean[numRefs];
			aliases = new Reference[numRefs];
			aliasesOp = new Operation[numRefs];
			aliasesOrigOp = new Operation[numRefs];
		} else {
			isACopy = null;
			aliases = null;
			aliasesOp = null;
			aliasesOrigOp = null;
		}
	}

	public Splitter(boolean splitObservers, TestCluster cluster, ReferenceFactory refFactory, Listener ... listener) {
		this(splitObservers, cluster, refFactory);

		if(listener != null) for(Listener l : listener)
			register(l);
	}

	/**
	 * The reference t is being modified or reassigned, so
	 * reset its aliasing status and rearrange all aliases referring to it
	 * @param t the reference being modified or reassigned
	 * @param modified if true, the reference is going to be modified
	 */
	private void deleteAliases(Reference t, boolean modified) {
		if(t == null) return;

		if(aliases == null) return;

		// t is being assigned: if was an alias, now it's a new value
		aliases[t.getId()] = null;
		isACopy[t.getId()] = false;

		// correct aliases of t
		for(int i = 0; i < aliases.length; i++) {
			if(aliases[i] == t) {

				Operation oldAliasesOp = aliasesOp[i];
				Operation oldAliasesOrigOp = aliasesOrigOp[i];

				// i is the first one: now it holds t's value
				aliases[i] = null;
				aliasesOrigOp[i] = null;
				aliasesOp[i] = null;

				// to determine if i is a copy, search if there is another sequence that contains operations[t.getId()]
				isACopy[i] = false;
				if(!modified) {
					for(int z = 0; z < operations.length; z++) {
						if(z == t.getId()) continue;

						if(operations[z] == operations[t.getId()] || operations[z].containsAll(operations[t.getId()])) {
							isACopy[i] = true;
							break;
						}
					}
				}

				Reference iRef = refs[i];
				for(int j = i+1; j < aliases.length; j++)
					if(aliases[j] == t) {
						aliases[j] = iRef;
						aliasesOrigOp[j] = oldAliasesOp;
					}

				// remove all useless accesses to t
				for(TreeSet<Operation> ops: operations) {
					Iterator<Operation> iter = ops.descendingIterator();
					while(iter.hasNext()) {
						Operation op = iter.next();

						if(op == oldAliasesOrigOp) {
							iter.remove();
							ops.add(oldAliasesOp);
							break;
						} else if(op.getUses().contains(t) || op instanceof ResetRepository || op.getDefs().contains(t)) {
							break;
						}
					}
				}

				break;
			}
		}
	}

	private Reference alias(Reference ref) {
		if(ref == null) return null;

		if(aliases == null || aliases[ref.getId()] == null) return ref;

		return aliases[ref.getId()];
	}

	private Reference[] alias(Reference[] refs) {
		Reference[] ret = new Reference[refs.length];
		for(int i = 0; i < refs.length; i++)
			ret[i] = alias(refs[i]);
		return ret;
	}

	private boolean isToEmit(Reference target) {
		if(target == null) return false;

		if(aliases == null) return true;

		if(aliases[target.getId()] != null) return false;

		if(isACopy[target.getId()]) return false;

		return true;
	}

	/**
	 * analyzes an operation. Notice that the algorithm relies on the
	 * sequenceNumber, so be sure that if an operation B comes after an operation
	 * A, B.seqNumber > a.seqNumber.
	 *
	 * @param op the opreration to analyze
	 */
	public void analyze(Operation op) {
		op.removeInfo(OperationPosition.KEY);
		op.addInfo(new OperationPosition(position++));

		if(logger.isLoggable(Level.FINEST)) logger.finest(op.getInfo(OperationPosition.KEY) + "\t" + op + "\n");

		if(op instanceof AssignPrimitive) analyze((AssignPrimitive) op);
		else if(op instanceof AssignConstant) analyze((AssignConstant) op);
		else if(op instanceof CreateObject) analyze((CreateObject) op);
		else if(op instanceof Invoke) analyze((Invoke) op);
		else if(op instanceof ResetRepository) analyze((ResetRepository) op);
		else logger.warning("Unknown operation: " + op.getClass().getName() + " - " + op);

		if(logger.isLoggable(Level.FINEST)) logger.finest(toString());
	}

	public void analyze(Invoke op) {
		// 0.  get useful information
		final MethodInformation methodInfo = op.getMethod().getMethodInformation();

		final Reference opTarget = op.getTarget();
		final int opTargetId = opTarget != null ? opTarget.getId() : -1;

		final Reference opThis;
		switch(methodInfo.getType()) {
		case STATIC:
			opThis = null;
			break;
		case MUTATOR:
			opThis = op.getThis();
			break;
		default:
			opThis = alias(op.getThis());
		}
		final int opThisId = opThis != null ? opThis.getId() : -1;

		final ParameterInformation[] paramsInfo = methodInfo.getParameters();
		final Clazz[] params = op.getMethod().getParameterTypes();

		// 1. verify if the invocation is doable:

		// 1.1 skip if is obj.meth() with obj null and meth not static
		if(!op.getMethod().isStatic() && operations[opThisId].isEmpty()) return;

		// 1.2 skip if there is a primitive parameter not initialized (i.e. null)
		for(int i = 0; i < params.length; i++)
			if(params[i] instanceof PrimitiveClazz && // the parameter is primitive
					!((PrimitiveClazz) params[i]).isClass() && // and the method uses the primitive version
					operations[op.getParams()[i].getId()].isEmpty()) // but the reference used is null
				return;

		// it this point is reached, the operation is doable

		// 2 prepare the operation

		// 2.1 delete aliases for modified parameters
		if(!splitObservers) {
			Reference[] paramsRef = op.getParams();
			for(int i = 0; i < paramsInfo.length; i++)
				if(paramsInfo[i].isCaptured() || paramsInfo[i].isCapturedByReturn() || paramsInfo[i].isMutated() || !paramsInfo[i].getCaptureStateOf().isEmpty())
					deleteAliases(paramsRef[i], true);
		}

		// 2.2 calculate the aliases of params
		final Reference[] paramsRef = alias(op.getParams());


		// 2.3 rewrite the operation, considering aliases
		if(!splitObservers) {
			Invoke orig = op;
			op = new Invoke(opTarget, opThis, op.getMethod(), paramsRef);
			op.addInfo(orig.getInfos());
		}

		// 3. calculate the set of operation "op" depends on (including "op")
		final TreeSet<Operation> newSet;
		if(opThisId >= 0) newSet = (TreeSet<Operation>) operations[opThisId].clone();
		else newSet = new TreeSet<Operation>(OperationPosition.orderComparator);

		newSet.add(op);
		for(Reference pRef : paramsRef)
			newSet.addAll(operations[pRef.getId()]);

		// 4. if there is a target, emit its operations and empty operations[op.getTarget()].getId()
		if(opTarget != null) {
			final boolean toEmit = isToEmit(opTarget);

			TreeSet<Operation> targetOps = operations[opTargetId];
			operations[opTargetId] = new TreeSet<Operation>(OperationPosition.orderComparator);
			deleteAliases(opTarget, false);

			if(!newSet.containsAll(targetOps) && toEmit)
				emit(targetOps);
		}

		// 5. check if op is an observer and there is an equivalent operation
		if(!splitObservers && methodInfo.getType() != Kind.MUTATOR) {
			Set<Reference> deps = op.getUses();

			Iterator<Operation> iter = newSet.descendingIterator();
			iter.next(); // skip op itself!
			while(iter.hasNext()) {
				Operation p = iter.next();

				if(p instanceof ResetRepository)
					break;

				if(Operation.existDefUse(p.getDefs(), deps))
					break;

				if(p instanceof Invoke &&
						((Invoke) p).getMethod().equals(op.getMethod()) &&
						Arrays.equals(((Invoke) p).getParams(), op.getParams())) {

					Reference pTarget = ((Invoke) p).getTarget();

					// the current operation is only using an observer, without storing the result anywhere.
					// I can skip it, since there is a previous invocation of the same observer
					if (opTarget == null) return;

					/** the reference is usable */
					boolean usable = false;

					/** the usable reference can be safely removed (no later uses) */
					boolean removable = true;

					// if the previous operation stores the result, verify if it still usable
					if(pTarget != null) {
						// verify if the target has been modified or reassigned
						Iterator<Operation> targetIter = operations[pTarget.getId()].descendingIterator();
						while(targetIter.hasNext()) {
							Operation tOp = targetIter.next();

							// if I find the same operation, the target is usable
							if(tOp == p) {
								usable = true;
								break;
							}

							// if there is a definition, the target has been modified
							if(tOp instanceof ResetRepository || tOp.getDefs().contains(pTarget))
								break;

						}

						// if there is an use between me and p, then it is not possible to remove it
						if(usable) {

							// verify if the target has been modified or reassigned
							Iterator<Operation> newSetIter = newSet.descendingIterator();
							while(newSetIter.hasNext()) {
								Operation nOp = newSetIter.next();

								// if I find the same operation, p is removable
								if(nOp == p) break;

								// if there is a use, the target cannot be deleted
								if(nOp.getUses().contains(pTarget))
									removable = false;

							}
						}

						// if I don't find p in operations[pTarget.getId()], the target has been reassigned
					}

					// if the targets are the same (!= null), the current operation is useless
					if (opTarget == pTarget && usable) return;

					if(isToEmit(opTarget)) emit(operations[opTargetId]);

					// if the previous operation saves the result in a different location, mark it as alias
					if(usable) { // true if pTarget != null
						isACopy[opTargetId] = true;
						aliases[opTargetId] = pTarget;
						aliasesOrigOp[opTargetId] = p;
						aliasesOp[opTargetId] = op;
					}

					// calculate the dependencies for the current operation, removing the previous operation
					if(removable) iter.remove();

					break;
				}
			}
		}

		if(splitObservers) {
			if(methodInfo.getType() == Kind.MUTATOR)
				operations[opThisId] = newSet;
			else
				emit(newSet);
		} else {
			if(methodInfo.getType() == Kind.MUTATOR || opTarget == null || aliases[opTargetId] == null) {
				if(opThisId >= 0)
					operations[opThisId] = newSet;
				else
					emit(newSet);

			}
		}

		if(opTarget != null) {
			operations[opTargetId] = (TreeSet<Operation>) newSet.clone();
			if(methodInfo.isReturnsState()) merge(opThisId, opTargetId);

			for(ParameterInformation p : paramsInfo)
				if(p.isCapturedByReturn()) merge(paramsRef[p.getPosition()].getId(), opTargetId);

		}

		// update parameters
		for(ParameterInformation p : paramsInfo) {
			int paramRefId = paramsRef[p.getPosition()].getId();
			if(p.isCaptured()) merge(opThisId, paramRefId);
			else if(p.isMutated()) operations[paramRefId].add(op);

			for(ParameterInformation p2 : p.getCaptureStateOf())
				merge(paramRefId, paramsRef[p2.getPosition()].getId());
		}
	}

	public void analyze(CreateObject op) {
		// 0.  get useful information
		final MethodInformation methodInfo = op.getConstructor().getMethodInformation();

		final Reference opTarget = op.getTarget();
		final int opTargetId = opTarget != null ? opTarget.getId() : -1;

		final ParameterInformation[] paramsInfo = methodInfo.getParameters();
		final Clazz[] params = op.getConstructor().getParameterTypes();

		// 1. verify if the invocation is doable:
		//    skip if there is a primitive parameter not initialized (i.e. null)
		for(int i = 0; i < params.length; i++)
			if(params[i] instanceof PrimitiveClazz && // the parameter is primitive
					!((PrimitiveClazz) params[i]).isClass() && // and the method uses the primitive version
					operations[op.getParams()[i].getId()].isEmpty()) // but the reference used is null
				return;

		// it this point is reached, the operation is doable

		// 2 prepare the operation

		// 2.1 delete aliases for modified parameters
		if(!splitObservers) {
			Reference[] paramsRef = op.getParams();
			for(int i = 0; i < paramsInfo.length; i++)
				if(paramsInfo[i].isCaptured() || paramsInfo[i].isCapturedByReturn() || paramsInfo[i].isMutated() || !paramsInfo[i].getCaptureStateOf().isEmpty())
					deleteAliases(paramsRef[i], true);
		}

		// 2.2 calculate the aliases of params
		final Reference[] paramsRef = alias(op.getParams());


		// 2.3 rewrite the operation, considering aliases
		if(!splitObservers) {
			CreateObject orig = op;
			op = new CreateObject(opTarget, op.getConstructor(), paramsRef);
			op.addInfo(orig.getInfos());
		}

		// 3. calculate the set of operation "op" depends on (including "op")
		TreeSet<Operation> newTarget = new TreeSet<Operation>(OperationPosition.orderComparator);
		newTarget.add(op);
		for(Reference pRef : paramsRef)
			newTarget.addAll(operations[pRef.getId()]);

		// 4. if there is a target, emit its operations and empty operations[op.getTarget()].getId()
		if(opTarget != null) {
			final boolean toEmit = isToEmit(opTarget);

			TreeSet<Operation> targetOps = operations[opTargetId];
			operations[opTargetId] = newTarget;
			deleteAliases(opTarget, false);

			if(toEmit) emit(targetOps);
		} else {
			// if target is null, emit the new operations
			emit(newTarget);
		}

		// update parameters
		for(ParameterInformation paramInfo : paramsInfo) {
			int paramRefId = paramsRef[paramInfo.getPosition()].getId();
			if(opTarget != null && paramInfo.isCaptured()) merge(opTargetId, paramRefId);
			else if(paramInfo.isMutated()) operations[paramRefId].add(op);

			for(ParameterInformation captured : paramInfo.getCaptureStateOf())
				merge(paramRefId, paramsRef[captured.getPosition()].getId());
		}

	}

	public void analyze(AssignConstant op) {
		if(op.getTarget() == null) return;

		int refId = op.getTarget().getId();

		TreeSet<Operation> tmp = operations[refId];
		operations[refId] = new TreeSet<Operation>(OperationPosition.orderComparator);
		operations[refId].add(op);
		if(isToEmit(op.getTarget())) emit(tmp);

		if(!splitObservers) deleteAliases(op.getTarget(), false);
	}

	public void analyze(AssignPrimitive op) {
		if(op.getTarget() == null) return;

		int refId = op.getTarget().getId();

		TreeSet<Operation> tmp = operations[refId];
		operations[refId] = new TreeSet<Operation>(OperationPosition.orderComparator);
		operations[refId].add(op);

		if(isToEmit(op.getTarget())) emit(tmp);

		if(!splitObservers) deleteAliases(op.getTarget(), false);
	}

	public void analyze(ResetRepository op) {
		flush();

		// insert ref = null
		for (Reference r : refs) {
			final Operation o;
			if(r.getClazz() instanceof PrimitiveClazz) o = new AssignPrimitive(r, null);
			else o = new AssignConstant(r, null);

			o.addInfo(op.getInfos());

			operations[r.getId()].add(o);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Ops:\n");
		for(int i = 0; i < operations.length; i++) {
			sb.append(" ref #");
			sb.append(i);
			sb.append(" ").append(refs[i]);

			if(isACopy != null && isACopy[i]) sb.append(" copy");
			if(aliases != null && aliases[i] != null) sb.append(" (").append(aliases[i]).append(")");
			if(aliasesOp != null && aliasesOp[i] != null) sb.append(" aOp:").append(aliasesOp[i]);
			if(aliasesOrigOp != null && aliasesOrigOp[i] != null) sb.append(" aOrigOp:").append(aliasesOrigOp[i]);

			sb.append("\n");

			for(Operation op : operations[i])
				sb.append("  ").append(op.getInfo(OperationPosition.KEY)).append("\t").append(op).append("\n");

		}

		return sb.toString();
	}

	/** Flush the repository, cleaning all the references it has */
	public void flush() {
		for(int i = 0; i < operations.length; i++) {
			TreeSet<Operation> tmp = operations[i];
			operations[i] = new TreeSet<Operation>(OperationPosition.orderComparator);
			if(isToEmit(refs[i])) {
				boolean emitted = emit(tmp);

				if(emitted)
					for(int j = 0; j < operations.length; j++)
						if((splitObservers || !isACopy[j]) &&
								(operations[j] == tmp || tmp.containsAll(operations[j])))
							operations[j] = new TreeSet<Operation>(OperationPosition.orderComparator);
			}
		}

		if(!splitObservers)
			for(int i = 0; i < aliases.length; i++) {
				isACopy[i] = false;
				aliases[i] = null;
				aliasesOp[i] = null;
				aliasesOrigOp[i] = null;
			}
	}

	/** emits a test */
	private boolean emit(SortedSet<Operation> ops) {
		if(ops.isEmpty()) return false;

		if(!interesting(ops)) return false;

		for(int i = 0; i < operations.length; i++)
			if((splitObservers || !isACopy[i]) &&
					(operations[i] == ops || operations[i].containsAll(ops)) )
				return false;

		if(logger.isLoggable(Level.FINEST)) {
			StringBuilder sb = new StringBuilder();
			sb.append("Emitting:\n");
			for(Operation op : ops)
				sb.append(op.getInfo(OperationPosition.KEY) + "\t" + op + "\n");

			logger.finest(sb.toString());
		}

		for(Listener l : listener)
			l.notify(cluster, refFactory, ops.toArray(new Operation[ops.size()]));

		return true;
	}

	private boolean interesting(SortedSet<Operation> ops) {
		for(Operation op : ops) {
			if(op instanceof Invoke && ((Invoke) op).getMethod().getClazz() == cluster.getCut()) return true;
			if(op instanceof CreateObject && ((CreateObject) op).getConstructor().getClazz() == cluster.getCut()) return true;
			if(op instanceof AssignConstant && ((AssignConstant)op).getValue() != null && ((AssignConstant)op).getValue().getDeclaringClass() == cluster.getCut())  return true;
		}
		return false;
	}

	private void merge(int pos1, int pos2) {
		if(pos1 < 0 || pos2 < 0) return;

		if(operations[pos1] == operations[pos2]) return;

		if(operations[pos1].isEmpty()) operations[pos1] = operations[pos2];
		else if(operations[pos2].isEmpty()) operations[pos2] = operations[pos1];
		else {
			operations[pos1].addAll(operations[pos2]);
			operations[pos2] = operations[pos1];
		}
	}
}
