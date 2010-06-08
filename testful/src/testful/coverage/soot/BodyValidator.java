/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010 Matteo Miraz
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

package testful.coverage.soot;

import java.util.Map;

import soot.Body;
import soot.BodyTransformer;

/**
 * Performs deep validation on methods
 * @author matteo
 */
public class BodyValidator extends BodyTransformer {

	public static final BodyValidator singleton = new BodyValidator();

	/* (non-Javadoc)
	 * @see soot.BodyTransformer#internalTransform(soot.Body, java.lang.String, java.util.Map)
	 */
	@Override
	@SuppressWarnings("rawtypes")
	protected void internalTransform(Body body, String phaseName, Map options) {

		System.out.println("validateLocals");
		body.validateLocals();

		System.out.println("validateTraps");
		body.validateTraps();

		System.out.println("validateUnitBoxes");
		body.validateUnitBoxes();

		System.out.println("validateValueBoxes");
		body.validateValueBoxes();

		System.out.println("validateUses");
		body.validateUses();

		System.out.println("validate");
		body.validate();
	}
}
