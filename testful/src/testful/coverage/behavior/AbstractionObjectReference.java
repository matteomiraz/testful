package testful.coverage.behavior;

public class AbstractionObjectReference extends Abstraction {

	private static final long serialVersionUID = 2417857549496965147L;

	private final boolean isNull;

	AbstractionObjectReference(String expression, boolean isNull) {
		super(expression);
		this.isNull = isNull;
	}

	public boolean isNull() {
		return isNull;
	}

	@Override
	public String toString() {
		return getExpression() + (isNull ? " is null" : " is not null");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (isNull ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;

		if(!(obj instanceof AbstractionObjectReference)) return false;
		AbstractionObjectReference other = (AbstractionObjectReference) obj;
		if(isNull != other.isNull) return false;
		return true;
	}
}
