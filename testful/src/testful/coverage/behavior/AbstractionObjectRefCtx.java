/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
