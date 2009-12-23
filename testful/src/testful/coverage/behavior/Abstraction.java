package testful.coverage.behavior;

import java.io.Serializable;

public abstract class Abstraction implements Serializable {

	private static final long serialVersionUID = -5699038898763653554L;

	private final String expression;

	/**
	 * Default constructor
	 * 
	 * @param expression : the abstracted expression (e.g. "this.size()")
	 */
	public Abstraction(String expression) {
		this.expression = expression;
	}

	public String getExpression() {
		return expression;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expression == null) ? 0 : expression.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof Abstraction)) return false;

		Abstraction other = (Abstraction) obj;
		if(expression == null) return other.expression == null;
		return expression.equals(other.expression);
	}

	@Override
	public abstract String toString();

	static final class AbstractionError extends Abstraction {

		private static final long serialVersionUID = 4015057413935518817L;

		public AbstractionError(String expression) {
			super(expression);
		}

		@Override
		public String toString() {
			return getExpression() + " -- evaluation error--";
		}
	}
}
