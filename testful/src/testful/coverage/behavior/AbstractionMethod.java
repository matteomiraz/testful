package testful.coverage.behavior;

import java.util.Arrays;

public class AbstractionMethod extends Abstraction {

	private static final long serialVersionUID = 8488220230181488768L;

	private final boolean isStatic;
	private final Abstraction[] abs;

	public AbstractionMethod(String methodName, boolean isStatic, Abstraction[] abs) {
		super(methodName);
		assert (abs != null);

		this.isStatic = isStatic;
		this.abs = abs;
	}

	public Abstraction[] getAbstractions() {
		return abs;
	}

	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getExpression()).append(" -");
		for(Abstraction a : abs)
			sb.append(" {").append(a).append("}");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(abs);
		result = prime * result + (isStatic ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;
		if(!(obj instanceof AbstractionMethod)) return false;
		AbstractionMethod other = (AbstractionMethod) obj;
		if(!Arrays.equals(abs, other.abs)) return false;
		if(isStatic != other.isStatic) return false;
		return true;
	}
}
