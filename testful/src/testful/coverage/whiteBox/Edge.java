package testful.coverage.whiteBox;

import java.io.Serializable;

public abstract class Edge implements Serializable {

	private static final long serialVersionUID = 5704806706857061576L;
	protected final Block from;
	protected Block to;

	public Edge(Block from) {
		this.from = from;
		from.post.add(this);
	}

	public void setTo(Block to) {
		this.to = to;
		to.pre.add(this);
	}

	public Block getFrom() {
		return from;
	}

	public Block getTo() {
		return to;
	}
}
