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

package testful.coverage.whiteBox;

import java.net.URL;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import testful.runner.ClassData;

/**
 * TODO describe me!
 * @author matteo
 */
public class WhiteBoxAnalysisData implements ClassData {
	private final Map<String, BlockClass> classes = new HashMap<String, BlockClass>();

	@Override
	public void load(String className, URL classURL) {
		BlockClass bClass = BlockClass.read(classURL);
		if(bClass == null) return;

		classes.put(bClass.getName(), bClass);
		conditionBlocks = null;
		mapBlockCondition = null;
		mapBranchCondition = null;
	}

	private transient BitSet conditionBlocks;
	private transient Map<Integer, BitSet> mapBlockCondition;
	public BitSet getReachableBranches(BitSet blockCoverage) {
		if(conditionBlocks == null) {
			conditionBlocks = new BitSet();
			mapBlockCondition = new HashMap<Integer, BitSet>();

			for(BlockClass bClass : classes.values()) {
				for(Block block : bClass) {
					Condition condition = block.getCondition();
					if(condition != null) {

						BitSet branches = new BitSet();
						if(condition instanceof ConditionIf) {
							ConditionIf cIf = (ConditionIf) condition;
							branches.set(cIf.getTrueBranch().getId());
							branches.set(cIf.getFalseBranch().getId());

						} else if(condition instanceof ConditionSwitch) {
							ConditionSwitch cSwi = (ConditionSwitch) condition;

							branches.set(cSwi.getDefaultBranch().getId());
							for(EdgeConditional br : cSwi.getBranches().values())
								branches.set(br.getId());
						}

						conditionBlocks.set(block.getId());
						mapBlockCondition.put(block.getId(), branches);
					}
				}
			}
		}

		blockCoverage = (BitSet) blockCoverage.clone();
		blockCoverage.and(conditionBlocks);

		BitSet reachable = new BitSet();
		for (int blockId = blockCoverage.nextSetBit(0); blockId >= 0; blockId = blockCoverage.nextSetBit(blockId+1))
			reachable.or(mapBlockCondition.get(blockId));

		return reachable;
	}

	private transient Map<Integer, Condition> mapBranchCondition;
	public Condition getConditionFromBranch(int branchId) {
		if(mapBranchCondition == null) {
			mapBranchCondition = new HashMap<Integer, Condition>();

			for(BlockClass bClass : classes.values()) {
				for(Block block : bClass) {
					Condition condition = block.getCondition();
					if(condition != null) {

						if(condition instanceof ConditionIf) {
							ConditionIf cIf = (ConditionIf) condition;
							mapBranchCondition.put(cIf.getTrueBranch().getId(), cIf);
							mapBranchCondition.put(cIf.getFalseBranch().getId(), cIf);

						} else if(condition instanceof ConditionSwitch) {
							ConditionSwitch cSwi = (ConditionSwitch) condition;

							mapBranchCondition.put(cSwi.getDefaultBranch().getId(), cSwi);
							for(EdgeConditional br : cSwi.getBranches().values())
								mapBranchCondition.put(br.getId(), cSwi);
						}
					}
				}
			}
		}

		return mapBranchCondition.get(branchId);
	}
}
