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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Object-state abstractor.
 * It leverages other abstractors, which analyze the state of the object (through observers)
 * and detect the abstracted state.
 * @author matteo
 */
public class AbstractorObjectState implements Serializable {

	private static final long serialVersionUID = -9199070129791166496L;

	private final Abstractor[] state;

	/**
	 * Create an object abstractor
	 * @param state the abstraction to be used to observe the state of the object
	 * 				and determine the abstract state
	 */
	public AbstractorObjectState(Abstractor[] state) {
		this.state = state;
	}

	/**
	 * Calculate the abstraction for a particular object
	 * @param _this the object to abstract
	 * @return the abstraction for the given object
	 */
	public Abstraction get(Object _this) {
		if(_this == null) return new AbstractionObjectReference("", true);

		// update ctx
		Map<String, Object> ctx = new HashMap<String, Object>();
		ctx.put("this", _this);

		int n = 0;
		Abstraction[] abs = new Abstraction[state.length];
		for(Abstractor abstractor : state)
			abs[n++] = abstractor.get(ctx);

		return new AbstractionObjectState(_this.getClass().getName(), abs);
	}
}
