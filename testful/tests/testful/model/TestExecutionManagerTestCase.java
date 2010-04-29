package testful.model;

import java.rmi.RemoteException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import testful.SimpleDummyTestCase;
import testful.runner.Context;

/**
 * @author matteo
 *
 */
public class TestExecutionManagerTestCase extends SimpleDummyTestCase {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private Operation[] execute(Test test) throws RemoteException, InterruptedException, ExecutionException {
		OperationResult.insert(test.getTest());
		Context<Operation[], TestExecutionManager> ctx = TestExecutionManager.getContext(getFinder(), test);
		ctx.setStopOnBug(false);
		Future<Operation[]> result = getExec().execute(ctx);

		return result.get();
	}

	public void testSimple1() throws Exception {

		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, mInc, new Reference[] { })
		});

		Operation[] testOperations = execute(test);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);
	}

	public void testSimple2() throws Exception {

		Test test = new Test(cluster, refFactory, new Operation[] {
				new CreateObject(c0, cns, new Reference[] { }),
				new Invoke(null, c0, mInc, new Reference[] { })
		});

		Operation[] ops = execute(test);
		for (int i = 0; i < ops.length; i++)
			ops[i] = ops[i].adapt(cluster, refFactory);
		Test test2 = new Test(cluster, refFactory, ops);

		Operation[] testOperations = execute(test2);
		assertEquals(2, testOperations.length);

		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.SUCCESSFUL, ((OperationResult)testOperations[1].getInfo(OperationResult.KEY)).status);

		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[0].getInfo(OperationResult.KEY)).status);
		assertEquals(OperationResult.Status.NOT_EXECUTED, ((OperationResult)test.getTest()[1].getInfo(OperationResult.KEY)).status);
	}
}
