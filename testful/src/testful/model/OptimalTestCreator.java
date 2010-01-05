package testful.model;

import java.io.File;
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
import java.util.zip.GZIPOutputStream;

import testful.coverage.CoverageInformation;
import testful.coverage.TestSizeInformation;
import testful.utils.ElementManager;
import testful.utils.TestfulLogger;

public class OptimalTestCreator {

	/**
	 * stores the combined coverage obtained so far. Key: coverage criteria;
	 * value: combined coverage criteria
	 */
	private final ElementManager<String, CoverageInformation> combinedCoverage;

	/**
	 * stores the optimal solution found so far. Key: coverage criteria; value:
	 * solutions
	 */
	private final Map<String, Set<TestCoverage>> optimal;

	public OptimalTestCreator() {
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

	public void write() {
		for(Entry<String, Set<TestCoverage>> entry : optimal.entrySet())
			try {
				String filename = "combined" + File.separator + entry.getKey();

				PrintWriter fw = TestfulLogger.singleton.getWriterWithBackup(filename + ".txt");
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

					ObjectOutput out = new ObjectOutputStream(new GZIPOutputStream(TestfulLogger.singleton.getOutputStreamWithBackup(filename + ".ser.gz")));
					out.writeObject(new Test(parts));
					out.close();
				}
			} catch(Exception e) {
				System.err.println("Exception thrown while writing optimal tests: " + e);
			}
	}

	public ElementManager<String, CoverageInformation> getCoverage() {
		return combinedCoverage;
	}

	public Map<String, Set<TestCoverage>> getOptimal() {
		return optimal;
	}
}
