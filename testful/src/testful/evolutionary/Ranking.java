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

	private final Solution<T>[] pop;

	/** c[i] contains the number of points that dominate i */
	private final int[] c;

	/** s[i] contains all elements dominated by i */
	private final List<Integer>[] s;

	/** elements belonging to the next front */
	private List<Integer> nextFront;

	/**
	 * Constructor.
	 * @param solutionSet The <code>SolutionSet</code> to be ranked.
	 */
	@SuppressWarnings("unchecked")
	public Ranking(SolutionSet<T> solutionSet) {

		pop = convert(solutionSet);
		c = new int[pop.length];
		s = new List[pop.length];
		for(int i = 0; i < s.length; i++) s[i] = new LinkedList<Integer>();

		nextFront = new LinkedList<Integer>();
		for (int i=0; i<pop.length; i++) {
			for (int k=i+1; k<pop.length; k++) {
				int result = compare(pop[i].getObjectives(), pop[k].getObjectives());
				if (result>0) {
					c[i]++;
					s[k].add(i);
				} else if (result<0) {
					c[k]++;
					s[i].add(k);
				}
			}
			if (c[i]==0) nextFront.add(i);
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
		List<Integer> lastFront = nextFront;
		nextFront = new LinkedList<Integer>();

		SolutionSet<T> ret = new SolutionSet<T>(lastFront.size());
		for (Integer idx : lastFront) {
			ret.add(pop[idx]);

			for (int k : s[idx]) {
				if(--c[k]==0)
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
	private static <T extends Variable> Solution<T>[] convert(SolutionSet<T> solutionSet) {
		Solution<T>[] pop = new Solution[solutionSet.size()];

		int i = 0;
		for (Solution<T> sol : solutionSet)
			pop[i++] = sol;

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
