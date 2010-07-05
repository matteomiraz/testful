package test.coverage;

public class AFault {
	public void f2() throws MyException {
		throw new MyException();
	}

	public void f3() throws MyException {
		throw new MyException();
	}

	public void f4() {
		throw new MyException();
	}
}
