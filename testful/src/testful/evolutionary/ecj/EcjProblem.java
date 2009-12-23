package testful.evolutionary.ecj;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import testful.TestfulException;
import testful.coverage.CoverageInformation;
import testful.model.Operation;
import testful.model.TestfulProblem;
import testful.runner.IRunner;
import testful.utils.ElementManager;
import ec.EvolutionState;
import ec.Individual;
import ec.multiobjective.MultiObjectiveFitness;
import ec.simple.SimpleProblemForm;
import ec.util.Parameter;
import ec.vector.GeneVectorIndividual;
import ec.vector.VectorGene;

public class EcjProblem extends FutureProblem implements SimpleProblemForm {

	private static final long serialVersionUID = -7667754622312022033L;

	// multi-objective fitness keys
	public static final String FITNESS_LEN = "len";
	public static final String FITNESS_BUG = "bug";
	public static final String FITNESS_BBD = "bbd";
	public static final String FITNESS_BBN = "bbn";
	public static final String FITNESS_BRD = "brd";
	public static final String FITNESS_BRN = "brn";

	// configuration properties
	public static final String PROP_FINESS = "fitness";
	public static final String PROP_CUT = "cut";
	public static final String PROP_CUT_SIZE = "repository-cut-size";
	public static final String PROP_AUX_SIZE = "repository-aux-size";
	public static final String PROP_MAXSIZE = "max-size";
	public static final String PROP_RELOAD_CLASSES = "reload";

	public static final String PROP_ENABLE_CACHE = "enable-cache";
	
	private int lastGen = 0;

	/**
	 * stores the combinedWriter coverage obtained with tests on bugs. Key:
	 * coverage criteria; value: combinedWriter coverage criteria
	 */
	//private static final Map<String, CoverageInformation> bugCoverage = new FastMap<String, CoverageInformation>();

	private TestfulProblem problem;
	
	@Override
	public void setup(final EvolutionState state, final Parameter base) {
		super.setup(state, base);

		if(!(state.initializer instanceof EcjInitializer)) state.output.fatal("The initializer should be a TestfulInitializer!!!");

		Parameter def = defaultBase();

		String cut = state.parameters.getString(base.push(PROP_CUT), def.push(PROP_CUT));

		boolean reloadClasses = state.parameters.getBoolean(base.push(PROP_RELOAD_CLASSES), def.push(PROP_RELOAD_CLASSES), false);

		boolean fitness_len = false, fitness_bug = false, fitness_bbd = false, fitness_bbn = false, fitness_brd = false, fitness_brn = false;
		String obj = state.parameters.getString(base.push(PROP_FINESS), def.push(PROP_FINESS));
		if(obj == null || obj.length() <= 0) state.output.fatal("You must specify at least one objective", base.push(PROP_FINESS), def.push(PROP_FINESS));
		for(String o : obj.split(":"))
			if(FITNESS_LEN.equals(o)) fitness_len = true;
			else if(FITNESS_BUG.equals(o)) fitness_bug = true;
			else if(FITNESS_BBD.equals(o)) fitness_bbd = true;
			else if(FITNESS_BBN.equals(o)) fitness_bbn = true;
			else if(FITNESS_BRD.equals(o)) fitness_brd = true;
			else if(FITNESS_BRN.equals(o)) fitness_brn = true;
			else state.output.error("Unrecognised objective: " + o, base.push(PROP_FINESS), def.push(PROP_FINESS));

		int maxIndSize = state.parameters.getInt(base.push(PROP_MAXSIZE), def.push(PROP_MAXSIZE), 1);
		int repoCutSize = state.parameters.getInt(base.push(PROP_CUT_SIZE), def.push(PROP_CUT_SIZE), 1);
		if(repoCutSize < 1) state.output.warning("You may override the number of CUT object storable in the repository by specifing this property:", base.push(PROP_CUT_SIZE), def.push(PROP_CUT_SIZE));
		else repoCutSize = 4;

		int repoSize = state.parameters.getInt(base.push(PROP_AUX_SIZE), def.push(PROP_AUX_SIZE), 0);
		if(repoSize < 0) state.output.warning("You may override the size of the repository by specifing this property:", base.push(PROP_AUX_SIZE), def.push(PROP_AUX_SIZE));
		else repoSize = 4;

		boolean enableCache = state.parameters.getBoolean(base.push(PROP_ENABLE_CACHE), def.push(PROP_ENABLE_CACHE), false);
		
		IRunner executor = Launcher.getSingleton().getExecutor();
		try {
			TestfulProblem.TestfulConfig config = new TestfulProblem.TestfulConfig();

			VariableGeneVectorIndividual.MAX_SIZE = maxIndSize;
			
			config.setCut(cut);
			config.cluster.setRepoSize(repoSize);
			config.cluster.setRepoCutSize(repoCutSize);
			
			config.fitness.toMinimize = false;
			config.fitness.len = fitness_len;
			config.fitness.bug = fitness_bug;
			config.fitness.bbd = fitness_bbd;
			config.fitness.bbn = fitness_bbn;
			config.fitness.brd = fitness_brd;
			config.fitness.brn = fitness_brn;
			
			problem = new TestfulProblem(executor, enableCache, reloadClasses, config);

			((EcjInitializer) state.initializer).setProblem(problem);

			state.output.message("Testing " + cut);
		} catch(TestfulException e) {
			state.output.error(e.getMessage());
		}
	}

	public void describe(Individual ind, EvolutionState state, int threadnum, int log, int verbosity) {}

	@Override
	public void evaluate(EvolutionState state, Individual _ind, int subpop, int threadnum) {
		if(_ind.evaluated) return;

		// process new generation
		if(lastGen < state.generation) {
			lastGen = state.generation;
			problem.doneGeneration(lastGen);
		}

		if(!(_ind instanceof GeneVectorIndividual)) state.output.fatal("ERRORE!!!");
		GeneVectorIndividual ind = (GeneVectorIndividual) _ind;

		if(!(_ind.fitness instanceof MultiObjectiveFitness)) state.output.fatal("ERRORE2!!!");
		MultiObjectiveFitness fit = (MultiObjectiveFitness) _ind.fitness;

		List<Operation> ops = new ArrayList<Operation>(ind.genome.length);
		for(VectorGene element : ind.genome)
			ops.add(((VGOperation) element).getOp());

		try {
			Future<ElementManager<String, CoverageInformation>> infos = problem.evaluate(ops);
			float[] fitness = problem.evaluate(lastGen, ops, infos.get());
			for(int i = 0; i < fitness.length; i++)
				fit.multifitness[i] = fitness[i];

			ind.evaluated = true;
		} catch(TestfulException e) {
			state.output.warning(e.getMessage());
		} catch(InterruptedException e) {
			state.output.warning(e.getMessage());
		} catch(ExecutionException e) {
			state.output.warning(e.getMessage());
		}
	}

	@Override
	public Future<Individual> evaluateFuture(final EvolutionState state, final Individual _ind, int subpop) {
		if(_ind.evaluated) return new Future<Individual>() {

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public Individual get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				return _ind;
			}

			@Override
			public Individual get() throws InterruptedException, ExecutionException {
				return _ind;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}
		};

		// process new generation
		if(lastGen < state.generation) {
			lastGen = state.generation;
			problem.doneGeneration(lastGen);
		}

		if(!(_ind instanceof GeneVectorIndividual)) state.output.fatal("ERRORE!!!");
		final GeneVectorIndividual ind = (GeneVectorIndividual) _ind;

		if(!(_ind.fitness instanceof MultiObjectiveFitness)) state.output.fatal("ERRORE2!!!");
		final MultiObjectiveFitness fit = (MultiObjectiveFitness) _ind.fitness;

		return new Future<Individual>() {

			private final GeneVectorIndividual individual;
			private final List<Operation> ops;
			private Future<ElementManager<String, CoverageInformation>> infos;

			{
				individual = ind;
				ops = new ArrayList<Operation>(ind.genome.length);

				for(VectorGene element : ind.genome)
					ops.add(((VGOperation) element).getOp());

				try {
					infos = problem.evaluate(ops);
				} catch(TestfulException e) {
					state.output.warning(e.getMessage());
				}
			}

			@Override
			public boolean isDone() {
				return infos.isDone();
			}

			@Override
			public boolean isCancelled() {
				return infos.isCancelled();
			}

			@Override
			public Individual get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
				float[] fitness = problem.evaluate(lastGen, ops, infos.get(timeout, unit));
				for(int i = 0; i < fitness.length; i++)
					fit.multifitness[i] = fitness[i];

				ind.evaluated = true;
				return individual;
			}

			@Override
			public Individual get() throws InterruptedException, ExecutionException {
				float[] fitness = problem.evaluate(lastGen, ops, infos.get());
				for(int i = 0; i < fitness.length; i++)
					fit.multifitness[i] = fitness[i];

				ind.evaluated = true;
				return individual;
			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return infos.cancel(mayInterruptIfRunning);
			}
		};
	}

	@Override
	public void describe(Individual ind, EvolutionState state, int subpopulation, int threadnum, int log, int verbosity) {

	}
}
