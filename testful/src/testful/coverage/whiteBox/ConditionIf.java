package testful.coverage.whiteBox;

public class ConditionIf extends Condition {

	private static final long serialVersionUID = -3003316502705186978L;
	private EdgeConditional trueBranch;
	private EdgeConditional falseBranch;
	
	private transient String condition;

	public ConditionIf(DataUse use1, DataUse use2, String condition) {
		super(use1, use2);
		this.condition = condition;
	}

	public ConditionIf(DataUse use) {
		super(use);
	}

	void setTrueBranch(EdgeConditional trueBranch) {
		this.trueBranch = trueBranch;
	}

	public EdgeConditional getTrueBranch() {
		return trueBranch;
	}

	void setFalseBranch(EdgeConditional falseBranch) {
		this.falseBranch = falseBranch;
	}

	public EdgeConditional getFalseBranch() {
		return falseBranch;
	}

	/** Avaliable only on "fresh" analysis (transient) */
	String getCondition() {
		return condition;
	}
}
