package test.coverage;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class DataFlowCtx {

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
