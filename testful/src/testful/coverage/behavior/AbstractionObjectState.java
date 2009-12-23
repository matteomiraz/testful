package testful.coverage.behavior;

import java.util.Arrays;

public class AbstractionObjectState extends Abstraction {

	private static final long serialVersionUID = -4782880343029591499L;

	private Abstraction[] abstraction;

	public AbstractionObjectState(String type, Abstraction[] abstraction) {
		super(type);

		if(abstraction != null) this.abstraction = abstraction;
		else this.abstraction = new Abstraction[0];
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(getExpression());
		sb.append(" (" + getExpression() + "):");

		for(Abstraction a : abstraction)
			sb.append(" {").append(a.toString()).append("}");

		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(abstraction);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;

		if(!(obj instanceof AbstractionObjectState)) return false;
		AbstractionObjectState other = (AbstractionObjectState) obj;
		if(!Arrays.equals(abstraction, other.abstraction)) return false;

		return true;
	}

}
