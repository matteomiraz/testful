package testful.coverage;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.jimple.toolkits.scalar.NopEliminator;
import testful.Configuration;
import testful.model.TestCluster;
import testful.runner.ClassFinderCaching;
import testful.runner.ClassFinderImpl;
import testful.runner.TestfulClassLoader;
import testful.utils.ActiveBodyTransformer;
import testful.utils.JimpleWriter;
import testful.utils.SootMain;

/**
 * Modify the class under test, adding trackers and other stuff.
 * 
 * @author matteo
 */
public class Launcher {

	@Option(required = true, name = "-cut", usage = "The class to test", metaVar = "full.qualified.ClassName")
	private String cut;

	@Option(required = false, name = "-baseDir", usage = "Specify the CUT's base directory")
	private String baseDir;

	private static final boolean preWriter     = false;
	private static final boolean instrumenter  = true;
	private static final boolean postWriter    = false;
	private static final boolean nopEliminator = true;

	public static void main(String[] args) {

		Launcher l = new Launcher();
		l.parseCommands(args);

		try {
			testful.TestFul.printHeader("Instrumenter");

			Configuration config = new Configuration(l.baseDir);
			config.setCut(l.cut);
			
			String[] SOOT_CONF = new String[] { "-validate", "--keep-line-number", "--xml-attributes", "-f", "c", "-output-dir", config.getDirInstrumented() };

			List<String> sootClassPath = new ArrayList<String>();
			sootClassPath.add(config.getDirJml());
			sootClassPath.add(config.getDirVanilla());
			sootClassPath.add(System.getProperty("java.class.path"));

			String bootClassPath = System.getProperty("sun.boot.class.path");
			if(bootClassPath != null) sootClassPath.add(bootClassPath); // vaild for sun and ibm jvm
			else {
				System.err.println("Unknown Java Vendor: " + System.getProperty("java.vm.vendor"));
				System.getProperties().list(System.out);
				System.exit(1);
			}

			ClassFinderCaching finder = new ClassFinderCaching(new ClassFinderImpl(new File(config.getDirInstrumented()), new File(config.getDirJml()), new File(config.getDirVanilla())));
			TestfulClassLoader tcl = new TestfulClassLoader(finder);
			TestCluster tc = new TestCluster(tcl, config);

			Collection<String> toInstrument = tc.getClassesToInstrument();
			System.out.println("Instrumenting: " + toInstrument);
			
			String params[] = new String[SOOT_CONF.length + 2 + toInstrument.size()];

			params[0] = "-cp";

			StringBuilder cpBuilder = null;
			for(String cp : sootClassPath) {
				if(cpBuilder == null) cpBuilder = new StringBuilder();
				else cpBuilder.append(File.pathSeparator);
				cpBuilder.append(cp);
			}
			params[1] = cpBuilder.toString();

			int i = 2;
			for(String s : SOOT_CONF)
				params[i++] = s;
			for(String s : toInstrument)
				params[i++] = s;

			SootMain.singleton.processCmdLine(params);

			if(instrumenter) {
				for(String className : toInstrument) {
					Scene.v().loadClassAndSupport(className);
					SootClass sClass = Scene.v().getSootClass(className);
					Instrumenter.singleton.preprocess(sClass);
				}
			}
			
			String last = null;
			if(preWriter) {
				String newPhase = "jtp.preWriter";
				PackManager.v().getPack("jtp").add(new Transform(newPhase, JimpleWriter.singleton));
				last = newPhase;
				System.out.println("Enabled phase: " + last);
			}

			if(instrumenter) {
				String newPhase = "jtp.coverageInstrumenter";
				System.out.println("Enabled phase: " + newPhase);
				if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, Instrumenter.singleton));
				else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, Instrumenter.singleton), last);
				last = newPhase;
			}

			if(postWriter) {
				String newPhase = "jtp.postWriter";
				System.out.println("Enabled phase: " + newPhase);
				if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)));
				else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)), last);
				last = newPhase;
			}

			if(nopEliminator) {
				String newPhase = "jtp.nopEliminator";
				System.out.println("Enabled phase: " + newPhase);
				if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(NopEliminator.v())));
				else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(NopEliminator.v())), last);
				last = newPhase;
			}

			System.out.println();

			SootMain.singleton.run();

			if(instrumenter)
				Instrumenter.singleton.done(config.getDirInstrumented(), l.cut);
			
			
			System.out.println("Done");
			System.exit(0);
			
		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
			e.printStackTrace();
			System.exit(1);
		}
	}

	private void parseCommands(String[] args) {
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
