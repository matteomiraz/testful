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

package testful.model.faults;

import java.io.Serializable;

/**
 * This exception represents a faulty execution of the class under testing.<br>
 *
 * Internally, this exception must store the stack trace of the fault.
 * This way, one can compare two instances of this exception and determine
 * if they share the same cause or not. <br>
 *
 * @author matteo
 */
public interface FaultyExecutionException extends Serializable {

	/**
	 * @see java.lang.Throwable#hashCode()
	 */
	String getMessage();

	/**
	 * @see java.lang.Throwable#getStackTrace()
	 */
	StackTraceElement[] getStackTrace();

	/**
	 * @see java.lang.Throwable#setStackTrace(StackTraceElement[])
	 */
	void setStackTrace(StackTraceElement[] pruned);

	/**
	 * @see java.lang.Throwable#fillInStackTrace()
	 */
	Throwable fillInStackTrace();

	/**
	 * @see java.lang.Throwable#getCause()
	 */
	Throwable getCause();
}
