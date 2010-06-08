package dummy;

public class Instrumenter3 extends Exception {

	public Instrumenter3(boolean b) {
		super(b?"a":"b");
	}
}
