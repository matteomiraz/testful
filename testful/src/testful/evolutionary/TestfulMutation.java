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

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.base.Solution;
import jmetal.base.operator.mutation.Mutation;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.transformation.Splitter;
import testful.model.transformation.SimplifierDynamic;
import ec.util.MersenneTwisterFast;

/**
 * Modifies a test by adding or removing operations
 * @author matteo
 */
public class TestfulMutation extends Mutation<Operation> {

	private static final Logger logger = Logger.getLogger("testful.evolutionary");

	private TestfulProblem problem;
	public TestfulMutation(TestfulProblem problem) {
		this.problem = problem;
	}

	private float probRemove = 0.5f;
	public void setProbRemove(float probRemove) {
		this.probRemove = probRemove;
	}

	private float probSimplify = 0.05f;
	public void setProbSimplify(float probSimplify) {
		this.probSimplify = probSimplify;
	}

	/**
	 * Executes the operation
	 *
	 * @param solution An object containing a solution to mutate
	 */
	@Override
	public void execute(Solution<Operation> solution) throws JMException {
		List<Operation> repr = solution.getDecisionVariables().variables_;

		MersenneTwisterFast random = PseudoRandom.getMersenneTwisterFast();
		TestCluster cluster = problem.getCluster();
		ReferenceFactory refFactory = problem.getReferenceFactory();

		if(repr.isEmpty()) {
			repr.addAll(problem.generateTest());
			return;
		}

		if(probSimplify > 0 && random.nextBoolean(probSimplify)) {
			try {
				Test test = problem.getTest(repr);

				// remove all useless operations
				test = SimplifierDynamic.singleton.perform(problem.getFinder(), test);
				test = Splitter.splitAndMerge(test);

				repr.clear();
				for(Operation operation : test.getTest())
					repr.add(operation);

				return;
			} catch (Exception e) {
				logger.log(Level.FINE, "Problem during mutation: " + e.getMessage(), e);
			}
		}

		for(int i = 0; i < repr.size(); i++) {
			if(random.nextBoolean(probability)) {

				if(random.nextBoolean(probRemove)) {
					repr.remove(random.nextInt(repr.size()));
					i--;
				} else {
					repr.add(random.nextInt(repr.size()), Operation.randomlyGenerate(cluster, refFactory, random));
					i++;
				}
			}
		}
	}
}
