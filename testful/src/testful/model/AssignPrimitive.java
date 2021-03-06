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

package testful.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import testful.TestFul;
import testful.utils.JavaUtils;
import ec.util.MersenneTwisterFast;

public class AssignPrimitive extends Operation {

	private static final long serialVersionUID = 5229328945478937607L;

	private final Reference ref;
	private final Serializable value;

	public AssignPrimitive(Reference ref, Serializable value) {
		super((ref != null ? ref.hashCode() * 29 : 0) + (value != null ? value.hashCode() : 0));

		if(TestFul.DEBUG) {
			if(ref == null) {
				final NullPointerException exc = new NullPointerException("REF cannot be null!");
				TestFul.debug(exc);
				throw exc;
			}

			if(!(ref.getClazz() instanceof PrimitiveClazz || ref.getClazz().getClassName().equals("java.lang.String"))) {
				final ClassCastException exc = new ClassCastException("REF must be a primitive type! It is " + ref.getClazz());
				TestFul.debug(exc);
				throw exc;
			}
		}

		this.ref = ref;
		this.value = value;
	}

	public Reference getTarget() {
		return ref;
	}

	public Serializable getValue() {
		return value;
	}

	@Override
	public String toString() {
		if(value == null) return ref + " = null";
		else if(ref.getClazz() instanceof PrimitiveClazz)  return ref + " = " + ((PrimitiveClazz) ref.getClazz()).getCast() + JavaUtils.escape(value);
		else return ref + " = " + JavaUtils.escape(value);
	}

	@Override
	public Operation adapt(TestCluster cluster, ReferenceFactory refFactory) {
		final AssignPrimitive ret = new AssignPrimitive(refFactory.adapt(ref), value);
		ret.addInfo(getInfos());
		return ret;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof AssignPrimitive)) return false;

		AssignPrimitive other = (AssignPrimitive) obj;
		if(hashCode != other.hashCode) return false;
		return (ref == null ? other.ref == null : ref.equals(other.ref)) &&
		(value == null ? other.value == null : value.equals(other.value));
	}

	@Override
	protected Set<Reference> calculateDefs() {
		Set<Reference> defs = new HashSet<Reference>();

		if(getTarget() != null)
			defs.add(getTarget());

		return defs;
	}

	@Override
	protected Set<Reference> calculateUses() {
		return emptyRefsSet;
	}

	public static AssignPrimitive generate(Clazz c, TestCluster cluster, ReferenceFactory refFactory, MersenneTwisterFast random) {
		if(c instanceof PrimitiveClazz)
			switch(((PrimitiveClazz) c).getType()) {
			case BooleanClass:
			case BooleanType:
				return new AssignPrimitive(refFactory.getReference(c, random), getBoolean(random));

			case ByteClass:
			case ByteType:
				return new AssignPrimitive(refFactory.getReference(c, random), getByte(random));

			case CharacterClass:
			case CharacterType:
				return new AssignPrimitive(refFactory.getReference(c, random), getCharacter(random));

			case DoubleClass:
			case DoubleType:
				return new AssignPrimitive(refFactory.getReference(c, random), getDouble(random));

			case FloatClass:
			case FloatType:
				return new AssignPrimitive(refFactory.getReference(c, random), getFloat(random));

			case IntegerClass:
			case IntegerType:
				return new AssignPrimitive(refFactory.getReference(c, random), getInteger(random));

			case LongClass:
			case LongType:
				return new AssignPrimitive(refFactory.getReference(c, random), getLong(random));

			case ShortClass:
			case ShortType:
				return new AssignPrimitive(refFactory.getReference(c, random), getShort(random));
			}

		if(c.getClassName().equals("java.lang.String")) return new AssignPrimitive(refFactory.getReference(c, random), getString(random));

		return null;
	}

	private static boolean getBoolean(MersenneTwisterFast r) {
		return r.nextBoolean();
	}

	private static byte getByte(MersenneTwisterFast r) {
		float nextFloat = r.nextFloat();

		if(nextFloat < Operation.GEN_BASIC_VALUES) switch(r.nextInt(5)) {
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return -1;
		case 3:
			return Byte.MAX_VALUE;
		case 4:
			return Byte.MIN_VALUE;
		}

		if(nextFloat < Operation.GEN_BASIC_VALUES + Operation.LIMITED_VALUES) return (byte) (r.nextInt(21) - 10);

		return (byte) (r.nextInt(java.lang.Byte.MAX_VALUE - java.lang.Byte.MIN_VALUE) - java.lang.Byte.MIN_VALUE);
	}

	private static final char[] BASE_CHARACTERS = {
		' ', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
	};

	private static final char[] SPECIAL_CHARACTERS = {
		'\0', ' ', '\n', '\r', '\t',
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
		'.', ',', ';', ':', '-',  '+', '?', '!', '\'', '`', '"',
		163, '$', 8364, '%', '&', '/', '(', ')', '=', '^', '#', 176, '[', ']', '{', '}', '~' };

	public static char getCharacter(MersenneTwisterFast r) {
		float nextFloat = r.nextFloat();

		if(nextFloat < Operation.GEN_BASIC_VALUES)
			return BASE_CHARACTERS[r.nextInt(BASE_CHARACTERS.length)];

		if(nextFloat < Operation.GEN_BASIC_VALUES + Operation.LIMITED_VALUES)
			return SPECIAL_CHARACTERS[r.nextInt(SPECIAL_CHARACTERS.length)];

		return (char) r.nextInt(256);
	}

	private static double getDouble(MersenneTwisterFast r) {
		float nextFloat = r.nextFloat();

		if(nextFloat < Operation.GEN_BASIC_VALUES) switch(r.nextInt(10)) {
		case 0:
			return 0.0f;
		case 1:
			return 1.0f;
		case 2:
			return -1.0f;
		case 3:
			return Double.MAX_VALUE;
		case 4:
			return -1 * Double.MAX_VALUE;
		case 5:
			return Double.MIN_VALUE; // number closest to 0+
		case 6:
			return -1 * Double.MIN_VALUE; // number closest to 0-
		case 7:
			return Double.POSITIVE_INFINITY;
		case 8:
			return Double.NEGATIVE_INFINITY;
		case 9:
			return Double.NaN;
		}

		if(nextFloat < Operation.GEN_BASIC_VALUES + Operation.LIMITED_VALUES)
			return (r.nextBoolean() ? 1 : -1) * r.nextDouble();

		return (r.nextBoolean() ? 1 : -1) * r.nextDouble() * Double.MAX_VALUE;
	}

	private static float getFloat(MersenneTwisterFast r) {
		float nextFloat = r.nextFloat();

		if(nextFloat < Operation.GEN_BASIC_VALUES) switch(r.nextInt(10)) {
		case 0:
			return 0.0f;
		case 1:
			return 1.0f;
		case 2:
			return -1.0f;
		case 3:
			return Float.MAX_VALUE;
		case 4:
			return -1 * Float.MAX_VALUE;
		case 5:
			return Float.MIN_VALUE; // number closest to 0+
		case 6:
			return -1 * Float.MIN_VALUE; // number closest to 0+
		case 7:
			return Float.POSITIVE_INFINITY;
		case 8:
			return Float.NEGATIVE_INFINITY;
		case 9:
			return Float.NaN;
		}

		if(nextFloat < Operation.GEN_BASIC_VALUES + Operation.LIMITED_VALUES) return (r.nextBoolean() ? 1 : -1) * r.nextFloat();

		return (r.nextBoolean() ? 1 : -1) * r.nextFloat() * Float.MAX_VALUE;
	}

	private static int getInteger(MersenneTwisterFast r) {
		float nextFloat = r.nextFloat();

		if(nextFloat < Operation.GEN_BASIC_VALUES) switch(r.nextInt(5)) {
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return -1;
		case 3:
			return Integer.MAX_VALUE;
		case 4:
			return Integer.MIN_VALUE;
		}

		if(nextFloat < Operation.GEN_BASIC_VALUES + Operation.LIMITED_VALUES) return r.nextInt(21) - 10;

		return r.nextInt();
	}

	private static long getLong(MersenneTwisterFast r) {
		float nextFloat = r.nextFloat();

		if(nextFloat < Operation.GEN_BASIC_VALUES) switch(r.nextInt(5)) {
		case 0:
			return 0L;
		case 1:
			return 1L;
		case 2:
			return -1L;
		case 3:
			return Long.MAX_VALUE;
		case 4:
			return Long.MIN_VALUE;
		}

		if(nextFloat < Operation.GEN_BASIC_VALUES + Operation.LIMITED_VALUES) return (long) r.nextInt(21) - 10;

		return r.nextLong();
	}

	private static short getShort(MersenneTwisterFast r) {
		float nextFloat = r.nextFloat();

		if(nextFloat < Operation.GEN_BASIC_VALUES) switch(r.nextInt(5)) {
		case 0:
			return 0;
		case 1:
			return 1;
		case 2:
			return -1;
		case 3:
			return Short.MAX_VALUE;
		case 4:
			return Short.MIN_VALUE;
		}

		if(nextFloat < Operation.GEN_BASIC_VALUES + Operation.LIMITED_VALUES) return (short) (r.nextInt(21) - 10);

		return (short) (r.nextInt(java.lang.Short.MAX_VALUE - java.lang.Short.MIN_VALUE) - java.lang.Short.MIN_VALUE);
	}

	public static String getString(MersenneTwisterFast r) {

		final int dim;
		if(r.nextBoolean(.75f)) dim = r.nextInt(10);
		else if(r.nextBoolean(.75f)) dim = r.nextInt(50);
		else dim = r.nextInt(200);

		char str[] = new char[dim];

		for(int i = 0; i < dim; i++)
			str[i] = getCharacter(r);

		return new java.lang.String(str);
	}

	@Override
	public Operation clone() {
		final AssignPrimitive clone = new AssignPrimitive(ref, value);
		clone.addInfo(getInfos());
		return clone;
	}
}
