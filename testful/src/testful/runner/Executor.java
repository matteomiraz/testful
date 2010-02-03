package testful.runner;

import java.io.Serializable;

/**
 * Interface for the test executor.<br/>
 * Classes implementing this interface must not execute directly the test, but
 * must create another instance of another class, loaded using the specified
 * class loader, and use it to run the test.
 * 
 * @author matteo
 */
public interface Executor extends Serializable {

	/**
	 * Run the test, using the specified class loader.
	 * @param stopOnBug true if the execution must stop as soon as the first bug
	 *          is revealed
	 * 
	 * @return the number of bug found
	 * @throws ClassNotFoundException if there is any problem resolving classes
	 */
	public int execute(boolean stopOnBug) throws ClassNotFoundException;

	/**
	 * Returns the length of the test
	 * @return the length of the test
	 */
	public int getTestLength();
}
