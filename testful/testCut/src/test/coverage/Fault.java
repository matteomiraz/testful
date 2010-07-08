package test.coverage;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Fault extends AFault {
	public Fault() { }

	public void a(Object o) {
		o.hashCode();
	}

	public void a1(Object o) {
		throw new NullPointerException("message");
	}

	public void a2(Object o) throws NullPointerException {
		throw new NullPointerException("message");
	}

	public void b() {
		throw new NullPointerException("message");
	}

	public void b1() throws NullPointerException {
		throw new NullPointerException("message");
	}

	public void c() {
		throw new ArithmeticException("message");
	}

	public void c1() throws Exception {
		throw new ArithmeticException("message");
	}

	public void c2() throws ArithmeticException {
		throw new ArithmeticException("message");
	}

	public void d() throws Exception {
		throw new Exception("message");
	}

	public void e() {
		throw new MyException();
	}

	public void e1() throws MyException {
		throw new MyException();
	}

	public void f() {
		throw new MyException();
	}

	public void f1() throws MyException {
		throw new MyException();
	}

	// f2 from AFault

	public void f3() {
		super.f3();
	}

	public void f4() throws MyException {
		super.f4();
	}

	final static class WrongIter implements Iterator<Object> {

		@Override
		public boolean hasNext() {
			throw new NullPointerException();
		}

		@Override
		public Object next() throws NoSuchElementException {
			throw new NullPointerException();
		}

		@Override
		public void remove() throws UnsupportedOperationException, IllegalStateException {
			throw new NullPointerException();
		}
	}

	public Iterator<Object> g() { return new UFault.WrongIter(); }
	public Iterator<Object> g1() {
		return new Iterator<Object>() {
			@Override
			public boolean hasNext() {
				throw new NullPointerException();
			}

			@Override
			public Object next() throws NoSuchElementException {
				throw new NullPointerException();
			}

			@Override
			public void remove() throws UnsupportedOperationException, IllegalStateException {
				throw new NullPointerException();
			}
		};
	}

}
