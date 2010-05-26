package testful.evolutionary.jMetal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import jmetal.base.Solution;
import jmetal.base.SolutionSet;
import jmetal.base.operator.localSearch.LocalSearchPopulation;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.AnalysisWhiteBox;
import testful.coverage.whiteBox.Condition;
import testful.coverage.whiteBox.ConditionTargetDatum;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageConditionTarget;
import testful.coverage.whiteBox.CoverageConditions;
import testful.coverage.whiteBox.Data;
import testful.coverage.whiteBox.DataUse;
import testful.coverage.whiteBox.WhiteBoxData;
import testful.model.AssignPrimitive;
import testful.model.Operation;
import testful.model.PrimitiveClazz;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestCoverage;
import testful.model.TestSplitter;
import testful.model.TestfulProblem;
import testful.utils.ElementManager;
import ec.util.MersenneTwisterFast;

public class LocalSearchBranch extends LocalSearchPopulation<Operation> {

	private static final Logger logger = Logger.getLogger("testful.evolutionary");

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

	private final float SCORE_COND_CODE = 5;
	private final float SCORE_COND_CONTRACT = 0;

	private final float SCORE_MISS_ATTEMPTS = -1000;

	/** if there is a constant, this is the probability to modify its value */
	private float probModify = 0.8f;
	private float probRemove = 0.2f;

	private int evaluations = 0;
	private int maxEvaluations = 1000;

	private TestfulProblem problem;
	private final AnalysisWhiteBox whiteAnalysis;

	private final BitSet condCode;
	private final BitSet condContract;

	/** for each branch (key) stores the number of attempts (value) */
	private final Map<Integer, Integer> attempts;

	private static final Comparator<BranchScore> scoreSorter = new Comparator<BranchScore>() {

		@Override
		public int compare(BranchScore s1, BranchScore s2) {
			if(s1 == null && s2 == null) return 0;
			if(s1 == null) return  1;
			if(s2 == null) return -1;

			if(s1.getQuality() < s2.getQuality()) return -1;
			if(s1.getQuality() > s2.getQuality()) return  1;
			return 0;

		}
	};

	public LocalSearchBranch(TestfulProblem testfulProblem) {
		random = PseudoRandom.getMersenneTwisterFast();

		problem = testfulProblem;
		whiteAnalysis = testfulProblem.getWhiteAnalysis();

		final WhiteBoxData data = whiteAnalysis.getData();

		condCode = data.getConditionsCode();
		condContract = data.getConditionsContract();

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
			BitSet execConds = getExecutedConditions(tests);
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

			BitSet execConds = getExecutedConditions(tests);

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

	private List<Operation> hillClimb(TestWithScore test) throws InterruptedException, ExecutionException, TestfulException {
		final int localSearchId = this.localSearchId.incrementAndGet();
		final int branchId = test.score.getBranchId();

		TrackerDatum[] data = new TrackerDatum[]{ new ConditionTargetDatum(branchId) };

		List<Operation> opsOrig = new ArrayList<Operation>(test.test.getTest().length);
		for(Operation op : test.test.getTest())
			opsOrig.add(op);

		final String KEY = CoverageConditionTarget.getKEY(branchId);

		final ElementManager<String, CoverageInformation> covs = problem.evaluate(opsOrig, data).get();
		CoverageConditionTarget covCondOrig = (CoverageConditionTarget)covs.get(KEY);

		logger.info("Selected branch: " + branchId + " (score: " + test.score.getQuality() + ")");

		logger.fine("coverageLocalSearch " + localSearchId + " branch=" + branchId + ";iter=" + 0 + ";cov=" + covCondOrig.getQuality() + ";distance=" + covCondOrig + ";len=" + test.test.getTest().length);

		Integer p = attempts.get(branchId);
		if(p == null) p = 0;

		for(int i = 0; i < maxEvaluations*(p+1); i++) {

			List<Operation> ops = new ArrayList<Operation>(opsOrig);

			mutate(problem.getCluster(), problem.getRefFactory(), ops, random, probModify, probRemove);

			ElementManager<String, CoverageInformation> cov = problem.evaluate(ops, data).get();
			CoverageConditionTarget covCond = (CoverageConditionTarget) cov.get(KEY);

			logger.fine("coverageLocalSearch " + localSearchId + " branch=" + branchId + ";iter=" + (i+1) + ";cov=" + covCond.getQuality() + ";distance=" + covCond + ";len=" + ops.size());

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

				if(ap.getTarget().getClazz() instanceof PrimitiveClazz) {
					switch(((PrimitiveClazz) ap.getTarget().getClazz()).getType()) {
					case BooleanClass:
					case BooleanType:
						value = !((Boolean)value);
						break;

					case ByteClass:
					case ByteType:
						value = (byte) (((Byte)value) + rand(random));
						break;

					case CharacterClass:
					case CharacterType:
						value = (char) (((Character)value) + rand(random));
						break;

					case DoubleClass:
					case DoubleType:

						if(random.nextBoolean(.75)) {
							value = ((Double)value) + random.nextGaussian();
						} else {
							value = random.nextDouble();
						}
						break;

					case FloatClass:
					case FloatType:
						if(random.nextBoolean(.75)) {
							value = (float) (((Float)value) + random.nextGaussian());
						} else {
							value = random.nextFloat();
						}
						break;

					case IntegerClass:
					case IntegerType:
						value = (int) (((Integer)value) + rand(random) * (random.nextBoolean(.75) ? 1 : 1000));
						break;

					case LongClass:
					case LongType:
						value = (long) (((Long)value) + rand(random) * (random.nextBoolean(.75) ? 1 : 1000));
						break;

					case ShortClass:
					case ShortType:
						value = (short) (((Integer)value) + rand(random));
						break;
					}
				} else {
					// TODO: do something for the string
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


	private BitSet getExecutedConditions(Collection<TestCoverage> tests) {
		BitSet ret = new BitSet();

		for(TestCoverage testCoverage : tests) {
			CoverageConditions cov;
			cov = (CoverageConditions) testCoverage.getCoverage().get(CoverageConditions.KEY_CODE);
			if(cov != null) ret.or(cov.getCoverage());

			cov = (CoverageConditions) testCoverage.getCoverage().get(CoverageConditions.KEY_CONTRACT);
			if(cov != null) ret.or(cov.getCoverage());
		}

		return ret;
	}

	private TestWithScore getBest(Set<TestWithScore> testScore) {

		BranchScore max = null;
		Set<TestWithScore> maxSet = new HashSet<TestWithScore>();

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
		return ret[random.nextInt(ret.length)];

	}

	private Set<TestCoverage> evalParts(Solution<Operation> solution) throws TestfulException, InterruptedException, ExecutionException {
		List<Test> parts = TestSplitter.split(true, problem.createTest(solution.getDecisionVariables().variables_));
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
		Set<TestWithScore> ret = new HashSet<TestWithScore>();

		for(TestCoverage t : tests) {

			BitSet cond = new BitSet();
			CoverageBasicBlocks bbCov;

			bbCov = (CoverageBasicBlocks) t.getCoverage().get(CoverageBasicBlocks.KEY_CODE);
			if(bbCov != null) cond.or(whiteAnalysis.getReachableBranches(bbCov.getCoverage()));

			bbCov = (CoverageBasicBlocks) t.getCoverage().get(CoverageBasicBlocks.KEY_CONTRACT);
			if(bbCov != null) cond.or(whiteAnalysis.getReachableBranches(bbCov.getCoverage()));

			cond.andNot(execConds);

			if(cond.isEmpty()) continue;

			for (int branchId = cond.nextSetBit(0); branchId >= 0; branchId = cond.nextSetBit(branchId+1)) {

				float score = 0;
				if(attempts.containsKey(branchId))
					score += SCORE_MISS_ATTEMPTS * attempts.get(branchId);

				if(condCode.get(branchId)) score += SCORE_COND_CODE;
				else if(condContract.get(branchId)) score += SCORE_COND_CONTRACT;
				else logger.fine("WARNING: branch not in code or contracts");

				//TBD: consider defs & uses
				// score type, fields/var/params
				Condition c = whiteAnalysis.getConditionFromBranch(branchId);

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
			return quality + "\t" + branchId;
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

	private static class TestWithScore {
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
