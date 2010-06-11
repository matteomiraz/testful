package test.coverage;

public class Fault {
	public Fault() { }
	
	public void a(Object o) {
		o.hashCode();
	}

	public void a1(Object o) {
		throw new NullPointerException();
	}

	public void a2(Object o) throws NullPointerException {
		throw new NullPointerException();
	}

	public void b() {
		throw new NullPointerException();
	}

	public void b1() throws NullPointerException {
		throw new NullPointerException();
	}

	public void c() {
		throw new ArithmeticException();
	}

	public void c1() throws Exception {
		throw new ArithmeticException();
	}

	public void c2() throws ArithmeticException {
		throw new ArithmeticException();
	}

	public void d() throws Exception {
		throw new Exception();
	}
	
	public void e() {
		throw new MyException();
	}

	
	public void e1() throws MyException {
		throw new MyException();
	}
}
