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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.runner.ClassData;

/**
 * Loads the white box analysis when the class is loaded.
 * @author matteo
 */
public class WhiteBoxAnalysisData implements ClassData {

	private static final Logger logger = Logger.getLogger("testful.coverage.whiteBox");

	private final Map<String, BlockClass> classes = new HashMap<String, BlockClass>();

	@Override
	public void load(String className, URL classURL) {
		final BlockClass bClass;

		try {
			bClass = BlockClass.read(classURL);

			if(bClass == null) throw new NullPointerException("The information read is null");
		} catch (Throwable e) {
			logger.log(Level.WARNING, "Cannot read whiteBox analysis information for class " + className, e);
			return;
		}

		logger.fine("Read whiteBox analysis information for class " + className);

		classes.put(bClass.getName(), bClass);
		conditionBlocks = null;
		mapBlockCondition = null;
		mapBranchCondition = null;
		mapDataDefs = null;
	}

	private transient BitSet conditionBlocks;
	private transient Map<Integer, Condition> mapBlockCondition;
	private transient Map<Integer, Condition> mapBranchCondition;
	private transient Map<Integer, DataDef> mapDataDefs;

	private void buildInternalState() {

		conditionBlocks = new BitSet();
		mapBlockCondition = new HashMap<Integer, Condition>();
		mapBranchCondition = new HashMap<Integer, Condition>();
		mapDataDefs = new HashMap<Integer, DataDef>();

		for(BlockClass bClass : classes.values()) {
			for(Block block : bClass) {

				Condition condition = block.getCondition();
				if(condition != null) {
					conditionBlocks.set(block.getId());
					mapBlockCondition.put(block.getId(), condition);
					for (int b : condition.getBranches()) {
						mapBranchCondition.put(b, condition);
					}
				}

				if(block instanceof BlockBasic) {
					for (DataDef def : ((BlockBasic) block).getDefs()) {
						mapDataDefs.put(def.getId(), def);
					}
				}
			}
		}
	}

	public Set<Condition> getEvaluatedConditions(BitSet blockCoverage) {
		if(mapBlockCondition == null) buildInternalState();

		BitSet bc = (BitSet) blockCoverage.clone();
		bc.and(conditionBlocks);

		Set<Condition> ret = new HashSet<Condition>();
		for (int blockId = bc.nextSetBit(0); blockId >= 0; blockId = bc.nextSetBit(blockId+1)) {
			ret.add(mapBlockCondition.get(blockId));
		}
		return ret;
	}

	public Condition getConditionFromBranch(int branchId) {
		if(mapBranchCondition == null) buildInternalState();

		return mapBranchCondition.get(branchId);
	}

	public DataDef getDataDef(int defId) {
		if(mapDataDefs == null) buildInternalState();

		return mapDataDefs.get(defId);
	}
}
