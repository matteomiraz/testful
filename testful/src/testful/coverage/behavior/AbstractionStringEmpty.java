package testful.coverage.behavior;

public class AbstractionStringEmpty extends Abstraction {

	private static final long serialVersionUID = -2491111474194919857L;

	private final boolean empty;

	public AbstractionStringEmpty(String expression, boolean empty) {
		super(expression);
		this.empty = empty;
	}

	@Override
	public String toString() {
		if(empty) return getExpression() + ": empty string";
		else return getExpression() + ": non-empty string";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (empty ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;

		if(!(obj instanceof AbstractionStringEmpty)) return false;
		AbstractionStringEmpty other = (AbstractionStringEmpty) obj;
		if(empty != other.empty) return false;

		return true;
	}

}
