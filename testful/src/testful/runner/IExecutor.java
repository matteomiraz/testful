/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
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

package testful.runner;

import java.io.Serializable;

/**
 * Interface to execution a computation with some inputs and collect the result.
 * @param <I> The type of information required by the execution manager
 * @param <R> The result of the execution manager
 * @author matteo
 */
public interface IExecutor<I extends Serializable, R extends Serializable> {

	/**
	 * Provides the input to use for the computation
	 * @param input the input to use for the computation
	 */
	void setInput(I input);

	/**
	 * Execute the computation and returns the result.
	 * Before executing this method, method {@link IExecutor#setInput(Serializable)} must be called.
	 * @return the desired result
	 * @throws Exception if something goes wrong
	 */
	R execute() throws Exception;

}
