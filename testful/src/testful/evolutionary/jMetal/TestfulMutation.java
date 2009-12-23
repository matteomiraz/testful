package testful.evolutionary.jMetal;

import java.util.List;

import jmetal.base.Solution;
import jmetal.base.operator.mutation.Mutation;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;
import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestSplitter;
import ec.util.MersenneTwisterFast;

public class TestfulMutation extends Mutation<Operation> {


	public TestfulMutation() {}

	private float probRemove = 0.5f;
	public void setProbRemove(float probRemove) {
		this.probRemove = probRemove;
	}

	private float probSimplify = 0.5f;
	public void setProbSimplify(float probSimplify) {
		this.probSimplify = probSimplify;
	}
	
	/**
	 * Executes the operation
	 * 
	 * @param solution An object containing a solution to mutate
	 */
	@Override
	public void execute(Solution<Operation> solution) throws JMException {
		List<Operation> repr = solution.getDecisionVariables().variables_;

		MersenneTwisterFast random = PseudoRandom.getMersenneTwisterFast();
		TestCluster cluster = JMProblem.currentProblem.getProblem().getCluster();
		ReferenceFactory refFactory = JMProblem.currentProblem.getProblem().getRefFactory();

		if(repr.isEmpty()) {
			//			if(random.nextBoolean(probability))
			repr.add(Operation.randomlyGenerate(cluster, refFactory, random));

			return;
		}

		if(random.nextBoolean(probSimplify)) {
			// remove all useless operations
			Test t = TestSplitter.splitAndMerge(new Test(cluster, refFactory, repr.toArray(new Operation[repr.size()])));

			repr.clear();
			for(Operation operation : t.getTest())
				repr.add(operation);

		} else {
			for(int i = 0; i < repr.size(); i++)
				if(random.nextBoolean(probability)) {

					if(random.nextBoolean(probRemove)) { 
						repr.remove(random.nextInt(repr.size()));
						i--;
					} else {
						repr.add(random.nextInt(repr.size()), Operation.randomlyGenerate(cluster, refFactory, random));
						i++;
					}
				}
		}
	}
}
