package testful.coverage.whiteBox;

import java.util.BitSet;

import testful.coverage.BitSetCoverage;
import testful.coverage.CoverageInformation;

public class CoverageConditions extends BitSetCoverage {
	private static final long serialVersionUID = 1841450132658247037L;

	private static String NAME = "Condition Coverage";
	public static String NAME_CODE = NAME + " (Code)";
	public static String NAME_CONTRACT = NAME + " (Contracts)";
	
	private static String KEY = "br";
	public static String KEY_CODE = KEY + "d";
	public static String KEY_CONTRACT = KEY + "n";

	private final String key;
	private final String name;
	
	private CoverageConditions(String key, String name) {
		super();
		this.key = key;
		this.name = name;
	}
	
	CoverageConditions(String key, String name, BitSet coverage) {
		super(coverage);
		this.key = key;
		this.name = name;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public CoverageInformation createEmpty() {
		return new CoverageConditions(key, name);
	}

	@Override
	public CoverageConditions clone() throws CloneNotSupportedException {
		return new CoverageConditions(key, name, (BitSet) coverage.clone());
	}
}
