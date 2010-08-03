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
import java.util.Map.Entry;
import java.util.Set;

import testful.TestFul;

public class ConditionSwitch extends Condition {

	private static final long serialVersionUID = -1921136469085209917L;

	private static int idGenerator = 0;

	private final int id = idGenerator++;
	private final Map<Integer, EdgeConditional> cases = new HashMap<Integer, EdgeConditional>();
	private EdgeConditional defaultCase;

	public ConditionSwitch(int basicBlock, Value v, DataUse use) {
		super(basicBlock, DataType.Number, v, use);
	}

	public int getId() {
		return id;
	}

	void setDefaultCase(EdgeConditional defaultCase) {
		this.defaultCase = defaultCase;
	}

	public EdgeConditional getDefaultCase() {
		return defaultCase;
	}

	void addCase(Integer value, EdgeConditional branch) {
		cases.put(value, branch);
	}

	public Map<Integer, EdgeConditional> getCases() {
		return cases;
	}

	public Set<Integer> getCaseValues() {
		return cases.keySet();
	}

	private transient int[] branches;
	@Override
	public int[] getBranches() {
		if(branches == null) {
			branches = new int[cases.size() + (defaultCase != null ? 1 : 0)];

			int i = 0;
			for (EdgeConditional c : cases.values())
				branches[i++] = c.getId();

			if(defaultCase != null)
				branches[i++] = defaultCase.getId();
		}


		return branches;
	}

	/**
	 * Returns the value that the key must assume to take the chosen branch (null for the default branch).
	 * @return the value that the key must assume to take the chosen branch (null for the default branch).
	 */
	public Integer getKeyValueByBrachId(int branchId) {
		if(defaultCase != null && branchId == defaultCase.getId()) return null;

		for (Entry<Integer, EdgeConditional> e : cases.entrySet())
			if(e.getValue().getId() == branchId) return e.getKey();

		if(TestFul.DEBUG) TestFul.debug("Invalid branch (" + branchId + ") cases: " + cases.keySet() + "; default: " + defaultCase);
		return null;
	}
}
