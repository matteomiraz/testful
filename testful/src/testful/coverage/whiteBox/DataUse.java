package testful.coverage.whiteBox;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Set;

public class DataUse implements Serializable {

	private static final long serialVersionUID = -372367168640928525L;
	private final int id;
	private static int idGenerator = 1;

	private final Data data;
	private final Block block;

	private final DataDef defInBlock;

	//TBD: lavorare sui contesti
	/**
	 * Create a data usage
	 * @param data the data being used
	 * @param blockDefs the block's definitions before this usage
	 */
	public DataUse(Block block, Data data, Set<DataDef> blockDefs) {
		id = idGenerator++;

		this.block = block;
		
		DataDef defInBlockTmp = null;
		for(DataDef def : blockDefs)
			if(def.getData() == data) {
				defInBlockTmp = def;
				break;
			}

		defInBlock = defInBlockTmp;

		this.data = data;
		this.data.addUse(this);
	}

	public int getId() {
		return id;
	}

	public Data getData() {
		return data;
	}

	public Block getBlock() {
		return block;
	}

	//	public Set<DefUse> getDefUse() {
	//
	//	}

	private BitSet getReachingDefs() {
		if(defInBlock != null) {
			BitSet ret = new BitSet();
			ret.set(defInBlock.getId());
			return ret;
		}

		BitSet defs = block.getIn();
		defs.and(data.getMask());
		return defs;
	}

	public int getDefUseNum() {
		return getReachingDefs().cardinality();
	}

	public boolean isDefAlive(DataDef d) {
		return getReachingDefs().get(d.getId());
	}

	public String getDefUseString() {
		BitSet defs = getReachingDefs();

		if(defs.isEmpty()) return "";

		boolean first = true;
		StringBuilder sb = new StringBuilder();
		for(int i = defs.nextSetBit(0); i >= 0; i = defs.nextSetBit(i + 1)) {
			if(first) first = false;
			else sb.append(",");
			sb.append(i);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return data.toString() + ":" + id;
	}
}
