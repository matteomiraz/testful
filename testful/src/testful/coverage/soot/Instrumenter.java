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

package testful.coverage.soot;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.PackManager;
import soot.PatchingChain;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Transform;
import soot.Trap;
import soot.Unit;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.toolkits.scalar.NopEliminator;
import soot.jimple.toolkits.scalar.UnreachableCodeEliminator;
import soot.tagkit.LineNumberTag;
import soot.tagkit.StringTag;
import soot.util.Chain;
import testful.IConfigProject;
import testful.TestFul;

public class Instrumenter {

	public static interface UnifiedInstrumentator {
		/**
		 * Preprocess a class. In this phase it is allowed to add methods to the class.
		 * Notice that new methods will be processed in subsequent phases. To skip them (or to
		 * skip pre-existent methods) annotate them with testful.utils.Skip.
		 * @param sClass the class being preprocessed
		 */
		public void preprocess(SootClass sClass);

		/**
		 * Called at the beginning of the method (after identity statements).
		 * Instrumenters can safely initialize their variables and do initial stuff.
		 * @param oldBody the old body (read only!)
		 * @param newBody the new body
		 * @param newUnits the chain that will be emitted
		 * @param paramDefs the parameters' definitions
		 */
		public void init(Body oldBody, Body newBody, Chain<Unit> newUnits, IdentityStmt[] paramDefs);

		/**
		 * Do something before an operation
		 * @param newUnits the chain that will be emitted
		 * @param op the operation being analyzed
		 */
		public void processPre(Chain<Unit> newUnits, Stmt op);

		/**
		 * Do something after an operation
		 * @param newUnits the chain that will be emitted
		 * @param op the operation being analyzed
		 */
		public void processPost(Chain<Unit> newUnits, Stmt op);

		/**
		 * If the method throws an exception, it is caught and handled in this block (before re-throwing it)
		 * @param newUnits the chain that will be emitted
		 * @param exc the exception is stored in this local (do not modify it)
		 */
		public void exceptional(Chain<Unit> newUnits, Local exc);

		/**
		 * This method is called at the end of the processing (after analyzing all classes' methods)
		 * @param config the configuration (directories)
		 */
		public void done(IConfigProject config);
	}

	private static final Logger logger = Logger.getLogger("testful.instrumenter");

	private static final boolean preWriter           = false;
	private static final boolean deadCodeRemoverPre  = true;
	private static final boolean bvcWriter           = false;
	private static final boolean nphWriter           = false;
	private static final boolean instrumenter        = true;
	private static final boolean postWriter          = false;
	private static final boolean nopEliminator       = true;
	private static final boolean deadCodeRemoverPost = true;
	private static final boolean finalWriter         = false;

	public static void prepare(IConfigProject config, List<String> toInstrument) {
		String[] SOOT_CONF = new String[] { "-validate", "-keep-line-number", "-f", "c", "-output-dir", config.getDirInstrumented().getAbsolutePath() };

		if(System.getProperty("sun.boot.class.path") == null) {
			logger.severe("Unknown Java Vendor: " + System.getProperty("java.vm.vendor"));
			System.exit(1);
		}

		logger.info("Instrumenting: " + toInstrument);

		String params[] = new String[SOOT_CONF.length + 2 + toInstrument.size()];

		params[0] = "-cp";
		params[1] = config.getDirCompiled().getAbsolutePath();
		for (File l : config.getLibraries())
			params[1] += File.pathSeparator + l.getAbsolutePath();
		params[1] += File.pathSeparator + System.getProperty("java.class.path");
		params[1] += File.pathSeparator + System.getProperty("sun.boot.class.path");

		int i = 2;
		for(String s : SOOT_CONF)
			params[i++] = s;
		for(String s : toInstrument)
			params[i++] = s;

		logger.config("Launching SOOT with command line parameters:\n" + Arrays.toString(params));

		SootMain.singleton.processCmdLine(params);
	}

	@SuppressWarnings("unused")
	public static void run(IConfigProject config, boolean bvc, boolean nph, List<String> toInstrument, UnifiedInstrumentator ... instrumenters) {

		TestfulInstrumenter instr = null;

		if(instrumenter) {
			instr = new TestfulInstrumenter(instrumenters);

			for(String className : toInstrument) {
				Scene.v().loadClassAndSupport(className);
				SootClass sClass = Scene.v().getSootClass(className);
				instr.preprocess(sClass);
			}
		}

		String last = null;
		if(logger.isLoggable(Level.FINER) || preWriter) {
			String newPhase = "jtp.preWriter";
			PackManager.v().getPack("jtp").add(new Transform(newPhase, JimpleWriter.singleton));
			last = newPhase;
			logger.fine("Enabled phase: " + last);
		}

		if(deadCodeRemoverPre) {
			String newPhase = "jtp.deadCodeRemoverPre";
			logger.fine("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(UnreachableCodeEliminator.v())));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(UnreachableCodeEliminator.v())), last);
			last = newPhase;
		}

		if(bvc) {
			logger.fine("Enabled phase: " + "jtp.bvc");
			if(last == null) PackManager.v().getPack("jtp").add(new Transform("jtp.bvc", ActiveBodyTransformer.v(BoundaryValueCoverageTransformer.singleton)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform("jtp.bvc", ActiveBodyTransformer.v(BoundaryValueCoverageTransformer.singleton)), last);
			last = "jtp.bvc";

			logger.fine("Enabled phase: " + "jtp.bvcNopEliminator");
			PackManager.v().getPack("jtp").insertAfter(new Transform("jtp.bvcNopEliminator", ActiveBodyTransformer.v(NopEliminator.v())), last);
			last = "jtp.bvcNopEliminator";

			if(bvcWriter) {
				logger.fine("Enabled phase: " + "jtp.bvcWriter");
				PackManager.v().getPack("jtp").insertAfter(new Transform("jtp.bvcWriter", ActiveBodyTransformer.v(JimpleWriter.singleton)), last);
				last = "jtp.bvcWriter";
			}
		}

		if(nph) {
			logger.fine("Enabled phase: " + "jtp.nph");
			if(last == null) PackManager.v().getPack("jtp").add(new Transform("jtp.nph", ActiveBodyTransformer.v(NullPointerHandlingTransformer.singleton)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform("jtp.nph", ActiveBodyTransformer.v(NullPointerHandlingTransformer.singleton)), last);
			last = "jtp.nph";

			logger.fine("Enabled phase: " + "jtp.nphNopEliminator");
			PackManager.v().getPack("jtp").insertAfter(new Transform("jtp.nphNopEliminator", ActiveBodyTransformer.v(NopEliminator.v())), last);
			last = "jtp.nphNopEliminator";

			if(nphWriter) {
				logger.fine("Enabled phase: " + "jtp.nphWriter");
				PackManager.v().getPack("jtp").insertAfter(new Transform("jtp.nphWriter", ActiveBodyTransformer.v(JimpleWriter.singleton)), last);
				last = "jtp.nphWriter";
			}
		}

		if(instrumenter) {
			String newPhase = "jtp.coverageInstrumenter";
			logger.fine("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, instr));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, instr), last);
			last = newPhase;
		}

		if(logger.isLoggable(Level.FINE) || postWriter) {
			String newPhase = "jtp.postWriter";
			logger.fine("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)), last);
			last = newPhase;
		}

		if(TestFul.DEBUG) {
			String newPhase = "jtp.postValidator";
			logger.fine("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(BodyValidator.singleton)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(BodyValidator.singleton)), last);
			last = newPhase;
		}

		if(nopEliminator) {
			String newPhase = "jtp.nopEliminator";
			logger.fine("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(NopEliminator.v())));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(NopEliminator.v())), last);
			last = newPhase;
		}

		if(deadCodeRemoverPost) {
			String newPhase = "jtp.deadCodeRemoverPost";
			logger.fine("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(UnreachableCodeEliminator.v())));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(UnreachableCodeEliminator.v())), last);
			last = newPhase;
		}

		if(logger.isLoggable(Level.FINEST) || finalWriter) {
			String newPhase = "jtp.finalWriter";
			logger.fine("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(JimpleWriter.singleton)), last);
			last = newPhase;
		}

		if(TestFul.DEBUG) {
			String newPhase = "jtp.finalValidator";
			logger.fine("Enabled phase: " + newPhase);
			if(last == null) PackManager.v().getPack("jtp").add(new Transform(newPhase, ActiveBodyTransformer.v(BodyValidator.singleton)));
			else PackManager.v().getPack("jtp").insertAfter(new Transform(newPhase, ActiveBodyTransformer.v(BodyValidator.singleton)), last);
			last = newPhase;
		}

		SootMain.singleton.run();

		if(instrumenter)
			instr.done(config);
	}

	private static class TestfulInstrumenter extends BodyTransformer {

		private static final Logger logger = Logger.getLogger("testful.instrumenter");

		/** java.lang.Throwable soot class */
		public static final SootClass throwableClass;

		static {
			Scene.v().loadClassAndSupport(Throwable.class.getName());
			throwableClass = Scene.v().getSootClass(Throwable.class.getName());
		}

		public TestfulInstrumenter(UnifiedInstrumentator ... instrumenters) {
			this.instrumenters = instrumenters;
		}

		private final UnifiedInstrumentator[] instrumenters;

		public void done(IConfigProject config) {
			for(UnifiedInstrumentator instr : instrumenters)
				instr.done(config);
		}

		/**
		 * Preprocess a class. In this phase it is allowed to add methods to the class.
		 * Notice that new methods will be processed in subsequent phases. To skip them (or to
		 * skip pre-existent methods) annotate them with testful.utils.Skip.
		 * @param sClass the class being preprocessed
		 */
		public void preprocess(SootClass sClass) {
			if(sClass.isInterface()) {
				logger.warning("Skipping " + sClass.getName() + ": it is an interface");
				return;
			}

			for(UnifiedInstrumentator i : instrumenters)
				i.preprocess(sClass);
		}

		@Override
		@SuppressWarnings("rawtypes")
		protected void internalTransform(Body oldBody, String phaseName, Map options) {
			final SootMethod method = oldBody.getMethod();
			final String methodName = method.getName();

			if(method.hasTag(Skip.NAME)) {
				logger.fine("Skipping " + method.getName());
				return;
			}

			final SootClass sClass = method.getDeclaringClass();
			final Iterator<Unit> oldStmtIt = oldBody.getUnits().snapshotIterator();

			final JimpleBody newBody = Jimple.v().newBody(method);
			method.setActiveBody(newBody);
			final PatchingChain<Unit> newUnits = newBody.getUnits();
			newBody.getLocals().addAll(oldBody.getLocals());

			final Local exc = Jimple.v().newLocal("__throwable_exc__", throwableClass.getType());
			newBody.getLocals().add(exc);

			logger.info("Instrumenting " + sClass.getName() + "." + methodName);

			/** stores the start of an operation (i.e. nopPre) */
			final Map<Unit, Unit> start = new HashMap<Unit, Unit>();

			// instrumentation structure:
			//   initial method code (@this=this, params, superCall)
			//
			// :NOP_PRE_INIT
			//   initial tracker code
			// :NOP_POST_INIT
			//
			// :NOP_BEGIN ( try { )
			//
			// ---- the operation is NOT an identity statement ----
			// :NOP_PRE
			// :NOP_PRE_TRACK
			//   tracking code pre
			// :NOP_ORIG
			//   original code
			// :NOP_POST_TRACK
			//   tracking code post
			// :NOP_AFTER
			//
			// ---- the operation IS an identity statement ----
			// :NOP_PRE
			// :NOP_ORIG
			//   original code
			// :NOP_PRE_TRACK
			//   tracking code
			// :NOP_POST_TRACK
			//   tracking code
			// :NOP_AFTER
			//
			// :NOP_END ( } catch(Throwable exc) { )
			//   exceptional tracker code
			//   throw exc

			// --------------------------------------------------------------------------
			// initial method code (@this=this, params, superCall)
			// --------------------------------------------------------------------------

			// skip special statements: this
			if(!method.isStatic()) newUnits.add(oldStmtIt.next());

			// skip special statements: params
			int nParams = method.getParameterCount();
			final IdentityStmt[] paramDefs = new IdentityStmt[nParams];
			for(int i = 0; i < nParams; i++) {
				Unit paramDef = oldStmtIt.next();
				newUnits.add(paramDef);
				paramDefs[i] = (IdentityStmt) paramDef;
			}

			// --------------------------------------------------------------------------
			// initialization
			// --------------------------------------------------------------------------

			{
				final Unit nopPre = Jimple.v().newNopStmt();
				nopPre.addTag(new StringTag("nopInitPre"));
				newUnits.add(nopPre);

				for(UnifiedInstrumentator i : instrumenters)
					i.init(oldBody, newBody, newUnits, paramDefs);

				final Unit nopPost = Jimple.v().newNopStmt();
				nopPost.addTag(new StringTag("nopInitAfter"));
				newUnits.add(nopPost);
			}

			final Unit nopMethodBegin = Jimple.v().newNopStmt();
			nopMethodBegin.addTag(new StringTag("nopMethodPre"));
			newUnits.add(nopMethodBegin);

			// --------------------------------------------------------------------------
			// statement instrumentation
			// --------------------------------------------------------------------------

			while(oldStmtIt.hasNext()) {
				Stmt stmt = (Stmt) oldStmtIt.next();

				final Unit nopPre = Jimple.v().newNopStmt();
				nopPre.addTag(new StringTag("nopPre"));
				start.put(stmt, nopPre);
				newUnits.add(nopPre);

				final Unit nopPreTrack = Jimple.v().newNopStmt();
				nopPreTrack.addTag(new StringTag("nopPreTrack"));

				final Unit nopOrig = Jimple.v().newNopStmt();
				nopOrig.addTag(new StringTag("nopOrig"));

				final Unit nopPostTrack = Jimple.v().newNopStmt();
				nopPostTrack.addTag(new StringTag("nopPostTrack"));

				final Unit nopAfter = Jimple.v().newNopStmt();
				nopAfter.addTag(new StringTag("nopAfter"));

				final Stmt newStmt = (Stmt) stmt.clone();
				LineNumberTag line = (LineNumberTag) stmt.getTag("LineNumberTag");
				if(line != null) newStmt.addTag(line);
				if(stmt instanceof IdentityStmt) {

					// insert original stmt
					newUnits.add(nopOrig);
					newUnits.add(newStmt);

					// preprocess
					newUnits.add(nopPreTrack);
					for(UnifiedInstrumentator i : instrumenters)
						i.processPre(newUnits, stmt);

					// postprocess
					newUnits.add(nopPostTrack);
					for(UnifiedInstrumentator i : instrumenters)
						i.processPost(newUnits, stmt);

				} else {

					// preprocess
					newUnits.add(nopPreTrack);
					for(UnifiedInstrumentator i : instrumenters)
						i.processPre(newUnits, stmt);

					// insert original stmt
					newUnits.add(nopOrig);
					newUnits.add(newStmt);

					// postprocess
					newUnits.add(nopPostTrack);
					for(UnifiedInstrumentator i : instrumenters)
						i.processPost(newUnits, stmt);
				}

				newUnits.add(nopAfter);
			}

			final Unit nopMethodEnd = Jimple.v().newNopStmt();
			nopMethodEnd.addTag(new StringTag("nopMethodEnd"));
			newUnits.add(nopMethodEnd);

			// Fix jumps (goto, if, switch)
			for(Unit unit : newUnits) {
				if(unit instanceof GotoStmt) {
					GotoStmt gotoStmt = (GotoStmt) unit;
					Unit newTarget = start.get(gotoStmt.getTarget());
					if(newTarget != null) gotoStmt.setTarget(newTarget);
				} else if(unit instanceof IfStmt) {
					IfStmt ifStmt = (IfStmt) unit;
					Unit newTarget = start.get(ifStmt.getTarget());
					if(newTarget != null) ifStmt.setTarget(newTarget);
				} else if(unit instanceof TableSwitchStmt) {
					TableSwitchStmt sw = (TableSwitchStmt) unit;

					Unit newTarget = start.get(sw.getDefaultTarget());
					if(newTarget != null) sw.setDefaultTarget(newTarget);

					final int lowIndex = sw.getLowIndex();
					for(int idx = 0; idx <= sw.getHighIndex()-lowIndex; idx++) {
						newTarget = start.get(sw.getTarget(idx));
						if(newTarget != null) sw.setTarget(idx, newTarget);
					}
				} else if(unit instanceof LookupSwitchStmt) {
					LookupSwitchStmt sw = (LookupSwitchStmt) unit;

					Unit newTarget = start.get(sw.getDefaultTarget());
					if(newTarget != null) sw.setDefaultTarget(newTarget);

					for(int i = 0; i < sw.getTargetCount(); i++) {
						newTarget = start.get(sw.getTarget(i));
						if(newTarget != null) sw.setTarget(i, newTarget);
					}
				}
			}

			// fix traps (try-catch)
			for(Trap trap : oldBody.getTraps()) {
				final Unit newBegin = start.get(trap.getBeginUnit());
				final Unit newEnd = start.get(trap.getEndUnit());
				final Unit newHandler = start.get(trap.getHandlerUnit());

				newBody.getTraps().add(Jimple.v().newTrap(trap.getException(), newBegin, newEnd, newHandler));
			}


			// --------------------------------------------------------------------------
			// Exceptional tracking activities
			// --------------------------------------------------------------------------
			final Unit nopCatchBegin = Jimple.v().newNopStmt();
			nopCatchBegin.addTag(new StringTag("nopCatchPre"));
			newUnits.add(nopCatchBegin);
			newUnits.add(Jimple.v().newIdentityStmt(exc, Jimple.v().newCaughtExceptionRef()));
			// update the traps
			newBody.getTraps().add(Jimple.v().newTrap(throwableClass, nopMethodBegin, nopMethodEnd, nopCatchBegin));
			for(UnifiedInstrumentator i : instrumenters)
				i.exceptional(newUnits, exc);
			// The last istruction of the handler is "throw exc"
			newUnits.add(Jimple.v().newThrowStmt(exc));
			final Unit nopPost = Jimple.v().newNopStmt();
			nopPost.addTag(new StringTag("nopCatchAfter"));
			newUnits.add(nopPost);
		}
	}
}
