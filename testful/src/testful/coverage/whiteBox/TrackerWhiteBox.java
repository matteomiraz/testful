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

import testful.coverage.CoverageInformation;
import testful.coverage.Tracker;
import testful.coverage.whiteBox.CoverageDataFlow.DefUse;
import testful.utils.ElementManager;

@SuppressWarnings("unused") // for the SAFE option
public class TrackerWhiteBox extends Tracker {

	private final static boolean SAFE = false;

	private static TrackerWhiteBox tracker;

	public static TrackerWhiteBox getTracker() {
		if(tracker == null)
			tracker = new TrackerWhiteBox();

		return tracker;
	}

	private WhiteBoxData whiteData;

	private TrackerWhiteBox() {
		reset();
	}

	@Override
	public ElementManager<String, CoverageInformation> getCoverage() {
		ElementManager<String, CoverageInformation> ret = new ElementManager<String, CoverageInformation>();

		if(whiteData == null || !whiteData.hasContracts()) {
			ret.put(new CoverageBasicBlocks(CoverageBasicBlocks.KEY_CODE, CoverageBasicBlocks.NAME_CODE, covBlocks));

			ret.put(new CoverageConditions(CoverageConditions.KEY_CODE, CoverageConditions.NAME_CODE, covConditions));

		} else {
			ret.put(new CoverageBasicBlocks(CoverageBasicBlocks.KEY_CODE, CoverageBasicBlocks.NAME_CODE, whiteData.getBlocksCode(covBlocks)));
			ret.put(new CoverageBasicBlocks(CoverageBasicBlocks.KEY_CONTRACT, CoverageBasicBlocks.NAME_CONTRACT, whiteData.getBlocksContract(covBlocks)));

			ret.put(new CoverageConditions(CoverageConditions.KEY_CODE, CoverageConditions.NAME_CODE, whiteData.getConditionsCode(covConditions)));
			ret.put(new CoverageConditions(CoverageConditions.KEY_CONTRACT, CoverageConditions.NAME_CONTRACT, whiteData.getConditionsContract(covConditions)));
		}

		ret.put(new CoverageDataFlow(defUse));
		ret.put(new CoverageDefExp(defExpo));

		if(condTarget != null)
			ret.put(condTarget);

		return ret;
	}

	@Override
	public void reset() {
		covBlocks = new BitSet();
		covConditions = new BitSet();

		whiteData = (WhiteBoxData) Tracker.getDatum(WhiteBoxData.KEY);

		stack = new LinkedList<Integer>();
		callNum = new HashMap<Integer, Integer>();

		ConditionTargetDatum condTargetDatum = (ConditionTargetDatum) Tracker.getDatum(ConditionTargetDatum.KEY);
		if(condTargetDatum != null) {
			condTargetId = condTargetDatum.getBranchId();
			condTarget = new CoverageConditionTarget(condTargetId);
		} else {
			condTargetId = -1;
			condTarget = null;
		}

		defUse = new LinkedHashSet<DefUse>();
		defExpo = new LinkedHashMap<Stack, Set<DataAccess>>();

	}

	// ---------------------- Basic Block coverage ------------------------------
	private BitSet covBlocks;
	public void trackBasicBlock(int blockId) {
		covBlocks.set(blockId);
	}

	// ----------------------- Condition coverage  -------------------------------
	/** condition coverage */
	private BitSet covConditions;

	/** id of the target; -1 if no target */
	private int condTargetId;

	/** target condition distance (condTarget != null <==> condTarget > -1 ) */
	private CoverageConditionTarget condTarget;

	public int getConditionTargetId() {
		return condTargetId;
	}

	/**
	 * Mark the branch as executed
	 * @param branchId the id of the executed branch
	 */
	public void trackBranch(int branchId) {
		covConditions.set(branchId);
	}

	/**
	 * Set the given distance as target distance
	 * @param distance the distance to reach the target
	 */
	public void setConditionTargetDistance1(double distance) {
		condTarget.setDistance(distance);
	}

	/**
	 * Set the distance as | v1 - v2 |.<br>
	 * Use  this method to reach the true branch of >=, &lt;=, ==
	 * @param v1 the first value
	 * @param v2 the second value
	 */
	public void setConditionTargetDistance2(double v1, double v2) {
		condTarget.setDistance(Math.abs(v1 - v2));
	}

	/**
	 * Set the distance as 1 + | v1 - v2 |.<br>
	 * Use  this method to reach the true branch of >, &lt;, !=
	 * @param v1 the first value
	 * @param v2 the second value
	 */
	public void setConditionTargetDistance3(double v1, double v2) {
		condTarget.setDistance(Math.abs(v1 - v2)+1);
	}

	// -------------------------- Stack tracker ---------------------------------
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
		if(!SAFE || num != null) {
			if(num <= 1) {
				stackCache = null;
				callNum.remove(ID);
				Integer LAST = stack.removeLast();

				if(SAFE && !LAST.equals(ID))
					System.err.println("WARN: called method is not the last on the stack!");
			} else {
				callNum.put(ID, num-1);
			}
		} else {
			System.err.println("WARN: no call for " + id);
		}
	}

	private Stack stackCache;
	private Stack getStack() {
		if(stackCache == null)
			stackCache = new Stack(stack.toArray(new Integer[stack.size()]));

		return stackCache;
	}

	// ------------------------ Def-Use coverage --------------------------------
	public DataAccess getDataAccess(int id) {
		return new DataAccess(id, getStack());
	}

	private Set<DefUse> defUse;
	public void manageDefUse(DataAccess def, DataAccess use) {
		defUse.add(new DefUse(def, use));
	}

	// ------------------------ Def exposition --------------------------------
	private Map<Stack, Set<DataAccess>> defExpo;

	private static final Integer VALUE = 1;
	public void manageDefExposition(Object obj) {
		if(!checkDefExposer(obj))
			return;

		final Stack stack = getStack();

		Set<DataAccess> def = defExpo.get(stack);
		if(def == null) {
			def = new LinkedHashSet<DataAccess>();
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
	}

	private void addAll(Set<DataAccess> def, Object[] objects) {
		if(objects == null) return;

		for (Object o : objects) {
			if(o != null) {
				if(o instanceof DataAccess) def.add((DataAccess) o);
				else if(o.getClass().isArray()) addAll(def, (Object[]) o);
				else System.err.println("DefExposer: unknown element: " + o + " - " + o.getClass().getCanonicalName());
			}
		}
	}

	// // without context
	//
	//	private Map<Integer[], BitSet> defExpo2;
	//	public void manageDefExposition2(Object obj) {
	//		if(!(obj instanceof DefExposer))
	//			return;
	//
	//		final DefExposer o = (DefExposer) obj;
	//
	//		final Integer[] stack = getStack();
	//
	//		BitSet def = defExpo2.get(stack);
	//		if(def == null) {
	//			def = new BitSet();
	//			defExpo2.put(stack, def);
	//		}
	//
	//
	//		final Queue<DefExposer> todo = new LinkedList<DefExposer>();
	//		final IdentityHashMap<DefExposer, Integer> processed = new IdentityHashMap<DefExposer, Integer>();
	//
	//		todo.add(o);
	//
	//		while (!todo.isEmpty()) {
	//			DefExposer d = todo.poll();
	//
	//			if (processed.put(d, VALUE) == null) {
	//
	//				for (DataAccess da : d.__testful_get_defs__())
	//					if (da != null)
	//						def.set(da.getId());
	//
	//				for (Object f : d.__testful_get_fields__())
	//					getDefExposers(f, todo);
	//
	//			}
	//		}
	//	}


	private static boolean checkDefExposer(Object o) {
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

	// ------------------------ Def-Array handling --------------------------------
	/**
	 * creates an array of DataAccess, all with the same defId.<br>
	 * Use this method when a new array (1 dimension) is created
	 * @param len the length of the array being created
	 * @param id the id of the definition
	 * @return the array containing the definitions (its length is len)
	 */
	public DataAccess[] newArrayDef(int len, int id) {
		DataAccess[] ret = new DataAccess[len];

		//DataAccess is an immutable object... I can share it!
		DataAccess d = getDataAccess(id);
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
		Object ret = Array.newInstance(DataAccess.class, len);

		DataAccess d = getDataAccess(id);
		_newMultiArrayDef((Object[]) ret, id, len.length, d);

		return ret;
	}

	private void _newMultiArrayDef(Object[] defs, int id, int dims, DataAccess d) {
		if(dims > 1) {
			for (int i = 0; i < defs.length; i++)
				_newMultiArrayDef((Object[]) defs[i], id, dims-1, d);
		} else {
			for (int i = 0; i < defs.length; i++)
				defs[i] = d;
		}


	}

	/**
	 * creates the definitions array for a given array with an arbirary number of dimensions.<br/>
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
		dataAccessArrayTypes[0] = DataAccess.class;
		for(int i = 1; i < dim; i++)
			dataAccessArrayTypes[i] = Array.newInstance(dataAccessArrayTypes[i-1], 0).getClass();

		DataAccess d = getDataAccess(id);

		return _arrayAssignmentDef(o, dim, id, dataAccessArrayTypes, d);
	}

	private Object _arrayAssignmentDef(Object o, int dim, int id, Class<?>[] dataAccessArrayTypes, DataAccess d) {

		if(o == null) return null;

		if(dim == 1) {
			DataAccess[] ret = new DataAccess[Array.getLength(o)];
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
