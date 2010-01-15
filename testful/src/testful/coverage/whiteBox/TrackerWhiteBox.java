package testful.coverage.whiteBox;

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
	public DataAccess getDataAccess(int id, boolean useContext) {
		return new DataAccess(id, useContext?getStack():null);
	}

	private Set<DefUse> defUse;
	public void manageDefUse(DataAccess def, DataAccess use) {
		defUse.add(new DefUse(def, use));
	}

	// ------------------------ Def exposition --------------------------------


	//TODO: far chiamare questo metodo ad ogni invocazione

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

				for (DataAccess da : d.__testful_get_defs__())
					if (da != null)
						def.add(da);

				for (Object f : d.__testful_get_fields__())
					getDefExposers(f, todo);

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
}
