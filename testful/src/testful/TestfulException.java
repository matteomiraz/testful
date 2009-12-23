package testful;

public class TestfulException extends Exception {

	private static final long serialVersionUID = -1299874566082625573L;

	public TestfulException(String msg, Throwable e) {
		super(msg, e);
	}

	public TestfulException(String msg) {
		super(msg);
	}

	public TestfulException(Exception e) {
		super(e);
	}
}
