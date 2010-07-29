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

package testful.coverage.whiteBox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.IntegerType;
import soot.Local;
import soot.LongType;
import soot.Modifier;
import soot.PatchingChain;
import soot.PrimType;
import soot.RefLikeType;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Trap;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.FieldRef;
import soot.jimple.FloatConstant;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThisRef;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.JimpleLocal;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.toolkits.scalar.CombinedAnalysis;
import soot.toolkits.scalar.CombinedDUAnalysis;
import soot.toolkits.scalar.UnitValueBoxPair;
import soot.util.Chain;
import testful.IConfigProject;
import testful.coverage.Launcher.ConfigInstrumenter;
import testful.coverage.soot.Instrumenter.UnifiedInstrumentator;
import testful.coverage.soot.Skip;
import testful.coverage.soot.SootUtils;

public class WhiteInstrumenter implements UnifiedInstrumentator {

	private static final Logger logger = Logger.getLogger("testful.coverage.instrumenter.white");

	private static SootClass objectClass;
	private static RefType objectType;
	private static SootClass exceptionClass;
	private static SootClass runtimeExceptionClass;
	private static SootClass defExposerClass;
	private static SootClass dataAccess;

	private static final SootClass trackerClass;
	private static SootMethod trackerSingleton;

	private static SootMethod trackerBasicBlock;

	/** SootMethod representation of TrackerWhiteBox.trackBranch */
	private static final SootMethod trackBranch;
	/** SootMethod representation of TrackerWhiteBox.getConditionTargetId */
	private static final SootMethod getConditionTargetId;
	/** SootMethod representation of TrackerWhiteBox.setConditionTargetDistance(double) */
	private static final SootMethod setConditionTargetDistance;
	/** SootMethod representation of TrackerWhiteBox.calculateConditionTargetDistance(double, double) */
	private static final SootMethod calculateConditionTargetDistance;

	/** SootMethod representation of TrackerWhiteBox.trackCall(int) */
	private static final SootMethod trackCall;
	/** SootMethod representation of TrackerWhiteBox.trackReturn(int) */
	private static final SootMethod trackReturn;

	/** SootMethod representation of TrackerWhiteBox.getDataAccess(int, boolean) */
	private static final SootMethod getDataAccess;

	/** SootMethod representation of TrackerWhiteBox.manageDefUse(DataAccess, int, boolean) */
	private static final SootMethod manageDefUse;

	/** SootMethod representation of TrackerWhiteBox.manageDefExposition(Object) */
	private static final SootMethod manageDefExposition;

	/** SootMethod representation of TrackerWhiteBox.newArrayDef(int len, int id) */
	private static final SootMethod newArrayDef;

	/** SootMethod representation of TrackerWhiteBox.newMultiArrayDef(int[] len, int id) */
	private static final SootMethod newMultiArrayDef;

	/** SootMethod representation of TrackerWhiteBox.arrayAssignmentDef(Object a, int id) */
	private static final SootMethod arrayAssignmentDef;

	static {
		Scene.v().loadClassAndSupport(Object.class.getName());
		objectClass  = Scene.v().getSootClass(Object.class.getName());
		objectType = objectClass.getType();

		Scene.v().loadClassAndSupport(Exception.class.getName());
		exceptionClass = Scene.v().getSootClass(Exception.class.getName());
		Scene.v().loadClassAndSupport(RuntimeException.class.getName());
		runtimeExceptionClass = Scene.v().getSootClass(RuntimeException.class.getName());

		final String TRACKER = TrackerWhiteBox.class.getName();
		Scene.v().loadClassAndSupport(TRACKER);
		trackerClass = Scene.v().getSootClass(TRACKER);
		trackerSingleton = trackerClass.getMethodByName("getTracker");

		// Control-Flow Graph stuff
		trackerBasicBlock = trackerClass.getMethodByName("trackBasicBlock");
		trackBranch = trackerClass.getMethodByName("trackBranch");
		getConditionTargetId = trackerClass.getMethodByName("getConditionTargetId");
		setConditionTargetDistance = trackerClass.getMethodByName("setConditionTargetDistance");
		calculateConditionTargetDistance = trackerClass.getMethodByName("calculateConditionTargetDistance");

		// contextual stuff
		trackCall = trackerClass.getMethodByName("trackCall");
		trackReturn = trackerClass.getMethodByName("trackReturn");
		Scene.v().loadClassAndSupport(DataAccess.class.getName());
		dataAccess = Scene.v().getSootClass(DataAccess.class.getName());
		getDataAccess = trackerClass.getMethodByName("getDataAccess");

		// Def-Use stuff
		newArrayDef = trackerClass.getMethodByName("newArrayDef");
		newMultiArrayDef = trackerClass.getMethodByName("newMultiArrayDef");
		arrayAssignmentDef = trackerClass.getMethodByName("arrayAssignmentDef");
		manageDefUse = trackerClass.getMethodByName("manageDefUse");

		// Def-Exposition stuff
		Scene.v().loadClassAndSupport(DefExposer.class.getName());
		defExposerClass = Scene.v().getSootClass(DefExposer.class.getName());
		manageDefExposition = trackerClass.getMethodByName("manageDefExposition");
	}

	private final ConfigInstrumenter config;
	public WhiteInstrumenter(ConfigInstrumenter config) {
		logger.config("White-Box instrumenter loaded");
		this.config = config;
	}

	private Analyzer analyzer;

	private Local localTracker;
	private Local localConditionTarget;
	private Local localTmpDouble1;
	private Local localTmpDouble2;
	private Local localDataAccessD;
	private Local localDataAccessU;

	@Override
	public void init(Body oldBody, Body newBody, Chain<Unit> newUnits, IdentityStmt[] paramDefs) {
		logger.finer(" processing " + newBody.getMethod().getName());

		// some useful constants
		final SootMethod method = newBody.getMethod();
		final SootClass sClass = method.getDeclaringClass();
		final BlockClass clazz = Factory.singleton.get(sClass, Factory.singleton.get(sClass.getFields()));

		localTracker = Jimple.v().newLocal("__testful__white_tracker__", trackerClass.getType());
		newBody.getLocals().add(localTracker);
		newUnits.add(Jimple.v().newAssignStmt(localTracker, Jimple.v().newStaticInvokeExpr(trackerSingleton.makeRef())));

		localConditionTarget = Jimple.v().newLocal("__testful_white_condition_target__", IntType.v());
		newBody.getLocals().add(localConditionTarget);
		newUnits.add(Jimple.v().newAssignStmt(localConditionTarget, Jimple.v().newVirtualInvokeExpr(localTracker, getConditionTargetId.makeRef())));

		localTmpDouble1 = Jimple.v().newLocal("__testful_white_tmp_double_1__", DoubleType.v());
		newBody.getLocals().add(localTmpDouble1);

		localTmpDouble2 = Jimple.v().newLocal("__testful_white_tmp_double_2__", DoubleType.v());
		newBody.getLocals().add(localTmpDouble2);

		analyzer = new Analyzer(newUnits, clazz, newBody, oldBody, paramDefs);
		localDataAccessD = Jimple.v().newLocal("__testful_white_data_access_d__", dataAccess.getType());
		newBody.getLocals().add(localDataAccessD);

		localDataAccessU = Jimple.v().newLocal("__testful_white_data_access_u__", dataAccess.getType());
		newBody.getLocals().add(localDataAccessU);

	}

	@Override
	public void preprocess(SootClass sClass) {


		// du-tracking: add tracking fields
		if(!config.isDuPairs()) return;

		final List<SootField> fields = new ArrayList<SootField>();
		for(SootField f : sClass.getFields())
			fields.add(f);

		for (SootField f : fields) {
			int modifiers = f.getModifiers();
			if(Modifier.isFinal(modifiers)) modifiers -= Modifier.FINAL;

			SootField trackField;
			if(f.getType() instanceof ArrayType) {
				trackField = new SootField(getTracker(f.getName()), ArrayType.v(dataAccess.getType(), ((ArrayType) f.getType()).numDimensions), modifiers);
			} else {
				trackField = new SootField(getTracker(f.getName()), dataAccess.getType(), modifiers);
			}
			sClass.addField(trackField);
		}


		// def-exposition preprocessing: adding GET_FIELDS and __testful_get_defs__ methods
		if(!config.isDefExposition()) return;

		if(sClass.implementsInterface(DefExposer.class.getName())) return;

		sClass.addInterface(defExposerClass);

		// generate GET_FIELDS
		{
			SootMethod getFields = new SootMethod(DefExposer.GET_FIELDS, Arrays.asList(new Type[] {}), ArrayType.v(objectType, 1), Modifier.PUBLIC );
			sClass.addMethod(getFields);
			getFields.addTag(Skip.s);

			JimpleBody body = Jimple.v().newBody(getFields);
			PatchingChain<Unit> units = body.getUnits();
			getFields.setActiveBody(body);

			Local _this = Jimple.v().newLocal("_this", sClass.getType());
			body.getLocals().add(_this);

			Local ret = Jimple.v().newLocal("ret", ArrayType.v(objectType, 1));
			body.getLocals().add(ret);

			Local tmp = Jimple.v().newLocal("tmp", objectType);
			body.getLocals().add(tmp);

			units.add(Jimple.v().newIdentityStmt(_this, new ThisRef(sClass.getType())));

			List<SootField> refFields = new ArrayList<SootField>();
			for(SootField f : fields)
				if(SootUtils.isReference(f.getType()))
					refFields.add(f);

			units.add(Jimple.v().newAssignStmt(ret, Jimple.v().newNewArrayExpr(objectType, IntConstant.v(refFields.size()))));

			int i = 0;
			for(SootField f : refFields) {
				if(f.isStatic()) {
					units.add(Jimple.v().newAssignStmt(tmp, Jimple.v().newStaticFieldRef(f.makeRef())));
					units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(ret, IntConstant.v(i++)), tmp));
				} else {
					units.add(Jimple.v().newAssignStmt(tmp, Jimple.v().newInstanceFieldRef(_this, f.makeRef())));
					units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(ret, IntConstant.v(i++)), tmp));
				}
			}

			units.add(Jimple.v().newReturnStmt(ret));
		}


		// generate GET_DEFS & add def-tracker fields in the class
		{
			SootMethod getDefs = new SootMethod(DefExposer.GET_DEFS, Arrays.asList(new Type[] {}), ArrayType.v(objectType, 1), Modifier.PUBLIC );
			sClass.addMethod(getDefs);
			getDefs.addTag(Skip.s);

			JimpleBody body = Jimple.v().newBody(getDefs);
			PatchingChain<Unit> units = body.getUnits();
			getDefs.setActiveBody(body);

			Local _this = Jimple.v().newLocal("_this", sClass.getType());
			body.getLocals().add(_this);

			Local ret = Jimple.v().newLocal("ret", ArrayType.v(objectType, 1));
			body.getLocals().add(ret);

			Local tmp = Jimple.v().newLocal("tmp", objectType);
			body.getLocals().add(tmp);

			units.add(Jimple.v().newIdentityStmt(_this, new ThisRef(sClass.getType())));


			units.add(Jimple.v().newAssignStmt(ret, Jimple.v().newNewArrayExpr(objectType, IntConstant.v(fields.size()))));

			int i = 0;
			for(SootField f : fields) {

				SootField trackField = sClass.getFieldByName(getTracker(f.getName()));

				if(Modifier.isStatic(f.getModifiers())) {
					units.add(Jimple.v().newAssignStmt(tmp, Jimple.v().newStaticFieldRef(trackField.makeRef())));
					units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(ret, IntConstant.v(i++)), tmp));
				} else {
					units.add(Jimple.v().newAssignStmt(tmp, Jimple.v().newInstanceFieldRef(_this, trackField.makeRef())));
					units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(ret, IntConstant.v(i++)), tmp));
				}
			}
			units.add(Jimple.v().newReturnStmt(ret));
		}
	}

	private static String getTracker(String name) {
		return "__track_def_of_" + name + "__";
	}

	@Override
	public void processPre(Chain<Unit> newUnits, Stmt op) {
		analyzer.process(newUnits, op);
	}

	@Override
	public void processPost(Chain<Unit> newUnits, Stmt op) {
		analyzer.processPost(newUnits, op);
	}

	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) {
		analyzer.exceptional(newUnits, exc);
	}

	@Override
	public void done(IConfigProject config) {
		for(BlockClass c : Factory.singleton.getClasses()) {
			try {
				c.performDataFlowAnalysis();
				c.write(config.getDirInstrumented());

				PrintWriter writer = new PrintWriter(new File(config.getDirInstrumented(), c.getName().replace('.', File.separatorChar) + ".dot"));
				writer.println(c.getDot());
				writer.close();
			} catch(FileNotFoundException e) {
				logger.log(Level.WARNING, "Cannot create the class diagram: " + e.getMessage(), e);
			}
		}
	}

	class Analyzer {

		/** the local that stores this */
		private final Local localThis;

		/** the begin of the current method */
		private final BlockFunctionEntry start;

		/** the end of the current method */
		private final BlockFunctionExit end;

		/** mark blocks belonging to the method being analyzed */
		private final BitSet blocks;

		/** mark conditions belonging to the method being analyzed */
		private final BitSet branches;

		/** the current building block */
		private Block current = null;

		/** blocks emitted<br/><b>key:</b> First unit of the block<br/><b>value</b>: block */
		private final Map<Unit, Block> done;

		/**
		 * if not null, toLink contains an edge outgoing frome the last block analyzed,
		 * and it has to be linked with this block
		 */
		private Edge toLink;

		/** link the first unit of each building block to the set of incoming edges (to complete) */
		private final Map<Unit, Set<Edge>> toLinkMap;

		/** units not inserted anywhere (i.e. dead code) */
		private final Set<Unit> deadCode;

		/** repository for local variables */
		private final Map<Local, Data> localRepository;

		/** list of temporary definition for the building block being analyzed */
		private Set<DataDef> defs;

		/** list of temporary uses for the building block being analyzed */
		private Set<DataUse> uses;

		/** traps on the original method (all!) */
		private final Collection<Trap> oldTraps;

		/** traps active when analyzing the current operation */
		private final Deque<Trap> activeTraps;

		private final Chain<Local> locals;

		private final CombinedAnalysis duAnalysis;

		/** key: original variable; value: tracking variable */
		private Map<Local, Local> trackingLocals = new HashMap<Local, Local>();

		private Local getTrackingLocal(Local local) {
			Local tracker = trackingLocals.get(local);

			if(tracker == null) {
				if(local.getType() instanceof ArrayType)
					tracker = Jimple.v().newLocal(getTracker(local.getName()), ArrayType.v(dataAccess.getType(), ((ArrayType)local.getType()).numDimensions));
				else
					tracker = Jimple.v().newLocal(getTracker(local.getName()), dataAccess.getType());

				trackingLocals.put(local, tracker);
				locals.add(tracker);
			}

			return tracker;
		}

		/**
		 * if the method uses arrays with n dimensions (e.g. int[][][] => n = 3),
		 * this fields has n places: at each position i (0 <= i < n) it stores an array with i+1 dimensions:
		 * <ol>
		 * <li> arrayTmpTrackers[0] = DataAccess[]</li>
		 * <li> arrayTmpTrackers[1] = DataAccess[][]</li>
		 * <li> arrayTmpTrackers[2] = DataAccess[][][]</li>
		 * </ol>
		 */
		private final Local[] arrayTmpTrackers;

		private Local getArrayTmpTracker(int n) {

			if(arrayTmpTrackers[n-1] == null) {
				arrayTmpTrackers[n-1] = new JimpleLocal(getTracker("array_" + n), ArrayType.v(dataAccess.getType(), n));
				locals.add(arrayTmpTrackers[n-1]);
			}

			return arrayTmpTrackers[n-1];
		}

		/** It is an array of integers */
		private Local arrInt;
		private Local getArrayIntegers() {
			if(arrInt == null) {
				arrInt = Jimple.v().newLocal("___testful__whiteBox_int_array__", ArrayType.v(IntType.v(), 1));
				locals.add(arrInt);
			}

			return arrInt;
		}

		/** It is an array of integers */
		private Local tmpObject;
		private Local getTmpObject() {
			if(tmpObject == null) {
				tmpObject = Jimple.v().newLocal("___testful__whiteBox_tmp_Object__", objectType);
				locals.add(tmpObject);
			}

			return tmpObject;
		}

		public Analyzer(Chain<Unit> newUnits, BlockClass clazz, Body newBody, Body oldBody, IdentityStmt[] paramDefs) {
			final SootMethod method = newBody.getMethod();
			final String methodName = method.getName();
			final boolean methodStatic = method.isStatic();
			final boolean methodPublic = method.isPublic();
			final boolean methodPrivate = method.isPrivate();
			locals = newBody.getLocals();

			final UnitGraph unitGraph = new ExceptionalUnitGraph(oldBody);
			duAnalysis = CombinedDUAnalysis.v(unitGraph);

			localRepository = new HashMap<Local, Data>();
			blocks = clazz.blocks;
			branches = clazz.branches;
			done = new HashMap<Unit, Block>();
			toLinkMap = new HashMap<Unit, Set<Edge>>();
			deadCode = new HashSet<Unit>();
			defs = new HashSet<DataDef>();
			uses = new HashSet<DataUse>();

			oldTraps = oldBody.getTraps();
			activeTraps = new LinkedList<Trap>();

			start = new BlockFunctionEntry(clazz, methodName, methodPublic);
			Block paramInit = new BlockBasic(defs, uses);
			new EdgeDirect(start).setTo(paramInit);
			toLink = new EdgeDirect(paramInit);
			end = new BlockFunctionExit(methodName, clazz);
			if(!methodStatic && !methodPrivate) {
				EdgeDirect.create(clazz, start);
				EdgeDirect.create(end, clazz);
			}

			//calculate max dimensions of arrays: consider local variables (also includes fields, parameters)
			int maxDim = 0;
			for(Local p : newBody.getLocals()) {
				if(p.getType() instanceof ArrayType) {
					int dim = ((ArrayType)p.getType()).numDimensions;
					if(dim > maxDim) maxDim = dim;
				}
			}
			// create array temporary dataAccess
			arrayTmpTrackers = new Local[maxDim];

			if(methodStatic) localThis = null;
			else localThis = newBody.getThisLocal();

			if(config.isContext())
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackCall.makeRef(), Arrays.asList(new Value[] { IntConstant.v(start.getId()) }))));

			// def-use analysis
			if(config.isDuPairs()) {

				// track this definitions (if it is not a constructor)
				if(config.isDefExposition() && localThis != null && !SootMethod.constructorName.equals(newBody.getMethod().getName()))
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, manageDefExposition.makeRef(), localThis)));

				//Track parameters
				for(int i = 0; i < paramDefs.length; i++) {

					final Local p = newBody.getParameterLocal(i);

					DataDef def = new DataDef(start, getDataParameter(p), null);
					manageDefs(def);

					// Consider the uses of the current definitions: if they do not have any additional definition, then it is possible to skip the du tracking
					boolean oneDef = true;
					for (Object unit : duAnalysis.getUsesOf(paramDefs[i])) {
						UnitValueBoxPair u = (UnitValueBoxPair) unit;
						if(duAnalysis.getDefsOfAt(p, u.getUnit()).size() > 1) {
							oneDef = false;
							break;
						}
					}
					if(oneDef) {
						logger.fine("Skipping instrumentation of the definition of parameter " + i + "(" + p + " in " + paramDefs[i] + "): it is the only definition for all of its uses");
						continue;
					}

					Type type = p.getType();
					if(type instanceof ArrayType) {

						Local tmpObj = getTmpObject();
						Local tr = getTrackingLocal(p);

						newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newCastExpr(p, objectType)));
						newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newVirtualInvokeExpr(localTracker, arrayAssignmentDef.makeRef(), Arrays.asList(tmpObj, IntConstant.v(def.getId())))));
						newUnits.add(Jimple.v().newAssignStmt(tr, Jimple.v().newCastExpr(tmpObj, tr.getType())));

					} else {
						newUnits.add(Jimple.v().newAssignStmt(
								getTrackingLocal(p),
								Jimple.v().newVirtualInvokeExpr(localTracker, getDataAccess.makeRef(), IntConstant.v(def.getId()))));
					}

					// track parameter's definitions (if it is not a prim type)
					if(config.isDefExposition() && !(type instanceof PrimType || type instanceof ArrayType))
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, manageDefExposition.makeRef(), p)));
				}

			}
		}

		public void exceptional(Chain<Unit> newUnits, Local exc) {
			if(config.isContext())
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new Value[] { IntConstant.v(start.getId()) }))));
		}

		public void process(Chain<Unit> newUnits, Stmt op) {
			preProcess(newUnits, op);

			if(op instanceof AssignStmt)
				process(newUnits, (AssignStmt) op);
			else if(op instanceof GotoStmt)
				process(newUnits, (GotoStmt) op);
			else if(op instanceof IdentityStmt)
				process(newUnits, (IdentityStmt) op);
			else if(op instanceof InvokeStmt)
				; // nothing to do
			else if(op instanceof IfStmt)
				process(newUnits, (IfStmt) op);
			else if(op instanceof LookupSwitchStmt)
				process(newUnits, (LookupSwitchStmt) op);
			else if(op instanceof TableSwitchStmt)
				process(newUnits, (TableSwitchStmt) op);
			else if(op instanceof RetStmt)
				process(newUnits, (RetStmt) op);
			else if(op instanceof ReturnStmt)
				process(newUnits, (ReturnStmt) op);
			else if(op instanceof ReturnVoidStmt)
				process(newUnits, (ReturnVoidStmt) op);
			else if(op instanceof ThrowStmt)
				process(newUnits, (ThrowStmt) op);
			else if(op instanceof EnterMonitorStmt)
				; // nothing to do
			else if(op instanceof ExitMonitorStmt)
				; // nothing to do
			else
				logger.warning("cannot analyze " + op + " (" + op.getClass().getName() + ")");
		}

		public void process(Chain<Unit> newUnits, AssignStmt u) {
			if(u.getLeftOp().getType() instanceof ArrayType && u.getRightOp() instanceof InvokeExpr)
				return;

			handleDef(newUnits, u);
		}

		public void process(Chain<Unit> newUnits, GotoStmt u) {
			add(new EdgeDirect(current), u.getTarget());
			current = null;
		}

		public void process(Chain<Unit> newUnits, IdentityStmt u) {
			// this method is invoked to store an exception (first statement in catch blocks)
			handleDef(newUnits, u);
		}

		public void process(Chain<Unit> newUnits, IfStmt u) {

			if(!(u.getCondition() instanceof ConditionExpr)) {
				logger.warning("Unknown condition: " + u.getCondition() + " (" + u.getCondition().getClass().getName() + ")");
				current = null;
				return;
			}

			ConditionExpr expr = (ConditionExpr) u.getCondition();

			Value op1 = expr.getOp1();
			Value op2 = expr.getOp2();
			Type type = op1.getType();

			DataUse use1 = handleUse(newUnits, u, op1);
			DataUse use2 = handleUse(newUnits, u, op2);

			ConditionIf c = new ConditionIf(use1, use2, expr.toString());
			current.setCondition(c);

			EdgeConditional trueBranch = new EdgeConditional(current, c);
			c.setTrueBranch(trueBranch);
			add(trueBranch, u.getTarget());
			branches.set(trueBranch.getId());

			EdgeConditional falseBranch = new EdgeConditional(current, c);
			c.setFalseBranch(falseBranch);
			toLink = falseBranch;
			branches.set(falseBranch.getId());

			Unit after = Jimple.v().newNopStmt();
			Unit handleTrue = Jimple.v().newNopStmt();

			// the expression "if(expr)" is instrumented this way:
			//  1. if(expr) goto 10
			//  2. trackBranch(false)
			//  3. if(condTarget != trueBranch) goto 7
			//  4. trackDistance(op1, op2);
			//  5. goto 17
			//  7. if(contTarget != falseBranch) goto 17
			//  8. trackDistance(0);
			//  9. goto 17

			// 10. trackBranch(false)
			// 11. if(condTarget != trueBranch) goto 14
			// 12. trackDistance(0);
			// 13. goto 17
			// 14. if(contTarget != falseBranch) goto 17
			// 15. trackDistance(op1, op2);
			// 16. goto 17
			// 17. nop (after)

			newUnits.add(Jimple.v().newIfStmt(expr, handleTrue));

			{ // handle false
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackBranch.makeRef(), IntConstant.v(falseBranch.getId()))));

				// calculate distance (true)
				Unit nop = Jimple.v().newNopStmt();
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(trueBranch.getId())), nop));

				if(type instanceof BooleanType) {
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1))));

				} else if(type instanceof IntegerType || type instanceof LongType || type instanceof FloatType || type instanceof DoubleType) {
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, Jimple.v().newCastExpr(op1, DoubleType.v())));
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2, Jimple.v().newCastExpr(op2, DoubleType.v())));
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), localTmpDouble1, localTmpDouble2)));

				} else if(type instanceof RefLikeType) {

					// localTmpDouble1 = (op1 == null) ? 0 : 1;
					Unit isNull = Jimple.v().newNopStmt();
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, DoubleConstant.v(0)));
					newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(op1, NullConstant.v()), isNull));
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, DoubleConstant.v(1)));
					newUnits.add(isNull);

					// localTmpDouble2 = (op2 == null) ? 0 : 1;
					isNull = Jimple.v().newNopStmt();
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2, DoubleConstant.v(0)));
					newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(op2, NullConstant.v()), isNull));
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2, DoubleConstant.v(1)));
					newUnits.add(isNull);

					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), localTmpDouble1, localTmpDouble2)));

				} else logger.warning("Unknown operand type: " + type + " (" + type.getClass().getName() + ") / " + op2.getType() + " (" + op2.getType().getClass().getName() + ")");

				newUnits.add(Jimple.v().newGotoStmt(after));

				// calculate distance (false)
				newUnits.add(nop);
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(falseBranch.getId())), after));
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(0))));
				newUnits.add(Jimple.v().newGotoStmt(after));
			}

			newUnits.add(handleTrue);

			{ // handle true
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackBranch.makeRef(), IntConstant.v(trueBranch.getId()))));

				// calculate distance (true)
				Unit nop = Jimple.v().newNopStmt();
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(trueBranch.getId())), nop));
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(0))));
				newUnits.add(Jimple.v().newGotoStmt(after));

				// calculate distance (false)
				newUnits.add(nop);
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(falseBranch.getId())), after));

				if(type instanceof BooleanType) {
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1))));

				} else if(type instanceof IntegerType || type instanceof LongType || type instanceof FloatType || type instanceof DoubleType) {
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, Jimple.v().newCastExpr(op1, DoubleType.v())));
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2, Jimple.v().newCastExpr(op2, DoubleType.v())));
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), localTmpDouble1, localTmpDouble2)));

				} else if(type instanceof RefLikeType) {

					// localTmpDouble1 = (op1 == null) ? 0 : 1;
					Unit isNull = Jimple.v().newNopStmt();
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, DoubleConstant.v(0)));
					newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(op1, NullConstant.v()), isNull));
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, DoubleConstant.v(1)));
					newUnits.add(isNull);

					// localTmpDouble2 = (op2 == null) ? 0 : 1;
					isNull = Jimple.v().newNopStmt();
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2, DoubleConstant.v(0)));
					newUnits.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(op2, NullConstant.v()), isNull));
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2, DoubleConstant.v(1)));
					newUnits.add(isNull);

					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), localTmpDouble1, localTmpDouble2)));

				} else logger.warning("Unknown operand type: " + type + " (" + type.getClass().getName() + ") / " + op2.getType() + " (" + op2.getType().getClass().getName() + ")");

				newUnits.add(Jimple.v().newGotoStmt(after));
			}

			// done
			newUnits.add(after);


			current = null;
		}

		public void process(Chain<Unit> newUnits, LookupSwitchStmt u) {
			final Value key = u.getKey();

			final DataUse use = handleUse(newUnits, u, key);
			ConditionSwitch c = new ConditionSwitch(use);
			current.setCondition(c);

			List<IntConstant> lookupValues = new ArrayList<IntConstant>();
			List<Unit> targets = new ArrayList<Unit>();

			int[] keys = new int[u.getTargetCount()];
			SortedMap<Integer, Unit> keyTarget = new TreeMap<Integer, Unit>();
			Map<Integer, Integer> keyBranchId = new HashMap<Integer, Integer>();
			for(int i = 0; i < u.getTargetCount(); i++) {
				final int value = u.getLookupValue(i);
				keys[i] = value;
				lookupValues.add(IntConstant.v(value));

				EdgeConditional edge = new EdgeConditional(current, c);
				c.addBranch(value, edge);
				add(edge, u.getTarget(i));
				branches.set(edge.getId());
				keyBranchId.put(value, edge.getId());

				Unit target = Jimple.v().newNopStmt();
				targets.add(target);
				keyTarget.put(value, target);
			}

			Unit defaultTarget = Jimple.v().newNopStmt();
			Integer defaultBranchId;
			{
				EdgeConditional edge = new EdgeConditional(current, c);
				c.setDefaultBranch(edge);
				add(edge, u.getDefaultTarget());
				branches.set(edge.getId());
				defaultBranchId = edge.getId();
			}

			Unit lastNop = Jimple.v().newNopStmt();

			newUnits.add(Jimple.v().newLookupSwitchStmt(key, lookupValues, targets, defaultTarget));
			processSwitch(newUnits, key, keys, keyTarget, keyBranchId, defaultTarget, defaultBranchId, lastNop);
			newUnits.add(lastNop);

			current = null;
		}

		public void process(Chain<Unit> newUnits, TableSwitchStmt u) {
			final Value key = u.getKey();
			final int lIndex = u.getLowIndex();
			final int hIndex = u.getHighIndex();

			DataUse use = handleUse(newUnits, u, key);
			ConditionSwitch c = new ConditionSwitch(use);
			current.setCondition(c);

			int[] keys = new int[hIndex-lIndex+1];
			List<Unit> targets = new ArrayList<Unit>();
			SortedMap<Integer, Unit> keyTarget = new TreeMap<Integer, Unit>();
			Map<Integer, Integer> keyBranchId = new HashMap<Integer, Integer>();

			for(int idx = 0; idx <= hIndex-lIndex; idx++) {
				final int value = idx+lIndex;
				keys[idx] = value;

				EdgeConditional edge = new EdgeConditional(current, c);
				c.addBranch(value, edge);
				add(edge, u.getTarget(idx));
				branches.set(edge.getId());
				keyBranchId.put(value, edge.getId());

				Unit target = Jimple.v().newNopStmt();
				targets.add(target);
				keyTarget.put(value, target);
			}

			Unit defaultTarget = Jimple.v().newNopStmt();
			Integer defaultBranchId;
			{
				EdgeConditional edge = new EdgeConditional(current, c);
				c.setDefaultBranch(edge);
				add(edge, u.getDefaultTarget());
				branches.set(edge.getId());
				defaultBranchId = edge.getId();
			}

			Unit lastNop = Jimple.v().newNopStmt();

			newUnits.add(Jimple.v().newTableSwitchStmt(key, u.getLowIndex(), u.getHighIndex(), targets, defaultTarget));
			processSwitch(newUnits, key, keys, keyTarget, keyBranchId, defaultTarget, defaultBranchId, lastNop);
			newUnits.add(lastNop);

			current = null;
		}

		private void processSwitch(Chain<Unit> newUnits, Value keyValue, int[] keys, SortedMap<Integer, Unit> keyTarget, Map<Integer, Integer> keyBranchId, Unit defaultTarget, int defaultBranchId, Unit lastNop) {
			for(final int key : keys) {
				final Unit start = keyTarget.get(key);
				final int branchId = keyBranchId.get(key);

				newUnits.add(start);
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackBranch.makeRef(), IntConstant.v(branchId))));

				// track distance

				// [0..i..n-1] i-th target; [n] -> default target;
				IntConstant[] ctLookupValues = new IntConstant[keys.length + 1];
				Unit[] ctTargets = new Unit[keys.length + 1];
				for(int i = 0; i < keys.length; i++) {
					ctLookupValues[i] = IntConstant.v(keyBranchId.get(keys[i]));
					ctTargets[i] = Jimple.v().newNopStmt();
				}
				ctLookupValues[keys.length] =  IntConstant.v(defaultBranchId);
				ctTargets[keys.length] = Jimple.v().newNopStmt();

				newUnits.add(Jimple.v().newLookupSwitchStmt(localConditionTarget, Arrays.asList(ctLookupValues), Arrays.asList(ctTargets), lastNop));

				for(int i = 0; i < keys.length; i++) {
					final int ctKey = keys[i];
					newUnits.add(ctTargets[i]);
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(Math.abs(ctKey - key)))));
					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}

				{ // handle default target
					newUnits.add(ctTargets[keys.length]);
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(switchDistanceToDefault(key, keyBranchId.keySet())))));
					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}
			}

			// manage default target
			{
				newUnits.add(defaultTarget);
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackBranch.makeRef(), IntConstant.v(defaultBranchId))));

				// track distance
				IntConstant[] ctLookupValues = new IntConstant[keys.length + 1];
				Unit[] ctTargets = new Unit[keys.length + 1];
				for(int i = 0; i < keys.length; i++) {
					ctLookupValues[i] = IntConstant.v(keyBranchId.get(keys[i]));
					ctTargets[i] = Jimple.v().newNopStmt();
				}
				ctLookupValues[keys.length] =  IntConstant.v(defaultBranchId);
				ctTargets[keys.length] = Jimple.v().newNopStmt();

				newUnits.add(Jimple.v().newLookupSwitchStmt(localConditionTarget, Arrays.asList(ctLookupValues), Arrays.asList(ctTargets), lastNop));

				for(int i = 0; i < keys.length; i++) {
					final int ctKey = keys[i];
					newUnits.add(ctTargets[i]);
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, Jimple.v().newCastExpr(keyValue, DoubleType.v())));
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), DoubleConstant.v(ctKey), localTmpDouble1)));
					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}

				{ // handle default target
					newUnits.add(ctTargets[keys.length]);
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(0))));
					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}
			}

		}

		private int switchDistanceToDefault(int key, Set<Integer> keySet) {
			if(!keySet.contains(key)) return 0;

			int d = 1;
			while(keySet.contains(key-d) && keySet.contains(key + d)) d++;

			return d;
		}

		public void process(Chain<Unit> newUnits, RetStmt u) {
			if(config.isContext())
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new Value[] { IntConstant.v(start.getId()) }))));

			EdgeDirect.create(current, end);
			current = null;
		}

		public void process(Chain<Unit> newUnits, ReturnStmt u) {
			if(config.isContext())
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new Value[] { IntConstant.v(start.getId()) }))));

			EdgeDirect.create(current, end);
			current = null;
		}

		public void process(Chain<Unit> newUnits, ReturnVoidStmt u) {
			if(config.isContext())
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new Value[] { IntConstant.v(start.getId()) }))));

			EdgeDirect.create(current, end);
			current = null;
		}

		public void process(Chain<Unit> newUnits, ThrowStmt u) {
			SootClass exc = ((RefType) u.getOp().getType()).getSootClass();

			boolean handled = false;
			for(Trap t : activeTraps)
				if(SootUtils.isAssignable(t.getException(), exc)) {
					EdgeExceptional edge = new EdgeExceptional(current, exc.getJavaStyleName());
					add(edge, t.getHandlerUnit());
					handled = true;
					break;
				}

			if(!handled) EdgeExceptional.create(current, end, exc.getJavaStyleName());

			current = null;
		}

		public void finalCheck() {
			for(Unit w : deadCode)
				logger.warning("ERROR: block starting from " + w + " seems dead!");
		}

		/**
		 * Returns the Data for a given parameter
		 * @param l the local in which the parameter is stored
		 * @return the data for the parameter
		 */
		private Data getDataParameter(Local l) {
			Data ret = localRepository.get(l);

			if(ret == null) {
				ret = Factory.getData(null, l.getType(), true);
				localRepository.put(l, ret);
			}

			return ret;
		}

		/**
		 * Returns the Data for a given value.
		 * For uses, it is possible to specify the current Unit and verify if the value is defined as the copy of a field.
		 * @param value the value
		 * @param u if not null and the value is a Local, search if there is exactly one definition reaching this use, and if it is an assignment of a field to the considered local
		 * @return the Data for the value
		 */
		private Data getData(Value value, Unit u) {
			if(value == localThis) return null;

			if(value instanceof Local) {
				Local local = (Local) value;

				Data ret = localRepository.get(local);
				if(ret != null) return ret;

				// check if the local is defined as copy of a field
				if(u != null) {
					List<Unit> defs = duAnalysis.getDefsOfAt(local, u);
					if(defs.size() == 1 && defs.get(0) instanceof AssignStmt && ((AssignStmt)defs.get(0)).getRightOp() instanceof FieldRef) {
						FieldRef field = (FieldRef) ((AssignStmt)defs.get(0)).getRightOp();
						logger.fine("Local " +  value + " (in statement " + u + ") is defined as copy of field " + field);
						return Factory.singleton.get(field.getField());
					}
				}

				ret = Factory.getData(null, local.getType(), false);
				localRepository.put(local, ret);

				return ret;
			}

			if(value instanceof FieldRef) return Factory.singleton.get(((FieldRef) value).getField());

			if(value instanceof ArrayRef) return getData(((ArrayRef) value).getBase(), u);

			return null;
		}

		/** this edge goes to the pointed unit */
		private void add(Edge edge, Unit stmt) {
			if(edge == null) throw new NullPointerException();

			Block d = done.get(stmt);
			if(d != null) {
				edge.setTo(d);
				deadCode.remove(stmt);
				return;
			}

			Set<Edge> set = toLinkMap.get(stmt);
			if(set == null) {
				set = new HashSet<Edge>();
				toLinkMap.put(stmt, set);
			}

			set.add(edge);
		}

		/**
		 * Updates the live definitions, by adding the new definition and killing those definitions that operate on the same variable
		 * @param def the new definition
		 */
		private void manageDefs(DataDef def) {
			Data data = def.getData();

			Iterator<DataDef> iter = defs.iterator();
			while(iter.hasNext())
				if(iter.next().getData() == data)
					iter.remove();

			defs.add(def);
		}

		/**
		 * Performs a preliminary analysis on the statement.
		 */
		private void preProcess(Chain<Unit> newUnits, Stmt stmt) {
			//remove expired traps
			Iterator<Trap> iterTraps = activeTraps.iterator();
			while(iterTraps.hasNext()) {
				if(iterTraps.next().getEndUnit() == stmt) {
					iterTraps.remove();
				}
			}

			// add new traps
			for(Trap t : oldTraps)
				if(t.getBeginUnit() == stmt) activeTraps.addLast(t);

			// If the current unit contains an invocation, create a new building block
			if(stmt.containsInvokeExpr()) {
				Edge link = null;
				if(current != null)
					link = new EdgeDirect(current);

				preCreateBuildingBlock();
				InvokeExpr expr = stmt.getInvokeExpr();
				current = new BlockFunctionCall(defs, uses, stmt.getInvokeExpr().getMethod().getName(), expr.getMethod().getDeclaringClass().getJavaStyleName(), expr instanceof StaticInvokeExpr);
				postCreateBuildingBlock(newUnits, stmt);

				if(link != null)
					link.setTo(current);

				for(SootClass exc : stmt.getInvokeExpr().getMethod().getExceptions()) {
					boolean handled = false;
					for(Trap t : activeTraps)
						if(SootUtils.isAssignable(t.getException(), exc)) {
							EdgeExceptional edge = new EdgeExceptional(current, exc.getJavaStyleName());
							add(edge, t.getHandlerUnit());
							handled = true;
							break;
						}

					if(!handled) EdgeExceptional.create(current, end, exc.getJavaStyleName());
				}
			}

			// if there is no active block, or there is a merge between two independent flows, create a new building block
			else if(current == null || stmt.getBoxesPointingToThis().size() > 0) {

				Edge link = null;
				if(current != null)
					link = new EdgeDirect(current);

				preCreateBuildingBlock();
				current = new BlockBasic(defs, uses);
				postCreateBuildingBlock(newUnits, stmt);

				if(link != null)
					link.setTo(current);
			}

			// take care of uses
			if(!(stmt instanceof IfStmt || stmt instanceof TableSwitchStmt || stmt instanceof LookupSwitchStmt)) {
				for(ValueBox use : stmt.getUseBoxes()) {
					handleUse(newUnits, stmt, use.getValue());
				}
			}
		}

		/**
		 * Insert the instrumentation code to track the use
		 * @param newUnits the new chain of operations
		 * @param v the variable to track
		 * @param useId the id of the use
		 */
		private DataUse handleUse(Chain<Unit> newUnits, Unit u, Value v) {

			if(!config.isDuPairs()) return null;

			if(v.getType() instanceof ArrayType) return null;

			if(v instanceof Local) {

				if(v.equals(localThis))
					return null;

				// if the use has only one reaching definition, I can skip its tracking
				if(duAnalysis.getDefsOfAt((Local) v, u).size() <= 1) {
					logger.fine(" Skipping instrumentation of use of " + v + " in " + u + ": only 1 reachable def def");
					return null;
				}

				Data data = getData(v, u);
				if(data == null) return null;

				DataUse use = new DataUse(current, data, defs);
				uses.add(use);

				newUnits.add(Jimple.v().newAssignStmt(
						localDataAccessU,
						Jimple.v().newVirtualInvokeExpr(
								localTracker,
								getDataAccess.makeRef(),
								IntConstant.v(use.getId()))));

				newUnits.add(Jimple.v().newInvokeStmt(
						Jimple.v().newVirtualInvokeExpr(
								localTracker,
								manageDefUse.makeRef(),
								getTrackingLocal((Local)v),
								localDataAccessU)));

				return use;

			} else if(v instanceof InstanceFieldRef) {

				Data data = getData(v, u);
				if(data == null) return null;

				DataUse use = new DataUse(current, data, defs);
				uses.add(use);

				try {
					InstanceFieldRef fr = (InstanceFieldRef) v;
					SootField field = fr.getField();
					SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

					newUnits.add(Jimple.v().newAssignStmt(
							localDataAccessD,
							Jimple.v().newInstanceFieldRef(fr.getBase(), tracker.makeRef())
					));

					newUnits.add(Jimple.v().newAssignStmt(
							localDataAccessU,
							Jimple.v().newVirtualInvokeExpr(
									localTracker,
									getDataAccess.makeRef(),
									IntConstant.v(use.getId()))));

					newUnits.add(Jimple.v().newInvokeStmt(
							Jimple.v().newVirtualInvokeExpr(
									localTracker,
									manageDefUse.makeRef(),
									localDataAccessD,
									localDataAccessU)));

				} catch (RuntimeException e) {
					logger.log(Level.WARNING, "Tracking field not found: " + e.getMessage(), e);
				}

				return use;

			} else if (v instanceof StaticFieldRef) {

				Data data = getData(v, u);
				if(data == null) return null;

				DataUse use = new DataUse(current, data, defs);
				uses.add(use);

				try {
					StaticFieldRef fr = (StaticFieldRef) v;
					SootField field = fr.getField();
					SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

					newUnits.add(Jimple.v().newAssignStmt(
							localDataAccessD,
							Jimple.v().newStaticFieldRef(tracker.makeRef())
					));

					newUnits.add(Jimple.v().newAssignStmt(
							localDataAccessU,
							Jimple.v().newVirtualInvokeExpr(
									localTracker,
									getDataAccess.makeRef(),
									IntConstant.v(use.getId()))));

					newUnits.add(Jimple.v().newInvokeStmt(
							Jimple.v().newVirtualInvokeExpr(
									localTracker,
									manageDefUse.makeRef(),
									localDataAccessD,
									localDataAccessU)));
				} catch (RuntimeException e) {
					logger.log(Level.WARNING, "Tracking field not found: " + e.getMessage(), e);
				}

				return use;

			}

			return null;
		}

		/**
		 * Track the definition
		 * @param newUnits the unit chain being built
		 * @param stmt the unit being processed
		 */
		private void handleDef(Chain<Unit> newUnits, DefinitionStmt stmt) {
			if(!config.isDuPairs()) return;

			final Value leftOp = stmt.getLeftOp();
			final Value rightOp = stmt.getRightOp();

			if(leftOp instanceof Local) {

				// Consider the uses of the current definitions: if they do not have any additional definition, then it is possible to skip the du tracking
				boolean oneDef = true;
				for (Object unit : duAnalysis.getUsesOf(stmt)) {
					UnitValueBoxPair u = (UnitValueBoxPair) unit;
					if(duAnalysis.getDefsOfAt((Local) leftOp, u.getUnit()).size() > 1) {
						oneDef = false;
						break;
					}
				}
				if(oneDef) {
					logger.fine(" Skipping instrumentation of the definition of " + leftOp + " in " + stmt + ": it is the only definition for all of its uses");
					return;
				}
			}

			Serializable value;
			if(stmt.getRightOp() instanceof IntConstant) value = ((IntConstant) stmt.getRightOp()).value;
			else if(stmt.getRightOp() instanceof LongConstant) value = ((LongConstant) stmt.getRightOp()).value;
			else if(stmt.getRightOp() instanceof FloatConstant) value = ((FloatConstant) stmt.getRightOp()).value;
			else if(stmt.getRightOp() instanceof DoubleConstant) value = ((DoubleConstant) stmt.getRightOp()).value;
			else value = null;

			final DataDef dataDef = new DataDef(current, getData(leftOp, null), value); // getData for a definition, hence the second parameter is null
			manageDefs(dataDef);

			// calculate the definition ID
			Value dataDefValue;
			if(leftOp.getType() instanceof ArrayType) {
				ArrayType type = (ArrayType) leftOp.getType();

				if(rightOp instanceof Local) {
					dataDefValue = getTrackingLocal((Local)rightOp);
				} else if(rightOp instanceof InstanceFieldRef) {
					InstanceFieldRef fr = (InstanceFieldRef) rightOp;
					SootField field = fr.getField();
					SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

					dataDefValue =  getArrayTmpTracker(type.numDimensions);
					newUnits.add(Jimple.v().newAssignStmt(dataDefValue, Jimple.v().newInstanceFieldRef(fr.getBase(), tracker.makeRef())));

				} else if(rightOp instanceof StaticFieldRef) {
					StaticFieldRef fr = (StaticFieldRef) rightOp;
					SootField field = fr.getField();
					SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

					dataDefValue = getArrayTmpTracker(type.numDimensions);
					newUnits.add(Jimple.v().newAssignStmt(dataDefValue, Jimple.v().newStaticFieldRef(tracker.makeRef())));

				} else if(rightOp instanceof ArrayRef) {
					ArrayRef arrayRef = (ArrayRef) rightOp;
					Value base = arrayRef.getBase();
					Value index = arrayRef.getIndex();

					dataDefValue = getArrayTmpTracker(type.numDimensions);
					newUnits.add(Jimple.v().newAssignStmt(dataDefValue, Jimple.v().newArrayRef(getTrackingLocal((Local) base), index)));
				} else if(rightOp instanceof NewArrayExpr) {

					NewArrayExpr newArray = (NewArrayExpr) rightOp;
					dataDefValue = getArrayTmpTracker(type.numDimensions);

					if(type.numDimensions == 1) {
						newUnits.add(Jimple.v().newAssignStmt(dataDefValue, Jimple.v().newVirtualInvokeExpr(localTracker, newArrayDef.makeRef(),
								Arrays.asList(newArray.getSize(), IntConstant.v(dataDef.getId())))));
					} else {
						Local arrInt = getArrayIntegers();
						Local tmpObject = getTmpObject();

						newUnits.add(Jimple.v().newAssignStmt(arrInt, Jimple.v().newNewArrayExpr(IntType.v(), IntConstant.v(type.numDimensions))));
						newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(arrInt, IntConstant.v(0)), newArray.getSize()));
						newUnits.add(Jimple.v().newAssignStmt(tmpObject, Jimple.v().newVirtualInvokeExpr(localTracker, newMultiArrayDef.makeRef(), Arrays.asList(arrInt, IntConstant.v(dataDef.getId())))));
						newUnits.add(Jimple.v().newAssignStmt(dataDefValue, Jimple.v().newCastExpr(tmpObject, dataDefValue.getType())));
					}
				} else if(rightOp instanceof NewMultiArrayExpr) {

					NewMultiArrayExpr newMultiArray = (NewMultiArrayExpr) rightOp;
					dataDefValue =  getArrayTmpTracker(type.numDimensions);

					Local arrInt = getArrayIntegers();
					Local tmpObject = getTmpObject();

					newUnits.add(Jimple.v().newAssignStmt(arrInt, Jimple.v().newNewArrayExpr(IntType.v(), IntConstant.v(type.numDimensions))));
					for(int i = 0; i < newMultiArray.getSizeCount(); i++)
						newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(arrInt, IntConstant.v(i)), newMultiArray.getSize(i)));

					newUnits.add(Jimple.v().newAssignStmt(tmpObject,Jimple.v().newVirtualInvokeExpr(localTracker, newMultiArrayDef.makeRef(), Arrays.asList(arrInt, IntConstant.v(dataDef.getId())))));
					newUnits.add(Jimple.v().newAssignStmt(dataDefValue, Jimple.v().newCastExpr(tmpObject, dataDefValue.getType())));

				} else {
					logger.warning("Unable to handle array definition: " + leftOp + " - " + leftOp.getClass().getName());

					return;
				}
			} else if(leftOp instanceof Local || leftOp instanceof InstanceFieldRef || leftOp instanceof StaticFieldRef) {
				dataDefValue =  Jimple.v().newVirtualInvokeExpr(localTracker, getDataAccess.makeRef(), IntConstant.v(dataDef.getId()));
			} else if(leftOp instanceof ArrayRef) {
				dataDefValue = localDataAccessD;
				newUnits.add(Jimple.v().newAssignStmt(localDataAccessD, Jimple.v().newVirtualInvokeExpr(localTracker, getDataAccess.makeRef(), IntConstant.v(dataDef.getId()))));
			} else {
				logger.warning("Unable to handle: " + leftOp + " - " + leftOp.getClass().getName());
				return;
			}

			// put the dataDef in the right place
			if(leftOp instanceof Local) {
				newUnits.add(Jimple.v().newAssignStmt(getTrackingLocal((Local) leftOp), dataDefValue));

			} else if(leftOp instanceof InstanceFieldRef) {
				InstanceFieldRef fr = (InstanceFieldRef) leftOp;
				SootField field = fr.getField();
				SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

				if(!(dataDefValue instanceof Local)) {
					newUnits.add(Jimple.v().newAssignStmt(localDataAccessD, dataDefValue));
					dataDefValue = localDataAccessD;
				}
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(fr.getBase(), tracker.makeRef()), dataDefValue));


			} else if(leftOp instanceof StaticFieldRef) {
				StaticFieldRef fr = (StaticFieldRef) leftOp;
				SootField field = fr.getField();
				SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

				if(!(dataDefValue instanceof Local)) {
					newUnits.add(Jimple.v().newAssignStmt(localDataAccessD, dataDefValue));
					dataDefValue = localDataAccessD;
				}
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newStaticFieldRef(tracker.makeRef()), dataDefValue));

			} else if(leftOp instanceof ArrayRef) {
				ArrayRef arrayRef = (ArrayRef) leftOp;
				Value base = arrayRef.getBase();
				Value index = arrayRef.getIndex();

				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(getTrackingLocal((Local) base), index), dataDefValue));
			}
		}

		public void processPost(Chain<Unit> newUnits, Stmt stmt) {
			if(config.isDuPairs() && (stmt instanceof AssignStmt)) {
				AssignStmt u = (AssignStmt) stmt;
				if(u.getLeftOp().getType() instanceof ArrayType && u.getRightOp() instanceof InvokeExpr) {
					Local leftOp = (Local) u.getLeftOp();
					Data data = getData(leftOp, null); // this is for a definition, hence the second parameter is null

					final DataDef def = new DataDef(current, data, null);
					manageDefs(def);

					Local tmpObj = getTmpObject();
					Local tr = getTrackingLocal(leftOp);

					newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newCastExpr(leftOp, objectType)));
					newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newVirtualInvokeExpr(localTracker, arrayAssignmentDef.makeRef(), Arrays.asList(tmpObj, IntConstant.v(def.getId())))));
					newUnits.add(Jimple.v().newAssignStmt(tr, Jimple.v().newCastExpr(tmpObj, tr.getType())));

				}
			}

			if(stmt.containsInvokeExpr()) {
				toLink = new EdgeDirect(current);
				current = null;
			}
		}

		private void preCreateBuildingBlock() {
			// clear stuff: defs and uses
			defs = new HashSet<DataDef>();
			uses = new HashSet<DataUse>();
		}

		private void postCreateBuildingBlock(Chain<Unit> newUnits, Stmt stmt) {
			// call the tracker for the building block coverage
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackerBasicBlock.makeRef(), Arrays.asList(new Value[] { IntConstant.v(current.getId()) }))));

			done.put(stmt, current);
			blocks.set(current.getId());

			// for each unchecked trap, create an exceptional edge
			for(Trap t : activeTraps) {
				SootClass exc = t.getException();
				if(!SootUtils.isAssignable(exceptionClass, exc) || SootUtils.isAssignable(runtimeExceptionClass, exc))
					add(new EdgeExceptional(current, t.getException().getJavaStyleName()), t.getHandlerUnit());
			}

			if(toLink != null) {
				toLink.setTo(current);
				toLink = null;
			}

			// for each edge to this block, complete it
			Set<Edge> set = toLinkMap.remove(stmt);
			if(set == null) {
				if(current.getPre().isEmpty())
					deadCode.add(stmt);

			} else {
				for(Edge e : set)
					e.setTo(current);
			}
		}
	}
}
