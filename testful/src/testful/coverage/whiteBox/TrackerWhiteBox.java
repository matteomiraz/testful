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

import java.util.BitSet;

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

	private TrackerWhiteBox() {
		reset();
	}

	@Override
	public ElementManager<String, CoverageInformation> getCoverage() {
		ElementManager<String, CoverageInformation> ret = new ElementManager<String, CoverageInformation>();

		ret.put(new CoverageBasicBlocks(CoverageBasicBlocks.KEY, CoverageBasicBlocks.NAME, covBlocks));
		ret.put(new CoverageBranch(CoverageBranch.KEY, CoverageBranch.NAME, covBranches));

		if(condTarget != null)
			ret.put(condTarget);

		return ret;
	}

	@Override
	public void reset() {
		covBlocks = new BitSet();
		covBranches = new BitSet();

		ConditionTargetDatum condTargetDatum = (ConditionTargetDatum) Tracker.getDatum(ConditionTargetDatum.KEY);
		if(condTargetDatum != null) {
			condTargetId = condTargetDatum.getBranchId();
			condTarget = new CoverageBranchTarget(condTargetId);
		} else {
			condTargetId = -1;
			condTarget = null;
		}
	}

	// ---------------------- Basic Block coverage ------------------------------
	private BitSet covBlocks;
	public void trackBasicBlock(int blockId) {
		covBlocks.set(blockId);
	}

	// ----------------------- Condition coverage  -------------------------------
	/** condition coverage */
	private BitSet covBranches;

	/** id of the target; -1 if no target */
	private int condTargetId;

	/** target condition distance (condTarget != null <==> condTarget > -1 ) */
	private CoverageBranchTarget condTarget;

	public int getConditionTargetId() {
		return condTargetId;
	}

	/**
	 * Mark the branch as executed
	 * @param branchId the id of the executed branch
	 */
	public void trackBranch(int branchId) {
		covBranches.set(branchId);
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
