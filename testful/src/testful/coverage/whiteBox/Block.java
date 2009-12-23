package testful.coverage.whiteBox;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class Block implements Serializable {

	private static final long serialVersionUID = 2633740207186003854L;
	
	private final int id;
	private static int idGenerator = 0;

	/** if != null, the basic block ends with this condition (if or switch) */
	protected Condition condition;
	
	/** CFG: edges incoming in this block */
	protected Set<Edge> pre = new HashSet<Edge>();

	/** CFG: edges outgoing from this block */
	protected Set<Edge> post = new HashSet<Edge>();

	/** reaching definitions incoming in this block */
	protected BitSet in;
	/** definitions outgoing from this block */
	protected BitSet out;

	public Block() {
		id = idGenerator++;
		in = new BitSet();
		out = new BitSet();
	}

	public int getId() {
		return id;
	}

	public Set<Edge> getPre() {
		return pre;
	}

	public Set<Edge> getPost() {
		return post;
	}

	public BitSet getIn() {
		return (BitSet) in.clone();
	}

	public BitSet getOut() {
		return (BitSet) out.clone();
	}
	
	void setCondition(Condition condition) {
		this.condition = condition;
	}
	
	public Condition getCondition() {
		return condition;
	}
	
	@Override
	public String toString() {
		return "B" + id;
	}
	
	public abstract boolean updateData();

	/**
	 * Returns true if exists a path without any definition of a variable (in
	 * which the def d is alive), which leads to the use u
	 * 
	 * @param d the definition
	 * @param u the use
	 * @return true if a definition-clear path to u exists
	 */
	public boolean existsPath(DataDef d, DataUse u) {

		Block defBlock = d.getBlock();
		Block useBlock = u.getBlock();
		int defId = d.getId();

		if(!u.isDefAlive(d)) return false;
		if(defBlock == this || useBlock == this) return true;
		if(!getIn().get(defId)) return false;

		// algo
		// starting from here, go towards the end of the method following def-free paths. Stop if either
		//    a. the use is found (and return true)
		//    b. there are no def-free paths to the end (and return false)
		//    c. the end of method is reached (and return true)

		Set<Block> traversed = new LinkedHashSet<Block>();
		Set<Block> todo = new LinkedHashSet<Block>();

		todo.add(this);

		while(!todo.isEmpty()) {
			Block b = todo.iterator().next();
			todo.remove(b);
			traversed.add(b);

			if(b == useBlock) return true;

			if(!b.getOut().get(defId)) continue;

			if(b instanceof BlockFunctionExit) return true;

			for(Edge post : b.getPost())
				if(!traversed.contains(post.getTo())) todo.add(post.getTo());
		}

		return false;

	}
}
