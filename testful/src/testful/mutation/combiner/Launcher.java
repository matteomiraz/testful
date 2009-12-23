package testful.mutation.combiner;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import testful.coverage.CoverageInformation;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestReader;

public class Launcher {

	public static void main(String[] args) {
		Combiner c = new Combiner();
		c.read(args);

		c.combine();

	}

	private static class Combiner extends TestReader {

		/** [covType, [testName, coverage]] */
		private Map<String, Map<String, CoverageInformation>> combined = new HashMap<String, Map<String, CoverageInformation>>();

		@Override
		protected void read(String fileName, Test test) {
			if(test instanceof TestCoverage) {
				TestCoverage tc = (TestCoverage) test;

				for(CoverageInformation cov : tc.getCoverage()) {

					Map<String, CoverageInformation> comb = combined.get(cov.getKey());
					if(comb == null) {
						comb = new HashMap<String, CoverageInformation>();
						combined.put(cov.getKey(), comb);
					}

					comb.put(fileName, cov);
				}
			}
		}

		public void combine() {
			for(Entry<String, Map<String, CoverageInformation>> e : combined.entrySet()) {
				System.out.println(e.getKey() + ": " + e.getValue().size());

				CoverageInformation sample = e.getValue().values().iterator().next();
				CoverageInformation combined = sample.createEmpty();

				for(CoverageInformation cov : e.getValue().values())
					combined.merge(cov);

			}
		}

	}
}
