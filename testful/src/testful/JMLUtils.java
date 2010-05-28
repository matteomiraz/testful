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
