package apache;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.Serializable;
import java.math.BigInteger;

/**
 * Representation of a rational number.
 *
 * implements Serializable since 2.0
 * 
 * @since 1.1
 * @version $Revision: 777526 $ $Date: 2009-05-22 15:55:22 +0200 (Fri, 22 May 2009) $
 */
public class Fraction extends Number implements Serializable {
	private static final long	serialVersionUID	= -7571263607986437088L;

	
    /** A fraction representing "2 / 1". */
    public static final Fraction TWO = new Fraction(2, 1);

    /** A fraction representing "1". */
    public static final Fraction ONE = new Fraction(1, 1);

    /** A fraction representing "0". */
    public static final Fraction ZERO = new Fraction(0, 1);

    /** A fraction representing "4/5". */
    public static final Fraction FOUR_FIFTHS = new Fraction(4, 5);

    /** A fraction representing "1/5". */
    public static final Fraction ONE_FIFTH = new Fraction(1, 5);

    /** A fraction representing "1/2". */
    public static final Fraction ONE_HALF = new Fraction(1, 2);

    /** A fraction representing "1/4". */
    public static final Fraction ONE_QUARTER = new Fraction(1, 4);

    /** A fraction representing "1/3". */
    public static final Fraction ONE_THIRD = new Fraction(1, 3);

    /** A fraction representing "3/5". */
    public static final Fraction THREE_FIFTHS = new Fraction(3, 5);

    /** A fraction representing "3/4". */
    public static final Fraction THREE_QUARTERS = new Fraction(3, 4);

    /** A fraction representing "4/5". */
    public static final Fraction TWO_FIFTHS = new Fraction(4, 5);

    /** A fraction representing "2/4". */
    public static final Fraction TWO_QUARTERS = new Fraction(2, 4);

    /** A fraction representing "2/3". */
    public static final Fraction TWO_THIRDS = new Fraction(2, 3);

    /** A fraction representing "-1 / 1". */
    public static final Fraction MINUS_ONE = new Fraction(-1, 1);
	
	/** The denominator. */
	private final int denominator;

	/** The numerator. */
	private final int numerator;

	/**
	 * Create a fraction given the double value.
	 * @param value the double value to convert to a fraction.
	 * @throws Exception 
	 * @throws FractionConversionException if the continued fraction failed to
	 *         converge.
	 */
	//@ ensures testful.JMLUtils.doubleCheck(this.doubleValue(), value, eps); 
	//@ signals (ArithmeticException e) true;
	public Fraction(double value) throws ArithmeticException {
		this(value, 1.0e-5, 100);
	}

	/**
	 * Create a fraction given the double value and maximum error allowed.
	 * <p>
	 * References:
	 * <ul>
	 * <li><a href="http://mathworld.wolfram.com/ContinuedFraction.html">
	 * Continued Fraction</a> equations (11) and (22)-(26)</li>
	 * </ul>
	 * </p>
	 * @param value the double value to convert to a fraction.
	 * @param epsilon maximum error allowed.  The resulting fraction is within
	 *        <code>epsilon</code> of <code>value</code>, in absolute terms.
	 * @param maxIterations maximum number of convergents
	 * @throws Exception 
	 * @throws FractionConversionException if the continued fraction failed to
	 *         converge.
	 */
	//@ requires !Double.isNaN(epsilon);
	//@ ensures testful.JMLUtils.doubleCheck(this.doubleValue(), value, epsilon); 
	//@ signals (ArithmeticException e) true;
	public Fraction(double value, double epsilon, int maxIterations) throws ArithmeticException {
		this(value, epsilon, Integer.MAX_VALUE, maxIterations);
	}

	/**
	 * Create a fraction given the double value and maximum denominator.
	 * <p>
	 * References:
	 * <ul>
	 * <li><a href="http://mathworld.wolfram.com/ContinuedFraction.html">
	 * Continued Fraction</a> equations (11) and (22)-(26)</li>
	 * </ul>
	 * </p>
	 * @param value the double value to convert to a fraction.
	 * @param maxDenominator The maximum allowed value for denominator
	 * @throws Exception 
	 * @throws FractionConversionException if the continued fraction failed to
	 *         converge
	 */
	/*@ requires maxDenominator > 1;
      @ ensures testful.JMLUtils.doubleCheck(this.doubleValue(), value, 1.0/(maxDenominator-1));
      @ signals (ArithmeticException e) true;
      @*/
	public Fraction(double value, int maxDenominator) throws ArithmeticException {
		this(value, 0, maxDenominator, 100);
	}

	/**
	 * Create a fraction given the double value and either the maximum error
	 * allowed or the maximum number of denominator digits.
	 * <p>
	 *
	 * NOTE: This constructor is called with EITHER
	 *   - a valid epsilon value and the maxDenominator set to Integer.MAX_VALUE
	 *     (that way the maxDenominator has no effect).
	 * OR
	 *   - a valid maxDenominator value and the epsilon value set to zero
	 *     (that way epsilon only has effect if there is an exact match before
	 *     the maxDenominator value is reached).
	 * </p><p>
	 *
	 * It has been done this way so that the same code can be (re)used for both
	 * scenarios. However this could be confusing to users if it were part of
	 * the public API and this constructor should therefore remain PRIVATE.
	 * </p>
	 *
	 * See JIRA issue ticket MATH-181 for more details:
	 *
	 *     https://issues.apache.org/jira/browse/MATH-181
	 *
	 * @param value the double value to convert to a fraction.
	 * @param epsilon maximum error allowed.  The resulting fraction is within
	 *        <code>epsilon</code> of <code>value</code>, in absolute terms.
	 * @param maxDenominator maximum denominator value allowed.
	 * @param maxIterations maximum number of convergents
	 * @throws FractionConversionException if the continued fraction failed to
	 *         converge.
	 */
	private Fraction(double value, double epsilon, 
			int maxDenominator, 
			int maxIterations) throws ArithmeticException {

		if(maxIterations > 1000) maxIterations = 1000;
		if(maxDenominator < 2) maxDenominator = 2;
		if(Double.isNaN(value)) throw new ArithmeticException("Cannot use NaN value");
		if(Double.isNaN(epsilon)) throw new ArithmeticException("Cannot use NaN epsilon");
		
		
		long overflow = Integer.MAX_VALUE;
		double r0 = value;
		long a0 = (long)Math.floor(r0);
		if (a0 > overflow) {
			throw new ArithmeticException("overflow");
		}

		// check for (almost) integer arguments, which should not go
		// to iterations.
		if (Math.abs(a0 - value) < epsilon) {
			this.numerator = (int) a0;
			this.denominator = 1;
			return;
		}

		long p0 = 1;
		long q0 = 0;
		long p1 = a0;
		long q1 = 1;

		long p2 = 0;
		long q2 = 1;

		int n = 0;
		boolean stop = false;
		do {
			++n;
			double r1 = 1.0 / (r0 - a0);
			long a1 = (long)Math.floor(r1);
			p2 = (a1 * p1) + p0;
			q2 = (a1 * q1) + q0;
			if ((p2 > overflow) || (q2 > overflow)) {
				throw new ArithmeticException("overflow");
			}

			double convergent = (double)p2 / (double)q2;
			if (n < maxIterations && Math.abs(convergent - value) > epsilon && q2 < maxDenominator) {
				p0 = p1;
				p1 = p2;
				q0 = q1;
				q1 = q2;
				a0 = a1;
				r0 = r1;
			} else {
				stop = true;
			}
		} while (!stop);

		if (n >= maxIterations) {
			throw new ArithmeticException("maxIter");
		}

		if (q2 < maxDenominator) {
			this.numerator = (int) p2;
			this.denominator = (int) q2;
		} else {
			this.numerator = (int) p1;
			this.denominator = (int) q1;
		}

	}

    /**
     * Create a fraction from an int. 
     * The fraction is num / 1.
     * @param num the numerator.
     */
	//@ ensures testful.JMLUtils.doubleCheck(this.doubleValue(), num, eps); 
    public Fraction(int num) {
        this(num, 1);
    }

	/**
	 * Create a fraction given the numerator and denominator.  The fraction is
	 * reduced to lowest terms.
	 * @param num the numerator.
	 * @param den the denominator.
	 * @throws ArithmeticException if the denomiator is <code>zero</code>
	 */
	//@ ensures testful.JMLUtils.doubleCheck(this.doubleValue(), 1.0 * num / den, eps);
	//@ signals (ArithmeticException e) den == 0 || num == Integer.MIN_VALUE || den == Integer.MIN_VALUE;
	public Fraction(int num, int den) {
		if(den == 0)  {
			throw new ArithmeticException("zero denominator");
		}
		if (den < 0) {
			if (num == Integer.MIN_VALUE ||
					den == Integer.MIN_VALUE) {
				throw new ArithmeticException("overflow: can't negate");
			}
			num = -num;
			den = -den;
		}
		// reduce numerator and denominator by greatest common denominator.
		final int d = gcd(num, den);
		if (d > 1) {
			num /= d;
			den /= d;
		}

		// move sign to numerator.
        if (den < 0) {
            num = -num;
            den = -den;
        }
		this.numerator = num;
		this.denominator = den;
	}

	/**
	 * Returns the absolute value of this fraction.
	 * @return the absolute value.
	 */
	//@ ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), Math.abs(doubleValue()), eps);
	//@ signals (ArithmeticException e) this.getNumerator() == Integer.MIN_VALUE;
	public Fraction abs() {
		Fraction ret;
		if (numerator >= 0) {
			ret = this;
		} else {
			ret = negate();
		}
		return ret;        
	}

	/**
	 * Compares this object to another based on size.
	 * @param object the object to compare to
	 * @return -1 if this is less than <tt>object</tt>, +1 if this is greater
	 *         than <tt>object</tt>, 0 if they are equal.
	 */
	//@ requires object != null;
	//@ ensures \result == 0 ==> testful.JMLUtils.doubleCheck(this.doubleValue(), object.doubleValue(), eps) && \result < 0 ==> this.doubleValue() < object.doubleValue() && \result > 0 ==> this.doubleValue() > object.doubleValue(); 
	public int compareTo(Fraction object) {
        long nOd = ((long) numerator) * object.denominator;
        long dOn = ((long) denominator) * object.numerator;
        return (nOd < dOn) ? -1 : ((nOd > dOn) ? +1 : 0);
	}

	/**
	 * Gets the fraction as a <tt>double</tt>. This calculates the fraction as
	 * the numerator divided by denominator.
	 * @return the fraction as a <tt>double</tt>
	 */
	//@ also ensures testful.JMLUtils.doubleCheck(\result, 1.0 * getNumerator() / getDenominator(), eps);
	public /*@ pure @*/ double doubleValue() {
		return (double)numerator / (double)denominator;
	}

	/**
	 * Test for the equality of two fractions.  If the lowest term
	 * numerator and denominators are the same for both fractions, the two
	 * fractions are considered to be equal.
	 * @param other fraction to test for equality to this fraction
	 * @return true if two fractions are equal, false if object is
	 *         <tt>null</tt>, not an instance of {@link Fraction}, or not equal
	 *         to this fraction instance.
	 */
	//@ also requires true;
	//@ ensures \result <==> (other instanceof Fraction && testful.JMLUtils.doubleCheck(this.doubleValue(), ((Fraction)other).doubleValue(), 0));
	public /*@ pure @*/ boolean equals(/*@ nullable @*/ Object other) {
		boolean ret;

		if (this == other) { 
			ret = true;
		} else if (other == null) {
			ret = false;
		} else {
			if(!(other instanceof Fraction)) ret = false;
			else {
				// since fractions are always in lowest terms, numerators and
				// denominators can be compared directly for equality.
				Fraction rhs = (Fraction)other;
				ret = (numerator == rhs.numerator) && (denominator == rhs.denominator);
			}
		}

		return ret;
	}

	/**
	 * Gets the fraction as a <tt>float</tt>. This calculates the fraction as
	 * the numerator divided by denominator.
	 * @return the fraction as a <tt>float</tt>
	 */
	//@ also ensures testful.JMLUtils.doubleCheck(\result, (1.0 * getNumerator()) / (1.0 * getDenominator()), eps);
	public /*@ pure @*/ float floatValue() {
		return (float)doubleValue();
	}

	/**
	 * Access the denominator.
	 * @return the denominator.
	 */
	public /*@ pure @*/ int getDenominator() {
		return denominator;
	}

	/**
	 * Access the numerator.
	 * @return the numerator.
	 */
	public /*@ pure @*/int getNumerator() {
		return numerator;
	}

	/**
	 * Gets a hashCode for the fraction.
	 * @return a hash code value for this object
	 */
	//@ also ensures \result == 37 * (37 * 17 + getNumerator()) + getDenominator();
	public /*@ pure @*/ int hashCode() {
		return 37 * (37 * 17 + getNumerator()) + getDenominator();
	}

	/**
	 * Gets the fraction as an <tt>int</tt>. This returns the whole number part
	 * of the fraction.
	 * @return the whole number fraction part
	 */
	//@ also ensures getDenominator() != 0 && testful.JMLUtils.doubleCheck(\result, (getNumerator() / getDenominator()), eps);
	//@ signals (ArithmeticException e) getDenominator() == 0;
	public /*@ pure @*/ int intValue() {
		return (int)doubleValue();
	}

	/**
	 * Gets the fraction as a <tt>long</tt>. This returns the whole number part
	 * of the fraction.
	 * @return the whole number fraction part
	 */
	//@ also ensures getDenominator() != 0 && testful.JMLUtils.doubleCheck(\result, (getNumerator() / getDenominator()), eps);
	//@ signals (ArithmeticException e) getDenominator() == 0;
	public /*@ pure @*/ long longValue() {
		return (long)doubleValue();
	}

	/**
	 * Return the additive inverse of this fraction.
	 * @return the negation of this fraction.
	 */
	//@ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), -1.0 * this.doubleValue(), eps);
	//@signals (ArithmeticException e) this.getNumerator() == Integer.MIN_VALUE;
	public Fraction negate() {
		if (numerator==Integer.MIN_VALUE) {
			throw new ArithmeticException("overflow: too large to negate");
		}
		return new Fraction(-numerator, denominator);
	}

	/**
	 * Return the multiplicative inverse of this fraction.
	 * @return the reciprocal fraction
	 */
	//Double.isInfinite(\result.doubleValue()) || Double.isInfinite(this.doubleValue()) || Double.isNaN(\result.doubleValue()) || Double.isNaN(this.doubleValue()) || 
	//@ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), 1.0 / this.doubleValue(), eps);
	//@signals (ArithmeticException e) this.getNumerator() == 0 || (this.getNumerator() < 0 && ( this.getNumerator() == Integer.MIN_VALUE || this.getDenominator() == Integer.MIN_VALUE ));
	public Fraction reciprocal() {
		return new Fraction(denominator, numerator);
	}

	/**
	 * <p>Adds the value of this fraction to another, returning the result in reduced form.
	 * The algorithm follows Knuth, 4.5.1.</p>
	 *
	 * @param fraction  the fraction to add, must not be <code>null</code>
	 * @return a <code>Fraction</code> instance with the resulting values
	 * @throws IllegalArgumentException if the fraction is <code>null</code>
	 * @throws ArithmeticException if the resulting numerator or denominator exceeds
	 *  <code>Integer.MAX_VALUE</code>
	 */
	//@ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), this.doubleValue() + fraction.doubleValue(), eps);
	//@signals (IllegalArgumentException e) fraction == null; 
	//@signals (ArithmeticException e) true;
	public Fraction add(Fraction fraction) {
		return addSub(fraction, true /* add */);
	}

    /**
     * Add an integer to the fraction.
     * @param i the <tt>integer</tt> to add.
     * @return this + i
     */
	//@ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), this.doubleValue() + i, eps);
	//@signals (ArithmeticException e) true;
    public Fraction add(final int i) {
        return new Fraction(addAndCheck(numerator, mulAndCheck(i, denominator)), denominator);
    }
	
	/**
	 * <p>Subtracts the value of another fraction from the value of this one, 
	 * returning the result in reduced form.</p>
	 *
	 * @param fraction  the fraction to subtract, must not be <code>null</code>
	 * @return a <code>Fraction</code> instance with the resulting values
	 * @throws IllegalArgumentException if the fraction is <code>null</code>
	 * @throws ArithmeticException if the resulting numerator or denominator
	 *   cannot be represented in an <code>int</code>.
	 */
	//@ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), this.doubleValue() - fraction.doubleValue(), eps);
	//@signals (IllegalArgumentException e) fraction == null; 
	//@signals (ArithmeticException e) true;
	public Fraction subtract(Fraction fraction) {
		return addSub(fraction, false /* subtract */);
	}

    /**
     * Subtract an integer from the fraction.
     * @param i the <tt>integer</tt> to subtract.
     * @return this - i
     */
	//@ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), this.doubleValue() - i, eps);
	//@signals (ArithmeticException e) true;
    public Fraction subtract(final int i) {
        return new Fraction(subAndCheck(numerator, mulAndCheck(i, denominator)), denominator);
    }

    /** 
	 * Implement add and subtract using algorithm described in Knuth 4.5.1.
	 * 
	 * @param fraction the fraction to subtract, must not be <code>null</code>
	 * @param isAdd true to add, false to subtract
	 * @return a <code>Fraction</code> instance with the resulting values
	 * @throws IllegalArgumentException if the fraction is <code>null</code>
	 * @throws ArithmeticException if the resulting numerator or denominator
	 *   cannot be represented in an <code>int</code>.
	 */
	private Fraction addSub(Fraction fraction, boolean isAdd) {
		if (fraction == null) {
			throw new IllegalArgumentException("The fraction must not be null");
		}

		// zero is identity for addition.
		if (numerator == 0) {
			return isAdd ? fraction : fraction.negate();
		}
		if (fraction.numerator == 0) {
			return this;
		}     
		// if denominators are randomly distributed, d1 will be 1 about 61%
		// of the time.
		int d1 = gcd(denominator, fraction.denominator);
		if (d1==1) {
			// result is ( (u*v' +/- u'v) / u'v')
			int uvp = mulAndCheck(numerator, fraction.denominator);
			int upv = mulAndCheck(fraction.numerator, denominator);
			return new Fraction
			(isAdd ? addAndCheck(uvp, upv) : 
				subAndCheck(uvp, upv),
				mulAndCheck(denominator, fraction.denominator));
		}
		// the quantity 't' requires 65 bits of precision; see knuth 4.5.1
		// exercise 7.  we're going to use a BigInteger.
		// t = u(v'/d1) +/- v(u'/d1)
		BigInteger uvp = BigInteger.valueOf(numerator)
		.multiply(BigInteger.valueOf(fraction.denominator/d1));
		BigInteger upv = BigInteger.valueOf(fraction.numerator)
		.multiply(BigInteger.valueOf(denominator/d1));
		BigInteger t = isAdd ? uvp.add(upv) : uvp.subtract(upv);
		// but d2 doesn't need extra precision because
		// d2 = gcd(t,d1) = gcd(t mod d1, d1)
		int tmodd1 = t.mod(BigInteger.valueOf(d1)).intValue();
		int d2 = (tmodd1==0)?d1:gcd(tmodd1, d1);

		// result is (t/d2) / (u'/d1)(v'/d2)
		BigInteger w = t.divide(BigInteger.valueOf(d2));
		if (w.bitLength() > 31) {
			throw new ArithmeticException("overflow: numerator too large after multiply");
		}
		return new Fraction (w.intValue(), 
				mulAndCheck(denominator/d1, 
						fraction.denominator/d2));
	}

	/**
	 * <p>Multiplies the value of this fraction by another, returning the 
	 * result in reduced form.</p>
	 *
	 * @param fraction  the fraction to multiply by, must not be <code>null</code>
	 * @return a <code>Fraction</code> instance with the resulting values
	 * @throws IllegalArgumentException if the fraction is <code>null</code>
	 * @throws ArithmeticException if the resulting numerator or denominator exceeds
	 *  <code>Integer.MAX_VALUE</code>
	 */
	//@ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), this.doubleValue() * fraction.doubleValue(), eps);
	//@signals (IllegalArgumentException e) fraction == null; 
	//@signals (ArithmeticException e) true;
	public Fraction multiply(Fraction fraction) {
		if (fraction == null) {
			throw new IllegalArgumentException("The fraction must not be null");
		}
        if (numerator == 0 || fraction.numerator == 0) {
            return new Fraction(0, 1); // was: ZERO
        }
		// knuth 4.5.1
		// make sure we don't overflow unless the result *must* overflow.
		int d1 = gcd(numerator, fraction.denominator);
		int d2 = gcd(fraction.numerator, denominator);
		return getReducedFraction
		(mulAndCheck(numerator/d1, fraction.numerator/d2),
				mulAndCheck(denominator/d2, fraction.denominator/d1));
	}

    /**
     * Multiply the fraction by an integer.
     * @param i the <tt>integer</tt> to multiply by.
     * @return this * i
     */
	//@ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), this.doubleValue() * i, eps);
	//@signals (ArithmeticException e) true;
    public Fraction multiply(final int i) {
        return new Fraction(mulAndCheck(numerator, i), denominator);
    }

	/**
	 * <p>Divide the value of this fraction by another.</p>
	 *
	 * @param fraction  the fraction to divide by, must not be <code>null</code>
	 * @return a <code>Fraction</code> instance with the resulting values
	 * @throws IllegalArgumentException if the fraction is <code>null</code>
	 * @throws ArithmeticException if the fraction to divide by is zero
	 * @throws ArithmeticException if the resulting numerator or denominator exceeds
	 *  <code>Integer.MAX_VALUE</code>
	 */
	//@ensures fraction != null && testful.JMLUtils.doubleCheck(\result.doubleValue(), this.doubleValue() / fraction.doubleValue(), eps);
	//@signals (IllegalArgumentException e) fraction == null;
	//@signals (ArithmeticException e) true;
	public Fraction divide(Fraction fraction) {
		if (fraction == null) {
			throw new IllegalArgumentException("The fraction must not be null");
		}

		if (fraction.numerator == 0) {
			throw new ArithmeticException("the fraction to divide by must not be zero");
		}
		return multiply(fraction.reciprocal());
	}

    /**
     * Divide the fraction by an integer.
     * @param i the <tt>integer</tt> to divide by.
     * @return this * i
     */
	//@ensures testful.JMLUtils.doubleCheck(\result.doubleValue(), this.doubleValue() / i, eps);
	//@signals (ArithmeticException e) true;
    public Fraction divide(final int i) {
        return new Fraction(numerator, mulAndCheck(denominator, i));
    }

	/**
	 * <p>Creates a <code>Fraction</code> instance with the 2 parts
	 * of a fraction Y/Z.</p>
	 *
	 * <p>Any negative signs are resolved to be on the numerator.</p>
	 *
	 * @param numerator  the numerator, for example the three in 'three sevenths'
	 * @param denominator  the denominator, for example the seven in 'three sevenths'
	 * @return a new fraction instance, with the numerator and denominator reduced
	 * @throws ArithmeticException if the denominator is <code>zero</code>
	 */
	private static Fraction getReducedFraction(int numerator, int denominator) {
		if (denominator == 0) {
			throw new ArithmeticException("The denominator must not be zero");
		}
		if (numerator==0) {
			return new Fraction(0, 1); // was ZERO; // normalize zero.
		}
		// allow 2^k/-2^31 as a valid fraction (where k>0)
		if (denominator==Integer.MIN_VALUE && (numerator&1)==0) {
			numerator/=2; denominator/=2;
		}
		if (denominator < 0) {
			if (numerator==Integer.MIN_VALUE ||
					denominator==Integer.MIN_VALUE) {
				throw new ArithmeticException("overflow: can't negate");
			}
			numerator = -numerator;
			denominator = -denominator;
		}
		// simplify fraction.
		int gcd = gcd(numerator, denominator);
		numerator /= gcd;
		denominator /= gcd;
		return new Fraction(numerator, denominator);
	}

	private static int gcd(final int p, final int q) {
        int u = p;
        int v = q;
        if ((u == 0) || (v == 0)) {
            if ((u == Integer.MIN_VALUE) || (v == Integer.MIN_VALUE)) {
                throw new ArithmeticException("overflow: gcd(" + p + ", " + q + ") is 2^31");
            }
            return (Math.abs(u) + Math.abs(v));
        }
        // keep u and v negative, as negative integers range down to
        // -2^31, while positive numbers can only be as large as 2^31-1
        // (i.e. we can't necessarily negate a negative number without
        // overflow)
        /* assert u!=0 && v!=0; */
        if (u > 0) {
            u = -u;
        } // make u negative
        if (v > 0) {
            v = -v;
        } // make v negative
        // B1. [Find power of 2]
        int k = 0;
        while ((u & 1) == 0 && (v & 1) == 0 && k < 31) { // while u and v are
                                                            // both even...
            u /= 2;
            v /= 2;
            k++; // cast out twos.
        }
        if (k == 31) {
            throw new ArithmeticException("overflow: gcd(" + p + ", " + q + ") is 2^31");
        }
        // B2. Initialize: u and v have been divided by 2^k and at least
        // one is odd.
        int t = ((u & 1) == 1) ? v : -(u / 2)/* B3 */;
        // t negative: u was odd, v may be even (t replaces v)
        // t positive: u was even, v is odd (t replaces u)
        do {
            /* assert u<0 && v<0; */
            // B4/B3: cast out twos from t.
            while ((t & 1) == 0) { // while t is even..
                t /= 2; // cast out twos
            }
            // B5 [reset max(u,v)]
            if (t > 0) {
                u = -t;
            } else {
                v = t;
            }
            // B6/B3. at this point both u and v should be odd.
            t = (v - u) / 2;
            // |u| larger: t positive (replace u)
            // |v| larger: t negative (replace v)
        } while (t != 0);
        return -u * (1 << k); // gcd is u*2^k
	}

	private static int mulAndCheck(int x, int y) {
		long m = ((long)x) * ((long)y);
		if (m < Integer.MIN_VALUE || m > Integer.MAX_VALUE) {
			throw new ArithmeticException("overflow: mul");
		}
		return (int)m;
	}

	private static int addAndCheck(int x, int y) {
		long s = (long)x + (long)y;
		if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
			throw new ArithmeticException("overflow: add");
		}
		return (int)s;
	}

	private static int subAndCheck(int x, int y) {
		long s = (long)x - (long)y;
		if (s < Integer.MIN_VALUE || s > Integer.MAX_VALUE) {
			throw new ArithmeticException("overflow: subtract");
		}
		return (int)s;
	}
	//@ also requires true;
	//@ ensures \result.equals(getNumerator() + " / " + getDenominator() + " (" + doubleValue() + ")");
	public /*@ pure @*/ String toString() {
		return getNumerator() + " / " + getDenominator() + " (" + doubleValue() + ")";
	}
}