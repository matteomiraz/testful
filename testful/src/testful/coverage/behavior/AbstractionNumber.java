package testful.coverage.behavior;

public class AbstractionNumber extends Abstraction {

	private static final long serialVersionUID = -6456493491208206791L;

	public static final String NEGATIVE = "< 0";
	public static final String ZERO = "= 0";
	public static final String POSITIVE = "> 0";

	public static final String P_INF = "+Inf";
	public static final String N_INF = "-Inf";
	public static final String NaN = "NaN";

	private final String label;

	public AbstractionNumber(String expression, String label) {
		super(expression);
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;

		if(!(obj instanceof AbstractionNumber)) return false;
		AbstractionNumber other = (AbstractionNumber) obj;
		if(label == null) {
			if(other.label != null) return false;
		} else if(!label.equals(other.label)) return false;

		return true;
	}
}
