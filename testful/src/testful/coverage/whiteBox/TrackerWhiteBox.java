package testful.coverage.whiteBox;

import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import testful.coverage.CoverageInformation;
import testful.coverage.Tracker;
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

		if(condTarget != null)
			ret.put(condTarget);
		
		return ret;
	}

	@Override
	public void reset() {
		this.covBlocks = new BitSet();
		this.covConditions = new BitSet();
		
		this.whiteData = (WhiteBoxData) Tracker.getDatum(WhiteBoxData.KEY);

		ConditionTargetDatum condTargetDatum = (ConditionTargetDatum) Tracker.getDatum(ConditionTargetDatum.KEY);
		if(condTargetDatum != null) {
			this.condTargetId = condTargetDatum.getBranchId();
			this.condTarget = new CoverageConditionTarget(condTargetId);
		} else {
			this.condTargetId = -1;
			this.condTarget = null;
		}
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
}
