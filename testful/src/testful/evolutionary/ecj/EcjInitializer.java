/*
 * Copyright 2006 by Sean Luke Licensed under the Academic Free License version
 * 3.0 See the file "LICENSE" for more information
 */

package testful.evolutionary.ecj;

import testful.model.TestfulProblem;
import ec.EvolutionState;
import ec.Initializer;
import ec.Population;
import ec.util.Parameter;

/*
 * SimpleInitializer.java Created: Tue Aug 10 21:07:42 1999 By: Sean Luke
 */

/**
 * Derived from SimpleInitializer, used to store the testcluster for the class
 * under test.
 * 
 * @author Matteo Miraz
 * @version 1.0
 */

public class EcjInitializer extends Initializer {

	private static final long serialVersionUID = 8619405436193470905L;

	private TestfulProblem problem;

	@Override
	public void setup(final EvolutionState state, final Parameter base) {}

	/**
	 * Creates, populates, and returns a new population by making a new
	 * population, calling setup(...) on it, and calling populate(...) on it,
	 * assuming an unthreaded environment (thread 0). Obviously, this is an
	 * expensive method. It should only be called once typically in a run.
	 */

	@Override
	public Population initialPopulation(final EvolutionState state, int thread) {
		Population p = setupPopulation(state, thread);
		p.populate(state, thread);
		return p;
	}

	@Override
	public Population setupPopulation(final EvolutionState state, int thread) {
		Parameter base = new Parameter(P_POP);
		Population p = (Population) state.parameters.getInstanceForParameterEq(base, null, Population.class); // Population.class is fine
		p.setup(state, base);
		return p;
	}

	public void setProblem(TestfulProblem problem) {
		this.problem = problem;
	}

	public TestfulProblem getProblem() {
		return problem;
	}
}
