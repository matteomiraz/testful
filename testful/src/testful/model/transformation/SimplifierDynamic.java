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

package testful.model.transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import testful.TestFul;
import testful.model.CreateObject;
import testful.model.Invoke;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.OperationResultExecutionManager;
import testful.model.ResetRepository;
import testful.model.Test;
import testful.runner.DataFinder;

/**
 * Use dynamic analysis ({@link OperationResult}) to simplify a test:
 * <ul>
 *   <li>Invalid operations are removed (this reduces the contract coverage)</li>
 *   <li>Valid operations are kept; if an exception is thrown, the assignment is removed</li>
 *   <li>After faulty operations, a ResetRepository is inserted</li>
 * </ul>
 * <br/>
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
 * @author matteo
 */
public class SimplifierDynamic implements TestTransformation {

	public static final SimplifierDynamic singleton = new SimplifierDynamic ();

	/**
	 * This method relies on the {@link OperationResult} Information.
	 * If it is not present, this class does not do anything.
	 */
	@Override
	public Test perform(Test test) {
		return new Test(test.getCluster(), test.getReferenceFactory(), perform(test.getTest()));
	}

	/**
	 * Like method <code>Test perform(Test test)</code>,
	 * but automatically calculates the {@link OperationResult} Information.
	 */
	public Test perform(DataFinder finder, Test orig, boolean reloadClasses) throws InterruptedException, ExecutionException {
		Operation[] ops = Arrays.copyOf(orig.getTest(), orig.getTest().length);

		OperationResult.insert(ops);
		OperationResultExecutionManager.execute(finder, new Test(orig.getCluster(), orig.getReferenceFactory(), ops), reloadClasses);

		ops = perform(ops);

		OperationResult.remove(ops);

		return new Test(orig.getCluster(), orig.getReferenceFactory(), ops);
	}

	/**
	 * This method relies on the {@link OperationResult} Information.
	 * If it is not present, this class does not do anything.
	 */
	public Operation[] perform(final Operation[] test) {
		List<Operation> ops = new ArrayList<Operation>();

		for (Operation op : test) {
			OperationResult info = (OperationResult) op.getInfo(OperationResult.KEY);
			if(info != null) {
				switch(info.getStatus()) {
				case NOT_EXECUTED: // skip invalid & not executed operations
				case PRECONDITION_ERROR:
					break;

				case POSTCONDITION_ERROR: // trigger a reset after a postcondition error
					ops.add(op);
					ops.add(ResetRepository.singleton);
					break;

				case SUCCESSFUL: // add normal operations as-is
					ops.add(op);
					break;

				case EXCEPTIONAL: // when an exception is thrown, the target is not set

					if(op instanceof CreateObject) {
						CreateObject o = new CreateObject(null, ((CreateObject) op).getConstructor(), ((CreateObject) op).getParams());
						o.addInfo(op.getInfos());
						ops.add(o);

					} else if(op instanceof Invoke) {
						Invoke o = new Invoke(null, ((Invoke) op).getThis(), ((Invoke) op).getMethod(), ((Invoke) op).getParams());
						o.addInfo(op.getInfos());
						ops.add(o);

					} else {
						Logger.getLogger("testful.model").warning("Unexpected operation: " + op.getClass().getName());

					}

					break;
				}

			} else {
				if(TestFul.DEBUG && (op instanceof Invoke || op instanceof CreateObject))
					TestFul.debug("An OperationResult is expected, but it is not found");

				ops.add(op);
			}
		}

		return ops.toArray(new Operation[ops.size()]);
	}
}
