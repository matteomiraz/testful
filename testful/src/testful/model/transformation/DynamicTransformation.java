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

package testful.model.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.ResetRepository;
import testful.model.Test;

/**
 * Transform a test using dynamic information ({@link OperationResult}):
 * <ul>
 *   <li>Invalid operations are removed</li>
 *   <li>Valid operations are kept</li>
 *   <li>After faulty operations, a ResetRepository is inserted</li>
 * </ul>
 * @author matteo
 */
public class DynamicTransformation implements TestTransformation {

	public static final DynamicTransformation singleton = new DynamicTransformation();

	private static final Logger logger = Logger.getLogger("testful.model.transformation");

	@Override
	@SuppressWarnings("unused")
	public Test perform(Test test) {
		List<Operation> ops = new ArrayList<Operation>(test.getTest().length);

		for (Operation o : test.getTest()) {
			OperationResult info = (OperationResult) o.getInfo(OperationResult.KEY);
			if(info != null) {
				switch(info.getStatus()) {

				case NOT_EXECUTED:
					if(TestFul.DEBUG) logger.warning("The operation has not been executed. Skipping it.");
					break;

				case PRECONDITION_ERROR:
					break;

				case SUCCESSFUL:
				case EXCEPTIONAL:
					ops.add(o);
					break;

				case POSTCONDITION_ERROR:
					ops.add(o);
					ops.add(ResetRepository.singleton);
					break;
				}

			} else {
				if(TestFul.DEBUG && (o instanceof Invoke || o instanceof CreateObject))
					logger.log(Level.WARNING, "An OperationResult is expected, but it is not found", new NullPointerException());

				ops.add(o);
			}
		}

		return new Test(test.getCluster(), test.getReferenceFactory(), ops.toArray(new Operation[ops.size()]));
	}
}
