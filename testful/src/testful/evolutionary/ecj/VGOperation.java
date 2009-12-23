package testful.evolutionary.ecj;

import testful.model.Operation;
import testful.model.TestfulProblem;
import ec.EvolutionState;
import ec.vector.VectorGene;

public class VGOperation extends VectorGene {

	private static final long serialVersionUID = -3556001366450336552L;

	private Operation op;

	public VGOperation() {}

	public VGOperation(Operation op) {
		this.op = op;
	}

	public Operation getOp() {
		return op;
	}

	@Override
	public void reset(EvolutionState state, int thread) {
		EcjInitializer testfulInitializer = (EcjInitializer) state.initializer;
		TestfulProblem problem = testfulInitializer.getProblem();
		op = Operation.randomlyGenerate(problem.getCluster(), problem.getRefFactory(), state.random[thread]);
	}

	@Override
	public int hashCode() {
		return((op == null) ? 0 : op.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(!(obj instanceof VGOperation)) return false;
		VGOperation other = (VGOperation) obj;
		if(op == null) {
			if(other.op != null) return false;
		} else if(!op.equals(other.op)) return false;
		return true;
	}

	@Override
	public String toString() {
		return op.toString() + "\n";
	}

}
