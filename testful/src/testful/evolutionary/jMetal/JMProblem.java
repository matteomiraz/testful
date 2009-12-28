package testful.evolutionary.jMetal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.model.Operation;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.TestfulProblem;
import testful.model.TestfulProblem.TestfulConfig;
import testful.runner.IRunner;
import testful.utils.ElementManager;

public class JMProblem extends Problem<Operation> {

	private static final long serialVersionUID = 1715317823344831168L;

	public static JMProblem currentProblem;

	private final TestfulProblem problem;

	public static JMProblem getProblem(IRunner executor, boolean enableCache, boolean reloadClasses, TestfulConfig config) throws JMException {
		currentProblem = new JMProblem(executor, enableCache, reloadClasses, config);
		return currentProblem;
	}

	private JMProblem(IRunner executor, boolean enableCache, boolean reloadClasses, TestfulConfig config) throws JMException {
		try {
			problemName_ = "Testful";

			config.fitness.toMinimize = true;

			problem = new TestfulProblem(executor, enableCache, reloadClasses, config);

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

		System.out.print("  Preparing...");
		long start = System.nanoTime();

		int n = 0;
		for(Solution<Operation> solution : set) {
			try {
				n++;
				futures.put(problem.evaluate(solution.getDecisionVariables().variables_), solution);
			} catch(TestfulException e) {
				System.err.println("Error during the evaluation of an individual: " + e);
			}
		}

		long prep = System.nanoTime();
		System.out.printf(" done (%.2f ms) Evaluating...", (prep - start)/1000000.0);

		try {
			for(Entry<Future<ElementManager<String, CoverageInformation>>, Solution<Operation>> entry : futures.entrySet()) {
				ElementManager<String, CoverageInformation> cov = entry.getKey().get();
				Solution<Operation> solution = entry.getValue();

				float[] fit = problem.evaluate(currentGeneration, solution.getDecisionVariables().variables_, cov);

				for (int i = 0; i < fit.length; i++)
					solution.setObjective(i, fit[i]);
			}
		} catch(Exception e) {
			throw new JMException(e);
		}

		long end = System.nanoTime();
		System.out.printf(" done (%.2f ms)\n", (end-prep)/1000000.0);

		return n;
	}


	@Override
	public List<Operation> generateNewDecisionVariable() {
		final int size = 10;
		List<Operation> ret = new ArrayList<Operation>(size);

		for (int i = 0; i < size; i++)
			ret.add(Operation.randomlyGenerate(problem.getCluster(), problem.getRefFactory(), PseudoRandom.getMersenneTwisterFast()));

		return ret;
	}

	TestfulProblem getProblem() {
		return problem;
	}

	@Override
	public void setCurrentGeneration(int currentGeneration) {
		super.setCurrentGeneration(currentGeneration);
		problem.doneGeneration(currentGeneration);
		if(problem.getRunnerCaching().isEnabled()) System.out.println(problem.getRunnerCaching().toString());
	}

	public Collection<TestCoverage> evaluate(Collection<Test> tests) throws InterruptedException {
		return problem.evaluate(tests);
	}

}
