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

	public int getIA()        { return i.getIA(); }
	public void setIA(int ia) { i.setIA(ia);      }

	public int getIB()        { return i.getIB(); }
	public void setIB(int ib) { i.setIB(ib);      }

	public void prova(boolean b) {

		int v = 0;

		if (b) v = a;

		if(v > 0) {
			done();
		}
	}

	private void done() {
	}
}
