/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package testful.evolutionary;

import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.util.JMException;
import testful.coverage.CoverageInformation;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageBranch;
import testful.coverage.whiteBox.CoverageDataFlow;
import testful.model.Collector;
import testful.model.Operation;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.utils.CoverageWriter;
import testful.utils.ElementManager;
import testful.utils.SimpleEntry;
import testful.utils.StopWatchNested;

/**
 * Testful problem for JMetal.
 * @author matteo
 */
public class JMProblem extends Problem<Operation> {

	private static final long serialVersionUID = -6806014368055641433L;

	private static final Logger logger = Logger.getLogger("testful.evolutionary");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);

	/** The reference to the testful problem: it contains the test cluster, the reference factory, and it is able to evaluate tests */
	private TestfulProblem problem;

	/** The list of coverage (their key) to consider */
	private final String[] coverageKeys;

	private final CoverageWriter coverageWriter;
	private final Collector[] collectors;

	public JMProblem(TestfulProblem problem, IConfigEvolutionary config) {
		problemName_ = "Testful";
		numberOfObjectives_  =
			(config.isBasicBlock() ? 1 : 0) +
			(config.isBranch() ? 1 : 0) +
			(config.isDefUse() ? 1 : 0) +
			1; // length

		coverageKeys = new String[numberOfObjectives_-1];
		{
			int i = 0;
			if(config.isBasicBlock()) coverageKeys[i++] = CoverageBasicBlocks.KEY;
			if(config.isBranch()) coverageKeys[i++] = CoverageBranch.KEY;
			if(config.isDefUse()) coverageKeys[i++] = CoverageDataFlow.KEY;
		}

		this.problem = problem;

		if(logger.isLoggable(Level.FINE)) {
			coverageWriter = new CoverageWriter("testful.evolutionary.coverage");
		} else {
			coverageWriter = null;
		}

		if(logger.isLoggable(Level.FINEST)) {
			collectors = new Collector[] {
					new Collector(config.getDirBase(), CoverageBasicBlocks.KEY),
					new Collector(config.getDirBase(), CoverageBranch.KEY)
			};
		} else {
			collectors = new Collector[0];
		}

	}

	public TestfulProblem getProblem() {
		return problem;
	}

	@Override
	public List<Operation> generateNewDecisionVariable() {
		return problem.generateTest();
	}

	@Override
	public void evaluate(Solution<Operation> solution) throws JMException {
		try {
			Future<ElementManager<String, CoverageInformation>> fut = problem.evaluate(problem.getTest(solution.getDecisionVariables().variables_));
			ElementManager<String, CoverageInformation> covs = fut.get();

			evaluateObjectives(solution, covs);

		} catch(Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e);
			throw new JMException(e);
		}
	}

	StopWatchNested t_eval = StopWatchNested.getRootTimer("eval");
	StopWatchNested t_prepare = t_eval.getSubTimer("eval.prepare");
	StopWatchNested t_prep_getTest = t_prepare.getSubTimer("eval.prepare.getTest");
	StopWatchNested t_prep_eval = t_prepare.getSubTimer("eval.prepare.eval");
	StopWatchNested t_prep_future = t_prepare.getSubTimer("eval.prepare.future");

	StopWatchNested t_wait = t_eval.getSubTimer("eval.wait");
	StopWatchNested t_wait_evaluateObjectives = t_wait.getSubTimer("eval.wait.evalObjs");

	@Override
	public int evaluate(Iterable<Solution<Operation>> set) throws JMException {
		t_eval.start();

		List<SimpleEntry<Future<ElementManager<String, CoverageInformation>>, Solution<Operation>>> futures = new LinkedList<SimpleEntry<Future<ElementManager<String,CoverageInformation>>,Solution<Operation>>>();

		long start = System.nanoTime();

		t_prepare.start();
		int n = 0;
		for(Solution<Operation> solution : set) {
			n++;
			t_prep_getTest.start();
			Test test = problem.getTest(solution.getDecisionVariables().variables_);
			t_prep_getTest.stop();

			t_prep_eval.start();
			Future<ElementManager<String, CoverageInformation>> future = problem.evaluate(test);
			t_prep_eval.stop();

			t_prep_future.start();
			futures.add(new SimpleEntry<Future<ElementManager<String, CoverageInformation>>, Solution<Operation>>(future, solution));
			t_prep_future.stop();
		}
		t_prepare.stop();

		long prep = System.nanoTime();
		if(LOG_FINE) logger.fine(String.format("Preparation time: %.2fms", (prep - start)/1000000.0));

		t_wait.start();
		try {
			for(Entry<Future<ElementManager<String, CoverageInformation>>, Solution<Operation>> entry : futures) {
				ElementManager<String, CoverageInformation> cov = entry.getKey().get();
				Solution<Operation> solution = entry.getValue();

				t_wait_evaluateObjectives.start();
				evaluateObjectives(solution, cov);
				t_wait_evaluateObjectives.stop();
			}
		} catch(Exception e) {
			logger.log(Level.WARNING, "Error during the evaluation of an individual: " + e.getMessage(), e);
			throw new JMException(e);
		}
		t_wait.stop();

		long end = System.nanoTime();

		if(LOG_FINE) logger.fine(String.format("Execution time: %.2fms", (end - prep)/1000000.0));

		t_eval.stop();

		return n;
	}

	private void evaluateObjectives(Solution<Operation> solution, ElementManager<String, CoverageInformation> covs) throws JMException {
		if(covs == null)
			throw new JMException("Cannot retrieve test's coverage information");

		int i = 0;
		float covTot = 1;
		for (String covKey : coverageKeys) {
			CoverageInformation cov = covs.get(covKey);
			if(cov != null) {
				covTot += cov.getQuality();
				solution.setObjective(i, -1 * cov.getQuality());
			}
			i++;
		}

		final int length = solution.getDecisionVariables().variables_.size();
		if(length > 1)
			solution.setObjective(i, -1.0f*covTot*covTot / (float) Math.log(length));
		else
			solution.setObjective(i, 0);

		if(coverageWriter != null)
			coverageWriter.write(currentGeneration, length, covs);

		TestCoverage testCoverage = new TestCoverage(problem.getTest(solution.getDecisionVariables().variables_), covs);
		problem.updateOptimal(testCoverage);
		for(Collector collector : collectors)
			collector.update(testCoverage);
	}

	@Override
	public void setCurrentGeneration(int currentGeneration, long time) {
		super.setCurrentGeneration(currentGeneration, time);

		problem.getOptimal().log(currentGeneration, problem.getNumberOfExecutedOperations(), time);
		for (Collector tracker : collectors)
			tracker.write();
	}
}
