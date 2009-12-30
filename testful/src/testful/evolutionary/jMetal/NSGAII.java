package testful.evolutionary.jMetal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jmetal.base.Algorithm;
import jmetal.base.Problem;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.Variable;
import jmetal.base.operator.crossover.Crossover;
import jmetal.base.operator.localSearch.LocalSearch;
import jmetal.base.operator.localSearch.LocalSearchPopulation;
import jmetal.base.operator.mutation.Mutation;
import jmetal.base.operator.selection.Selection;
import jmetal.util.Distance;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import jmetal.util.Ranking;
import testful.IUpdate;

/**
 * This class implements the NSGA-II algorithm.
 */
public class NSGAII<V extends Variable>
extends Algorithm<V, Crossover<V>, Mutation<V>, Selection<V, Solution<V>>, LocalSearch<V>>
implements IUpdate {

	private static final long serialVersionUID = 4970928169851043408L;

	private List<Callback> callbacks = new LinkedList<Callback>();

	@Override
	public void register(Callback c) {
		this.callbacks.add(c);
	}

	@Override
	public void unregister(Callback c) {
		this.callbacks.remove(c);
	}

	private void update(long start, long current, long end, Map<String, Float> coverage) {
		for(Callback c : callbacks)
			c.update(start, current, end, coverage);
	}

	/** stores the problem  to solve */
	private Problem<V> problem_;

	/** probability to inherit the fitness */
	private final float INHERIT_PROBABILITY = 0.55f;

	/** is fitness inheritance enabled */
	private FitnessInheritance inherit = FitnessInheritance.DISABLED;

	/** period of the local search (in generations) */
	private int localSearchPeriod = 20;

	/**
	 * Constructor
	 * @param problem Problem to solve
	 */
	public NSGAII(Problem<V> problem) {
		this.problem_ = problem;
	} // NSGAII

	public void setInherit(FitnessInheritance inherit) {
		this.inherit = inherit;
	}

	public FitnessInheritance getInherit() {
		return inherit;
	}

	public void setLocalSearchPeriod(int localSearchPeriod) {
		this.localSearchPeriod = localSearchPeriod;
	}


	public int getLocalSearchPeriod() {
		return localSearchPeriod;
	}

	/**
	 * Runs the NSGA-II algorithm.
	 * @return a <code>SolutionSet</code> that is a set of non dominated solutions
	 * as a result of the algorithm execution
	 * @throws JMException if something goes wrong
	 */
	@Override
	public SolutionSet<V> execute() throws JMException {
		long startTime = System.currentTimeMillis();

		SolutionSet<V> population;
		SolutionSet<V> union;

		//Read the parameters
		int populationSize = getPopulationSize();
		int maxTime        = getMaxEvaluations();

		//Initialize the variables
		population = new SolutionSet<V>(populationSize);
		int evaluations = 0;

		System.out.println("Evaluating generation 0");

		// Create the initial solutionSet
		for (int i = 0; i < populationSize; i++)
			population.add(new Solution<V>(problem_));

		evaluations += problem_.evaluate(population);

		for(Solution<V> solution : population)
			problem_.evaluateConstraints(solution);

		int currentGeneration = 0;
		problem_.setCurrentGeneration(currentGeneration);

		// Generations ...
		while ((System.currentTimeMillis() - startTime) < maxTime) {
			currentGeneration++;

			update(startTime, System.currentTimeMillis(), startTime+maxTime, new HashMap<String, Float>());

			// perform the improvement
			if(improvement != null && currentGeneration % localSearchPeriod == 0) {
				SolutionSet<V> front = new Ranking<V>(population).getSubfront(0);

				System.out.println("Local search on fronteer (" + front.size() + ")");

				if(improvement instanceof LocalSearchPopulation<?>) {
					SolutionSet<V> mutated = ((LocalSearchPopulation<V>)improvement).execute(front);
					if(mutated != null) problem_.evaluate(mutated);

				} else {
					Solution<V> solution = front.get(PseudoRandom.getMersenneTwisterFast().nextInt(front.size()));
					solution = improvement.execute(solution);
					if(solution != null) problem_.evaluate(solution);
				}

				problem_.setCurrentGeneration(currentGeneration++);
			}

			System.out.printf("Evaluating generation %d (%5.2f%%)\n", currentGeneration, + (100.0*(System.currentTimeMillis() - startTime) / maxTime));

			SolutionSet<V> offspringPopulation = new SolutionSet<V>(populationSize);

			// Create the offSpring solutionSet
			for (int i = 0; i < (populationSize / 2); i++) {
				//obtain parents
				Solution<V> parent1 = selectionOperator.execute(population);
				Solution<V> parent2 = selectionOperator.execute(population);
				Solution<V>[] offSpring = crossoverOperator.execute(parent1, parent2);
				mutationOperator.execute(offSpring[0]);
				mutationOperator.execute(offSpring[1]);
				offspringPopulation.add(offSpring[0]);
				offspringPopulation.add(offSpring[1]);
				evaluations += 2;
			}

			// select individuals to evaluate
			Iterable<Solution<V>> toEval = offspringPopulation;

			switch (inherit) {
			case UNIFORM:
				List<Solution<V>> tmpu = new ArrayList<Solution<V>>();
				for(Solution<V> s : offspringPopulation)
					if(!PseudoRandom.getMersenneTwisterFast().nextBoolean(INHERIT_PROBABILITY))
						tmpu.add(s);

				toEval = tmpu;
				break;

			case FRONTEER:
				List<Solution<V>> tmpf = new ArrayList<Solution<V>>();

				final Ranking<V> ranking = new Ranking<V>(population);
				final SolutionSet<V> fronteer = ranking.getSubfront(0);
				final List<Solution<V>> others = new ArrayList<Solution<V>>();

				for(int i = 1; i < ranking.getNumberOfSubfronts(); i++)
					for(Solution<V> s : ranking.getSubfront(i))
						others.add(s);

				final int n = offspringPopulation.size();
				final int f = fronteer.size();

				//TBD: try with fixed probabilities
				final float k = 0.5f;
				final float pf = k * INHERIT_PROBABILITY * n / (n + f*(k - 1.0f));
				final float po = (pf / k) >= 1 ? 1 : pf / k;

				for(Solution<V> s : fronteer) {
					if(!PseudoRandom.getMersenneTwisterFast().nextBoolean(pf)) {
						tmpf.add(s);
					}
				}

				for(Solution<V> s : others) {
					if(!PseudoRandom.getMersenneTwisterFast().nextBoolean(po))  {
						tmpf.add(s);
					}
				}

				toEval = tmpf;
				break;
			}


			// evaluate individuals
			problem_.evaluate(toEval);
			for(Solution<V> solution : toEval)
				problem_.evaluateConstraints(solution);

			// Create the solutionSet union of solutionSet and offSpring
			union = population.union(offspringPopulation);

			// Ranking the union
			Ranking<V> ranking = new Ranking<V>(union);

			int remain = populationSize;
			int index = 0;
			SolutionSet<V> front = null;
			population.clear();

			// Obtain the next front
			front = ranking.getSubfront(0);

			while ((remain > 0) && (remain >= front.size())) {
				//Assign crowding distance to individuals
				Distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
				//Add the individuals of this front
				for(Solution<V> s : front)
					population.add(s);

				//Decrement remain
				remain = remain - front.size();

				//Obtain the next front
				if (remain > 0)
					front = ranking.getSubfront(++index);
			} // while

			// Remain is less than front(index).size, insert only the best one
			if (remain > 0) {  // front contains individuals to insert
				Distance.crowdingDistanceAssignment(front, problem_.getNumberOfObjectives());
				front.sort(new jmetal.base.operator.comparator.CrowdingComparator<V>());
				for (int k = 0; k < remain; k++)
					population.add(front.get(k));

				remain = 0;
			} // if

			problem_.setCurrentGeneration(currentGeneration);

		} // while

		setEvaluations(evaluations);

		// Return the first non-dominated front
		Ranking<V> ranking = new Ranking<V>(population);
		return ranking.getSubfront(0);
	} // execute
} // NSGA-II
