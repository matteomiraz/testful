package testful.coverage.whiteBox;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

/**
 * A simple linear sequence of instructions (no branches, no function call)
 * 
 * @author matteo
 */
public class BlockBasic extends Block {

	private static final long serialVersionUID = 8097005848082617400L;
	protected final Set<DataDef> defs;
	protected final Set<DataUse> uses;

	protected BitSet gens = null;
	protected BitSet kills = null;

	/**
	 * Create a new basic block
	 * 
	 * @param defs definitions of this block. Notice that there can be at most 1
	 *          definition per variable!
	 */
	public BlockBasic(Set<DataDef> defs, Set<DataUse> uses) {

		this.uses = uses;
		this.defs = defs;
	}

	protected void addDef(DataDef def) {
		defs.add(def);
		gens = null;
		kills = null;
	}

	public Set<DataDef> getDefs() {
		return new HashSet<DataDef>(defs);
	}

	public Set<DataUse> getUses() {
		return new HashSet<DataUse>(uses);
	}

	BitSet getGens() {
		if(gens == null) gens = DataDef.toGensBitSet(defs);

		return gens;
	}

	BitSet getKills() {
		if(kills == null) {
			kills = new BitSet();
			for(DataDef def : defs)
				kills.or(def.toKillsBitSet());
		}
		return kills;
	}

	@Override
	public boolean updateData() {
		BitSet oldIn = in;
		in = new BitSet();
		for(Edge e : pre)
			in.or(e.getFrom().out);

		BitSet oldOut = out;
		out = (BitSet) in.clone();
		out.andNot(getKills());
		out.or(getGens());

		oldIn.xor(in);
		oldOut.xor(out);
		return !(oldIn.isEmpty() && oldOut.isEmpty());
	}

}
