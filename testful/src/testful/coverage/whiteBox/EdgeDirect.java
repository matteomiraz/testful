package testful.coverage.whiteBox;

/**
 * A Simple goto.
 * 
 * @author matteo
 */
public class EdgeDirect extends Edge {

	private static final long serialVersionUID = 8996745596858488180L;

	public EdgeDirect(Block from) {
		super(from);
	}

	public static void create(Block from, Block to) {
		EdgeDirect e = new EdgeDirect(from);
		e.setTo(to);
	}

}
