package testful.coverage.bug;

import java.util.LinkedHashSet;
import java.util.Set;

import testful.coverage.CoverageInformation;

public class BugCoverage implements CoverageInformation {

	private static final long serialVersionUID = -7544654531090656602L;

	public static final String KEY = "bug";
	public static final String NAME = "bug discovered";

	public static BugCoverage getEmpty() {
		return new BugCoverage();
	}

	/** only BugTracker can access directly this field! */
	public final Set<Bug> bugs;

	private BugCoverage() {
		bugs = new LinkedHashSet<Bug>();
	}

	@Override
	public float getQuality() {
		return bugs.size();
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(!(other instanceof BugCoverage)) return false;

		return bugs.containsAll(((BugCoverage) other).bugs);
	}

	@Override
	public void merge(CoverageInformation other) {
		if(other instanceof BugCoverage) bugs.addAll(((BugCoverage) other).bugs);
	}

	@Override
	public CoverageInformation createEmpty() {
		return new BugCoverage();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for(Bug exc : bugs) {
			sb.append("--- Exception: ").append(exc.getFaultyExecutionException().getClass().getCanonicalName()).append(
					" -> " + exc.getFaultyExecutionException().getCause().getClass().getCanonicalName() + " ---\n");
			for(StackTraceElement ste : exc.getStackTrace())
				sb.append("  ").append(ste.toString()).append("\n");
		}

		return sb.toString();
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public BugCoverage clone() {
		BugCoverage ret = new BugCoverage();
		for(Bug b : bugs)
			ret.bugs.add(b);
		
		return ret;
	}
}
