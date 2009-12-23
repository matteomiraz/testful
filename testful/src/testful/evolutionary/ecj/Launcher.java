package testful.evolutionary.ecj;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import testful.runner.IRunner;
import testful.runner.RunnerPool;
import testful.utils.TestfulLogger;

public class Launcher {

	private static Launcher singleton;

	public static Launcher getSingleton() {
		if(singleton == null) singleton = new Launcher();

		return singleton;
	}

	private IRunner executor = null;

	private Launcher() {}

	public IRunner getExecutor() {
		if(executor == null) 
			synchronized(this) {
				if(executor == null) { 
					executor = RunnerPool.createExecutor("testful-SPEA2", noLocal);
					executor.addRemoteWorker(remote);
				}
			}

		return executor;
	}

	@Option(required = true, name = "-cut", usage = "The class to test", metaVar = "full.qualified.ClassName")
	private String cut;

	@Option(required = false, name = "-cutSize", usage = "Number of places in the repository for the CUT")
	private int cutSize = 4;

	@Option(required = false, name = "-auxSize", usage = "Number of places in the repository for auxiliary classes")
	private int auxSize = 4;

	@Option(required = false, name = "-testSize", usage = "Maximum test length (n° of invocations)")
	private int maxSize = 10000;

	@Option(required = false, name = "-popSize", usage = "Size of the population)")
	private int popSize = 512;

	@Option(required = false, name = "-reload", usage = "Reload classes before each run (reinitialize static fields)")
	private boolean reload = false;

	@Option(required = false, name = "-disableLength", usage = "Removes the length of test from the multi-objective fitness")
	private boolean disableLength = false;

	@Option(required = false, name = "-enableBug", usage = "Inserts the number of bug found in the multi-objective fitness")
	private boolean enableBug = false;

	@Option(required = false, name = "-disableBranch", usage = "Removes the branch coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
	private boolean disableBranch = false;

	@Option(required = false, name = "-disableBranchCode", usage = "Removes the branch coverage on the code from the multi-objective fitness")
	private boolean disableBranchCode = false;

	@Option(required = false, name = "-disableBranchContract", usage = "Removes the branch coverage on contracts from the multi-objective fitness")
	private boolean disableBranchContract = false;

	@Option(required = false, name = "-disableBasicBlock", usage = "Removes the basic block coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
	private boolean disableBasicBlock = false;

	@Option(required = false, name = "-disableBasicBlockCode", usage = "Removes the basic block coverage on the code from the multi-objective fitness")
	private boolean disableBasicBlockCode = false;

	@Option(required = false, name = "-disableBasicBlockContract", usage = "Removes the basic block coverage on contracts from the multi-objective fitness")
	private boolean disableBasicBlockContract = false;


	@Option(required = false, name = "-remote", usage = "Use the specified remote evaluator")
	private String remote;

	@Option(required = false, name = "-noLocal", usage = "Do not use local evaluators")
	private boolean noLocal;

	@Option(required = false, name = "-enableCache", usage = "Enable evaluation cache. Notice that it can degrade performances")
	private boolean enableCache;

	private static void getDefaultParams(Set<String> params) {
		params.add("verbosity=0");
		params.add("flush=true");
		params.add("store=true");
		params.add("breedthreads=1");
		params.add("evalthreads=1");
		params.add("seed.0=time");
		params.add("state=ec.simple.SimpleEvolutionState");
		params.add("pop=ec.Population");
		params.add("init=" + EcjInitializer.class.getCanonicalName());
		params.add("finish=ec.simple.SimpleFinisher");
		params.add("stat =ec.simple.SimpleStatistics");
		params.add("exch=ec.simple.SimpleExchanger");
		params.add("eval=" + ParallelSPEA2Evaluator.class.getCanonicalName());
		params.add("breed=ec.multiobjective.spea2.SPEA2Breeder");
		params.add("generations=1000000");
		params.add("quit-on-run-complete=true");
		params.add("checkpoint=false");
		params.add("prefix=ec");
		params.add("checkpoint-modulo=1");
		params.add("pop.subpops=1");
		params.add("pop.subpop.0=ec.multiobjective.spea2.SPEA2Subpopulation");
		params.add("pop.subpop.0.archive-size=128");
		params.add("pop.subpop.0.duplicate-retries=0");
		params.add("pop.subpop.0.species=ec.vector.GeneVectorSpecies");
		params.add("pop.subpop.0.species.ind=" + VariableGeneVectorIndividual.class.getCanonicalName());
		params.add("pop.subpop.0.species.gene=" + VGOperation.class.getCanonicalName());
		params.add("pop.subpop.0.species.genome-size=50");
		params.add("pop.subpop.0.species.crossover-type=two");
		params.add("pop.subpop.0.species.crossover-prop=0.75");
		params.add("pop.subpop.0.species.mutation-prob=0.01");
		params.add("pop.subpop.0.species.fitness=ec.multiobjective.spea2.SPEA2MultiObjectiveFitness");
		params.add("pop.subpop.0.species.pipe=ec.vector.breed.VectorMutationPipeline");
		params.add("pop.subpop.0.species.pipe.source.0=ec.vector.breed.VectorCrossoverPipeline");
		params.add("pop.subpop.0.species.pipe.source.0.source.0=ec.multiobjective.spea2.SPEA2TournamentSelection");
		params.add("pop.subpop.0.species.pipe.source.0.source.1=ec.multiobjective.spea2.SPEA2TournamentSelection");
		params.add("select.tournament.size=4");
		params.add("eval.problem=" + EcjProblem.class.getCanonicalName());
	}

	public static void main(String[] args) throws IOException {
		testful.TestFul.printHeader("Testful");

		Launcher opt = Launcher.getSingleton();
		opt.parseArgs(args);

		String baseDir = TestfulLogger.singleton.getBaseDir();

		Set<String> params = new HashSet<String>();
		getDefaultParams(params);
		params.add("stat.file=" + baseDir + File.separator + "stats.txt");
		params.add("pop.subpop.0.size=" + opt.popSize);

		{
			Collection<String> objs = new ArrayList<String>();
			if(!opt.disableLength) objs.add(EcjProblem.FITNESS_LEN);
			if(opt.enableBug) objs.add(EcjProblem.FITNESS_BUG);
			if(!opt.disableBasicBlock) {
				if(!opt.disableBasicBlockCode) objs.add(EcjProblem.FITNESS_BBD);
				if(!opt.disableBasicBlockContract) objs.add(EcjProblem.FITNESS_BBN);
			}
			if(!opt.disableBranch) {
				if(!opt.disableBranchCode) objs.add(EcjProblem.FITNESS_BRD);
				if(!opt.disableBranchContract) objs.add(EcjProblem.FITNESS_BRN);
			}

			if(objs.isEmpty()) {
				System.err.println("FATAL: you must specify at least one objective");
				System.exit(1);
			}

			params.add("pop.subpop.0.species.fitness.numobjectives=" + objs.size());

			String obj = null;
			for(String o : objs)
				if(obj == null) obj = o;
				else obj += ":" + o;
			params.add("problem." + EcjProblem.PROP_FINESS + "=" + obj);
		}

		params.add("problem." + EcjProblem.PROP_RELOAD_CLASSES + "=" + opt.reload);

		params.add("problem." + EcjProblem.PROP_CUT_SIZE + "=" + opt.cutSize);
		params.add("problem." + EcjProblem.PROP_CUT + "=" + opt.cut);

		params.add("problem." + EcjProblem.PROP_MAXSIZE + "=" + opt.maxSize);

		params.add("problem." + EcjProblem.PROP_AUX_SIZE + "=" + opt.auxSize);

		params.add("problem." + EcjProblem.PROP_ENABLE_CACHE + "=" + opt.enableCache);
		
		String propFile = TestfulLogger.singleton.writeParameters(params);

		ec.Evolve.main(new String[] { "-file", propFile });
	}

	public void parseArgs(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);

		try {
			// parse the arguments.
			parser.parseArgument(args);

		} catch(CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java " + Launcher.class.getCanonicalName() + " [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("   Example: java " + Launcher.class.getCanonicalName() + parser.printExample(org.kohsuke.args4j.ExampleMode.REQUIRED));

			System.exit(1);
		}
	}
}
