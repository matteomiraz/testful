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
