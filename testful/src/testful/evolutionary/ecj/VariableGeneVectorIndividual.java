package testful.evolutionary.ecj;

import java.util.ArrayList;
import java.util.List;

import testful.model.Operation;
import testful.model.ReferenceFactory;
import testful.model.Test;
import testful.model.TestCluster;
import testful.model.TestSplitter;
import testful.model.TestfulProblem;
import ec.EvolutionState;
import ec.util.MersenneTwisterFast;
import ec.vector.GeneVectorIndividual;
import ec.vector.GeneVectorSpecies;
import ec.vector.VectorGene;
import ec.vector.VectorIndividual;
import ec.vector.VectorSpecies;

public class VariableGeneVectorIndividual extends GeneVectorIndividual {

	private static final long serialVersionUID = -6211010818051083441L;
	public static float PROB_MODEL_CX = 0;
	public static int MAX_SIZE = 10000;
	
	@Override
	public void defaultCrossover(EvolutionState state, int thread, VectorIndividual vectorInd) {
		GeneVectorSpecies s = (GeneVectorSpecies) species;
		GeneVectorIndividual ind = (GeneVectorIndividual) vectorInd;
		MersenneTwisterFast random = state.random[thread];

		if(PROB_MODEL_CX > 0.0f && genome.length > 0 && ind.genome.length > 0 && random.nextBoolean(PROB_MODEL_CX)) modelCrossover(state, s, ind, random);
		else blindCrossover(state, s, ind, random);
	}

	private void modelCrossover(EvolutionState state, GeneVectorSpecies s, GeneVectorIndividual ind, MersenneTwisterFast random) {
		int thisSize = genome.length;
		int otherSize = ind.genome.length;

		TestfulProblem problem = ((EcjInitializer) state.initializer).getProblem();
		TestCluster cluster = problem.getCluster();
		ReferenceFactory refFactory = problem.getRefFactory();

		List<Operation[]> thisParts = split(genome, cluster, refFactory);
		List<Operation[]> otherParts = split(ind.genome, cluster, refFactory);

		int thisPoint = thisParts.size() > 0 ? random.nextInt(thisParts.size()) : 0;
		int otherPoint = otherParts.size() > 0 ? random.nextInt(otherParts.size()) : 0;

		List<Operation> newThis = new ArrayList<Operation>();
		List<Operation> newOther = new ArrayList<Operation>();

		for(int i = 0; i < thisPoint; i++)
			for(Operation op : thisParts.get(i))
				newThis.add(op);

		for(int i = 0; i < otherPoint; i++)
			for(Operation op : otherParts.get(i))
				newOther.add(op);

		for(int i = thisPoint; i < thisParts.size(); i++)
			for(Operation op : thisParts.get(i))
				newOther.add(op);

		for(int i = otherPoint; i < otherParts.size(); i++)
			for(Operation op : otherParts.get(i))
				newThis.add(op);

		genome = convert(newThis);
		ind.genome = convert(newOther);

		System.err.println("MODEL CX: from " + thisSize + ", " + otherSize + " to " + genome.length + ", " + ind.genome.length);
	}

	private VectorGene[] convert(List<Operation> ops) {
		VectorGene[] ret = new VectorGene[ops.size()];

		int i = 0;
		for(Operation op : ops)
			ret[i++] = new VGOperation(op);

		return ret;
	}

	private List<Operation[]> split(VectorGene[] vg, TestCluster cluster, ReferenceFactory refFactory) {
		Operation[] ops = new Operation[vg.length];

		int i = 0;
		for(VectorGene gene : vg)
			ops[i++] = ((VGOperation) gene).getOp();

		List<Test> parts = TestSplitter.split(true, new Test(cluster, refFactory, ops));

		List<Operation[]> ret = new ArrayList<Operation[]>();

		for(Test test : parts)
			ret.add(test.getTest());

		return ret;
	}

	private void blindCrossover(EvolutionState state, GeneVectorSpecies s, GeneVectorIndividual ind, MersenneTwisterFast random) {
		VectorGene[][] pieces1, pieces2;

		switch(s.crossoverType) {
			case VectorSpecies.C_ONE_POINT:
				pieces1 = new VectorGene[2][];
				pieces2 = new VectorGene[2][];

				int p = random.nextInt((genome.length / s.chunksize) + 1);
				this.split(new int[] { p }, pieces1);

				p = random.nextInt((ind.genome.length / s.chunksize) + 1);
				ind.split(new int[] { p }, pieces2);

				join(new VectorGene[][] { pieces1[0], pieces2[1] });
				if(MAX_SIZE > 0 && genome.length > MAX_SIZE) {
					this.split(new int[] { MAX_SIZE }, pieces1);
					genome = pieces1[0];
				}

				ind.join(new VectorGene[][] { pieces2[0], pieces1[1] });
				if(MAX_SIZE > 0 && ind.genome.length > MAX_SIZE) {
					ind.split(new int[] { MAX_SIZE }, pieces2);
					ind.genome = pieces2[0];
				}

				break;

			case VectorSpecies.C_TWO_POINT:
				pieces1 = new VectorGene[3][];
				pieces2 = new VectorGene[3][];

				int p1,
				p2;

				p1 = random.nextInt((genome.length / s.chunksize) + 1);
				p2 = random.nextInt((genome.length / s.chunksize) + 1);
				if(p1 > p2) {
					int tmp = p1;
					p1 = p2;
					p2 = tmp;
				}
				this.split(new int[] { p1, p2 }, pieces1);

				p1 = random.nextInt((ind.genome.length / s.chunksize) + 1);
				p2 = random.nextInt((ind.genome.length / s.chunksize) + 1);
				if(p1 > p2) {
					int tmp = p1;
					p1 = p2;
					p2 = tmp;
				}
				ind.split(new int[] { p1, p2 }, pieces2);

				join(new VectorGene[][] { pieces1[0], pieces2[1], pieces1[2] });
				if(MAX_SIZE > 0 && genome.length > MAX_SIZE) {
					VectorGene[][] pieces = new VectorGene[2][];
					this.split(new int[] { MAX_SIZE }, pieces);
					genome = pieces[0];
				}

				ind.join(new VectorGene[][] { pieces2[0], pieces1[1], pieces2[2] });
				if(MAX_SIZE > 0 && ind.genome.length > MAX_SIZE) {
					VectorGene[][] pieces = new VectorGene[2][];
					ind.split(new int[] { MAX_SIZE }, pieces);
					ind.genome = pieces[0];
				}

				break;

			default:
				state.output.fatal("The selected type of crossover is not available for Variable-length individuals");
		}
	}

	/**
	 * Destructively mutates the individual in some default manner. The default
	 * form simply randomizes genes to a uniform distribution from the min and max
	 * of the gene values.
	 */
	@Override
	public void defaultMutate(EvolutionState state, int thread) {
		GeneVectorSpecies s = (GeneVectorSpecies) species;
		if(s.mutationProbability > 0.0) for(int x = 0; x < genome.length; x++)
			if(state.random[thread].nextBoolean(s.mutationProbability)) {
				VectorGene vectorGene = genome[x];
				vectorGene.mutate(state, thread);
			}
	}

	/** Initializes the individual by calling reset(...) on each gene. */
	@Override
	public void reset(EvolutionState state, int thread) {
		GeneVectorSpecies s = (GeneVectorSpecies) species;

		for(int x = 0; x < genome.length; x++) {
			// first create the gene if it doesn't exist
			if(genome[x] == null) genome[x] = (VectorGene) (s.genePrototype.clone());

			VectorGene vectorGene = genome[x];

			// now reset it
			vectorGene.reset(state, thread);
		}
	}

}
