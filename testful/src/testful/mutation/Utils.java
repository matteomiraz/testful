package testful.mutation;

import testful.model.faults.FaultyExecutionException;


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
		if(value == 0) throw new ZPushException();
	}

	/**
	 * ABS ZPush: if value is zero, throws a postconditionViolation Exception
	 * (i.e., the mutant is killed)
	 */
	public static final void zpush(long value) {
		if(value == 0) throw new ZPushException();
	}

	/**
	 * ABS ZPush: if value is zero, throws a postconditionViolation Exception
	 * (i.e., the mutant is killed)
	 */
	public static final void zpush(float value) {
		if(value == 0) throw new ZPushException();
	}

	/**
	 * ABS ZPush: if value is zero, throws a postconditionViolation Exception
	 * (i.e., the mutant is killed)
	 */
	public static final void zpush(double value) {
		if(value == 0) throw new ZPushException();
	}

	public static class ZPushException extends RuntimeException implements FaultyExecutionException {

		private static final long serialVersionUID = 3739209292146438050L;

	}
}
