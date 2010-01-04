package testful.coverage.whiteBox;

import java.io.Serializable;

public abstract class Condition implements Serializable {

	private static final long serialVersionUID = 5318886334221554655L;
	private final DataUse use1;
	private final DataUse use2;

	public Condition(DataUse use1, DataUse use2) {
		this.use1 = use1;
		this.use2 = use2;
	}

	public Condition(DataUse use) {
		this(use, null);
	}

	public DataUse getUse1() {
		return use1;
	}

	public DataUse getUse2() {
		return use2;
	}
}
