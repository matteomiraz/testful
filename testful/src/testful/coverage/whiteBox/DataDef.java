package testful.coverage.whiteBox;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Set;

public class DataDef implements Serializable {

	private static final long serialVersionUID = -7674828835545817305L;

	private final int id;
	private static int idGenerator = 1;

	private final Data data;
	private final Block block;

	private BitSet killBitSet;

	/** if the assignment is to a static value, this is the value */
	private final Serializable value;

	public DataDef(Block block, Data data, Serializable value) {
		id = idGenerator++;
		this.block = block;
		
		this.data = data;
		this.value = value;
		data.addDef(this);
		Factory.singleton.updateFieldsMask(this);
	}

	public int getId() {
		return id;
	}

	public Data getData() {
		return data;
	}

	public boolean isField() {
		return data.isField();
	}

	public Block getBlock() {
		return block;
	}
	
	public Serializable getValue() {
		return value;
	}

	public static BitSet toGensBitSet(Set<DataDef> gens) {
		BitSet ret = new BitSet();

		for(DataDef g : gens)
			ret.set(g.id);

		return ret;
	}

	public BitSet toKillsBitSet() {
		if(killBitSet == null) {
			killBitSet = new BitSet();
			killBitSet.or(data.getMask());
			killBitSet.set(id, false);
		}
		return (BitSet) killBitSet.clone();
	}

	@Override
	public String toString() {
		return data.toString() + ":" + id;
	}
}
