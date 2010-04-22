package testful;

public class JMLUtils {

	/**
	 * Checks if two doubles (a, b) are equals (given the eps threshold).<br>
	 * If the threshold is infinite, the result is true; similarly, if the
	 * threshold is NaN, the result is false.<br>
	 * If a is NaN, the result is true iff b is NaN<br>
	 * If a is +Infinite (-Infinite), the result is true iff b is +Infinite
	 * (-Infinite)<br>
	 * Otherwize, call d the value Math(a - b); <br>
	 * <ul>
	 * <li>if eps <= 0 the result is true iff d == 0</li>
	 * <li>if epx > 0 the result is true iff d <= Math.max(eps, eps *
	 * Math.min(Math.abs(a), Math.abs(b)))</li>
	 * </ul>
	 * 
	 * @param a the first double
	 * @param b the second double
	 * @param eps the threshold
	 * @return true if a,b are similar
	 */
	public static/* @ pure @ */boolean doubleCheck(double a, double b, double eps) {

		if(Double.isInfinite(eps)) return true;
		if(Double.isNaN(eps)) return false;

		if(Double.isNaN(a) && Double.isNaN(b)) return true;
		if(Double.isInfinite(a) && Double.isInfinite(b)) {
			if(a > 0 && b > 0) return true;
			if(a < 0 && b < 0) return true;
			return false;
		}

		if(eps <= 0) return a == b;

		return Math.abs(a - b) <= Math.max(eps, eps * Math.min(Math.abs(a), Math.abs(b)));
	}

}
