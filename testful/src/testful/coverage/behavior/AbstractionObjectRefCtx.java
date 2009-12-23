package testful.coverage.behavior;

import java.util.Arrays;
import java.util.Collection;

public class AbstractionObjectRefCtx extends Abstraction {

	private static final long serialVersionUID = -6360750602591705714L;

	public static AbstractionObjectRefCtx get(String expression, Collection<String> refs) {
		if(refs == null || refs.size() == 0) return new AbstractionObjectRefCtx(expression, new String[0]);;

		int i = 0;
		String[] array = new String[refs.size()];
		for(String ref : refs)
			array[i++] = ref;

		return new AbstractionObjectRefCtx(expression, array);
	}

	private final String[] ctxRef;

	private AbstractionObjectRefCtx(String expression, String[] ctxRef) {
		super(expression);
		this.ctxRef = ctxRef;
	}

	@Override
	public String toString() {
		return getExpression() + ": " + ctxRef == null ? "none" : Arrays.toString(ctxRef);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(ctxRef);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;

		if(!(obj instanceof AbstractionObjectRefCtx)) return false;
		AbstractionObjectRefCtx other = (AbstractionObjectRefCtx) obj;
		if(!Arrays.equals(ctxRef, other.ctxRef)) return false;
		return true;
	}
}
