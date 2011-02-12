package testful.mutation.combiner;

import java.util.logging.Logger;

import testful.coverage.CoverageInformation;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestReader;
import testful.mutation.MutationCoverage;

public class Launcher {
	private static Logger logger = Logger.getLogger("testful.evolutionary");

	public static void main(String[] args) {
		Combiner c = new Combiner();
		c.read(args);

		c.combine();

	}

	private static class Combiner extends TestReader {

		private final MutationCoverage combined = new MutationCoverage();

		@Override
		protected void read(String fileName, Test test) {
			if(test instanceof TestCoverage) {
				TestCoverage tc = (TestCoverage) test;

				CoverageInformation mut = tc.getCoverage().get(MutationCoverage.KEY);

				if(mut != null) {
					combined.merge(mut);
				} else {
					System.err.println("No mutation coverage for " + fileName);
				}

			} else {
				System.err.println("No coverage info for " + fileName);
			}
		}

		public void combine() {
			System.out.println(combined.getKilled());
		}

		@Override
		protected Logger getLogger() {
			return logger;
		}
	}
}
