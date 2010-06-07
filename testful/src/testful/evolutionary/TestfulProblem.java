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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import jmetal.util.PseudoRandom;
import testful.IConfigGeneration;
import testful.coverage.CoverageExecutionManager;
import testful.coverage.CoverageInformation;
import testful.coverage.TrackerDatum;
import testful.coverage.whiteBox.WhiteBoxAnalysisData;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestCoverage;
import testful.model.TestSuite;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.Context;
import testful.runner.RunnerPool;
import testful.runner.TestfulClassLoader;
import testful.utils.ElementManager;

/**
 * Describe the problem being addressed.
 * @author matteo
 */
public class TestfulProblem implements Serializable {

	private static final long serialVersionUID = 519447614813889830L;

	private static final Logger logger = Logger.getLogger("testful.evolutionary");

	private final ClassFinderCaching finder;
	private final TestCluster cluster;
	private final ReferenceFactory refFactory;
	private final WhiteBoxAnalysisData whiteAnalysis;
	private final TrackerDatum[] data;
	private final boolean reload;

	/** cumulative number of invocations */
	private AtomicLong invTot = new AtomicLong(0);

	public TestfulProblem(IConfigGeneration config) throws ClassNotFoundException {
		try {
			reload = config.isReload();

			final ClassFinderImpl finderImpl = new ClassFinderImpl(config);
			whiteAnalysis = new WhiteBoxAnalysisData();
			finderImpl.addClassData(whiteAnalysis);

			finder = new ClassFinderCaching(finderImpl);
			TestfulClassLoader tcl = new TestfulClassLoader(finder);

			cluster = new TestCluster(tcl, config);
			cluster.clearCache();

			data = new TrackerDatum[0];//TODO: Utils.readData(whiteAnalysis);

			refFactory = new ReferenceFactory(cluster, config.getNumVarCut(), config.getNumVar());
		} catch (RemoteException e) {
			// never happens
			logger.log(Level.WARNING, "Remote exception (should never happen): " + e.toString(), e);
			throw new ClassNotFoundException("Cannot contact the remote class loading facility", e);
		}
	}

	public TestCluster getCluster() {
		return cluster;
	}

	public ReferenceFactory getReferenceFactory() {
		return refFactory;
	}

	public ClassFinderCaching getFinder() {
		return finder;
	}

	public TrackerDatum[] getData() {
		return data;
	}

	public Future<ElementManager<String, CoverageInformation>> evaluate(Test test) {
		return evaluate(test, data);
	}

	public Future<ElementManager<String, CoverageInformation>> evaluate(Test test, TrackerDatum[] data) {
		if(data == null) data = this.data;

		invTot.addAndGet(test.getTest().length);

		Context<ElementManager<String, CoverageInformation>, CoverageExecutionManager> ctx = CoverageExecutionManager.getContext(finder, test, data);
		ctx.setRecycleClassLoader(reload);
		return RunnerPool.getRunnerPool().execute(ctx);
	}

	public long getNumberOfExecutedOperations() {
		return invTot.get();
	}

	public Test getTest1(Operation[] ops) {
		return new Test(cluster, refFactory, ops);
	}

	public Test getTest(List<Operation> ops) {
		return new Test(cluster, refFactory, ops.toArray(new Operation[ops.size()]));
	}

	public WhiteBoxAnalysisData getWhiteAnalysis() {
		return whiteAnalysis;
	}

	private final TestSuite reserve = new TestSuite();

	/**
	 * Add tests to the reserve
	 * @param tests the tests to add
	 */
	public void addReserve(TestSuite tests){
		if(tests == null) return;

		reserve.add(tests);
	}

	/**
	 * Add a test to the reserve
	 * @param test the test to add
	 */
	public void addReserve(TestCoverage test){
		if(test == null) return;

		reserve.add(test);
	}

	public List<Operation> generateTest() {
		Operation[] ops = reserve.getBestTest();
		if (ops!=null) {
			ops = Operation.adapt(ops, cluster, refFactory);
			List<Operation> ret = new ArrayList<Operation>(ops.length);
			for (Operation o : ops) ret.add(o);
			return ret;
		}

		List<Operation> ret = new ArrayList<Operation>(10);

		for (int i = 0; i < 10; i++)
			ret.add(Operation.randomlyGenerate(cluster, refFactory, PseudoRandom.getMersenneTwisterFast()));

		return ret;
	}
}
