package testful.coverage.whiteBox;

/**
 * A conditional edge.
 * 
 * @author matteo
 */
public class EdgeConditional extends Edge {

	private static final long serialVersionUID = -3750809221197553448L;
	private final int id;
	private static int idGenerator = 0;

	private final Condition condition;

	public EdgeConditional(Block from, Condition c) {
		super(from);
		id = idGenerator++;
		condition = c;
	}

	public int getId() {
		return id;
	}

	public Condition getCondition() {
		return condition;
	}
}
