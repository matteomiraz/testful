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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.base.EvaluationTerminationCriterion;
import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.localSearch.LocalSearchPopulation;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.Condition;
import testful.coverage.whiteBox.ConditionTargetDatum;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageBranch;
import testful.coverage.whiteBox.CoverageBranchTarget;
import testful.coverage.whiteBox.Data;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.Operation;
import testful.model.PrimitiveClazz;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.transformation.SimplifierDynamic;
import testful.model.transformation.Splitter;
import testful.utils.ElementManager;
import ec.util.MersenneTwisterFast;

/**
 * This class integrates the evolutionary engine, with a Local Search that targets an uncovered branch.
 *
 * Luciano Baresi, Pier Luca Lanzi, Matteo Miraz<br>
 * <a href="http://doi.ieeecomputersociety.org/10.1109/ICST.2010.54">TestFul: An Evolutionary Test Approach for Java</a><br>
 * in Proceedints of the Third International Conference on Software Testing, Verification and Validation(ICST) 2010, pp.185-194<br>
 *
 * @author matteo
 */
public class LocalSearchBranch extends LocalSearchPopulation<Operation> {

	private static final Logger logger = Logger.getLogger("testful.evolutionary.localSearch");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);
	private static final boolean LOG_FINER = logger.isLoggable(Level.FINER);
	private static final boolean LOG_FINEST = logger.isLoggable(Level.FINEST);

	private static final int ITERATIONS = 1000;

	private final AtomicInteger localSearchId = new AtomicInteger(0);

	private final MersenneTwisterFast random;

	/** Number of attempts when a new operation is targeted */
	private final int TTL_FIRST = 10;
	/** Number of attempts for operations able to reduce the branch distance */
	private final int TTL_IMPROVEMENT  = 50;

	private final float SCORE_BOOL = 0;
	private final float SCORE_ARRAY = 0;
	private final float SCORE_REF = 0;
	private final float SCORE_STRING = 0;
	private final float SCORE_CHAR = 10;
	private final float SCORE_NUMBER = 10;

	private final float SCORE_FIELD = -100;
	private final float SCORE_PARAM =  200;
	private final float SCORE_ZERO_USES = Float.NEGATIVE_INFINITY;
	private final float SCORE_ONE_USE = 0;
	private final float SCORE_TWO_USES = -100;

	private final float SCORE_MISS_ATTEMPTS = -1000;

	/** probability to add an operation before the selected operation */
	private float probAdd = 0.05f;

	/** probability to remove the selected operation */
	private float probRemove = 0.15f;

	/** if an AssignPrimitive is selected, this is the probability to modify the value */
	private float probModify = 0.8f;

	private final TestfulProblem problem;

	/** for each branch (key) stores the number of attempts (value) */
	private final Map<Integer, Integer> attempts;

	private static final Comparator<BranchScore> scoreSorter = new Comparator<BranchScore>() {

		@Override
		public int compare(BranchScore s1, BranchScore s2) {
			if(s1 == s2) return 0;
			if(s1 == null) return  1;
			if(s2 == null) return -1;

			if(s1.getQuality() < s2.getQuality()) return -1;
			if(s1.getQuality() > s2.getQuality()) return  1;
			return 1;
		}
	};

	public LocalSearchBranch(TestfulProblem testfulProblem) {
		problem = testfulProblem;
		attempts = new HashMap<Integer, Integer>();
		random = PseudoRandom.getMersenneTwisterFast();
	}

	/**
	 * Sets the probability to add an operation before the selected operation
	 * @param probAdd the probability to add an operation before the selected operation
	 */
	public void setProbAdd(float probAdd) {
		this.probAdd = probAdd;
	}

	/**
	 * Sets the probability to modify the value (if an AssignPrimitive is selected)
	 * @param probModify  the probability to modify the value (if an AssignPrimitive is selected)
	 */
	public void setProbModify(float probModify) {
		this.probModify = probModify;
	}

	/**
	 * Sets the probability to remove the selected operation
	 * @param probRemove the probability to remove the selected operation
	 */
	public void setProbRemove(float probRemove) {
		this.probRemove = probRemove;
	}

	@Override
	public Solution<Operation> execute(Solution<Operation> solution) throws JMException {
		try {
			Collection<TestCoverage> tests = evalParts(solution);
			Set<TestWithScore> testScore = addSearchScore(tests);
			TestWithScore test = getBest(testScore);
			if(test == null || test.score == null) return null;

			List<Operation> result = hillClimb(test);
			if(result == null) return null;

			for(Operation op : result)
				solution.getDecisionVariables().variables_.add(op);

			return solution;


		} catch(Throwable e) {
			throw new JMException(e);
		}
	}

	@Override
	public SolutionSet<Operation> execute(SolutionSet<Operation> solutionSet) throws JMException {
		try {
			Set<TestCoverage> tests = new LinkedHashSet<TestCoverage>();
			for(Solution<Operation> solution : solutionSet)
				tests.addAll(evalParts(solution));

			Set<TestWithScore> testScore = addSearchScore(tests);
			TestWithScore test = getBest(testScore);
			if(test == null || test.score == null) return null;

			List<Operation> result = hillClimb(test);
			if(result == null) return null;

			SolutionSet<Operation> ret = new SolutionSet<Operation>(solutionSet.size());
			for(Solution<Operation> s : solutionSet) {
				for(Operation op : result) s.getDecisionVariables().variables_.add(op);
				ret.add(s);
			}

			return ret;

		} catch(Throwable e) {
			throw new JMException(e);
		}
	}

	private List<Operation> hillClimb(TestWithScore test) throws InterruptedException, ExecutionException {
		final int localSearchId = this.localSearchId.incrementAndGet();
		final int branchId = test.score.getBranchId();

		TrackerDatum[] data = new TrackerDatum[]{ new ConditionTargetDatum(branchId) };


		final ElementManager<String, CoverageInformation> covs = problem.evaluate(test.test, data).get();
		CoverageBranchTarget covCondOrig = (CoverageBranchTarget)covs.get(CoverageBranchTarget.KEY);

		logger.info("Selected branch: " + branchId + " (score: " + test.score.getQuality() + " length: " + test.test.getTest().length + ")");

		if(LOG_FINE) logger.fine("coverageLocalSearch " + localSearchId + " branch=" + branchId + ";iter=" + 0 + ";cov=" + covCondOrig.getQuality() + ";distance=" + covCondOrig + ";len=" + test.test.getTest().length);

		List<Operation> opsOrig = new LinkedList<Operation>();
		for(Operation op : test.test.getTest())
			opsOrig.add(op);

		Integer nAttempts = attempts.get(branchId);
		if(nAttempts == null) nAttempts = 0;

		int pos = 0; // the position to target
		int ttl = 0; // how many times the position can be targeted again
		for(int i = 0; i < ITERATIONS*(nAttempts+1) && !terminationCriterion.isTerminated(); i++) {

			List<Operation> ops = new LinkedList<Operation>(opsOrig);

			if(--ttl < 0) {
				pos = ops.isEmpty() ? -1 : random.nextInt(ops.size());
				ttl = TTL_FIRST;
			}

			boolean canContinue = mutate(ops, pos);

			ElementManager<String, CoverageInformation> cov = problem.evaluate(problem.getTest(ops), data).get();
			CoverageBranchTarget covCond = (CoverageBranchTarget) cov.get(CoverageBranchTarget.KEY);
			if(covCond == null) covCond = new CoverageBranchTarget(branchId);

			if(LOG_FINE) logger.fine("coverageLocalSearch " + localSearchId + " branch=" + branchId + ";iter=" + (i+1) + ";cov=" + covCond.getQuality() + ";distance=" + covCond + ";len=" + ops.size());

			if(LOG_FINEST) {
				StringBuilder sb = new StringBuilder();

				if(covCond.getQuality() < covCondOrig.getQuality()) sb.append(" ");
				else if(covCond.getQuality() == covCondOrig.getQuality()) {
					if(ops.size() >= opsOrig.size()) sb.append(" ");
					else sb.append("S");
				} else sb.append("I");

				if(canContinue) sb.append("C");
				else sb.append(" ");

				sb.append(" p:" + pos + " #" + ttl + " q:" + covCond.getQuality() + " oq:" + covCondOrig.getQuality() + " d:" + covCond + " od:" + covCondOrig).append("\n");

				sb.append("Origiginal Test:\n");
				for (Operation o : opsOrig)
					sb.append("  " + o).append("\n");
				sb.append("Modified Test:\n");
				for (Operation o : ops)
					sb.append("  " + o).append("\n");
				logger.finest(sb.append("---").toString());
			}

			if(covCond.getQuality() < covCondOrig.getQuality()) continue;
			if(covCond.getQuality() == covCondOrig.getQuality() && ops.size() >= opsOrig.size()) continue;

			if(!canContinue) ttl = 0;
			else ttl = TTL_IMPROVEMENT;

			opsOrig = ops;
			covCondOrig = covCond;

			if(covCond.getQuality() == Float.POSITIVE_INFINITY) {
				logger.info("Branch " + branchId + " hit");
				attempts.remove(branchId);
				return ops;
			}
		}

		attempts.put(branchId, ++nAttempts);

		logger.info("Branch " + branchId + " missed " + nAttempts + " times");

		return null;
	}

	/**
	 * Generate a gaussian integer number.
	 * In a test of 1 billion extraction of n:
	 * <ul>
	 *	<li>It was never generated a number abs(n) > 60 </li>
	 *	<li>p(n) in [-50, 50] = 0.9999998</li>
	 *	<li>p(n) in [-40, 40] = 0.999959</li>
	 *	<li>p(n) in [-30, 30] = 0.9980</li>
	 *	<li>p(n) in [-20, 20] = 0.9643</li>
	 *	<li>p(n) in [-10, 10] = 0.7286</li>
	 *	<li>p(n) in [- 6,  6] = 0.5160</li>
	 *	<li>p(n) in [- 5,  5] = 0.4515</li>
	 *	<li>p(n) in [- 4,  4] = 0.3829</li>
	 *	<li>p(n) in [- 3,  3] = 0.3108</li>
	 *	<li>p(n) in [- 2,  2] = 0.2358</li>
	 *	<li>p(n) in [- 1,  1] = 0.1585</li>
	 * </ul>
	 * @return a gaussian integer number
	 */
	private int gaussianInteger() {
		final double g = random.nextGaussian();
		Integer n = (int) (g * 10);
		if(n != 0) return n;

		if(g >= 0) return 1;
		else return -1;
	}

	/**
	 * Mutate the operations
	 * @param ops the operations to mutate
	 * @param pos the position to mutate
	 * @return true if it is possible to work on the position (i.e., ops is not modified by removing or introducing operations)
	 */
	public boolean mutate(List<Operation> ops, int pos) {

		final boolean isModifiable = pos >= 0 && (ops.get(pos) instanceof AssignPrimitive);
		final boolean isRemovable = pos >= 0;

		/** 0 => add; 1 => modify; 2 => remove */
		final int choice;
		if(isModifiable && isRemovable) {
			float c = random.nextFloat() * (probAdd + probModify + probRemove);
			if(c < probAdd) choice = 0;
			else if(c < probAdd + probModify) choice = 1;
			else choice = 2;
		} else if(isModifiable) {
			float c = random.nextFloat() * (probAdd + probModify);
			if(c < probAdd) choice = 0;
			else choice = 1;
		} else if(isRemovable) {
			float c = random.nextFloat() * (probAdd + probRemove);
			if(c < probAdd) choice = 0;
			else choice = 2;
		} else choice = 0;

		switch(choice) {

		case 0:
			if(pos < 0) pos = 0;
			int num = random.nextInt(10);
			for(int i = 0; i < num; i++)
				ops.add(pos, Operation.randomlyGenerate(problem.getCluster(), problem.getReferenceFactory(), random));
			return false;

		case 1:
			AssignPrimitive ap = (AssignPrimitive) ops.get(pos);
			Serializable newValue = modify(ap);
			ops.set(pos, new AssignPrimitive(ap.getTarget(), newValue));
			return true;

		case 2:
			ops.remove(pos);
			return false;

		default:
			logger.fine("Invalid choice: " + choice);
			return true;
		}
	}

	/**
	 * Modifies the value used in an assingPrimitive operation
	 * @param ap the assignPrimitive operation to mutate
	 * @return the new value to use in the assignPrimitive Operation
	 */
	private Serializable modify(AssignPrimitive ap) {
		final Serializable value = ap.getValue();
		final Clazz type = ap.getTarget().getClazz();

		if(type instanceof PrimitiveClazz) {
			switch(((PrimitiveClazz) type).getType()) {
			case BooleanClass:
			case BooleanType:
				if(value == null)
					return random.nextBoolean();

				return !((Boolean)value);

			case ByteClass:
			case ByteType:
				if(value == null)
					return random.nextByte();

				return (byte) (((Byte)value) + gaussianInteger());

			case CharacterClass:
			case CharacterType:
				if(value == null)
					return random.nextChar();

				return (char) (((Character)value) + gaussianInteger());

			case DoubleClass:
			case DoubleType:
				if(value == null)
					return random.nextDouble();

				if(random.nextBoolean(.9))
					return ((Double)value) + random.nextGaussian();
				else
					return random.nextDouble();

			case FloatClass:
			case FloatType:
				if(value == null)
					return random.nextFloat();

				if(random.nextBoolean(.9))
					return (float) (((Float)value) + random.nextGaussian());
				else
					return random.nextFloat();

			case IntegerClass:
			case IntegerType:
				if(value == null)
					return random.nextInt();

				return (int) (((Integer)value) + gaussianInteger() * (random.nextBoolean(.9) ? 1 : 1000));

			case LongClass:
			case LongType:
				if(value == null)
					return random.nextLong();

				return (long) (((Long)value) + gaussianInteger() * (random.nextBoolean(.9) ? 1 : 1000));

			case ShortClass:
			case ShortType:
				if(value == null)
					return random.nextShort();

				return (short) (((Short)value) + gaussianInteger());
			}

		} else {

			if(type.getClassName().equals("java.lang.String")) {

				if(value == null) {
					return AssignPrimitive.getString(random);

				} else {

					byte[] bytes = ((String)value).getBytes();
					final int lBytes = bytes.length;

					byte[] newBytes;

					// if lBytes = 0, the string is empty. In this case we only insert new characters
					switch( (lBytes > 0 ? random.nextInt(3) : 1) ) {
					case 1: // add one or more character(s)
					{
						int howMany = random.nextInt(10)+1;
						SortedSet<Integer> nPos = new TreeSet<Integer>();
						while(nPos.size() < howMany)
							nPos.add(random.nextInt(lBytes + howMany));

						newBytes = new byte[lBytes + howMany];

						int j = 0;
						Iterator<Integer> iter = nPos.iterator();
						Integer next = iter.next();
						for (int i = 0; i < newBytes.length; i++) {
							if(next != null && i == next) {
								newBytes[i] = (byte) AssignPrimitive.getCharacter(random);
								if(iter.hasNext()) next = iter.next();
								else next = null;

							} else newBytes[i] = bytes[j++];
						}

						break;
					}

					case 2: // remove a character
					{
						int howMany = 1 + (lBytes == 1 ? 0 : random.nextInt(lBytes-1));
						SortedSet<Integer> nPos = new TreeSet<Integer>();
						while(nPos.size() < howMany)
							nPos.add(random.nextInt(lBytes));

						newBytes = new byte[lBytes - howMany];

						int j = 0;
						Iterator<Integer> iter = nPos.iterator();
						Integer next = iter.next();
						for (int i = 0; i < bytes.length; i++) {

							if(next != null && i == next) {
								if(iter.hasNext()) next = iter.next();
								else next = null;

							} else newBytes[j++] = bytes[i];
						}

						break;
					}

					default: // change a character
					{
						newBytes = bytes;

						int howMany = 1 + (lBytes == 1 ? 0 : random.nextInt(lBytes-1));
						SortedSet<Integer> nPos = new TreeSet<Integer>();
						while(nPos.size() < howMany)
							nPos.add(random.nextInt(lBytes));

						for (Integer i : nPos) {
							newBytes[i] = (byte) AssignPrimitive.getCharacter(random);
						}

					}

					}

					return new String(newBytes);
				}

			} else {
				logger.fine("Unknown type in AssignPrimitive"  + type.getClassName() + " (" + ap + ")");
			}
		}

		return value;
	}

	private TestWithScore getBest(Set<TestWithScore> testScore) {

		BranchScore max = null;
		SortedSet<TestWithScore> maxSet = new TreeSet<TestWithScore>();

		for(TestWithScore t : testScore) {
			if(max == null) {
				max = t.score;
				maxSet.add(t);
			} else {
				int compare = scoreSorter.compare(max, t.score);

				if(compare < 0) {
					max = t.score;
					maxSet.clear();
					maxSet.add(t);
				} else if(compare == 0) {
					maxSet.add(t);
				}
			}
		}

		if(maxSet.isEmpty()) return null;

		TestWithScore[] ret = maxSet.toArray(new TestWithScore[maxSet.size()]);

		if(logger.isLoggable(Level.FINEST)) {
			StringBuilder sb = new StringBuilder("Tests with maximum score:\n");

			for (int i = 0; i < ret.length; i++) {
				sb.append("Test # ").append(i).append(" - ").append(ret[i].score.toString()).append("\n");
				sb.append(ret[i].test.toString()).append("\n");
			}

			logger.finest(sb.toString());
		}

		final int nextInt = random.nextInt(ret.length);
		logger.finest("Selected test #" + nextInt + " (out of " + ret.length + ")");
		return ret[nextInt];

	}

	private Set<TestCoverage> evalParts(Solution<Operation> solution) throws InterruptedException, ExecutionException {

		List<Test> parts = Splitter.split(true,
				SimplifierDynamic.singleton.perform(problem.getFinder(),
						problem.getTest(solution.getDecisionVariables().variables_)));

		List<Future<ElementManager<String, CoverageInformation>>> futures = new ArrayList<Future<ElementManager<String, CoverageInformation>>>(parts.size());
		for(Test t : parts) futures.add(problem.evaluate(t));

		if(terminationCriterion instanceof EvaluationTerminationCriterion)
			((EvaluationTerminationCriterion)terminationCriterion).addEvaluations(parts.size());

		Set<TestCoverage> tests = new LinkedHashSet<TestCoverage>();

		// iterate both on parts and on futures
		Iterator<Test> partsIter = parts.iterator();
		Iterator<Future<ElementManager<String, CoverageInformation>>> futuresIter = futures.iterator();
		while(partsIter.hasNext())
			tests.add(new TestCoverage(partsIter.next(), futuresIter.next().get()));

		return tests;
	}

	private Set<TestWithScore> addSearchScore(Collection<TestCoverage> tests) {

		// calculate the executed branches
		final BitSet execBranches = new BitSet();
		for(TestCoverage t : tests) {
			CoverageBranch cov = (CoverageBranch) t.getCoverage().get(CoverageBranch.KEY);
			if(cov != null) execBranches.or(cov.getCoverage());
		}

		final Set<TestWithScore> ret = new TreeSet<TestWithScore>();
		for(TestCoverage t : tests) {

			final CoverageBasicBlocks bbCov = (CoverageBasicBlocks) t.getCoverage().get(CoverageBasicBlocks.KEY);
			if(bbCov == null) continue;

			// retrieve the set of branches reachable (1st line) but not executed (2nd line)
			final BitSet branches = (BitSet) problem.getWhiteAnalysis().getReachableBranches(bbCov.getCoverage()).clone();
			branches.andNot(execBranches);

			// if the set is empty, continue with the next branch
			if(branches.isEmpty()) continue;

			for (int branchId = branches.nextSetBit(0); branchId >= 0; branchId = branches.nextSetBit(branchId+1)) {

				float score = 0;
				if(attempts.containsKey(branchId))
					score += SCORE_MISS_ATTEMPTS * attempts.get(branchId);

				// score type, fields/var/params
				Condition c = problem.getWhiteAnalysis().getConditionFromBranch(branchId);

				if(c.getUse1() == null) { // 0 uses
					score += SCORE_ZERO_USES;

				} else { // 1 or 2 uses

					switch(c.getUse1().getData().getType()) {
					case Boolean: score += SCORE_BOOL; break;
					case Array: score += SCORE_ARRAY; break;
					case Reference: score += SCORE_REF; break;
					case String:	score += SCORE_STRING; break;
					case Character: score += SCORE_CHAR; break;
					case Number: score += SCORE_NUMBER; break;
					}

					if(c.getUse2() == null) { // 1 use
						score += SCORE_ONE_USE;

						Data data1 = c.getUse1().getData();
						if(data1.isParam()) score += SCORE_PARAM;
						else if(data1.isField()) score += SCORE_FIELD;

					} else { // 2 uses
						score += SCORE_TWO_USES;

						Data data1 = c.getUse1().getData();
						Data data2 = c.getUse2().getData();
						if(data1.isParam() || data2.isParam()) score += SCORE_PARAM;
						if(data1.isField()) score += SCORE_FIELD;
						if(data2.isField()) score += SCORE_FIELD;

					}
				}

				ret.add(new TestWithScore(t, new BranchScore(branchId, score)));
			}
		}

		if(logger.isLoggable(Level.FINEST)) {
			StringBuilder sb = new StringBuilder("Candidate tests:\n");

			for (TestWithScore t : ret) {
				sb.append("Test ").append(t.score.toString()).append("\n");
				sb.append(t.test.toString()).append("\n");
			}

			logger.finest(sb.toString());
		}

		return ret;
	}


	private class BranchScore implements CoverageInformation {
		private static final long serialVersionUID = 3532944197426242932L;

		public static final String KEY= "BranchScore";

		private final int branchId;
		private final float quality;

		public BranchScore(int branchId, float quality) {
			this.branchId = branchId;
			this.quality = quality;
		}

		public int getBranchId() {
			return branchId;
		}

		@Override
		public float getQuality() {
			return quality;
		}

		@Override
		public String toString() {
			return "[branch:" + branchId + "] quality: " + quality;
		}

		@Override
		public String getKey() {
			return KEY;
		}

		@Override
		public String getName() {
			return KEY;
		}

		@Override
		public boolean contains(CoverageInformation other) {
			throw new NullPointerException("Cannot create this method");
		}

		@Override
		public CoverageInformation createEmpty() {
			throw new NullPointerException("Cannot create this method");
		}

		@Override
		public void merge(CoverageInformation other) {
			throw new NullPointerException("Cannot create this method");
		}

		@Override
		public BranchScore clone() {
			throw new NullPointerException("Cannot create this method");
		}
	}

	private static class TestWithScore implements Comparable<TestWithScore> {
		final TestCoverage test;
		final BranchScore score;

		public TestWithScore(TestCoverage test, BranchScore score) {
			this.test = test;
			this.score = score;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((score == null) ? 0 : score.hashCode());
			result = prime * result + ((test == null) ? 0 : test.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) return true;
			if(obj == null) return false;
			if(!(obj instanceof TestWithScore)) return false;
			TestWithScore other = (TestWithScore) obj;
			if(score == null) {
				if(other.score != null) return false;
			} else if(!score.equals(other.score)) return false;
			if(test == null) {
				if(other.test != null) return false;
			} else if(!test.equals(other.test)) return false;
			return true;
		}

		@Override
		public String toString() {
			return score.branchId + ": " + score.quality;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(TestWithScore o) {
			if(score.branchId != o.score.branchId) return score.branchId - o.score.branchId;

			if(score.quality > o.score.quality) return 1;
			else if(score.quality < o.score.quality) return -1;

			if(test.getTest().length != o.test.getTest().length) return test.getTest().length - o.test.getTest().length;

			if(test.hashCode() == o.test.hashCode()) {
				if(LOG_FINER)
					logger.finer("Same Tests! " +
							"\n T1 " + score + " hash:" + test.hashCode() + " \n" + test +
							"\n T2 " + o.score + " hash:" + o.test.hashCode() + " \n" + o.test);
			}

			return test.hashCode() - o.test.hashCode();
		}
	}
}
