/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
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

package testful.coverage.whiteBox;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;

/**
 * Manages the dynamic stack of calls.
 * @author matteo
 */
public final class Stack implements Serializable {
	private static final long serialVersionUID = 4163032478310472158L;

	private final Integer[] stack;
	private final int hash;
	public Stack(Integer[] s) {
		stack = s;
		hash = Arrays.hashCode(stack);
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;

		if(!(obj instanceof Stack)) return false;

		Stack other = (Stack) obj;
		if(hash != other.hash) return false;

		return Arrays.equals(stack, other.stack);
	}

	@Override
	public String toString() {
		return Arrays.toString(stack);
	}

	public static void write(Stack v, ObjectOutput out) throws IOException {

		if(v != null) {
			out.writeBoolean(true);
			out.writeShort(v.stack.length);
			for (Integer s : v.stack)
				out.writeInt(s);

		} else {
			out.writeBoolean(false);
		}
	}

	public static Stack read(ObjectInput in) throws IOException {
		if(!in.readBoolean()) return null;

		short stackLen = in.readShort();
		Integer[] stack = new Integer[stackLen];
		for (int i = 0; i < stackLen; i++)
			stack[i] = in.readInt();

		return new Stack(stack);
	}
}
