package testful.coverage;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.Transform;
import soot.jimple.toolkits.scalar.NopEliminator;
import testful.ConfigCut;
import testful.IConfigCut;
import testful.TestFul;
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

	private static final Logger logger = Logger.getLogger("testful.coverage.instrumenter");

	private static final boolean preWriter     = false;
	private static final boolean instrumenter  = true;
	private static final boolean postWriter    = false;
	private static final boolean nopEliminator = true;

	public static void main(String[] args) {

		IConfigCut config  = new ConfigCut();

		TestFul.parseCommandLine(config, args, Launcher.class, "Instrumenter");

		try {

			if(!config.isQuiet())
				testful.TestFul.printHeader("Instrumenter");

			testful.TestFul.setupLogging(config);

			String[] SOOT_CONF = new String[] { "-validate", "--keep-line-number", "--xml-attributes", "-f", "c", "-output-dir", config.getDirInstrumented().getAbsolutePath() };

			List<String> sootClassPath = new ArrayList<String>();
			sootClassPath.add(config.getDirContracts().getAbsolutePath());
			sootClassPath.add(config.getDirCompiled().getAbsolutePath());
			sootClassPath.add(System.getProperty("java.class.path"));

			String bootClassPath = System.getProperty("sun.boot.class.path");
			if(bootClassPath != null) sootClassPath.add(bootClassPath); // vaild for sun and ibm jvm
			else {
				logger.severe("Unknown Java Vendor: " + System.getProperty("java.vm.vendor"));
				System.exit(1);
			}

			ClassFinderCaching finder = new ClassFinderCaching(new ClassFinderImpl(config.getDirInstrumented(), config.getDirContracts(), config.getDirCompiled()));
			TestfulClassLoader tcl = new TestfulClassLoader(finder);
			TestCluster tc = new TestCluster(tcl, config);

			Collection<String> toInstrument = tc.getClassesToInstrument();
			logger.info("Instrumenting: " + toInstrument);

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

			logger.config("Launching SOOT with command line parameters:\n" + Arrays.toString(params));

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
				logger.fine("Enabled phase: " + last);
			}

			if(instrumenter) {
				String newPhase = "jtp.coverageInstrumenter";
				logger.fine("Enabled phase: " + newPhase);
				if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, Instrumenter.singleton));
				else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, Instrumenter.singleton), last);
				last = newPhase;
			}

			if(postWriter) {
				String newPhase = "jtp.postWriter";
				logger.fine("Enabled phase: " + newPhase);
				if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)));
				else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)), last);
				last = newPhase;
			}

			if(nopEliminator) {
				String newPhase = "jtp.nopEliminator";
				logger.fine("Enabled phase: " + newPhase);
				if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(NopEliminator.v())));
				else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(NopEliminator.v())), last);
				last = newPhase;
			}

			SootMain.singleton.run();

			if(instrumenter)
				Instrumenter.singleton.done(config.getDirInstrumented(), config.getCut());


			logger.info("Done");
			System.exit(0);

		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error: " + e.getMessage(), e);
			System.exit(1);
		}
	}
}
