package testful.model;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;

import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.coverage.TestSizeInformation;
import testful.utils.ElementManager;

public class OptimalTestCreator {

	private final File baseDir;

	private final Logger log;

	/**
	 * stores the combined coverage obtained so far.
	 * Key: coverage criteria;
	 * value: combined coverage criteria
	 */
	private final ElementManager<String, CoverageInformation> combinedCoverage;

	/**
	 * stores the optimal solution found so far.
	 * Key: coverage criteria;
	 * value: solutions
	 */
	private final Map<String, Set<TestCoverage>> optimal;

	public OptimalTestCreator() {
		this(null, null);
	}

	public OptimalTestCreator(File baseDir, Logger log) {
		this.log = log;
		this.baseDir = baseDir;

		combinedCoverage = new ElementManager<String, CoverageInformation>();
		optimal = new HashMap<String, Set<TestCoverage>>();
	}

	/**
	 * Updates optimal solutions and combined coverage
	 * 
	 * @param test the test with coverage
	 * @return true if the test is "innovative" (achieves higher coverage
	 *         criteria), false otherwise.
	 */
	public boolean update(TestCoverage test) {
		boolean innovative = false;

		for(CoverageInformation coverage : test.getCoverage()) {
			if(coverage instanceof TestSizeInformation) continue;

			String key = coverage.getKey();
			CoverageInformation combined = combinedCoverage.get(key);
			if(combined == null) {
				combined = coverage.createEmpty();
				combinedCoverage.put(combined);
			}

			innovative = innovative | update(test, coverage);
		}

		return innovative;
	}

	private boolean update(TestCoverage test, CoverageInformation coverage) {
		String key = coverage.getKey();
		CoverageInformation combined = combinedCoverage.get(key);

		// updates the optimal solutions
		Set<TestCoverage> optimalSet = optimal.get(key);
		if(optimalSet == null) {
			optimalSet = new HashSet<TestCoverage>();
			optimal.put(key, optimalSet);
		}

		// check if the solution is innovative (it contains new elements)
		boolean innovative = !combined.contains(coverage);

		// check if any pre-existent optimal solutions can be removed
		Set<TestCoverage> toRemove = new HashSet<TestCoverage>();
		for(TestCoverage t : optimalSet)
			if(coverage.contains(t.getCoverage().get(key))) toRemove.add(t);

		if(innovative) // if the solution is innovative, update the combined coverage
			combined.merge(coverage);
		else {
			// the not-innovative solution must be shorter than the sum of the replacing solutions
			int tot = 0;
			for(TestCoverage t : toRemove)
				tot += t.getTest().length;

			if(test.getTest().length > tot) toRemove.clear();
		}

		for(TestCoverage t : toRemove)
			optimalSet.remove(t);

		if(innovative || !toRemove.isEmpty()) optimalSet.add(test);

		return(innovative || !toRemove.isEmpty());
	}

	public List<TestCoverage> get() {
		List<TestCoverage> ret = new ArrayList<TestCoverage>();

		for(Entry<String, Set<TestCoverage>> entry : optimal.entrySet())
			for(TestCoverage t : entry.getValue())
				ret.add(t);

		return ret;
	}

	public ElementManager<String, CoverageInformation> getCoverage() {
		return combinedCoverage;
	}

	public Map<String, Set<TestCoverage>> getOptimal() {
		return optimal;
	}

	public void write(Integer currentGeneration, long totInvocation, long time) {
		if(baseDir == null) return;

		for(Entry<String, Set<TestCoverage>> entry : optimal.entrySet()) {
			try {
				final File dir = new File(baseDir, "combined");

				PrintWriter fw = new PrintWriter(TestFul.createFileWithBackup(dir, entry.getKey() + ".txt"));
				CoverageInformation combinedInfo = combinedCoverage.get(entry.getKey());
				fw.println("Combined coverage: " + combinedInfo.getQuality());
				fw.println("Composed of " + entry.getValue().size() + " tests.");
				fw.println();
				fw.println(combinedInfo.toString());
				fw.close();

				if(!entry.getValue().isEmpty()) {
					int i = 0;
					TestCoverage[] parts = new TestCoverage[entry.getValue().size()];
					for(TestCoverage t : entry.getValue())
						parts[i++] = t;

					ObjectOutput out = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(TestFul.createFileWithBackup(dir, entry.getKey() + ".ser.gz"))));
					out.writeObject(new Test(parts));
					out.close();
				}
			} catch(Exception e) {
				Logger.getLogger("testful.model").log(Level.WARNING, "Exception thrown while writing optimal tests: " + e.getMessage(), e);
			}
		}

		StringBuilder sb = new StringBuilder("combinedCoverage ");
		sb.append("inv=").append(totInvocation);
		sb.append(";").append("time=").append(time);
		if(currentGeneration != null) sb.append(";").append("gen=").append(currentGeneration);

		for (Entry<String, Set<TestCoverage>> e : optimal.entrySet()) {
			int tot = 0;
			for(Test t : e.getValue())
				tot += t.getTest().length;

			sb.append(";").append(e.getKey()).append("-cov").append("=").append(combinedCoverage.get(e.getKey()).getQuality());
			sb.append(";").append(e.getKey()).append("-tests").append("=").append(e.getValue().size());
			sb.append(";").append(e.getKey()).append("-length").append("=").append(tot);
		}
		log.finer(sb.toString());
	}
}
