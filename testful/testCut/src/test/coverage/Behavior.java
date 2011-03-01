package test.coverage;

public class Behavior {

	public static void sMethod0(boolean p0, boolean p1) { }

	public static void sMethod1(int p0, int p1) { }

	public static void sMethod2(Object p0, Object p1) { }

	public static void sMethod3(String p0, String p1) { }

	// ------------
	
	public Behavior() { }

	public Behavior(boolean p) { }

	public Behavior(int p) { this.n = p; }
	
	public Behavior(Object o) { }

	public Behavior(String s) { }

	public Behavior(Behavior b) { this.n = b.n; }

	// ------------

	private int n = 0;
	
	public int getN() { return n; }

	public int getThree() { return 3; }

	public void method0(boolean increment) { 
		if(increment) n++;
		else n--;
	}

	public void method1(int p0, int p1) { }

	public void method2(Object p0, Object p1) { }

	public void method3(String p0, String p1) { }
	
}