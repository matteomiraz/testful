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
import testful.coverage.whiteBox.CoverageConditionTarget;
import testful.coverage.whiteBox.Data;
import testful.coverage.whiteBox.DataUse;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.Operation;
import testful.model.PrimitiveClazz;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
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

	private final AtomicInteger localSearchId = new AtomicInteger(0);

	private final MersenneTwisterFast random;

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

	/** if there is a constant, this is the probability to modify its value */
	private float probModify = 0.8f;
	private float probRemove = 0.2f;

	private int evaluations = 0;
	private int maxEvaluations = 1000;

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
		random = PseudoRandom.getMersenneTwisterFast();

		problem = testfulProblem;

		attempts = new HashMap<Integer, Integer>();
	}

	public void setProbRemove(float probRemove) {
		this.probRemove = probRemove;
	}

	public void setProbModify(float probModify) {
		this.probModify = probModify;
	}

	@Override
	public int getEvaluations() {
		return evaluations;
	}

	public void setMaxEvaluations(int maxEvaluations) {
		this.maxEvaluations = maxEvaluations;
	}

	@Override
	public Solution<Operation> execute(Solution<Operation> solution) throws JMException {
		try {
			Collection<TestCoverage> tests = evalParts(solution);
			BitSet execConds = getExecutedBranches(tests);
			Set<TestWithScore> testScore = addSearchScore(tests, execConds);
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

			BitSet execConds = getExecutedBranches(tests);

			Set<TestWithScore> testScore = addSearchScore(tests, execConds);
			TestWithScore test = getBest(testScore);
			if(test == null || test.score == null) return null;

			List<Operation> result = hillClimb(test);

			if(result == null) return null;

			Solution<Operation> solution = ((SolutionRef)test.test.getCoverage().get(SolutionRef.KEY)).getSolution();
			for(Operation op : result)
				solution.getDecisionVariables().variables_.add(op);

			SolutionSet<Operation> ret = new SolutionSet<Operation>(1);
			ret.add(solution);
			return ret;

		} catch(Throwable e) {
			throw new JMException(e);
		}
	}

	private List<Operation> hillClimb(TestWithScore test) throws InterruptedException, ExecutionException {
		final int localSearchId = this.localSearchId.incrementAndGet();
		final int branchId = test.score.getBranchId();

		TrackerDatum[] data = new TrackerDatum[]{ new ConditionTargetDatum(branchId) };

		final String KEY = CoverageConditionTarget.getKEY(branchId);

		final ElementManager<String, CoverageInformation> covs = problem.evaluate(test.test, data).get();
		CoverageConditionTarget covCondOrig = (CoverageConditionTarget)covs.get(KEY);

		logger.info("Selected branch: " + branchId + " (score: " + test.score.getQuality() + " length: " + test.test.getTest().length + ")");

		if(LOG_FINE) logger.fine("coverageLocalSearch " + localSearchId + " branch=" + branchId + ";iter=" + 0 + ";cov=" + covCondOrig.getQuality() + ";distance=" + covCondOrig + ";len=" + test.test.getTest().length);

		List<Operation> opsOrig = new ArrayList<Operation>(test.test.getTest().length);
		for(Operation op : test.test.getTest())
			opsOrig.add(op);

		Integer p = attempts.get(branchId);
		if(p == null) p = 0;

		for(int i = 0; i < maxEvaluations*(p+1); i++) {

			List<Operation> ops = new ArrayList<Operation>(opsOrig);

			mutate(problem.getCluster(), problem.getReferenceFactory(), ops, random, probModify, probRemove);

			ElementManager<String, CoverageInformation> cov = problem.evaluate(problem.getTest(ops), data).get();
			CoverageConditionTarget covCond = (CoverageConditionTarget) cov.get(KEY);

			if(covCond == null)
				covCond = new CoverageConditionTarget(branchId);

			if(LOG_FINE) logger.fine("coverageLocalSearch " + localSearchId + " branch=" + branchId + ";iter=" + (i+1) + ";cov=" + covCond.getQuality() + ";distance=" + covCond + ";len=" + ops.size());

			if(covCond.getQuality() < covCondOrig.getQuality()) continue;
			if(covCond.getQuality() == covCondOrig.getQuality() && ops.size() >= opsOrig.size()) continue;

			opsOrig = ops;
			covCondOrig = covCond;

			if(covCond.getQuality() == Float.POSITIVE_INFINITY) {

				logger.info("Branch " + branchId + " hit");
				attempts.remove(branchId);
				return ops;
			}
		}

		attempts.put(branchId, ++p);

		logger.info("Branch " + branchId + " missed " + p + " times");

		return null;
	}

	private static int rand(MersenneTwisterFast random) {
		int ret = 1;
		while(random.nextBoolean(0.9)) ret++;

		return random.nextBoolean() ? ret : -ret;
	}


	public static void mutate(TestCluster cluster, ReferenceFactory refFactory, List<Operation> ops, MersenneTwisterFast random, float probModify, float probRemove) {
		if(!ops.isEmpty() && random.nextBoolean(probModify)) {
			int pos = random.nextInt(ops.size());
			Operation op = ops.get(pos);

			if(op instanceof AssignPrimitive) {
				AssignPrimitive ap = (AssignPrimitive) op;

				Serializable value = ap.getValue();
				final Clazz type = ap.getTarget().getClazz();

				if(type instanceof PrimitiveClazz) {
					switch(((PrimitiveClazz) type).getType()) {
					case BooleanClass:
					case BooleanType:
						if(value == null) {
							value = random.nextBoolean();
							break;
						}
						value = !((Boolean)value);
						break;

					case ByteClass:
					case ByteType:
						if(value == null) {
							value = random.nextByte();
							break;
						}
						value = (byte) (((Byte)value) + rand(random));
						break;

					case CharacterClass:
					case CharacterType:
						if(value == null) {
							value = random.nextChar();
							break;
						}
						value = (char) (((Character)value) + rand(random));
						break;

					case DoubleClass:
					case DoubleType:
						if(value == null) {
							value = random.nextDouble();
							break;
						}

						if(random.nextBoolean(.75))
							value = ((Double)value) + random.nextGaussian();
						else
							value = random.nextDouble();

						break;

					case FloatClass:
					case FloatType:
						if(value == null) {
							value = random.nextFloat();
							break;
						}

						if(random.nextBoolean(.75)) {
							value = (float) (((Float)value) + random.nextGaussian());
						} else {
							value = random.nextFloat();
						}
						break;

					case IntegerClass:
					case IntegerType:
						if(value == null) {
							value = random.nextInt();
							break;
						}

						value = (int) (((Integer)value) + rand(random) * (random.nextBoolean(.75) ? 1 : 1000));
						break;

					case LongClass:
					case LongType:
						if(value == null) {
							value = random.nextLong();
							break;
						}

						value = (long) (((Long)value) + rand(random) * (random.nextBoolean(.75) ? 1 : 1000));
						break;

					case ShortClass:
					case ShortType:
						if(value == null) {
							value = random.nextShort();
							break;
						}

						value = (short) (((Integer)value) + rand(random));
						break;
					}
				} else {

					if(type.getClassName().equals("java.lang.String")) {

						if(value == null) {
							value = AssignPrimitive.getString(random);

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
								int howMany = random.nextInt(lBytes-1)+1;
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

								int howMany = random.nextInt(lBytes-1)+1;
								SortedSet<Integer> nPos = new TreeSet<Integer>();
								while(nPos.size() < howMany)
									nPos.add(random.nextInt(lBytes));

								for (Integer i : nPos) {
									newBytes[i] = (byte) AssignPrimitive.getCharacter(random);
								}

							}

							}

							value = new String(newBytes);
						}

					} else {
						logger.fine("Unknown type in AssignPrimitive"  + type.getClassName() + " (" + ap + ")");

					}
				}


				ops.set(pos, new AssignPrimitive(ap.getTarget(), value));
				return;
			}
		}

		if(!ops.isEmpty() && random.nextBoolean(probRemove)) {
			ops.remove(random.nextInt(ops.size()));
			return;
		}

		int num = random.nextInt(10);
		for(int i = 0; i < num; i++)
			ops.add(ops.isEmpty() ? 0 : random.nextInt(ops.size()), Operation.randomlyGenerate(cluster, refFactory, random));
		return;
	}


	private BitSet getExecutedBranches(Collection<TestCoverage> tests) {
		BitSet ret = new BitSet();

		for(TestCoverage testCoverage : tests) {
			CoverageBranch cov = (CoverageBranch) testCoverage.getCoverage().get(CoverageBranch.KEY);
			if(cov != null) ret.or(cov.getCoverage());
		}

		return ret;
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
		Test test = problem.getTest(solution.getDecisionVariables().variables_);
		test = SimplifierDynamic.singleton.perform(problem.getFinder(), test);

		List<Test> parts = Splitter.split(true, test);
		int size = parts.size();

		List<Future<ElementManager<String, CoverageInformation>>> futures = new ArrayList<Future<ElementManager<String, CoverageInformation>>>(size);
		for(Test t : parts) futures.add(problem.evaluate(t));

		Set<TestCoverage> tests = new LinkedHashSet<TestCoverage>();
		Iterator<Test> partsIter = parts.iterator();
		Iterator<Future<ElementManager<String, CoverageInformation>>> futuresIter = futures.iterator();
		while(partsIter.hasNext()) {
			ElementManager<String, CoverageInformation> cov = futuresIter.next().get();
			cov.put(new SolutionRef(solution));
			tests.add(new TestCoverage(partsIter.next(), cov));
		}

		evaluations += size;

		return tests;
	}

	private Set<TestWithScore> addSearchScore(Collection<TestCoverage> tests, BitSet execConds) {
		Set<TestWithScore> ret = new TreeSet<TestWithScore>();

		for(TestCoverage t : tests) {

			BitSet cond = new BitSet();
			CoverageBasicBlocks bbCov;

			bbCov = (CoverageBasicBlocks) t.getCoverage().get(CoverageBasicBlocks.KEY);
			if(bbCov != null) cond.or(problem.getWhiteAnalysis().getReachableBranches(bbCov.getCoverage()));

			cond.andNot(execConds);

			if(cond.isEmpty()) continue;

			for (int branchId = cond.nextSetBit(0); branchId >= 0; branchId = cond.nextSetBit(branchId+1)) {

				float score = 0;
				if(attempts.containsKey(branchId))
					score += SCORE_MISS_ATTEMPTS * attempts.get(branchId);

				//TBD: consider defs & uses
				// score type, fields/var/params
				Condition c = problem.getWhiteAnalysis().getConditionFromBranch(branchId);

				DataUse u = c.getUse1();
				if(u != null) {
					if(c.getUse2() != null) {
						score += SCORE_TWO_USES;

						if(!u.getData().isParam() && c.getUse2().getData().isParam())
							u = c.getUse2();
						else
							if(u.getData().isField() && !c.getUse2().getData().isField())
								u = c.getUse2();
					}
				} else {
					if(c.getUse2() == null) score += SCORE_ZERO_USES;
					else score += SCORE_ONE_USE;
				}

				if(u != null) {
					Data data = u.getData();
					switch(data.getType()) {
					case Boolean: score += SCORE_BOOL; break;
					case Array: score += SCORE_ARRAY; break;
					case Reference: score += SCORE_REF; break;
					case String:	score += SCORE_STRING; break;
					case Character: score += SCORE_CHAR; break;
					case Number: score += SCORE_NUMBER; break;
					}

					//TBD: non funziona: isField ritorna sempre false! Lavorare sulla propagazione delle def-use?
					if(data.isParam()) score += SCORE_PARAM;
					else if(data.isField()) score += SCORE_FIELD;
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

	private static class SolutionRef implements CoverageInformation {
		private static final long serialVersionUID = -5289426823204679546L;

		static final String KEY = "SolutionRef";

		private final Solution<Operation> solution;

		public SolutionRef(Solution<Operation> solution) {
			this.solution = solution;
		}

		public Solution<Operation> getSolution() {
			return solution;
		}

		@Override
		public boolean contains(CoverageInformation other) {
			return false;
		}

		@Override
		public CoverageInformation createEmpty() {
			return this;
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
		public float getQuality() {
			return 0;
		}

		@Override
		public void merge(CoverageInformation other) {
		}

		@Override
		public SolutionRef clone() throws CloneNotSupportedException {
			return this;
		}
	}
}
