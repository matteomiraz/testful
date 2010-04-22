package testful.mutation;

import org.jmlspecs.jmlrac.runtime.JMLAssertionError;
import org.jmlspecs.jmlrac.runtime.JMLEntryPreconditionError;
import org.jmlspecs.jmlrac.runtime.JMLInternalPreconditionError;
import org.jmlspecs.jmlrac.runtime.JMLInvariantError;
import org.jmlspecs.jmlrac.runtime.JMLPostconditionError;

import testful.model.ExceptionRaisedException;
import testful.model.FaultyExecutionException;
import testful.model.InternalPreConditionViolationException;
import testful.model.InvariantViolationException;
import testful.model.PostConditionViolationException;

/**
 * Just a bunch of useful methods
 */
public class Utils {

	static final String CUR_MUTATION_PREFIX = "__cur_mutation_";
	static final String CUR_MUTATION_SUFFIX = "__";

	public static final String CONFIG_CLASS = "testful.mutation.Config";

	public static final String EXECUTED_MUTANTS = "__executed_mutants__";

	public static String getCurField(String className) {
		return CUR_MUTATION_PREFIX + encodeClassName(className) + CUR_MUTATION_SUFFIX;
	}

	public static String getMaxField(String className) {
		return "__max_mutation_" + encodeClassName(className) + "__";
	}

	public static String encodeClassName(String className) {
		return className.replaceAll("_", "__").replace('.', '_');
	}

	public static String decodeClassName(String className) {
		return className.replace('_', '.').replaceAll("\\.\\.", "_");
	}


	/**
	 * ABS ZPush: if value is zero, throws a postconditionViolation Exception
	 * (i.e., the mutant is killed)
	 */
	public static final void zpush(int value) {
		if(value == 0) throw new PostConditionViolationException("ZPush Mutation", null);
	}

	/**
	 * ABS ZPush: if value is zero, throws a postconditionViolation Exception
	 * (i.e., the mutant is killed)
	 */
	public static final void zpush(long value) {
		if(value == 0) throw new PostConditionViolationException("ZPush Mutation", null);
	}

	/**
	 * ABS ZPush: if value is zero, throws a postconditionViolation Exception
	 * (i.e., the mutant is killed)
	 */
	public static final void zpush(float value) {
		if(value == 0) throw new PostConditionViolationException("ZPush Mutation", null);
	}

	/**
	 * ABS ZPush: if value is zero, throws a postconditionViolation Exception
	 * (i.e., the mutant is killed)
	 */
	public static final void zpush(double value) {
		if(value == 0) throw new PostConditionViolationException("ZPush Mutation", null);
	}

	/**
	 * Converts the exception into a testful exception.
	 * 
	 * @param exc the exception thrown
	 * @param hasContracts true if the class has contracts
	 * @throws Throwable this method always throws an exception, either a testful
	 *           one or a plain one.
	 */
	public static void processException(Throwable exc, boolean hasContracts) throws Throwable {

		if(!hasContracts) return;

		// nested call..
		if(exc instanceof FaultyExecutionException) throw (FaultyExecutionException) exc;

		Throwable fault;
		if(!(exc instanceof JMLAssertionError)) fault = exc; // it's a user-defined exception in a class with contracts: it's ok!
		else if(exc instanceof JMLEntryPreconditionError) fault = exc; // it's a precondition violation: throwing the exception
		else  if(exc instanceof JMLInternalPreconditionError) fault = new InternalPreConditionViolationException(exc.getMessage(), exc);
		else if(exc instanceof JMLPostconditionError) fault = new PostConditionViolationException(exc.getMessage(), exc);
		else if(exc instanceof JMLInvariantError) fault = new InvariantViolationException(exc.getMessage(), exc);
		else if(exc instanceof JMLAssertionError) fault = new ExceptionRaisedException(exc.getMessage(), exc);
		else fault = new ExceptionRaisedException(exc);

		throw fault;
	}

}
