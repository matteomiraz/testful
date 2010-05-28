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

/**
 * This exception represents a faulty execution of the class under testing.<br>
 * Internally, this exception stores the stacktrace of the fault, and thus it is
 * possible to compare two instances of this exception and find out if them are
 * manifestations of the same bug or not.
 * 
 * @author matteo
 */
public abstract class FaultyExecutionException extends RuntimeException {

	private static final long serialVersionUID = -6674159663143126864L;

	public FaultyExecutionException(String msg, Throwable e) {
		super(msg, e);
	}
}
