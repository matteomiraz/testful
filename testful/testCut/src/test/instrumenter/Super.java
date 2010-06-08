package test.instrumenter;

public class Super extends Exception {

	private static final long serialVersionUID = 1L;

	public Super(boolean b) {
		super(b?"a":"b");
	}
}
