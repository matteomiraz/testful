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


package testful.regression;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.ResetRepository;
import testful.model.Test;

/**
 * Use dynamic analysis to simplify a test and remove operations with false
 * preconditions (this reduces the contract coverage).<br>
 * For example,
 * <ol>
 * <li>integer_0 = -10;</li>
 * <li>integer_0 = noNegativeParameters(integer_0); //<b>FALSE precondition</b></li>
 * <li>integer_1 = foo(integer_0); // <b>throws a legal exception</b></li>
 * </ol>
 * becomes
 * <ol>
 * <li>integer_0 = -10;</li>
 * <li>//<i>skip the invalid operation</i></li>
 * <li>foo(integer_0); // <i>skip the assignment since an exception is thrown</i></li>
 * </ul>
 * </li>
 * 
 * @author matteo
 */
public class TestSimplifier {
	/**
	 * This class relies on the following Operation Information:
	 * <ul> <li>OperationStatus</li>
	 *      <li>OperationPrimitiveResult</li> </ul>
	 * If tests will not have those information, this class does not do anything.
	 */
	public Test process(Test test) {
		return new Test(test.getCluster(), test.getReferenceFactory(), process(test.getTest()));
	}

	public Operation[] process(final Operation[] test) {
		List<Operation> ops = new ArrayList<Operation>();
		for(Operation op : test) {
			OperationResult status = (OperationResult) op.getInfo(OperationResult.KEY);

			if(status != null) {
				switch(status.getStatus()) {
				case NOT_EXECUTED: // skip invalid & not executed operations
				case PRECONDITION_ERROR:
					continue;

				case POSTCONDITION_ERROR: // trigger a reset after a postcondition error
					ops.add(op);
					ops.add(ResetRepository.singleton);
					break;

				case SUCCESSFUL: // add normal operations as-is
					ops.add(op);
					break;

				case EXCEPTIONAL: // when an exception is thrown, the target is not set

					if(op instanceof CreateObject)
						ops.add(new CreateObject(null, ((CreateObject) op).getConstructor(), ((CreateObject) op).getParams()));

					else if(op instanceof Invoke)
						ops.add(new Invoke(null, ((Invoke) op).getThis(), ((Invoke) op).getMethod(), ((Invoke) op).getParams()));

					else
						Logger.getLogger("testful.model").warning("Unexpected operation: " + op.getClass().getCanonicalName());

					break;
				}
			} else ops.add(op);
		}

		return ops.toArray(new Operation[ops.size()]);
	}
}
