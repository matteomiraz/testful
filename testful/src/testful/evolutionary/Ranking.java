package testful.evolutionary;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.Variable;
import testful.TestFul;

/**
 * Performs a non-dominated Sorting, using the NSGA-II algorithm reported in IEEE TRANSACTIONS ON EVOLUTIONARY COMPUTATION (http://dx.doi.org/10.1109/4235.996017)
 * 
 * @author matteo
 */
public class Ranking<T extends Variable> implements Iterator<SolutionSet<T>> {

	private static class RankedElement<T extends Variable> {
		final Solution<T> solution;

		/** number of points that dominate this solution */
		int dominatedBy;

		/** elements dominated by this solution */
		final List<RankedElement<T>> dominated;

		public RankedElement(Solution<T> solution) {
			this.solution = solution;
			dominated = new LinkedList<RankedElement<T>>();
		}

		/**
		 * This solution dominates the other solution
		 * @param d the dominated solution
		 */
		public void dominates(RankedElement<T> d) {
			dominated.add(d);
			d.dominatedBy++;
		}

		/**
		 * Removes an element that dominates this solution, and check if the solution is not dominated
		 * @return whether the solution is NOT dominated
		 */
		public boolean promoteAndCheck() {
			return --dominatedBy == 0;
		}
	}

	/** elements belonging to the next front */
	private List<RankedElement<T>> nextFront;

	/**
	 * Constructor.
	 * @param solutionSet The <code>SolutionSet</code> to be ranked.
	 */
	public Ranking(SolutionSet<T> solutionSet) {

		RankedElement<T>[] pop = convert(solutionSet);

		nextFront = new LinkedList<RankedElement<T>>();
		for (int i=0; i<pop.length; i++) {
			for (int k=i+1; k<pop.length; k++) {
				int result = compare(pop[i].solution.getObjectives(), pop[k].solution.getObjectives());
				if (result>0) pop[k].dominates(pop[i]);
				else if (result<0) pop[i].dominates(pop[k]);
			}

			if (pop[i].dominatedBy==0) nextFront.add(pop[i]);
		}
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
		return !nextFront.isEmpty();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public SolutionSet<T> next() {
		// process a new frontier: the next frontier becomes the last one
		List<RankedElement<T>> lastFront = nextFront;
		nextFront = new LinkedList<RankedElement<T>>();

		SolutionSet<T> ret = new SolutionSet<T>(lastFront.size());
		for (RankedElement<T> e : lastFront) {
			ret.add(e.solution);

			for (RankedElement<T> k : e.dominated) {
				if(k.promoteAndCheck())
					nextFront.add(k);
			}
		}

		return ret;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("unchecked")
	private static <T extends Variable> RankedElement<T>[] convert(SolutionSet<T> solutionSet) {
		RankedElement<T>[] pop = new RankedElement[solutionSet.size()];

		int i = 0;
		for (Solution<T> sol : solutionSet)
			pop[i++] = new RankedElement<T>(sol);

		return pop;
	}

	private static int compare(double[] obj1, double[] obj2) {
		if(TestFul.DEBUG) {
			if(obj1 == null) TestFul.debug("The first array of objectives is null");
			if(obj2 == null) TestFul.debug("The second array of objectives is null");
			if(obj1.length != obj2.length) TestFul.debug("The objectives must have the same length");
		}

		boolean greaterInP1 = false;  // Found an element in p1 that is greater than the corresponding in p2
		boolean greaterInP2 = false;  // Found an element in p2 that is greater than the corresponding in p1

		for(int i=0; i < obj1.length; i++) {
			if (obj1[i]<obj2[i]) {
				if (greaterInP1) return 0;
				greaterInP2 = true;
			} else if (obj1[i]>obj2[i]) {
				if (greaterInP2) return 0;
				greaterInP1 = true;
			}
		}

		if (greaterInP1) return 1;
		else if (greaterInP2) return -1;
		else return 0;

	}

} // Ranking
