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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public final class PrimitiveClazz extends Clazz {

	private static final Logger logger = Logger.getLogger("testful.model");

	private static final long serialVersionUID = -2125532247454284282L;

	public static enum PrimTypes {

		BooleanClass, BooleanType,
		ByteClass, ByteType,
		CharacterClass, CharacterType,
		DoubleClass, DoubleType,
		FloatClass, FloatType,
		IntegerClass, IntegerType,
		LongClass, LongType,
		ShortClass, ShortType;

		public static PrimTypes valueOf(Class<?> c) {
			if(Boolean.class == c) return BooleanClass;
			if(Boolean.TYPE == c) return BooleanType;

			if(Byte.class == c) return ByteClass;
			if(Byte.TYPE == c) return ByteType;

			if(Character.class == c) return CharacterClass;
			if(Character.TYPE == c) return CharacterType;

			if(Double.class == c) return DoubleClass;
			if(Double.TYPE == c) return DoubleType;

			if(Float.class == c) return FloatClass;
			if(Float.TYPE == c) return FloatType;

			if(Integer.class == c) return IntegerClass;
			if(Integer.TYPE == c) return IntegerType;

			if(Long.class == c) return LongClass;
			if(Long.TYPE == c) return LongType;

			if(Short.class == c) return ShortClass;
			if(Short.TYPE == c) return ShortType;

			logger.warning("Cannot find the primitive type of " + c.getName());
			return null;
		}

		boolean isClass() {
			return this == PrimTypes.BooleanClass || this == PrimTypes.ByteClass ||
			this == PrimTypes.CharacterClass || this == PrimTypes.ShortClass ||
			this == PrimTypes.IntegerClass || this == PrimTypes.LongClass ||
			this == PrimTypes.FloatClass || this == PrimTypes.DoubleClass;
		}

		PrimTypes other() {
			switch(this) {
			case BooleanClass:
				return BooleanType;
			case BooleanType:
				return BooleanClass;

			case ByteClass:
				return ByteType;
			case ByteType:
				return ByteClass;

			case CharacterClass:
				return CharacterType;
			case CharacterType:
				return CharacterClass;

			case DoubleClass:
				return DoubleType;
			case DoubleType:
				return DoubleClass;

			case FloatClass:
				return FloatType;
			case FloatType:
				return FloatClass;

			case IntegerClass:
				return IntegerType;
			case IntegerType:
				return IntegerClass;

			case LongClass:
				return LongType;
			case LongType:
				return LongClass;

			case ShortClass:
				return ShortType;
			case ShortType:
				return ShortClass;

			default:
				logger.warning("Primitive type not known: " + this);
				return null;
			}
		}


		/* (non-Javadoc)
		 * @see java.lang.Enum#toString()
		 */
		@Override
		public String toString() {
			switch(this) {
			case BooleanClass:
				return Boolean.class.getName();
			case BooleanType:
				return Boolean.TYPE.getName();

			case ByteClass:
				return Byte.class.getName();
			case ByteType:
				return Byte.TYPE.getName();

			case CharacterClass:
				return Character.class.getName();
			case CharacterType:
				return Character.TYPE.getName();

			case DoubleClass:
				return Double.class.getName();
			case DoubleType:
				return Double.TYPE.getName();

			case FloatClass:
				return Float.class.getName();
			case FloatType:
				return Float.TYPE.getName();

			case IntegerClass:
				return Integer.class.getName();
			case IntegerType:
				return Integer.TYPE.getName();

			case LongClass:
				return Long.class.getName();
			case LongType:
				return Long.TYPE.getName();

			case ShortClass:
				return Short.class.getName();
			case ShortType:
				return Short.TYPE.getName();

			default:
				logger.warning("Primitive type not known: " + this);
				return null;
			}

		}
	};

	private final PrimTypes realType;

	private final PrimitiveClazz clazzType;
	private final PrimitiveClazz clazzObject;

	/**
	 * Create a primitive Class
	 *
	 * @param cluster : the test cluster
	 * @param type : the Class type (e.g., Integer)
	 */
	private PrimitiveClazz(AtomicInteger idGenerator, PrimTypes type, Clazz[] assignableTo) {
		super(idGenerator.incrementAndGet(), type.toString(), false);
		realType = type;
		this.assignableTo = assignableTo;
		PrimitiveClazz other = new PrimitiveClazz(idGenerator, this);

		clazzObject = type.isClass() ? this : other;
		clazzType = type.isClass() ? other : this;
	}

	private PrimitiveClazz(AtomicInteger idGenerator, PrimitiveClazz other) {
		super(idGenerator.incrementAndGet(), other.realType.other().toString(), false);
		realType = other.realType.other();
		assignableTo = other.assignableTo;

		clazzObject = realType.isClass() ? this : other;
		clazzType = realType.isClass() ? other : this;
	}

	@Override
	public Clazz getReferenceClazz() {
		return clazzObject;
	}

	public Clazz getPrimitiveClazz() {
		return clazzType;
	}

	/**
	 * Returns the type
	 */
	public PrimTypes getType() {
		return realType;
	}

	/** true if using the class-type of the primitive */
	public boolean isClass() {
		return realType.isClass();
	}

	public String getCast() {
		return "(" + clazzType.getClassName() + ")";
	}

	static PrimitiveClazz[] createPrimitive(AtomicInteger idGenerator) {
		PrimitiveClazz[] ret = new PrimitiveClazz[16];

		PrimitiveClazz[] equiv = new PrimitiveClazz[2];
		PrimitiveClazz o = new PrimitiveClazz(idGenerator, PrimTypes.BooleanClass, equiv);
		ret[0] = equiv[0] = o.clazzObject;
		ret[1] = equiv[1] = o.clazzType;

		equiv = new PrimitiveClazz[2];
		PrimitiveClazz c = new PrimitiveClazz(idGenerator, PrimTypes.CharacterClass, equiv);
		ret[2] = equiv[0] = c.clazzObject;
		ret[3] = equiv[1] = c.clazzType;

		equiv = new PrimitiveClazz[12];
		PrimitiveClazz b = new PrimitiveClazz(idGenerator, PrimTypes.ByteClass, equiv);
		ret[4] = equiv[0] = b.clazzObject;
		ret[5] = equiv[1] = b.clazzType;

		PrimitiveClazz i = new PrimitiveClazz(idGenerator, PrimTypes.IntegerClass, equiv);;
		ret[6] = equiv[2] = i.clazzObject;
		ret[7] = equiv[3] = i.clazzType;

		PrimitiveClazz s = new PrimitiveClazz(idGenerator, PrimTypes.ShortClass, equiv);
		ret[8] = equiv[4] = s.clazzObject;
		ret[9] = equiv[5] = s.clazzType;

		PrimitiveClazz l = new PrimitiveClazz(idGenerator, PrimTypes.LongClass, equiv);
		ret[10] = equiv[6] = l.clazzObject;
		ret[11] = equiv[7] = l.clazzType;

		PrimitiveClazz f = new PrimitiveClazz(idGenerator, PrimTypes.FloatClass, equiv);
		ret[12] = equiv[8] = f.clazzObject;
		ret[13] = equiv[9] = f.clazzType;

		PrimitiveClazz d = new PrimitiveClazz(idGenerator, PrimTypes.DoubleClass, equiv);
		ret[14] = equiv[10] = d.clazzObject;
		ret[15] = equiv[11] = d.clazzType;

		return ret;
	}

	public Object cast(Object object) {
		if(object instanceof Number) {
			Number num = (Number) object;
			switch(realType) {
			case ByteClass:
			case ByteType:
				return num.byteValue();
			case ShortClass:
			case ShortType:
				return num.shortValue();
			case IntegerClass:
			case IntegerType:
				return num.intValue();
			case LongClass:
			case LongType:
				return num.longValue();
			case FloatClass:
			case FloatType:
				return num.floatValue();
			case DoubleClass:
			case DoubleType:
				return num.doubleValue();
			}
		}

		if(object instanceof Character) return ((Character) object).charValue();

		if(object instanceof Boolean) return ((Boolean) object).booleanValue();

		logger.warning("Cannot perform the conversion for type: " + object.getClass().getName());
		return null;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!super.equals(obj)) return false;
		if(!(obj instanceof PrimitiveClazz)) return false;

		return realType.equals(((PrimitiveClazz) obj).realType);
	}
}
