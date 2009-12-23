package testful.coverage.whiteBox;

import java.util.BitSet;
import java.util.Set;

public class BlockFunctionCall extends BlockBasic {

	private static final long serialVersionUID = 7644484386896369190L;
	private final boolean isStatic;
	private final String className;
	private final String methodName;

	public BlockFunctionCall(Set<DataDef> defs, Set<DataUse> uses, String methodName, String className, boolean isStatic) {
		super(defs, uses);
		this.isStatic = isStatic;
		this.className = className;
		this.methodName = methodName;
	}

	public String getClassName() {
		return className;
	}

	public String getMethodName() {
		return methodName;
	}

	public boolean isStatic() {
		return isStatic;
	}
	
	public void updateGenKills(BitSet def) {
		defs.addAll(defs);
	}

	@Override
	public String toString() {
		return super.toString() + " " + (isStatic ? "static " : "") + className + "." + methodName;
	}

}
