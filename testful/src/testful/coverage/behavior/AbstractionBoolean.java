package testful.coverage.behavior;

public class AbstractionBoolean extends Abstraction {

	private static final long serialVersionUID = -1735480378740327016L;

	private final boolean value;

	AbstractionBoolean(String expression, boolean value) {
		super(expression);
		this.value = value;
	}

	@Override
	public String toString() {
		if(value) return getExpression() + ": True";
		else return getExpression() + ": False";
	}

	@Override
	public int hashCode() {
		return 31 * super.hashCode() + (value ? 1231 : 1237);
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;
		if(!(obj instanceof AbstractionBoolean)) return false;
		AbstractionBoolean other = (AbstractionBoolean) obj;
		if(value != other.value) return false;
		return true;
	}

}
