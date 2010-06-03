package test.instrument;

import java.io.Serializable;
import java.util.Iterator;

public class Nested {

	public int foo(final int n) {
		Iterator i = new Iterator() {

			public boolean hasNext() {
				return true;
			}

			public Object next() {
				return new Integer(n);
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			
		};
		if (n > 0)
			i.next();
		
		return ((Integer) i.next()).intValue();
	}

	public Serializable getPrivate() {
		PrivateStatic privateStatic = new PrivateStatic();
		privateStatic.foo(1);
		return privateStatic;
	}
	
	private static class PrivateStatic implements Serializable {
		private static final long serialVersionUID = 4632127712133012682L;

		public int foo(int n) {
			if (n > 0)
				return n;
			return n - 1;
		}
	}
	
	public static class InnerStatic {
		public int foo(int n) {
			if (n > 0)
				return n;
			return n - 1;
		}

		public static class InnerStatic2 {
			public int foo(int n) {
				if (n > 0)
					return n;
				return n - 1;
			}
		}

		public class Inner2 {
			public int foo(int n) {
				if (n > 0)
					return n;
				return n - 1;
			}
		}

	}

	public class Inner {
		public int foo(int n) {
			if (n > 0)
				return n;
			return n - 1;
		}

		public class Inner3 {
			public int foo(int n) {
				if (n > 0)
					return n;
				return n - 1;
			}
		}
	}
}
