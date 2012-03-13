package testful.model;

import testful.model.faults.PreconditionViolationException;

public class ArrayAdapterTestCase extends junit.framework.TestCase {

	public void testFul1() throws Exception {
		new intArray(null);
	}

	public void testFul1a() throws Exception {
		intArray a = new intArray();

		new intArray(a == null ? null : a.toArray());
	}

	public void testFul2() throws Exception {

		intArray a0 = new intArray();

		try {
			a0.getHead();
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul3() throws Exception {

		intArray a0 = new intArray();

		try {
			a0.set(-1, 0);
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul4() throws Exception {

		intArray a0 = new intArray();

		try {
			a0.delHead();
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul5() throws Exception {

		intArray a0 = new intArray();

		try {
			a0.setHead(-1);
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul6() throws Exception {

		intArray a0 = new intArray();

		a0.addTail(10);
		assertEquals(10, a0.getHead());
		assertEquals(10, a0.getTail());

		a0.delTail();
	}

	public void testFul7() throws Exception {

		intArray a0 = new intArray();
		intArray a1 = new intArray();

		a0.merge(a1);

	}

	public void testFul8() throws Exception {

		intArray a0 = new intArray();

		try {
			a0.setTail(-1);
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul9() throws Exception {

		java.lang.Integer java_lang_Integer_0 = null, java_lang_Integer_1 = null;
		intArray a0 = null, a1 = null;

		java_lang_Integer_0 = (int)2147483647;
		java_lang_Integer_1 = (int)725958717;
		a0 = new intArray();

		a0.addHead(java_lang_Integer_1);
		assertEquals(725958717, a0.getHead());
		assertEquals(725958717, a0.getTail());

		a0.toArray();
		assertEquals(725958717, a0.getHead());
		assertEquals(725958717, a0.getTail());

		a1 = new intArray(a0 == null ? null : a0.toArray());
		assertEquals(725958717, a1.getHead());
		assertEquals(725958717, a1.getTail());

		a0.setTail(java_lang_Integer_0);
		assertEquals(2147483647, a0.getHead());
		assertEquals(2147483647, a0.getTail());

	}

	public void testFul10() throws Exception {

		intArray a0 = new intArray();
		intArray a1 = new intArray();

		a1.addHead(-1);
		assertEquals(-1, a1.getHead());
		assertEquals(-1, a1.getTail());

		a0.addTail(-2147480649);
		assertEquals(-2147480649, a0.getHead());
		assertEquals(-2147480649, a0.getTail());

		a0.delHead();

		int tmp0 = a1.get(0);
		assertEquals(-1, tmp0);
		assertEquals(-1, a1.getHead());
		assertEquals(-1, a1.getTail());

		int tmp1 = a1.getTail();
		assertEquals(-1, tmp1);
		assertEquals(-1, a1.getHead());
		assertEquals(-1, a1.getTail());

		try {
			a0.get(-1);
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul11() throws Exception {

		intArray a0 = new intArray();

		try {
			a0.set(2147483647, 2147483647);
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul12() throws Exception {

		intArray a0 = new intArray();

		try {
			a0.getTail();
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul13() throws Exception {

		intArray a0 = new intArray();

		try {
			a0.delTail();
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul14() throws Exception {

		intArray a0 = new intArray();

		try {
			a0.get(2147483647);
			fail("Expecting an exception");
		} catch (Exception e) {
		}
	}

	public void testFul15() throws Exception {

		java.lang.Integer java_lang_Integer_0 = null, java_lang_Integer_1 = null, java_lang_Integer_2 = null;
		intArray a0 = null, a1 = null, a2 = null;

		java_lang_Integer_0 = (int)-1;
		java_lang_Integer_1 = (int)0;
		java_lang_Integer_2 = (int)5;
		a0 = new intArray();

		a0.addTail(java_lang_Integer_0);
		assertEquals(-1, a0.getHead());
		assertEquals(-1, a0.getTail());

		try {
			a0.merge(a1);
			fail("Expecting a java.lang.NullPointerException");
		} catch(java.lang.NullPointerException e) {
			assertEquals(-1, a0.getHead());
			assertEquals(-1, a0.getTail());
		}

		a0.setHead(java_lang_Integer_2);
		assertEquals(5, a0.getHead());
		assertEquals(5, a0.getTail());

		a0.set(java_lang_Integer_1, java_lang_Integer_2);
		assertEquals(5, a0.getHead());
		assertEquals(5, a0.getTail());

		a2 = new intArray(a0 == null ? null : a0.toArray());
		assertEquals(5, a2.getHead());
		assertEquals(5, a2.getTail());

	}
}


class intArray {

	private int[] array = null;

	public intArray() { }

	public intArray(int[] array) {
		this.array = array;
	}

	public int[] toArray() { return array; }

	public void merge(intArray o) {
		if(array == null) array = new int[0];
		if(o.array == null) return;

		final int n1 = array.length;
		final int n2 = o.array.length;
		int[] newArray = new int[n1 + n2];

		System.arraycopy(array, 0, newArray, 0, n1);
		System.arraycopy(o.array, 0, newArray, n1, n2);

		array = newArray;
	}

	public void addHead(int o) {
		if(array == null) array = new int[0];

		final int n = array.length;
		int[] newArray = new int[n+1];

		newArray[0] = o;
		System.arraycopy(array, 0, newArray, 1, n);

		array = newArray;
	}

	public void addTail(int o) {
		if(array == null) array = new int[0];

		final int n = array.length;
		int[] newArray = new int[n+1];

		newArray[n] = o;
		System.arraycopy(array, 0, newArray, 0, n);

		array = newArray;
	}

	private void checkAccess(int pos) {
		if(array == null || pos < 0 || pos >= array.length) PreconditionViolationException.Impl.throwException();
	}

	public int getHead() {
		checkAccess(0);
		return array[0];
	}

	public int get(int n) {
		checkAccess(n);
		return array[n];
	}

	public int getTail() {
		final int pos = array == null ? -1 : array.length - 1;
		checkAccess(pos);
		return array[pos];
	}

	public void setHead(int o) {
		checkAccess(0);
		array[0] = o;
	}

	public void set(int n, int o) {
		checkAccess(n);
		array[n] = o;
	}

	public void setTail(int o) {
		final int pos = array.length - 1;
		checkAccess(pos);
		array[pos] = o;
	}

	public void delHead() {
		checkAccess(0);

		int[] newArray = new int[array.length-1];
		System.arraycopy(array, 1, newArray, 0, newArray.length);

		array = newArray;
	}

	public void delTail() {
		final int pos = array.length - 1;
		checkAccess(pos);

		int[] newArray = new int[pos];
		System.arraycopy(array, 0, newArray, 0, pos);

		array = newArray;
	}
}
