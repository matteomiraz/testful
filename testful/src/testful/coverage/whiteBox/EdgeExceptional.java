package testful.coverage.whiteBox;

/**
 * A Simple goto.
 * 
 * @author matteo
 */
public class EdgeExceptional extends Edge {

	private static final long serialVersionUID = -3028339351314419571L;
	private final String exceptionClass;

	public EdgeExceptional(Block from, String exceptionClass) {
		super(from);
		this.exceptionClass = exceptionClass;
	}

	public static void create(Block from, Block to, String exceptionClass) {
		EdgeExceptional e = new EdgeExceptional(from, exceptionClass);
		e.setTo(to);
	}

	public String getExceptionClass() {
		return exceptionClass;
	}
}
