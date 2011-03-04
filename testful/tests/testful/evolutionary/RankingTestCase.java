/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
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

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.Variable;
import junit.framework.TestCase;

/**
 * Testing the ranking algorithm
 * @author matteo
 */
public class RankingTestCase extends TestCase {

	public void testJmetalRanking() throws Exception {

		SolutionSet<Variable> pop = new SolutionSet<Variable>(5);

		@SuppressWarnings("unchecked")
		Solution<Variable>[][] expected = new Solution[][] {
			{ createAndAdd(pop, 1, 1) },
			{ createAndAdd(pop, 4, 6) },
			{ createAndAdd(pop, 6, 7) },
			{ createAndAdd(pop, 8, 8) },
			{ createAndAdd(pop, 9, 9) },
		};

		jmetal.util.Ranking<Variable> r = new jmetal.util.Ranking<Variable>(pop);

		assertEquals(expected.length, r.getNumberOfSubfronts());

		for (int i = 0; i < expected.length; i++) {

			SolutionSet<Variable> front = r.getSubfront(i);
			assertEquals(expected[i].length, front.size());

			for (int j = 0; j < expected[i].length; j++)
				assertTrue("front " + i + " does not contain element " + expected[i][j] + ": " + front, contains(front, expected[i][j]));
		}
	}

	@SuppressWarnings("unchecked")
	public void testOnePerFrontier() throws Exception {

		SolutionSet<Variable> pop = new SolutionSet<Variable>(5);

		Solution<Variable> s0 = createAndAdd(pop, 1, 1);
		Solution<Variable> s1 = createAndAdd(pop, 4, 6);
		Solution<Variable> s2 = createAndAdd(pop, 6, 7);
		Solution<Variable> s3 = createAndAdd(pop, 8, 8);
		Solution<Variable> s4 = createAndAdd(pop, 9, 9);

		check(new Ranking<Variable>(pop), new Solution[][] {
			{ s0 },
			{ s1 },
			{ s2 },
			{ s3 },
			{ s4 },
		});
	}

	@SuppressWarnings("unchecked")
	public void testOnePerFrontierReverse() throws Exception {

		SolutionSet<Variable> pop = new SolutionSet<Variable>(5);

		Solution<Variable> s4 = createAndAdd(pop, 9, 9);
		Solution<Variable> s3 = createAndAdd(pop, 8, 8);
		Solution<Variable> s2 = createAndAdd(pop, 6, 7);
		Solution<Variable> s1 = createAndAdd(pop, 4, 6);
		Solution<Variable> s0 = createAndAdd(pop, 1, 1);

		check(new Ranking<Variable>(pop), new Solution[][] {
			{ s0 },
			{ s1 },
			{ s2 },
			{ s3 },
			{ s4 },
		});
	}

	@SuppressWarnings("unchecked")
	public void testOnePerFrontierRandom() throws Exception {

		SolutionSet<Variable> pop = new SolutionSet<Variable>(5);

		final Solution<Variable> s1 = createAndAdd(pop, 4, 6);
		final Solution<Variable> s3 = createAndAdd(pop, 8, 8);
		final Solution<Variable> s0 = createAndAdd(pop, 1, 1);
		final Solution<Variable> s2 = createAndAdd(pop, 6, 7);
		final Solution<Variable> s4 = createAndAdd(pop, 9, 9);

		check(new Ranking<Variable>(pop), new Solution[][] {
			{ s0 },
			{ s1 },
			{ s2 },
			{ s3 },
			{ s4 },
		});
	}

	@SuppressWarnings("unchecked")
	public void testSame() throws Exception {

		SolutionSet<Variable> pop = new SolutionSet<Variable>(6);

		final Solution<Variable> s0 = createAndAdd(pop, 1, 1);
		final Solution<Variable> s1 = createAndAdd(pop, 5, 5);
		final Solution<Variable> s2 = createAndAdd(pop, 5, 5);
		final Solution<Variable> s3 = createAndAdd(pop, 5, 6);
		final Solution<Variable> s4 = createAndAdd(pop, 6, 5);
		final Solution<Variable> s5 = createAndAdd(pop, 6, 6);

		check(new Ranking<Variable>(pop), new Solution[][] {
			{ s0 },
			{ s1, s2 },
			{ s3, s4 },
			{ s5 },
		});
	}

	@SuppressWarnings("unchecked")
	public void testOneFrontier() throws Exception {

		SolutionSet<Variable> pop = new SolutionSet<Variable>(5);

		final Solution<Variable> s1 = createAndAdd(pop, 9, 1);
		final Solution<Variable> s3 = createAndAdd(pop, 8, 2);
		final Solution<Variable> s0 = createAndAdd(pop, 5, 5);
		final Solution<Variable> s2 = createAndAdd(pop, 2, 8);
		final Solution<Variable> s4 = createAndAdd(pop, 1, 9);

		check(new Ranking<Variable>(pop), new Solution[][] {
			{ s0, s1, s2, s3, s4 },
		});
	}

	public void testFrontier1() throws Exception {

		SolutionSet<Variable> pop = new SolutionSet<Variable>(5);

		Solution<Variable> s0 = createAndAdd(pop, 1, 1);
		createAndAdd(pop, 4, 6);
		createAndAdd(pop, 6, 7);
		createAndAdd(pop, 8, 8);
		createAndAdd(pop, 9, 9);

		@SuppressWarnings("unchecked")
		Solution<Variable>[] expected = new Solution[] { s0 };
		SolutionSet<Variable> front = Ranking.getFrontier(pop);

		assertEquals(expected.length, front.size());
		for (Solution<Variable> s : front)
			assertTrue(s + "must belong to the frontier", contains(front, s));
	}

	public void testFrontier2() throws Exception {

		SolutionSet<Variable> pop = new SolutionSet<Variable>(5);

		final Solution<Variable> s1 = createAndAdd(pop, 9, 1);
		final Solution<Variable> s3 = createAndAdd(pop, 8, 2);
		final Solution<Variable> s0 = createAndAdd(pop, 5, 5);
		final Solution<Variable> s2 = createAndAdd(pop, 2, 8);
		final Solution<Variable> s4 = createAndAdd(pop, 1, 9);

		@SuppressWarnings("unchecked")
		Solution<Variable>[] expected = new Solution[] { s0, s1, s2, s3, s4 };
		SolutionSet<Variable> front = Ranking.getFrontier(pop);

		assertEquals(expected.length, front.size());
		for (Solution<Variable> s : front)
			assertTrue(s + "must belong to the frontier", contains(front, s));
	}

	@SuppressWarnings("unused")
	private void print(Ranking<Variable> r) {
		int n = 0;
		while(r.hasNext()) {
			System.err.println("Frontier " + (n++));

			SolutionSet<Variable> front = r.next();
			for (Solution<Variable> s : front)
				System.err.println("  " + s);
		}
	}

	private boolean contains(SolutionSet<Variable> front, Solution<Variable> solution) {
		for (Solution<Variable> s : front)
			if(s == solution)
				return true;

		return false;
	}

	private Solution<Variable> createAndAdd(SolutionSet<Variable> pop, int x, int y) {
		Solution<Variable> s = new Solution<Variable>(2);
		s.setObjective(0, x);
		s.setObjective(1, y);
		pop.add(s);
		return s;
	}

	private void check(Ranking<Variable> ranking, Solution<Variable>[][] expected) {

		for (int i = 0; i < expected.length; i++) {
			assertTrue("Frontier " + i, ranking.hasNext());

			SolutionSet<Variable> front = ranking.next();
			assertEquals("Frontier " + i, expected[i].length, front.size());

			for (int j = 0; j < expected[i].length; j++)
				assertTrue("front " + i + " does not contain element " + expected[i][j] + ": " + front, contains(front, expected[i][j]));
		}

		assertFalse(ranking.hasNext());
	}
}
