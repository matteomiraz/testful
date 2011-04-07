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
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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
import testful.TestFul;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.Condition;
import testful.coverage.whiteBox.Condition.DataType;
import testful.coverage.whiteBox.ConditionIf;
import testful.coverage.whiteBox.ConditionIf.ConditionType;
import testful.coverage.whiteBox.ConditionSwitch;
import testful.coverage.whiteBox.ConditionTargetDatum;
import testful.coverage.whiteBox.Constant;
import testful.coverage.whiteBox.ContextualId;
import testful.coverage.whiteBox.CoverageBasicBlocks;
import testful.coverage.whiteBox.CoverageBranch;
import testful.coverage.whiteBox.CoverageBranchTarget;
import testful.coverage.whiteBox.CoverageDataFlow;
import testful.coverage.whiteBox.CoveragePUse;
import testful.coverage.whiteBox.CoveragePUse.PUse;
import testful.coverage.whiteBox.Data;
import testful.coverage.whiteBox.DataDef;
import testful.coverage.whiteBox.DataUse;
import testful.coverage.whiteBox.Value;
import testful.model.AssignPrimitive;
import testful.model.Clazz;
import testful.model.Operation;
import testful.model.OperationResult;
import testful.model.OperationResultTestExecutor;
import testful.model.PrimitiveClazz;
import testful.model.Test;
import testful.model.TestCoverage;
import testful.model.transformation.SimplifierDynamic;
import testful.model.transformation.Splitter;
import testful.utils.ElementManager;
import testful.utils.ElementWithKey;
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

	private static final int ITERATIONS = 1000;

	private final AtomicInteger localSearchId = new AtomicInteger(0);

	private final MersenneTwisterFast random;

	/** Number of attempts when a new operation is targeted */
	private final int TTL_FIRST = 10;
	/** Number of attempts for operations able to reduce the branch distance */
	private final int TTL_IMPROVEMENT  = 50;

	private final float SCORE_IMPOSSIBLE = Float.NEGATIVE_INFINITY;

	private final float SCORE_BOOL = 0;
	private final float SCORE_ARRAY = 0;
	private final float SCORE_REF = 0;
	private final float SCORE_STRING = 0;
	private final float SCORE_CHAR = 10;
	private final float SCORE_NUMBER = 10;

	private final float SCORE_FIELD = -100;
	private final float SCORE_PARAM =  200;

	private final float SCORE_NO_DATA  =-250;
	private final float SCORE_NO_USES  = 250;
	private final float SCORE_ONE_USE  = 100;
	private final float SCORE_TWO_USES = 0;

	private final float SCORE_MISS_ATTEMPTS = -1000;
	private final float SCORE_AMBIGUOUS = -2000;

	// prefer branches never executed
	private final float SCORE_PUSE = -500;

	/** probability to add an operation before the selected operation */
	private float probAdd = 0.05f;

	/** probability to remove the selected operation */
	private float probRemove = 0.15f;

	/** if an AssignPrimitive is selected, this is the probability to modify the value */
	private float probModify = 0.8f;

	private final TestfulProblem problem;

	/** for each target (key) stores the number of attempts (value) */
	private final Map<ConditionTargetDatum, Integer> attempts;

	public LocalSearchBranch(TestfulProblem testfulProblem) {
		problem = testfulProblem;
		attempts = new HashMap<ConditionTargetDatum, Integer>();
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
			@SuppressWarnings("unchecked")
			Collection<TestCoverage> tests = evalParts(Arrays.asList(solution));

			Set<TestWithScore> testScore = getTargets(tests);
			if(LOG_FINER) logger.finer("Targets: " + testScore);

			TestWithScore test = getBest(testScore);
			if(test == null) return null;

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

			tests = evalParts(solutionSet);

			Set<TestWithScore> testScore = getTargets(tests);

			if(LOG_FINER) logger.finer("Targets: " + testScore);

			TestWithScore test = getBest(testScore);
			if(test == null) return null;

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

		logger.info("Selected target: " + test.target + " (score: " + test.score + " length: " + test.test.getTest().length + ")");

		TrackerDatum[] data = new TrackerDatum[]{ test.target };
		final int localSearchId = this.localSearchId.incrementAndGet();
		Integer nAttempts = attempts.containsKey(test.target) ? attempts.get(test.target) : 0;

		final ElementManager<String, CoverageInformation> covs = problem.evaluate(test.test, data).get();
		CoverageBranchTarget covCondOrig = (CoverageBranchTarget)covs.get(CoverageBranchTarget.KEY);

		if(TestFul.DEBUG && covCondOrig == null) {
			TestFul.debug("hillClimb: cannot retrieve the CoverageBranchTarget");
		}

		final boolean branchFeasible;
		{
			CoverageDataFlow duCov = (CoverageDataFlow) test.test.getCoverage().get(CoverageDataFlow.KEY);
			if(duCov == null) branchFeasible = false;
			else branchFeasible = checkFeasibility(test.test, test.target, duCov) >= 0;
		}

		if(LOG_FINE) logger.fine("coverageLocalSearch " + localSearchId + " target=" + test.target + ";iter=" + 0 + ";cov=" + covCondOrig.getQuality() + ";distance=" + covCondOrig + ";len=" + test.test.getTest().length + ";feasible=" + (branchFeasible ? "true" : "n/a"));

		List<Operation> opsOrig = new LinkedList<Operation>();
		for(Operation op : test.test.getTest()) opsOrig.add(op);

		int pos = 0; // the position to target
		int ttl = 0; // how many times the position can be targeted again
		for(int i = 0; i < ITERATIONS*(nAttempts+1) && !terminationCriterion.isTerminated(); i++) {

			List<Operation> ops = new LinkedList<Operation>(opsOrig);

			if(--ttl < 0) {
				pos = ops.isEmpty() ? -1 : random.nextInt(ops.size());
				ttl = TTL_FIRST;
			}

			boolean canContinue = mutate(ops, pos);

			final Test newTest = problem.getTest(ops);
			ElementManager<String, CoverageInformation> cov = problem.evaluate(newTest, data).get();
			CoverageBranchTarget covCond = (CoverageBranchTarget) cov.get(CoverageBranchTarget.KEY);
			if(covCond == null) covCond = new CoverageBranchTarget(test.target.getBranchId(), test.target.isPUse(), test.target.getDefinitionId());

			final boolean stillFeasible;
			if(branchFeasible) {
				CoverageDataFlow duCov = (CoverageDataFlow) test.test.getCoverage().get(CoverageDataFlow.KEY);
				stillFeasible = duCov != null && checkFeasibility(new TestCoverage(newTest, cov), test.target, duCov) >= 0;
			} else
				stillFeasible = true;

			if(LOG_FINE) logger.fine("coverageLocalSearch " + localSearchId + " target=" + test.target + ";iter=" + (i+1) + ";cov=" + covCond.getQuality() + ";distance=" + covCond + ";len=" + ops.size() + ";feasible=" + (branchFeasible ? stillFeasible : "n/a"));

			if(!stillFeasible) continue;

			if(covCond.getQuality() < covCondOrig.getQuality()) continue;
			if(covCond.getQuality() == covCondOrig.getQuality() && ops.size() >= opsOrig.size()) continue;

			if(!canContinue) ttl = 0;
			else ttl = TTL_IMPROVEMENT;

			opsOrig = ops;
			covCondOrig = covCond;

			if(covCond.getQuality() == Float.POSITIVE_INFINITY) {
				logger.info("Target " + test.target + " hit");
				attempts.remove(test.target);
				return ops;
			}
		}

		attempts.put(test.target, ++nAttempts);

		logger.info("Target " + test.target + " missed " + nAttempts + " times");

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

		TestWithScore max = null;
		SortedSet<TestWithScore> maxSet = new TreeSet<TestWithScore>();

		for(TestWithScore t : testScore) {
			if(max == null) {
				max = t;
				maxSet.add(t);
			} else {
				if(t.score > max.score) {
					max = t;
					maxSet.clear();
					maxSet.add(t);
				} else if(t.score == max.score) {
					maxSet.add(t);
				}
			}
		}

		if(maxSet.isEmpty()) return null;

		TestWithScore[] ret = maxSet.toArray(new TestWithScore[maxSet.size()]);

		if(logger.isLoggable(Level.FINEST)) {
			StringBuilder sb = new StringBuilder("Tests with maximum score:\n");

			for (int i = 0; i < ret.length; i++) {
				sb.append("Test # ").append(i).append(", target ").append(ret[i].target).append(", score ").append(ret[i].score).append("\n");
				sb.append(ret[i].test.toString()).append("\n");
			}

			logger.finest(sb.toString());
		}

		final int nextInt = random.nextInt(ret.length);
		logger.finest("Selected test #" + nextInt + " (out of " + ret.length + ")");
		return ret[nextInt];

	}

	/**
	 * Given a set of tests, this function returns an equivalent set of simpler tests
	 * (the set is bigger than the original one, but each test is smaller).
	 * Each returned test is associated with the coverage information.
	 * @param solutionSet the set of tests to work on
	 * @return a set of simple tests, with their coverage information
	 * @throws InterruptedException if it is interrupted
	 * @throws ExecutionException if something goes bad during the test execution
	 */
	private Set<TestCoverage> evalParts(Iterable<Solution<Operation>> solutionSet) throws InterruptedException, ExecutionException {

		List<Future<Test>> opResultFuture = new LinkedList<Future<Test>>();
		for (Solution<Operation> solution : solutionSet) {
			Test t = problem.getTest(solution.getDecisionVariables().variables_);
			OperationResult.insert(t.getTest());
			opResultFuture.add(OperationResultTestExecutor.executeAsync(problem.getFinder(), t, problem.isReloadClasses(), problem.getData()));
		}

		List<Test> parts = new ArrayList<Test>();
		for (Future<Test> opResult : opResultFuture) {
			Test simpl = SimplifierDynamic.singleton.perform(opResult.get());
			OperationResult.remove(simpl.getTest());
			parts.addAll(Splitter.split(true, simpl));
		}

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

	private Set<TestWithScore> getTargets(Collection<TestCoverage> tests) {

		// calculate the executed branches
		final BitSet execBranches = new BitSet();
		for(TestCoverage t : tests) {
			CoverageBranch cov = (CoverageBranch) t.getCoverage().get(CoverageBranch.KEY);
			if(cov != null) execBranches.or(cov.getCoverage());
		}

		final Set<TestWithScore> ret = new TreeSet<TestWithScore>();
		for(TestCoverage t : tests) {

			final CoverageBasicBlocks bbCov = (CoverageBasicBlocks) t.getCoverage().get(CoverageBasicBlocks.KEY);
			final CoverageBranch brCov = (CoverageBranch) t.getCoverage().get(CoverageBranch.KEY);
			if(bbCov == null || brCov == null) continue;

			// collect reachable branches, i.e., those branches whose condition has been evaluated
			ElementManager<Integer, BranchTrack> reachableBranches = new ElementManager<Integer, LocalSearchBranch.BranchTrack>();
			for (Condition ec : problem.getWhiteAnalysis().getEvaluatedConditions(bbCov.getCoverage())) {
				Set<ContextualId> condDefs = new HashSet<ContextualId>();
				for (int brId : ec.getBranches()) {
					reachableBranches.put(new BranchTrack(brId, condDefs));
				}
			}

			// mark executed branches
			BitSet brCovBS = brCov.getCoverage();
			for (int branchId = brCovBS.nextSetBit(0); branchId >= 0; branchId = brCovBS.nextSetBit(branchId+1)) {
				BranchTrack br = reachableBranches.get(branchId);
				if(br != null) br.setExecuted();
				else if(TestFul.DEBUG) TestFul.debug("[LocalSearchBranch] It seems that the executed branch " + branchId + " is not reachable");
			}

			// mark p-uses
			final CoveragePUse puCov = (CoveragePUse) t.getCoverage().get(CoveragePUse.KEY);
			if(puCov != null) {
				for (PUse pu : puCov.getCoverage()) {
					BranchTrack br = reachableBranches.get(pu.getBranchId());
					if(br != null) br.addDef(pu.getDef());
					else if(TestFul.DEBUG) TestFul.debug("[LocalSearchBranch] It seems that the executed branch " + pu.getBranchId() + " is not reachable (pUse).");
				}
			}

			// calculate targets
			Set<ConditionTargetDatum> targets = new HashSet<ConditionTargetDatum>();
			for (BranchTrack br : reachableBranches) {
				if(!br.isExecuted()) {
					if(!execBranches.get(br.getKey()))
						targets.add(new ConditionTargetDatum(br.getKey()));
				} else {
					for (ContextualId m : br.getMissingDefs())
						targets.add(new ConditionTargetDatum(br.getKey(), m));
				}
			}

			// calculate scores
			for (ConditionTargetDatum target : targets) {
				ret.add(calculateScore(t, target));
			}
		}

		if(logger.isLoggable(Level.FINEST)) {
			StringBuilder sb = new StringBuilder("Candidate tests:\n");

			for (TestWithScore t : ret) {
				sb.append("Target ").append(t.target).append(" score: ").append(t.score).append("\n");
				sb.append(t.test.toString()).append("\n");
			}

			logger.finest(sb.toString());
		}

		return ret;
	}

	private TestWithScore calculateScore(TestCoverage t, ConditionTargetDatum target) {
		final Condition c = problem.getWhiteAnalysis().getConditionFromBranch(target.getBranchId());
		float score = 0;

		if(attempts.containsKey(target))
			score += SCORE_MISS_ATTEMPTS * attempts.get(target);

		if(target.isPUse())
			score += SCORE_PUSE;

		switch(c.getType()) {
		case Boolean: score += SCORE_BOOL; break;
		case Array: score += SCORE_ARRAY; break;
		case Reference: score += SCORE_REF; break;
		case String:	score += SCORE_STRING; break;
		case Character: score += SCORE_CHAR; break;
		case Number: score += SCORE_NUMBER; break;
		}

		if(!(c.getV1() instanceof Data) && !(c.getV2() instanceof Data)) {
			// it is not working on any data
			score += SCORE_NO_DATA;

		} else {
			if(c.getUse1() == null && c.getUse2() == null) { // 0 uses
				score += SCORE_NO_USES;
			} else if(c.getUse1() != null ^ c.getUse2() != null) { // 1 use
				score += SCORE_ONE_USE;
			} else { // 2 uses
				score += SCORE_TWO_USES;
			}
		}

		Value v1 = c.getV1();
		if(v1 != null && v1 instanceof Data) {
			Data data = (Data) v1;
			if(data.isParam()) score += SCORE_PARAM;
			else if(data.isField()) score += SCORE_FIELD;
		}

		Value v2 = c.getV2();
		if(v2 != null && v2 instanceof Data) {
			Data data = (Data) v2;
			if(data.isParam()) score += SCORE_PARAM;
			else if(data.isField()) score += SCORE_FIELD;
		}

		CoverageDataFlow duCov = (CoverageDataFlow) t.getCoverage().get(CoverageDataFlow.KEY);
		if(duCov != null)
			score += checkFeasibility(t, target, duCov);

		return new TestWithScore(t, target, score);
	}

	/**
	 * Uses data flow analysis to determine whether a definition is able to reach a certain branch
	 * @param t the test (with du coverage)
	 * @param target the target to reach
	 * @param duCov the def-use coverage (must not be null)
	 * @return the score (the higher the better). Negative Infinite means not feasible.
	 */
	private float checkFeasibility(TestCoverage t, ConditionTargetDatum target, CoverageDataFlow duCov) {
		if(duCov == null) {
			if(TestFul.DEBUG) TestFul.debug("Null DU coverage.");
			return SCORE_AMBIGUOUS;
		}

		final Condition c = problem.getWhiteAnalysis().getConditionFromBranch(target.getBranchId());
		final DataType cType = c.getType();
		final DataUse use1 = c.getUse1();
		final DataUse use2 = c.getUse2();

		// can work only on numbers and boolean values
		if(cType != DataType.Boolean && cType != DataType.Character && cType != DataType.Number) return 0;

		if (target.isPUse()) {

			if(use1 == null && use2 == null) {
				if(TestFul.DEBUG) TestFul.debug("No uses in condition and p-use enabled");
				return SCORE_AMBIGUOUS;

			} else if(use1 != null && use2 == null) { // p-use is on use1
				DataDef def1 = null;
				if(target.getDefinitionId() != null) {
					def1 = problem.getWhiteAnalysis().getDataDef(target.getDefinitionId().getId());
					if(TestFul.DEBUG) {
						if(def1 == null) TestFul.debug("Cannot retrieve definition for the chosen p-use (1)");
						else if(def1.getData().getId() != use1.getData().getId()) TestFul.debug("Data mismatch (1)");
					}
				}

				if(c instanceof ConditionIf) {

					if(c.getV2() == null) return 0;

					if(!(c.getV2() instanceof Constant) && !(c.getV2() instanceof Data)) {
						if(TestFul.DEBUG) TestFul.debug("Ambiguous Value (1): " + c.getV2() + (c.getV2() == null ? "" : " (" + c.getV2().getClass().getName() + ")"));
						return SCORE_AMBIGUOUS;
					}

					return checkFeasibilityIf(
							getStaticValue(def1),
							((ConditionIf) c).getConditionType(),
							getDynamicValues(duCov, c.getV2(), use2) );

				} else if(c instanceof ConditionSwitch) {
					return checkFeasibilitySwitch(getStaticValue(def1), target.getBranchId(), (ConditionSwitch) c);

				} else {
					if(TestFul.DEBUG) TestFul.debug("Unexpected contition type: " + c.getClass().getName());
					return SCORE_AMBIGUOUS;
				}

			} else if(use1 == null && use2 != null) { // p-use is on use2
				if(!(c instanceof ConditionIf)) {
					if(TestFul.DEBUG) TestFul.debug("Unexpected contition type: " + c.getClass().getName());
					return SCORE_AMBIGUOUS;
				}

				if(c.getV1() == null) return 0;

				if(!(c.getV1() instanceof Constant) && !(c.getV1() instanceof Data)) {
					if(TestFul.DEBUG) TestFul.debug("Ambiguous Value (2): " + c.getV1() + (c.getV1() == null ? "" : " (" + c.getV1().getClass().getName() + ")"));
					return SCORE_AMBIGUOUS;
				}

				DataDef def2 = null;
				if(target.getDefinitionId() != null) {
					def2 = problem.getWhiteAnalysis().getDataDef(target.getDefinitionId().getId());
					if(TestFul.DEBUG) {
						if(def2 == null) TestFul.debug("Cannot retrieve definition for the chosen p-use (2)");
						else if(def2.getData().getId() != use2.getData().getId()) TestFul.debug("Data mismatch (2)");
					}
				}

				return checkFeasibilityIf(
						getDynamicValues(duCov, c.getV1(), use1),
						((ConditionIf) c).getConditionType(),
						getStaticValue(def2) );

			} else { // use1 != null && use2 != null
				if(!(c instanceof ConditionIf)) {
					if(TestFul.DEBUG) TestFul.debug("Unexpected contition type: " + c.getClass().getName());
					return SCORE_AMBIGUOUS;
				}

				// This can happen with "if(this.field == other.field)"
				if (use1.getId() == use2.getId())
					return 0;

				// TBD: better support of default initialization
				if (target.getDefinitionId() == null) {
					logger.fine("Skipping ambiguous default initialization " + target);
					return SCORE_AMBIGUOUS;
				}

				DataDef def = problem.getWhiteAnalysis().getDataDef(target.getDefinitionId().getId());
				if(def.getData().getId() == use1.getData().getId()) {
					// p-use is on use1

					DataDef def1 = problem.getWhiteAnalysis().getDataDef(target.getDefinitionId().getId());
					if(TestFul.DEBUG) {
						if(def1 == null) TestFul.debug("Cannot retrieve definition for the chosen p-use (3a)");
						else if(def1.getData().getId() != use1.getData().getId()) TestFul.debug("Data mismatch (3a)");
					}

					if(c.getV2() == null) return 0;

					if(!(c.getV2() instanceof Constant) && !(c.getV2() instanceof Data)) {
						if(TestFul.DEBUG) TestFul.debug("Ambiguous Value (3a): " + c.getV2() + (c.getV2() == null ? "" : " (" + c.getV2().getClass().getName() + ")"));
						return SCORE_AMBIGUOUS;
					}

					return checkFeasibilityIf(
							getStaticValue(def1),
							((ConditionIf) c).getConditionType(),
							getDynamicValues(duCov, c.getV2(), use2) );


				} else if(def.getData().getId() == use2.getData().getId()) {
					// p-use is on use2

					if(c.getV1() == null) return 0;

					if(!(c.getV1() instanceof Constant) && !(c.getV1() instanceof Data)) {
						if(TestFul.DEBUG) TestFul.debug("Ambiguous Value (3b): " + c.getV1() + (c.getV1() == null ? "" : " (" + c.getV1().getClass().getName() + ")"));
						return SCORE_AMBIGUOUS;
					}

					DataDef def2 = problem.getWhiteAnalysis().getDataDef(target.getDefinitionId().getId());
					if(TestFul.DEBUG) {
						if(def2 == null) TestFul.debug("Cannot retrieve definition for the chosen p-use (3b)");
						else if(def2.getData().getId() != use2.getData().getId()) TestFul.debug("Data mismatch (3b)");
					}

					return checkFeasibilityIf(
							getDynamicValues(duCov, c.getV1(), use1),
							((ConditionIf) c).getConditionType(),
							getStaticValue(def2) );

				} else {
					if(TestFul.DEBUG) TestFul.debug("Data mismatch. Expected " + use1.getData().getId() + " or " + use2.getData().getId() + "; found: " + def.getData().getId());
					return SCORE_AMBIGUOUS;
				}
			}
		}

		if(c.getV1() == null) return 0;

		if(!(c.getV1() instanceof Constant) && !(c.getV1() instanceof Data)) {
			if(TestFul.DEBUG) TestFul.debug("Ambiguous Value (4): " + c.getV1() + (c.getV1() == null ? "" : " (" + c.getV1().getClass().getName() + ")"));
			return SCORE_AMBIGUOUS;
		}

		if(c instanceof ConditionIf) {
			if(c.getV2() == null) return 0;

			if(!(c.getV2() instanceof Constant) && !(c.getV2() instanceof Data)) {
				if(TestFul.DEBUG) TestFul.debug("Ambiguous Value (4b): " + c.getV2() + (c.getV2() == null ? "" : " (" + c.getV2().getClass().getName() + ")"));
				return SCORE_AMBIGUOUS;
			}

			return checkFeasibilityIf(
					getDynamicValues(duCov, c.getV1(), use1),
					((ConditionIf) c).getConditionType(),
					getDynamicValues(duCov, c.getV2(), use2) );

		} else if(c instanceof ConditionSwitch) {
			return checkFeasibilitySwitch(getDynamicValues(duCov, c.getV1(), use1), target.getBranchId(), (ConditionSwitch) c);

		} else {
			if(TestFul.DEBUG) TestFul.debug("Unexpected contition type: " + c.getClass().getName());
			return SCORE_AMBIGUOUS;
		}
	}

	/**
	 * Check if a SWITCH condition is feasible or not
	 * @param values
	 * @param branchId
	 * @param c
	 * @return 0 if the branch is feasible; SCORE_IMPOSSIBLE if the branch is not feasible.
	 */
	private float checkFeasibilitySwitch(Set<Constant> values, int branchId, ConditionSwitch c) {
		if(values == null)
			return 0;

		if(values.isEmpty()){
			logger.finer("Empty values: switch " + branchId + " is impossible");
			return SCORE_IMPOSSIBLE;
		}

		Integer keyValue = c.getKeyValueByBrachId(branchId);

		if(keyValue != null) {
			for (Constant value : values) {
				if(value.getValue().intValue() == keyValue)
					return 0;
			}

			if(LOG_FINER) logger.finer("Detected impossible branch: " + branchId + " with values " + values);
			return SCORE_IMPOSSIBLE;
		}

		Set<Integer> cases = c.getCaseValues();
		for (Constant value : values) {
			if(!cases.contains(value.getValue().intValue()))
				return 0;
		}

		if(LOG_FINER) logger.finer("Detected impossible branch: " + branchId + " with values " + values);
		return SCORE_IMPOSSIBLE;
	}

	/**
	 * Check if an IF condition is feasible or not
	 * @param a the first set of values (null if there is any free variable)
	 * @param t the type of the comparison
	 * @param b the second set of values (null if there is any free variable)
	 * @return 0 if the branch is feasible; SCORE_IMPOSSIBLE if the branch is not feasible.
	 */
	private float checkFeasibilityIf(Set<Constant> a, ConditionType t, Set<Constant> b) {
		if(a == null || b == null) // Free variables means feasible condition
			return 0;

		if(a.isEmpty() ||  b.isEmpty()){
			logger.finer("Empty values: if is impossible");
			return SCORE_IMPOSSIBLE;
		}

		for (Constant v1 : a) {
			for (Constant v2 : b) {
				switch(t) {
				case LT: if(v1.getValue().doubleValue() <  v2.getValue().doubleValue()) return 0;
				case LE: if(v1.getValue().doubleValue() <= v2.getValue().doubleValue()) return 0;
				case NE: if(v1.getValue().doubleValue() != v2.getValue().doubleValue()) return 0;
				case EQ: if(v1.getValue().doubleValue() == v2.getValue().doubleValue()) return 0;
				case GE: if(v1.getValue().doubleValue() >= v2.getValue().doubleValue()) return 0;
				case GT: if(v1.getValue().doubleValue()  > v2.getValue().doubleValue()) return 0;
				}
			}
		}
		if(LOG_FINER) logger.finer("Detected impossible IF: " + a + " " + t + " " + b);
		return SCORE_IMPOSSIBLE;
	}

	private Set<Constant> getStaticValue(DataDef def) {
		if(def == null) return Collections.singleton(new Constant(0));

		if(def.getValue() == null) return null;
		if(def.getValue() instanceof Data) return null;
		if(def.getValue() instanceof Constant)
			return Collections.singleton((Constant) def.getValue());

		if(TestFul.DEBUG)
			TestFul.debug("Unexpected value: " + def.getValue().getClass().getName());

		return null;
	}

	private Set<Constant> getDynamicValues(CoverageDataFlow duCov, Value v, DataUse use) {
		if(TestFul.DEBUG && (v == null || !(v instanceof Constant) && !(v instanceof Data)))
			TestFul.debug("Invalid parameters for getDynamicValues");

		if (v instanceof Constant) {
			Set<Constant> values = new HashSet<Constant>();
			values.add((Constant) v);
			return values;
		}

		if(use == null)
			return null; // the value is a data, with only 1 du feasible. Hence (constant propagation) it's a free variable!

		Set<Constant> values = new HashSet<Constant>();
		for (Integer defId : duCov.getDefsOfUse(use.getId())) {
			if(defId == null) {
				values.add(new Constant(0));

			} else {
				DataDef def = problem.getWhiteAnalysis().getDataDef(defId);
				if(TestFul.DEBUG && def == null) TestFul.debug("Null DataDef for def " + defId);
				if(def != null) {
					if(def.getValue() == null || def.getValue() instanceof Data)
						return null; // there is a free variable: the condition is feasible!

					if(def.getValue() instanceof Constant)
						values.add((Constant) def.getValue());
				}
			}
		}

		return values;
	}

	private static class BranchTrack implements ElementWithKey<Integer> {
		private final int branchId;

		private boolean executed;
		private final Set<ContextualId> branchDefs;
		private final Set<ContextualId> conditionDefs;

		public BranchTrack(int branchId, Set<ContextualId> conditionDefs) {
			this.branchId = branchId;
			executed = false;
			branchDefs = new HashSet<ContextualId>();
			this.conditionDefs = conditionDefs;
		}

		public boolean isExecuted() {
			return executed;
		}

		public void setExecuted() {
			executed = true;
		}

		public void addDef(ContextualId def) {
			branchDefs.add(def);
			conditionDefs.add(def);
		}

		public Set<ContextualId> getMissingDefs() {
			Set<ContextualId> missing = new HashSet<ContextualId>();

			for (ContextualId def : conditionDefs)
				if(!branchDefs.contains(def))
					missing.add(def);

			return missing;
		}

		@Override
		public Integer getKey() {
			return branchId;
		}

		@Override
		public BranchTrack clone() {
			return this;
		}
	}

	private static class TestWithScore implements Comparable<TestWithScore> {
		final TestCoverage test;
		final ConditionTargetDatum target;
		final float score;

		private final int hashCode;

		public TestWithScore(TestCoverage test, ConditionTargetDatum target, float score) {
			if(TestFul.DEBUG) {
				if(test == null) TestFul.debug("[TestWithScore] Test cannot be null");
				if(target == null) TestFul.debug("[TestWithScore] Target cannot be null");
			}

			this.test = test;
			this.target = target;
			this.score = score;

			hashCode = 31 * ((31 * test.hashCode()) + target.hashCode()) + Float.floatToIntBits(score);
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return hashCode;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;

			if (!(obj instanceof TestWithScore)) return false;
			TestWithScore other = (TestWithScore) obj;

			if (!test.equals(other.test)) return false;
			if (!target.equals(other.target)) return false;
			if (Float.floatToIntBits(score) != Float .floatToIntBits(other.score)) return false;

			return true;
		}

		@Override
		public String toString() {
			return target + ": " + score;
		}

		/* (non-Javadoc)
		 * @see java.lang.Comparable#compareTo(java.lang.Object)
		 */
		@Override
		public int compareTo(TestWithScore o) {
			return hashCode - o.hashCode;
		}
	}
}
