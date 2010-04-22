package testful.mutation;

import testful.model.FaultyExecutionException;

/**
 * Used for interrupting the execution of the test.
 * 
 * @author matteo
 */
public class TestStoppedException extends FaultyExecutionException {

	/** if set to true, stops the execution */
	public static volatile boolean stop = false;

	/** stores the name of the stop field */
	public static final String STOP_NAME = "stop";

	private static final long serialVersionUID = -7939628914571439861L;

	public TestStoppedException() {
		super("Test stopped", null);
	}
}
