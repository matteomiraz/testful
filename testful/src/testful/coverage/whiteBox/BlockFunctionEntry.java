package testful.coverage.whiteBox;

import java.util.BitSet;
import java.util.HashSet;

/**
 * Represents the entry point of a function. 
 * 
 * @author matteo
 */
public class BlockFunctionEntry extends Block {

	private static final long serialVersionUID = -3816067359592430951L;
	
	private final BlockClass clazz;
	private final String fullQualifiedName;
	private final boolean isPublic;
	private final boolean contract;

	public BlockFunctionEntry(BlockClass clazz, String fullQualifiedName, boolean isPublic, boolean contract) {
		this.fullQualifiedName = fullQualifiedName;
		this.clazz = clazz;
		this.isPublic = isPublic;
		this.contract = contract;

		pre = new HashSet<Edge>();
		post = new HashSet<Edge>();

		clazz.addMethod(this);
	}

	public String getFullQualifiedName() {
		return fullQualifiedName;
	}

	public BlockClass getClazz() {
		return clazz;
	}
	
	public boolean isPublic() {
		return isPublic;
	}
	
	public boolean isContract() {
		return contract;
	}
	
	@Override
	public boolean updateData() {
		BitSet oldIn = in;
		out = in = new BitSet();
		for(Edge e : pre)
			in.or(e.getFrom().out);

		oldIn.xor(in);
		return !oldIn.isEmpty();
	}

}
