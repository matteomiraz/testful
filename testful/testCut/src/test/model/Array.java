package test.model;

/**
 * Class to test the Array compatibility
 * @author matteo
 */
public class Array {

	public static Array a;
	public static Array[] a1;
	public static int i;
	public static int[] i1;

	public Array() { }
	public Array(int[] a) { }

	public void m1(int[] a) { }
	public int[] m2 (int a) { return null; }
	public int[] m3 (int[] a) { return null; }

	public static void m4(int[] a) { }
	public static int[] m5 (int a) { return null; }
	public static int[] m6 (int[] a) { return null; }
}
