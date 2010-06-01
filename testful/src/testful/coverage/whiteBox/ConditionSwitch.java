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

import java.util.HashMap;
import java.util.Map;

public class ConditionSwitch extends Condition {

	private static final long serialVersionUID = -8845331609006428322L;

	private static int idGenerator = 0;
	
	private final int id = idGenerator++;
	private final Map<Integer, EdgeConditional> branches = new HashMap<Integer, EdgeConditional>();
	private EdgeConditional defaultBranch;
	
	public ConditionSwitch(DataUse use) {
		super(use);
	}

	public int getId() {
		return id;
	}
	
	void setDefaultBranch(EdgeConditional defaultBranch) {
		this.defaultBranch = defaultBranch;
	}

	public EdgeConditional getDefaultBranch() {
		return defaultBranch;
	}

	void addBranch(Integer value, EdgeConditional branch) {
		branches.put(value, branch);
	}

	public Map<Integer, EdgeConditional> getBranches() {
		return branches;
	}
}
