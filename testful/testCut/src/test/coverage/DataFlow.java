package test.coverage;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DataFlow {

	public static class Inner {
		private int ia, ib;

		public int getIA()        { return ia;    }
		public void setIA(int ia) { this.ia = ia; }

		public int getIB()        { return ib;    }
		public void setIB(int ib) { this.ib = ib; }
	}

	private int a;
	private Inner i = new Inner();

	public int getA() {
		return a;
	}

	public void setA(int a) {
		this.a = a;
	}

	public Inner getI() {
		return i;
	}

	public void setI(Inner i) {
		this.i = i;
	}

	public int getIA()        { return i.getIA(); }
	public void setIA(int ia) { i.setIA(ia);      }

	public int getIB()        { return i.getIB(); }
	public void setIB(int ib) { i.setIB(ib);      }

	/**
	 * Skip the instrumentation: the two uses are using the same def, hence
	 * the complete control-flow graph coverage ensures the complete data-flow graph coverage
	 */
	public static void local1Def2Uses(boolean u) {

		int v = 1;          // def

		if(u) Math.abs(v);  // use1
		else  Math.abs(v);  // use2
	}

	/**
	 * In this case the complete control-flow graph coverage does not ensure a complete data-flow graph coverage.
	 * In fact, one definition can kill another other definition.
	 */
	public static void local2Defs1Use(boolean d) {

		int v = 0;        	// def1
		if(d) v = 1;        // def2

		Math.abs(v);        // use
	}

	/**
	 * The complete control-flow graph coverage DOES NOT ensures a complete data-flow coverage.
	 * In this case, it is required to instrument defs and uses to collect the data-flow coverage.
	 */
	public static void local2Defs2Uses(boolean d, boolean u) {

		int v;
		if(d) v = 0;       // def1
		else  v = 1;       // def2

		if(u) Math.abs(v); // use1
		else  Math.abs(v); // use2
	}

	public static void usePropagation(boolean d) {

		float v;  //defs
		if(d) v = 1.0f;
		else  v = 7.0f;

		if(d) Math.abs(v);
		else Math.abs((int)v);

		if(v == 7.0d) Math.abs(0);
		else Math.abs(1);
	}
}
