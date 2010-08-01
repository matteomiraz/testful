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

package testful.coverage.whiteBox;

import java.lang.reflect.Array;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.coverage.Tracker;
import testful.coverage.whiteBox.CoverageDataFlow.DefUse;
import testful.coverage.whiteBox.CoveragePUse.PUse;
import testful.utils.ElementManager;

public class TrackerWhiteBox extends Tracker {

	private static final Logger logger = Logger.getLogger("testful.coverage.whiteBox");

	private static TrackerWhiteBox tracker;

	public static TrackerWhiteBox getTracker() {
		if(tracker == null)
			tracker = new TrackerWhiteBox();

		return tracker;
	}

	private TrackerWhiteBox() {
		reset();
	}

	@Override
	public ElementManager<String, CoverageInformation> getCoverage() {
		ElementManager<String, CoverageInformation> ret = new ElementManager<String, CoverageInformation>();

		ret.put(new CoverageBasicBlocks(CoverageBasicBlocks.KEY, CoverageBasicBlocks.NAME, covBlocks));
		ret.put(new CoverageBranch(CoverageBranch.KEY, CoverageBranch.NAME, covBranches));
		ret.put(new CoverageDataFlow(defUse));
		ret.put(new CoveragePUse(pUse));
		ret.put(new CoverageDefExp(defExpo));

		if(condTarget != null)
			ret.put(condTarget);

		return ret;
	}

	@Override
	public void reset() {
		covBlocks = new BitSet();
		covBranches = new BitSet();
		stack = new LinkedList<Integer>();
		callNum = new HashMap<Integer, Integer>();

		ConditionTargetDatum condTargetDatum = (ConditionTargetDatum) Tracker.getDatum(ConditionTargetDatum.KEY);
		if(condTargetDatum != null) {
			condTarget = new CoverageBranchTarget(condTargetDatum);
			condTargetId = condTargetDatum.getBranchId();
			condTargetUse = condTargetDatum.isPUse();
			condDefinitionId = condTargetDatum.getDefinitionId();
		} else {
			condTarget = null;
			condTargetId = -1;
			condTargetUse = false;
			condDefinitionId  = null;
		}

		defUse = new LinkedHashSet<DefUse>();
		pUse = new LinkedHashSet<PUse>();
		defExpo = new LinkedHashMap<Stack, Set<ContextualId>>();

	}

	// ---------------------- Basic Block coverage ----------------------------
	private BitSet covBlocks;
	public void trackBasicBlock(int blockId) {
		covBlocks.set(blockId);
	}

	// ----------------------- Condition coverage  ----------------------------
	/** condition coverage */
	private BitSet covBranches;

	/**
	 * Mark the branch as executed
	 * @param branchId the id of the executed branch
	 */
	public void trackBranch(int branchId) {
		covBranches.set(branchId);
	}

	// ------------------------ Local Search ----------------------------------

	/** target condition distance (condTarget != null <==> condTarget > -1 ) */
	private CoverageBranchTarget condTarget;

	/** id of the target branch; -1 if no target branch */
	private int condTargetId;

	/** If true, targets a branch with a precise definition (i.e., a p-use) */
	private boolean condTargetUse;

	/** id of the definition to use in the target branch; null means default definition */
	private ContextualId condDefinitionId;

	/**
	 * Returns the id of the target branch; -1 if no target branch
	 * @return the id of the target branch; -1 if no target branch
	 */
	public int getConditionTargetId() {
		return condTargetId;
	}

	/**
	 * Set the given distance as target distance. -1 if the branch has been executed; >= 0 otherwise
	 * @param distance the distance to reach the target
	 */
	public void setConditionTargetDistance(double distance, ContextualId def) {
		if(!condTargetUse || (condDefinitionId == null && def == null) || (condDefinitionId != null && condDefinitionId.equals(def)))
			condTarget.setDistance(distance);
	}

	/**
	 * The branch has not been executed; calculate the distance as | v1 - v2 |.<br>
	 * @param v1 the first value
	 * @param v2 the second value
	 */
	public void calculateConditionTargetDistance(double v1, double v2, ContextualId def) {
		if(!condTargetUse || (condDefinitionId == null && def == null) || (condDefinitionId != null && condDefinitionId.equals(def)))
			condTarget.setDistance(Math.abs(v1 - v2));
	}

	// ------------------------ Context tracking ------------------------------
	private static final Integer ONE = 1;

	/** stores the number of calls to each method */
	private Map<Integer, Integer> callNum;
	/** stores the stack trace, without recursion */
	private Deque<Integer> stack;
	public void trackCall(int id) {
		Integer ID = id;

		Integer num = callNum.get(ID);
		if(num == null) {
			stackCache = null;
			stack.addLast(ID);
			callNum.put(ID, ONE);
		} else {
			callNum.put(ID, num+1);
		}
	}

	public void trackReturn(int id) {
		Integer ID = id;

		Integer num = callNum.get(ID);
		if(!TestFul.DEBUG || num != null) {
			if(num <= 1) {
				stackCache = null;
				callNum.remove(ID);
				Integer LAST = stack.removeLast();

				if(TestFul.DEBUG && !LAST.equals(ID))
					TestFul.debug("Context: the method called is not the last on the stack!");
			} else {
				callNum.put(ID, num-1);
			}
		} else {
			logger.fine("WARN: no call for " + id);
		}
	}

	private Stack stackCache;
	public Stack getStack() {
		if(stackCache == null)
			stackCache = new Stack(stack.toArray(new Integer[stack.size()]));

		return stackCache;
	}

	/**
	 * Returns the Contextual Identification
	 * @param id the non-contextual identification
	 * @return the contextual identification
	 */
	public ContextualId getDataAccess(int id) {
		return new ContextualId(id, getStack());
	}

	// ------------------------ Def-Use coverage ------------------------------
	private Set<DefUse> defUse;
	public void manageDefUse(ContextualId def, ContextualId use) {
		defUse.add(new DefUse(def, use));
	}

	// ------------------------ P-Use coverage --------------------------------
	private Set<PUse> pUse;
	public void trackPUse(int branchId, ContextualId def) {
		pUse.add(new PUse(branchId, def));
	}

	// ------------------------ Def exposition --------------------------------
	private Map<Stack, Set<ContextualId>> defExpo;

	private static final Integer VALUE = 1;
	public void manageDefExposition(Object obj) {
		try {
			if(!checkDefExposer(obj))
				return;

			final Stack stack = getStack(); //TODO: def-exposition does not work if we are using non-contextual analysis

			Set<ContextualId> def = defExpo.get(stack);
			if(def == null) {
				def = new LinkedHashSet<ContextualId>();
				defExpo.put(stack, def);
			}


			final Queue<DefExposer> todo = new LinkedList<DefExposer>();
			final IdentityHashMap<DefExposer, Integer> processed = new IdentityHashMap<DefExposer, Integer>();

			getDefExposers(obj, todo);

			while (!todo.isEmpty()) {
				DefExposer d = todo.poll();

				if (processed.put(d, VALUE) == null) {

					addAll(def, d.__testful_get_defs__());

					for (Object f : d.__testful_get_fields__())
						getDefExposers(f, todo);

				}
			}
		} catch (Throwable e) {
			logger.log(Level.FINE, "Error in manageDefExposition: " + e, e);
		}
	}

	private void addAll(Set<ContextualId> def, Object[] objects) {
		if(objects == null) return;

		for (Object o : objects) {
			if(o != null) {
				if(o instanceof ContextualId) def.add((ContextualId) o);
				else if(o.getClass().isArray()) addAll(def, (Object[]) o);
				else logger.fine("DefExposer: unknown element: " + o + " - " + o.getClass().getName());
			}
		}
	}

	private static boolean checkDefExposer(Object o) {
		if(o == null) return false;

		Class<?> c = o.getClass();

		if(c.isArray())
			return DefExposer.class.isAssignableFrom(c.getComponentType());
		else
			return DefExposer.class.isAssignableFrom(c);
	}

	/**
	 * Given an object, it is added to the to-do list if it is an instance of "DefExposer".
	 * @param o the object to add. The method handles null values as well as arrays.
	 * @param todo the to-do list.
	 */
	private static void getDefExposers(Object o, Queue<DefExposer> todo) {
		if(o == null) return;

		if(o.getClass().isArray()) {
			for (Object a : (Object[]) o) {
				getDefExposers(a, todo);
			}

			return;
		}

		if(o instanceof DefExposer)
			todo.add((DefExposer) o);

	}

	// ------------------------ Def-Array handling ----------------------------
	/**
	 * creates an array of DataAccess, all with the same defId.<br>
	 * Use this method when a new array (1 dimension) is created
	 * @param len the length of the array being created
	 * @param id the id of the definition
	 * @return the array containing the definitions (its length is len)
	 */
	public ContextualId[] newArrayDef(int len, int id) {
		ContextualId[] ret = new ContextualId[len];

		//DataAccess is an immutable object... I can share it!
		ContextualId d = getDataAccess(id);
		for(int i = 0; i < len; i++)
			ret[i] = d;

		return ret;
	}

	/**
	 * Creates an multi-array of DataAccess, all with the same defId.<br>
	 * Use this method when a new multi-array (> 1 dimension) is created
	 * @param len the length of the various dimensions of the array being created
	 * @param id the id of the definition
	 * @return the array containing the definitions (its lengths are those reported in len)
	 */
	public Object newMultiArrayDef(int[] len, int id) {
		Object ret = Array.newInstance(ContextualId.class, len);

		ContextualId d = getDataAccess(id);
		_newMultiArrayDef((Object[]) ret, id, len.length, d);

		return ret;
	}

	private void _newMultiArrayDef(Object[] defs, int id, int dims, ContextualId d) {
		if(dims > 1) {
			for (int i = 0; i < defs.length; i++)
				_newMultiArrayDef((Object[]) defs[i], id, dims-1, d);
		} else {
			for (int i = 0; i < defs.length; i++)
				defs[i] = d;
		}


	}

	/**
	 * creates the definitions array for a given array with an arbitrary number of dimensions.<br/>
	 * Use this method when an array is returned from a method invocation (e.g. list.toArray()).
	 * @param o the array created
	 * @param id the definition id
	 * @return the n-ary array of definitions. It has the same number of dimensions and the same length of the given array.
	 */
	public Object arrayAssignmentDef(Object o, int id) {
		if(o == null) return null;

		// calculate num of dimensions
		int dim = 0;
		for(Class<?> c = o.getClass(); c.isArray(); c = c.getComponentType()) dim++;

		// create DataAccess array types
		Class<?>[] dataAccessArrayTypes = new Class[dim];
		dataAccessArrayTypes[0] = ContextualId.class;
		for(int i = 1; i < dim; i++)
			dataAccessArrayTypes[i] = Array.newInstance(dataAccessArrayTypes[i-1], 0).getClass();

		ContextualId d = getDataAccess(id);

		return _arrayAssignmentDef(o, dim, id, dataAccessArrayTypes, d);
	}

	private Object _arrayAssignmentDef(Object o, int dim, int id, Class<?>[] dataAccessArrayTypes, ContextualId d) {

		if(o == null) return null;

		if(dim == 1) {
			ContextualId[] ret = new ContextualId[Array.getLength(o)];
			for (int i = 0; i < ret.length; i++) ret[i] = d;
			return ret;
		}

		Object[] ret = (Object[]) Array.newInstance(dataAccessArrayTypes[dim-1], Array.getLength(o));
		for (int i = 0; i < ret.length; i++) {
			Object e = Array.get(o, i);
			if(e != null) {
				ret[i] = _arrayAssignmentDef(e, dim-1, id, dataAccessArrayTypes, d);
			}
		}

		return ret;
	}

}
