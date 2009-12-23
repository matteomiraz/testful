package testful.evolutionary.ecj;

import java.util.concurrent.Future;

import ec.EvolutionState;
import ec.Individual;
import ec.Problem;
import ec.simple.SimpleProblemForm;

public abstract class FutureProblem extends Problem implements SimpleProblemForm {

	private static final long serialVersionUID = 3998941558100988736L;

	public abstract Future<Individual> evaluateFuture(EvolutionState state, Individual _ind, int subpop);
}
