package testful.coverage.whiteBox;

import java.util.BitSet;
import java.util.HashSet;

/**
 * Represents the (only) exit point of a function. 
 * 
 * @author matteo
 */
public class BlockFunctionExit extends Block {

	private static final long serialVersionUID = -6797021434840618557L;
	private final String fullQualifiedName;
	private final BlockClass clazz;

	private final BitSet fieldMask;

	public BlockFunctionExit(String fullQualifiedName, BlockClass clazz) {
		this.fullQualifiedName = fullQualifiedName;
		this.clazz = clazz;

		pre = new HashSet<Edge>();
		post = new HashSet<Edge>();
		fieldMask = Factory.singleton.getFieldsMask();
	}

	public BlockClass getClazz() {
		return clazz;
	}

	public String getFullQualifiedName() {
		return fullQualifiedName;
	}

	@Override
	public boolean updateData() {
		BitSet oldIn = in;
		out = in = new BitSet();
		for(Edge e : pre)
			in.or(e.getFrom().out);

		// KILL all non-field defs
		in.and(fieldMask);

		oldIn.xor(in);
		return !oldIn.isEmpty();
	}
}
