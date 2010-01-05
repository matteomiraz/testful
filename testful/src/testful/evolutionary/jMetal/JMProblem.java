package testful.evolutionary.jMetal;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.operator.localSearch.LocalSearch;
import jmetal.util.JMException;
import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.evolutionary.IConfigEvolutionary;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestCoverage;
import testful.model.TestSuite;
import testful.model.TestfulProblem;
import testful.runner.ClassFinder;
import testful.utils.ElementManager;

public class JMProblem extends Problem<Operation> {

	private static final Logger logger = Logger.getLogger("testful.evolutionary");

	private static final long serialVersionUID = 1715317823344831168L;

	private final TestfulProblem problem;

	public JMProblem(IConfigEvolutionary config) throws JMException {
		try {
			problemName_ = "Testful";

			config.setToMinimize(true);

			problem = new TestfulProblem(config);

			numberOfObjectives_ = problem.getNumObjs();

		} catch (TestfulException e) {
			throw new JMException(e.getMessage());
		}
	}

	public Test getTest(Solution<Operation> sol) {
		return problem.createTest(sol.getDecisionVariables().variables_);
	}

	@Override
	public void evaluate(Solution<Operation> solution) throws JMException {
		try {
			List<Operation> vars = solution.getDecisionVariables().variables_;

			Future<ElementManager<String, CoverageInformation>> fut = problem.evaluate(vars);
			ElementManager<String, CoverageInformation> cov = fut.get();

			float[] fit = problem.evaluate(currentGeneration, vars, cov);

			for (int i = 0; i < fit.length; i++)
				solution.setObjective(i, fit[i]);

		} catch(Exception e) {
			throw new JMException(e);
		}
	}

	@Override
	public int evaluate(Iterable<Solution<Operation>> set) throws JMException {
		Map<Future<ElementManager<String, CoverageInformation>>, Solution<Operation>> futures = new LinkedHashMap<Future<ElementManager<String,CoverageInformation>>, Solution<Operation>>();

		long start = System.nanoTime();

		int n = 0;
		for(Solution<Operation> solution : set) {
			try {
				n++;
				futures.put(problem.evaluate(solution.getDecisionVariables().variables_), solution);
			} catch(TestfulException e) {
				logger.log(Level.WARNING, "Error during the evaluation of an individual: " + e.getMessage(), e);
			}
		}

		long prep = System.nanoTime();
		logger.fine(String.format("Preparation time: %.2fms", (prep - start)/1000000.0));

		try {
			for(Entry<Future<ElementManager<String, CoverageInformation>>, Solution<Operation>> entry : futures.entrySet()) {
				ElementManager<String, CoverageInformation> cov = entry.getKey().get();
				Solution<Operation> solution = entry.getValue();

				float[] fit = problem.evaluate(currentGeneration, solution.getDecisionVariables().variables_, cov);

				for (int i = 0; i < fit.length; i++)
					solution.setObjective(i, fit[i]);
			}
		} catch(Exception e) {
			logger.log(Level.WARNING, "Error during the evaluation of an individual: " + e.getMessage(), e);
			throw new JMException(e);
		}

		long end = System.nanoTime();

		logger.fine(String.format("Execution time: %.2fms", (end - prep)/1000000.0));

		return n;
	}


	/**
	 * Add tests to the reserve
	 * @param tests the tests to add
	 */
	public void addReserve(TestSuite tests){
		problem.addReserve(tests);
	}

	/**
	 * Add a test to the reserve
	 * @param test the test to add
	 */
	public void addReserve(TestCoverage test){
		problem.addReserve(test);
	}

	@Override
	public List<Operation> generateNewDecisionVariable() {
		return problem.generateTest();
	}

	@Override
	public void setCurrentGeneration(int currentGeneration, long time) {
		super.setCurrentGeneration(currentGeneration, time);
		problem.doneGeneration(currentGeneration, time);
	}

	public Collection<TestCoverage> evaluate(Collection<Test> tests) throws InterruptedException {
		return problem.evaluate(tests);
	}

	public LocalSearch<Operation> getLocalSearch() {
		return new LocalSearchBranch(problem);
	}

	public TestCluster getCluster() {
		return problem.getCluster();
	}

	public ReferenceFactory getRefFactory() {
		return problem.getRefFactory();
	}

	public ClassFinder getFinder() {
		return problem.getFinder();
	}
}
