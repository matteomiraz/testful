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
import java.util.ArrayList;
import java.util.Arrays;
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
import soot.ByteType;
import soot.CharType;
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
import soot.jimple.ArrayRef;
import soot.jimple.AssignStmt;
import soot.jimple.CastExpr;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DefinitionStmt;
import soot.jimple.DoubleConstant;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.EqExpr;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.Expr;
import soot.jimple.FieldRef;
import soot.jimple.FloatConstant;
import soot.jimple.GeExpr;
import soot.jimple.GotoStmt;
import soot.jimple.GtExpr;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InstanceFieldRef;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.LeExpr;
import soot.jimple.LongConstant;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.LtExpr;
import soot.jimple.NeExpr;
import soot.jimple.NewArrayExpr;
import soot.jimple.NewMultiArrayExpr;
import soot.jimple.NullConstant;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StaticFieldRef;
import soot.jimple.StaticInvokeExpr;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
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
import testful.TestFul;
import testful.coverage.Launcher.ConfigInstrumenter;
import testful.coverage.Launcher.ConfigInstrumenter.DataFlowCoverage;
import testful.coverage.soot.Instrumenter.UnifiedInstrumentator;
import testful.coverage.soot.Skip;
import testful.coverage.soot.SootUtils;
import testful.coverage.whiteBox.ConditionIf.ConditionType;

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
	/** SootMethod representation of TrackerWhiteBox.setConditionTargetDistance(double, ContextualId) */
	private static final SootMethod setConditionTargetDistance;
	/** SootMethod representation of TrackerWhiteBox.calculateConditionTargetDistance(double, double, ContextualId, ContextualId) */
	private static final SootMethod calculateConditionTargetDistance;

	/** SootMethod representation of TrackerWhiteBox.trackCall(int) */
	private static final SootMethod trackCall;
	/** SootMethod representation of TrackerWhiteBox.trackReturn(int) */
	private static final SootMethod trackReturn;

	/** SootMethod representation of TrackerWhiteBox.getDataAccess(int, boolean) */
	private static final SootMethod getDataAccess;

	/** SootMethod representation of TrackerWhiteBox.manageDefUse(DataAccess, int, boolean) */
	private static final SootMethod manageDefUse;

	/** SootMethod representation of TrackerWhiteBox.trackPUse(int branchId, ContextualId def) */
	private static final SootMethod trackPUse;

	/** SootMethod representation of TrackerWhiteBox.manageDefExposition(Object) */
	private static final SootMethod manageDefExposition;

	/** SootMethod representation of TrackerWhiteBox.newArrayDef(int len, int id) */
	private static final SootMethod newArrayDef;

	/** SootMethod representation of TrackerWhiteBox.newMultiArrayDef(int[] len, int id) */
	private static final SootMethod newMultiArrayDef;

	/** SootMethod representation of TrackerWhiteBox.arrayAssignmentDef(Object a, int id) */
	private static final SootMethod arrayAssignmentDef;

	/** SootMethod representation of TrackerWhiteBox.manageArrayDefUses(Object defs, ContextualId use) */
	private static final SootMethod manageArrayDefUses;

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
		Scene.v().loadClassAndSupport(ContextualId.class.getName());
		dataAccess = Scene.v().getSootClass(ContextualId.class.getName());
		getDataAccess = trackerClass.getMethodByName("getDataAccess");

		// Def-Use stuff
		newArrayDef = trackerClass.getMethodByName("newArrayDef");
		newMultiArrayDef = trackerClass.getMethodByName("newMultiArrayDef");
		arrayAssignmentDef = trackerClass.getMethodByName("arrayAssignmentDef");
		manageArrayDefUses = trackerClass.getMethodByName("manageArrayDefUses");
		manageDefUse = trackerClass.getMethodByName("manageDefUse");
		trackPUse = trackerClass.getMethodByName("trackPUse");

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
		if(config.getDataFlowCoverage() == DataFlowCoverage.DISABLED) return;

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
		if(config.getDataFlowCoverage() != DataFlowCoverage.EXPDEF) return;

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

	public static Condition.DataType getType(soot.Value v) {

		Type type = v.getType();

		if(type instanceof BooleanType)
			return Condition.DataType.Boolean;

		if(type instanceof CharType)
			return Condition.DataType.Character;

		if(type instanceof PrimType)
			return Condition.DataType.Number;

		if(type instanceof RefType) {
			if(((RefType) type).getClassName().equals("java.lang.String"))
				return Condition.DataType.String;

			return Condition.DataType.Reference;
		}

		if(type instanceof ArrayType)
			return Condition.DataType.Array;

		if(TestFul.DEBUG) TestFul.debug("Unknown data type: " + type);


		return Condition.DataType.Reference;
	}

	class Analyzer {

		/** the local that stores this */
		private final Local localThis;

		/** the begin of the current method */
		private final BlockFunctionEntry start;

		/** the end of the current method */
		private final BlockFunctionExit end;

		/** the current building block */
		private Block current = null;

		/** blocks emitted<br/><b>key:</b> First unit of the block<br/><b>soot.Value</b>: block */
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

		/** key: original variable; soot.Value: tracking variable */
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
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackCall.makeRef(), Arrays.asList(new soot.Value[] { IntConstant.v(start.getId()) }))));

			// def-use analysis
			if(config.getDataFlowCoverage() != DataFlowCoverage.DISABLED) {

				// track this definitions (if it is not a constructor)
				if(config.getDataFlowCoverage() == DataFlowCoverage.EXPDEF)
					if(localThis != null && !SootMethod.constructorName.equals(newBody.getMethod().getName()))
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, manageDefExposition.makeRef(), localThis)));

				//Track parameters
				for(int i = 0; i < paramDefs.length; i++) {

					final Local localParam = newBody.getParameterLocal(i);

					Data dataParam = new Data(null, true);
					localRepository.put(localParam, dataParam);

					DataDef def = new DataDef(start, dataParam, null);
					manageDefs(def);

					// Consider the uses of the current definitions: if they do not have any additional definition, then it is possible to skip the du tracking
					boolean oneDef = true;

					if(localParam.getType() instanceof ArrayType) {
						//TBD: optimize du tracking for arrays
						oneDef = false;

					} else {
						@SuppressWarnings("unchecked")
						List<UnitValueBoxPair> usesOf = duAnalysis.getUsesOf(paramDefs[i]);
						for (UnitValueBoxPair unit : usesOf) {
							if(duAnalysis.getDefsOfAt(localParam, unit.getUnit()).size() > 1) {
								oneDef = false;
								break;
							}
						}
					}

					if(oneDef) {
						logger.fine("Skipping instrumentation of the definition of parameter " + i + "(" + localParam + " in " + paramDefs[i] + "): it is the only definition for all of its uses");
						continue;
					}

					Type type = localParam.getType();
					if(type instanceof ArrayType) {

						Local tmpObj = getTmpObject();
						Local tr = getTrackingLocal(localParam);

						newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newCastExpr(localParam, objectType)));
						newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newVirtualInvokeExpr(localTracker, arrayAssignmentDef.makeRef(), Arrays.asList(tmpObj, IntConstant.v(def.getId())))));
						newUnits.add(Jimple.v().newAssignStmt(tr, Jimple.v().newCastExpr(tmpObj, tr.getType())));

					} else {
						newUnits.add(Jimple.v().newAssignStmt(
								getTrackingLocal(localParam),
								Jimple.v().newVirtualInvokeExpr(localTracker, getDataAccess.makeRef(), IntConstant.v(def.getId()))));
					}

					// track parameter's definitions (if it is not a prim type)
					if(config.getDataFlowCoverage() == DataFlowCoverage.EXPDEF)
						if(!(type instanceof PrimType || type instanceof ArrayType))
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, manageDefExposition.makeRef(), localParam)));
				}

			}
		}

		public void exceptional(Chain<Unit> newUnits, Local exc) {
			if(config.isContext())
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new soot.Value[] { IntConstant.v(start.getId()) }))));
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
			else {
				if(TestFul.DEBUG) TestFul.debug("cannot analyze " + op + " (" + op.getClass().getName() + ")");
			}
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
				if(TestFul.DEBUG) TestFul.debug("Unknown condition: " + u.getCondition() + " (" + u.getCondition().getClass().getName() + ")");
				current = null;
				return;
			}

			ConditionExpr expr = (ConditionExpr) u.getCondition();

			soot.Value op1 = expr.getOp1();
			soot.Value op2 = expr.getOp2();
			Type type = op1.getType();
			Stmt useStmt = u;

			// check if this is a comparison between floats or doubles
			if(type instanceof ByteType && op1 instanceof Local && op2 instanceof IntConstant && ((IntConstant)op2).value == 0) {
				List<Unit> defs = duAnalysis.getDefsOfAt((Local) op1, u);

				if(defs.size() == 1) {
					Stmt def = (Stmt) defs.get(0);

					if (def instanceof AssignStmt) {
						soot.Value rightOp = ((AssignStmt)def).getRightOp();

						if (rightOp instanceof CmplExpr) {
							op1 = ((CmplExpr)rightOp).getOp1();
							op2 = ((CmplExpr)rightOp).getOp2();
							type = op1.getType();
							useStmt = def;

							logger.fine("The conditon " + u + " has been recognized as a comparison between " + op1 + " and " + op2);
						} else if (rightOp instanceof CmpExpr) {
							op1 = ((CmpExpr)rightOp).getOp1();
							op2 = ((CmpExpr)rightOp).getOp2();
							type = op1.getType();
							useStmt = def;

							logger.fine("The conditon " + u + " has been recognized as a comparison between " + op1 + " and " + op2);
						} else if (rightOp instanceof CmpgExpr) {
							op1 = ((CmpgExpr)rightOp).getOp1();
							op2 = ((CmpgExpr)rightOp).getOp2();
							type = op1.getType();
							useStmt = def;

							logger.fine("The conditon " + u + " has been recognized as a comparison between " + op1 + " and " + op2);
						}

					}
				}
			}

			Condition.DataType condDataType = getType(op1);

			Value v1 = getValue(getRealUse(op1, useStmt, false));
			Value v2 = getValue(getRealUse(op2, useStmt, false));

			final DataUse use1 = handleUse(newUnits, op1, useStmt);
			final DataUse use2 = handleUse(newUnits, op2, useStmt);

			ConditionIf.ConditionType condType = null;
			if(expr instanceof LtExpr) condType = ConditionType.LT;
			if(expr instanceof LeExpr) condType = ConditionType.LE;
			if(expr instanceof NeExpr) condType = ConditionType.NE;
			if(expr instanceof EqExpr) condType = ConditionType.EQ;
			if(expr instanceof GeExpr) condType = ConditionType.GE;
			if(expr instanceof GtExpr) condType = ConditionType.GT;
			if(TestFul.DEBUG && condType == null) TestFul.debug("Unknown condition type: " + expr + " (" + expr.getClass().getCanonicalName() + ")");

			ConditionIf c = new ConditionIf(expr.toString(), current.getId(), condDataType, v1, use1, condType, v2, use2);
			current.setCondition(c);

			EdgeConditional trueBranch = new EdgeConditional(current, c);
			c.setTrueBranch(trueBranch);
			add(trueBranch, u.getTarget());

			EdgeConditional falseBranch = new EdgeConditional(current, c);
			c.setFalseBranch(falseBranch);
			toLink = falseBranch;

			Unit after = Jimple.v().newNopStmt();
			Unit handleTrue = Jimple.v().newNopStmt();

			// the expression "if(expr)" is instrumented this way:
			//  1. if(expr) goto 10
			//  2. trackBranch(false)
			//  2a trackPUse(false, usesOfExpr)  { if PUse tracking}
			//  3. if(condTarget != trueBranch) goto 7
			//  4. trackDistance(op1, op2);
			//  5. goto 17
			//  7. if(contTarget != falseBranch) goto 17
			//  8. trackDistance(0);
			//  9. goto 17

			// 10. trackBranch(true)
			// 10a trackPUse(true, usesOfExpr)  { if PUse tracking}
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

				if(config.getDataFlowCoverage().isPUse()) {
					if(use1 != null) {
						Local localDef = getTrackingDef(newUnits, op1, useStmt);
						if(localDef != null)
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackPUse.makeRef(), IntConstant.v(falseBranch.getId()), localDef)));
					}

					if(use2 != null) {
						Local localDef = getTrackingDef(newUnits, op2, useStmt);
						if(localDef != null)
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackPUse.makeRef(), IntConstant.v(falseBranch.getId()), localDef)));
					}
				}

				// calculate distance (true)
				Unit nop = Jimple.v().newNopStmt();
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(trueBranch.getId())), nop));

				if(type instanceof BooleanType) {

					if(config.getDataFlowCoverage().isPUse()) {
						boolean tracked = false;
						if(use1 != null) {
							Local localDef = getTrackingDef(newUnits, op1, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1), localDef)));
							}
						}

						if(use2 != null) {
							Local localDef = getTrackingDef(newUnits, op2, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1), localDef)));
							}
						}

						if(!tracked) {
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1), NullConstant.v())));
						}

					} else {
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1), NullConstant.v())));
					}

				} else if(type instanceof IntegerType || type instanceof LongType || type instanceof FloatType || type instanceof DoubleType) {

					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1,
							(op1.getType() instanceof DoubleType) ? op1 : Jimple.v().newCastExpr(op1, DoubleType.v())));

					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2,
							(op2.getType() instanceof DoubleType) ? op2 : Jimple.v().newCastExpr(op2, DoubleType.v())));

					if(config.getDataFlowCoverage().isPUse()) {
						boolean tracked = false;
						if(use1 != null) {
							Local localDef = getTrackingDef(newUnits, op1, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, localDef))));
							}
						}

						if(use2 != null) {
							Local localDef = getTrackingDef(newUnits, op2, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, localDef))));
							}
						}

						if(!tracked) {
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, NullConstant.v()))));
						}

					} else {
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, NullConstant.v()))));
					}

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

					if(config.getDataFlowCoverage().isPUse()) {
						boolean tracked = false;
						if(use1 != null) {
							Local localDef = getTrackingDef(newUnits, op1, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, localDef))));
							}
						}

						if(use2 != null) {
							Local localDef = getTrackingDef(newUnits, op2, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, localDef))));
							}
						}

						if(!tracked) {
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, NullConstant.v()))));
						}

					} else {
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, NullConstant.v()))));
					}

				} else if(TestFul.DEBUG) TestFul.debug("Unknown operand type: " + type + " (" + type.getClass().getName() + ") / " + op2.getType() + " (" + op2.getType().getClass().getName() + ")");

				newUnits.add(Jimple.v().newGotoStmt(after));

				// calculate distance (false)
				newUnits.add(nop);
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(falseBranch.getId())), after));

				if(config.getDataFlowCoverage().isPUse()) {
					boolean tracked = false;
					if(use1 != null) {
						Local localDef = getTrackingDef(newUnits, op1, useStmt);
						if(localDef != null) {
							tracked = true;
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1), localDef)));
						}
					}

					if(use2 != null) {
						Local localDef = getTrackingDef(newUnits, op2, useStmt);
						if(localDef != null) {
							tracked = true;
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1), localDef)));
						}
					}

					if(!tracked) {
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1), NullConstant.v())));
					}

				} else {
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1), NullConstant.v())));
				}

				newUnits.add(Jimple.v().newGotoStmt(after));
			}

			newUnits.add(handleTrue);

			{ // handle true
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackBranch.makeRef(), IntConstant.v(trueBranch.getId()))));

				if (config.getDataFlowCoverage().isPUse()) {

					if (use1 != null) {
						Local localDef = getTrackingDef(newUnits, op1, useStmt);
						if (localDef != null)
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackPUse.makeRef(),IntConstant.v(trueBranch.getId()),localDef)));
					}

					if (use2 != null) {
						Local localDef = getTrackingDef(newUnits, op2, useStmt);
						if (localDef != null)
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackPUse.makeRef(),IntConstant.v(trueBranch.getId()),localDef)));
					}
				}

				// calculate distance (true)
				Unit nop = Jimple.v().newNopStmt();
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(trueBranch.getId())), nop));

				if(config.getDataFlowCoverage().isPUse()) {
					boolean tracked = false;
					if(use1 != null) {
						Local localDef = getTrackingDef(newUnits, op1, useStmt);
						if(localDef != null) {
							tracked = true;
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1), localDef)));
						}
					}

					if(use2 != null) {
						Local localDef = getTrackingDef(newUnits, op2, useStmt);
						if(localDef != null) {
							tracked = true;
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1), localDef)));
						}
					}

					if(!tracked) {
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1), NullConstant.v())));
					}

				} else {
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1), NullConstant.v())));
				}

				newUnits.add(Jimple.v().newGotoStmt(after));

				// calculate distance (false)
				newUnits.add(nop);
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(falseBranch.getId())), after));

				if(type instanceof BooleanType) {

					if(config.getDataFlowCoverage().isPUse()) {
						boolean tracked = false;
						if(use1 != null) {
							Local localDef = getTrackingDef(newUnits, op1, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1), localDef)));
							}
						}

						if(use2 != null) {
							Local localDef = getTrackingDef(newUnits, op2, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1), localDef)));
							}
						}

						if(!tracked) {
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1), NullConstant.v())));
						}

					} else {
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(1), NullConstant.v())));
					}

				} else if(type instanceof IntegerType || type instanceof LongType || type instanceof FloatType || type instanceof DoubleType) {
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1,
							(op1.getType() instanceof DoubleType) ? op1 : Jimple.v().newCastExpr(op1, DoubleType.v())));

					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2,
							(op2.getType() instanceof DoubleType) ? op2 : Jimple.v().newCastExpr(op2, DoubleType.v())));

					if(config.getDataFlowCoverage().isPUse()) {
						boolean tracked = false;
						if(use1 != null) {
							Local localDef = getTrackingDef(newUnits, op1, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, localDef))));
							}
						}

						if(use2 != null) {
							Local localDef = getTrackingDef(newUnits, op2, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, localDef))));
							}
						}

						if(!tracked) {
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, NullConstant.v()))));
						}

					} else {
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, NullConstant.v()))));
					}

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

					if(config.getDataFlowCoverage().isPUse()) {
						boolean tracked = false;
						if(use1 != null) {
							Local localDef = getTrackingDef(newUnits, op1, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, localDef))));
							}
						}

						if(use2 != null) {
							Local localDef = getTrackingDef(newUnits, op2, useStmt);
							if(localDef != null) {
								tracked = true;
								newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, localDef))));
							}
						}

						if(!tracked) {
							newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, NullConstant.v()))));
						}

					} else {
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(localTmpDouble1, localTmpDouble2, NullConstant.v()))));
					}

				} else if(TestFul.DEBUG) TestFul.debug("Unknown operand type: " + type + " (" + type.getClass().getName() + ") / " + op2.getType() + " (" + op2.getType().getClass().getName() + ")");

				newUnits.add(Jimple.v().newGotoStmt(after));
			}

			// done
			newUnits.add(after);


			current = null;
		}

		public void process(Chain<Unit> newUnits, LookupSwitchStmt u) {
			final soot.Value key = u.getKey();

			final DataUse use = handleUse(newUnits, key, u);
			ConditionSwitch c = new ConditionSwitch(current.getId(), getValue(getRealUse(key, u, false)),  use);
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
				c.addCase(value, edge);
				add(edge, u.getTarget(i));
				keyBranchId.put(value, edge.getId());

				Unit target = Jimple.v().newNopStmt();
				targets.add(target);
				keyTarget.put(value, target);
			}

			Unit defaultTarget = Jimple.v().newNopStmt();
			Integer defaultBranchId;
			{
				EdgeConditional edge = new EdgeConditional(current, c);
				c.setDefaultCase(edge);
				add(edge, u.getDefaultTarget());
				defaultBranchId = edge.getId();
			}

			Unit lastNop = Jimple.v().newNopStmt();

			newUnits.add(Jimple.v().newLookupSwitchStmt(key, lookupValues, targets, defaultTarget));
			processSwitch(newUnits, u, key, keys, keyTarget, keyBranchId, defaultTarget, defaultBranchId, lastNop);
			newUnits.add(lastNop);

			current = null;
		}

		public void process(Chain<Unit> newUnits, TableSwitchStmt u) {
			final soot.Value key = u.getKey();
			final int lIndex = u.getLowIndex();
			final int hIndex = u.getHighIndex();

			DataUse use = handleUse(newUnits, key, u);
			ConditionSwitch c = new ConditionSwitch(current.getId(), getValue(getRealUse(key, u, false)), use);
			current.setCondition(c);

			int[] keys = new int[hIndex-lIndex+1];
			List<Unit> targets = new ArrayList<Unit>();
			SortedMap<Integer, Unit> keyTarget = new TreeMap<Integer, Unit>();
			Map<Integer, Integer> keyBranchId = new HashMap<Integer, Integer>();

			for(int idx = 0; idx <= hIndex-lIndex; idx++) {
				final int value = idx+lIndex;
				keys[idx] = value;

				EdgeConditional edge = new EdgeConditional(current, c);
				c.addCase(value, edge);
				add(edge, u.getTarget(idx));
				keyBranchId.put(value, edge.getId());

				Unit target = Jimple.v().newNopStmt();
				targets.add(target);
				keyTarget.put(value, target);
			}

			Unit defaultTarget = Jimple.v().newNopStmt();
			Integer defaultBranchId;
			{
				EdgeConditional edge = new EdgeConditional(current, c);
				c.setDefaultCase(edge);
				add(edge, u.getDefaultTarget());
				defaultBranchId = edge.getId();
			}

			Unit lastNop = Jimple.v().newNopStmt();

			newUnits.add(Jimple.v().newTableSwitchStmt(key, u.getLowIndex(), u.getHighIndex(), targets, defaultTarget));
			processSwitch(newUnits, u, key, keys, keyTarget, keyBranchId, defaultTarget, defaultBranchId, lastNop);
			newUnits.add(lastNop);

			current = null;
		}

		private void processSwitch(Chain<Unit> newUnits, Unit switchUnit, soot.Value keyValue, int[] keys, SortedMap<Integer, Unit> keyTarget, Map<Integer, Integer> keyBranchId, Unit defaultTarget, int defaultBranchId, Unit lastNop) {

			for(final int key : keys) {
				final Unit start = keyTarget.get(key);
				final int branchId = keyBranchId.get(key);

				newUnits.add(start);
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackBranch.makeRef(), IntConstant.v(branchId))));

				// get the definition of the key
				final Local keyDef;
				if(config.getDataFlowCoverage().isPUse())
					keyDef = getTrackingDef(newUnits, keyValue, switchUnit);
				else
					keyDef = null;

				// track P-Uses
				if(keyDef != null)
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackPUse.makeRef(), IntConstant.v(branchId), keyDef)));


				// track distance

				// [0..i..n-1] i-th target; [n] -> default target;
				IntConstant[] ctlookupValues = new IntConstant[keys.length + 1];
				Unit[] ctTargets = new Unit[keys.length + 1];
				for(int i = 0; i < keys.length; i++) {
					ctlookupValues[i] = IntConstant.v(keyBranchId.get(keys[i]));
					ctTargets[i] = Jimple.v().newNopStmt();
				}
				ctlookupValues[keys.length] =  IntConstant.v(defaultBranchId);
				ctTargets[keys.length] = Jimple.v().newNopStmt();

				newUnits.add(Jimple.v().newLookupSwitchStmt(localConditionTarget, Arrays.asList(ctlookupValues), Arrays.asList(ctTargets), lastNop));

				for(int i = 0; i < keys.length; i++) {
					final int ctKey = keys[i];
					newUnits.add(ctTargets[i]);

					if(ctKey == key) // branch executed!
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1.0), (keyDef == null ? NullConstant.v() : keyDef))));
					else
						newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(Math.abs(ctKey - key)), (keyDef == null ? NullConstant.v() : keyDef))));

					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}

				{ // handle default target
					newUnits.add(ctTargets[keys.length]);
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(switchDistanceToDefault(key, keyBranchId.keySet())), (keyDef == null ? NullConstant.v() : keyDef))));
					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}
			}

			// manage default target
			{
				newUnits.add(defaultTarget);
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackBranch.makeRef(), IntConstant.v(defaultBranchId))));

				// get the definition of the key
				final Local keyDef;
				if(config.getDataFlowCoverage().isPUse())
					keyDef = getTrackingDef(newUnits, keyValue, switchUnit);
				else
					keyDef = null;

				// track P-Uses
				if(keyDef != null)
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackPUse.makeRef(), IntConstant.v(defaultBranchId), keyDef)));

				// track distance
				IntConstant[] ctlookupValues = new IntConstant[keys.length + 1];
				Unit[] ctTargets = new Unit[keys.length + 1];
				for(int i = 0; i < keys.length; i++) {
					ctlookupValues[i] = IntConstant.v(keyBranchId.get(keys[i]));
					ctTargets[i] = Jimple.v().newNopStmt();
				}
				ctlookupValues[keys.length] =  IntConstant.v(defaultBranchId);
				ctTargets[keys.length] = Jimple.v().newNopStmt();

				newUnits.add(Jimple.v().newLookupSwitchStmt(localConditionTarget, Arrays.asList(ctlookupValues), Arrays.asList(ctTargets), lastNop));

				for(int i = 0; i < keys.length; i++) {
					final int ctKey = keys[i];
					newUnits.add(ctTargets[i]);
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, Jimple.v().newCastExpr(keyValue, DoubleType.v())));
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, calculateConditionTargetDistance.makeRef(), Arrays.asList(DoubleConstant.v(ctKey), localTmpDouble1, (keyDef == null ? NullConstant.v() : keyDef)))));
					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}

				{ // handle default target
					newUnits.add(ctTargets[keys.length]);
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance.makeRef(), DoubleConstant.v(-1), (keyDef == null ? NullConstant.v() : keyDef))));
					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}
			}

		}

		private Local getTrackingDef(Chain<Unit> newUnits, soot.Value v, Unit u) {

			v = getRealUse(v, u, true);
			if(v == null) return null;

			if(v instanceof Local)
				return getTrackingLocal((Local) v);

			if(v instanceof InstanceFieldRef) {

				try {
					InstanceFieldRef fr = (InstanceFieldRef) v;
					SootField field = fr.getField();
					SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

					newUnits.add(Jimple.v().newAssignStmt(localDataAccessD, Jimple.v().newInstanceFieldRef(fr.getBase(), tracker.makeRef())));

					return localDataAccessD;

				} catch (RuntimeException e) {
					logger.log(Level.WARNING, "Tracking field not found: " + e.getMessage(), e);
				}

			}

			if (v instanceof StaticFieldRef) {

				try {
					StaticFieldRef fr = (StaticFieldRef) v;
					SootField field = fr.getField();
					SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

					newUnits.add(Jimple.v().newAssignStmt(localDataAccessD, Jimple.v().newStaticFieldRef(tracker.makeRef())));

					return localDataAccessD;

				} catch (RuntimeException e) {
					logger.log(Level.WARNING, "Tracking field not found: " + e.getMessage(), e);
				}

			}


			return null;
		}

		private int switchDistanceToDefault(int key, Set<Integer> keySet) {
			if(!keySet.contains(key)) return 0;

			int d = 1;
			while(keySet.contains(key-d) && keySet.contains(key + d)) d++;

			return d;
		}

		public void process(Chain<Unit> newUnits, RetStmt u) {
			if(config.isContext())
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new soot.Value[] { IntConstant.v(start.getId()) }))));

			EdgeDirect.create(current, end);
			current = null;
		}

		public void process(Chain<Unit> newUnits, ReturnStmt u) {
			if(config.isContext())
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new soot.Value[] { IntConstant.v(start.getId()) }))));

			EdgeDirect.create(current, end);
			current = null;
		}

		public void process(Chain<Unit> newUnits, ReturnVoidStmt u) {
			if(config.isContext())
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new soot.Value[] { IntConstant.v(start.getId()) }))));

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
		 * Returns the Data for a given soot.Value.
		 * For uses, it is possible to specify the current Unit and verify if the soot.Value is defined as the copy of a field.
		 * @param value the soot.Value
		 * @return the Data for the soot.Value
		 */
		private Data getData(soot.Value value) {

			if(value == null) return null;
			if(value instanceof Constant) return null;
			if(value instanceof Expr) return null;
			if(value.equals(localThis)) return null;

			if(value instanceof Local) {
				Local local = (Local) value;

				Data ret = localRepository.get(local);
				if(ret == null) {
					ret = new Data(null, false);
					localRepository.put(local, ret);
				}
				return ret;
			}

			if(value instanceof FieldRef) return Factory.singleton.get(((FieldRef) value).getField());

			if(value instanceof ArrayRef) return getData(((ArrayRef) value).getBase());

			return null;
		}

		public Value getValue(soot.Value v) {

			if (v instanceof soot.jimple.Constant) {

				if(v instanceof NullConstant) return new Constant(null);
				if(v instanceof IntConstant) return new Constant(((IntConstant) v).value);
				if(v instanceof LongConstant) return new Constant(((LongConstant) v).value);
				if(v instanceof FloatConstant) return new Constant(((FloatConstant) v).value);
				if(v instanceof DoubleConstant) return new Constant(((DoubleConstant) v).value);
				if(v instanceof StringConstant) return null;

				if(TestFul.DEBUG) TestFul.debug("Unknown constant: " + v.getClass().getName());
				return null;
			}

			if(v instanceof Local || v instanceof FieldRef)
				return getData(v);

			if(v instanceof Expr) return null;

			if(v instanceof ArrayRef) return null;

			if(TestFul.DEBUG) TestFul.debug("Unknown Value type: " + v + " (" + v.getClass().getName() + ")");
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

				if(useToInstrument(stmt)) {
					for(soot.ValueBox useBox : stmt.getUseBoxes()) {
						handleUse(newUnits, useBox.getValue(), stmt);
					}
				}
			}
		}

		private boolean useToInstrument(Unit u) {
			if(u instanceof AssignStmt) {
				@SuppressWarnings("unchecked")
				final List<UnitValueBoxPair> uses = duAnalysis.getUsesOf(u);
				final AssignStmt a = (AssignStmt) u;
				final soot.Value leftOp = a.getLeftOp();
				final soot.Value rightOp = a.getRightOp();

				// Skipping field uses to define temporary variables
				if(leftOp instanceof Local && rightOp instanceof FieldRef && uses.size() == 1) {

					Unit use = uses.get(0).getUnit();
					if(duAnalysis.getDefsOfAt((Local) leftOp, use).size() == 1) {
						logger.fine("Skipped use of " + rightOp + ": it is assigned to the temporary local " + leftOp + ".");
						return false;
					}
				}

				// skipping floating-point number comparisons
				if(leftOp instanceof Local && leftOp.getType() instanceof ByteType &&
						(rightOp instanceof CmplExpr || rightOp instanceof CmpExpr || rightOp instanceof CmpgExpr) &&
						uses.size() == 1) {

					Unit use = uses.get(0).getUnit();
					if(use instanceof IfStmt) {
						logger.fine("Skipped use of " + rightOp + ": it is used as temporary soot.Value (" + leftOp + ") for floating-point comparison {" + use + "}.");
						return false;
					}
				}

				// skipping casts assignments
				if(leftOp instanceof Local && rightOp instanceof CastExpr) {
					logger.fine("Skipped use of " + rightOp + ": it is just a cast of the soot.Value " + leftOp + ".");
					return false;
				}
			}

			return true;
		}

		private soot.Value getRealUse(soot.Value v, Unit u, boolean skipTrivial) {
			if(v == null) return null;

			if(v.equals(localThis) ||
					v instanceof Constant ||
					v instanceof Expr)
				return skipTrivial ? null : v;

			if(u == null) return v;

			if(v instanceof Local) {

				List<Unit> reachingDefs = duAnalysis.getDefsOfAt((Local) v, u);

				if(reachingDefs.size() < 1) {
					if(skipTrivial) return null;
					return v;
				}

				// if the use has only one reaching definition, I can skip its tracking or check if it is a temporary variable
				if(reachingDefs.size() == 1) {

					// check if it is a temporary variable used to access a field
					Unit def = reachingDefs.get(0);

					if(def instanceof AssignStmt) {
						AssignStmt a = (AssignStmt)def;
						soot.Value rightOp = a.getRightOp();

						if((rightOp instanceof FieldRef) && duAnalysis.getUsesOf(def).size() == 1) {
							logger.fine("Found temporary local " + v + " tracking uses of " + rightOp + " instead");
							return getRealUse(rightOp, def, skipTrivial);

						} else if(rightOp instanceof CastExpr) {
							soot.Value op = ((CastExpr) rightOp).getOp();
							logger.fine("Found cast " + u + ": tracking uses of " + op + " instead");
							return getRealUse(op, def, skipTrivial);

						}
					}

					if(skipTrivial) {
						logger.fine("Skipping instrumentation of use of " + v + " in " + u + ": only 1 reachable def-use");
						return null;
					}

					return v;
				}
			}

			return v;
		}

		/**
		 * Insert the instrumentation code to track the use
		 * @param newUnits the new chain of operations
		 * @param u the unit in which the variable is used.
		 * 			This information is used to enable advanced controls (null to disable them).
		 * @param v the variable to track
		 */
		private DataUse handleUse(Chain<Unit> newUnits, soot.Value v, Stmt u) {

			if(config.getDataFlowCoverage() == DataFlowCoverage.DISABLED) return null;

			if(v == null) return null;
			if(v.getType() instanceof ArrayType) {

				// check if the all array is used (e.g., return array;)
				boolean trackArrayUse = false;

				if(u instanceof ReturnStmt) {
					trackArrayUse = v.equals(((ReturnStmt) u).getOp());
				}

				if(u.containsInvokeExpr()) {
					InvokeExpr invoke = u.getInvokeExpr();
					for (int i = 0; !trackArrayUse && i < invoke.getArgCount(); i++) {
						if(invoke.getArg(i).equals(v)) trackArrayUse = true;
					}
				}

				if(trackArrayUse) {
					if(TestFul.DEBUG && !(v instanceof Local)) TestFul.debug("handleArrayUse: " + v + " (" + u + ") was supposed to be a Local!");

					Data data = getData(v);
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
									manageArrayDefUses.makeRef(),
									getTrackingLocal((Local)v),
									localDataAccessU)));
				}

				return null;
			}

			v = getRealUse(v, u, true);
			if(v == null) return null;

			if(v instanceof Local) {

				Data data = getData(v);
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

				Data data = getData(v);
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
					if(TestFul.DEBUG) TestFul.debug("Tracking field not found", e);
				}

				return use;

			} else if (v instanceof StaticFieldRef) {

				Data data = getData(v);
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
					if(TestFul.DEBUG) TestFul.debug("Tracking field not found", e);
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
			if(config.getDataFlowCoverage() == DataFlowCoverage.DISABLED) return ;

			final soot.Value leftOp = stmt.getLeftOp();
			soot.Value rightOp = stmt.getRightOp();

			//TBD: optimize du tracking for arrays
			if(leftOp instanceof Local && !(leftOp.getType() instanceof ArrayType)) {

				// Consider the uses of the current definitions: if they do not have any additional definition, then it is possible to skip the du tracking
				boolean oneDef = true;
				@SuppressWarnings("unchecked")
				List<UnitValueBoxPair> usesOf = duAnalysis.getUsesOf(stmt);
				for (UnitValueBoxPair unit : usesOf) {
					if(duAnalysis.getDefsOfAt((Local) leftOp, unit.getUnit()).size() > 1) {
						oneDef = false;
						break;
					}
				}
				if(oneDef) {
					logger.fine("Skipping instrumentation of the definition of " + leftOp + " in " + stmt + ": it is the only definition for all of its uses");
					return;
				}
			}

			final DataDef dataDef = new DataDef(current, getData(leftOp), getValue(rightOp));
			manageDefs(dataDef);

			// calculate the definition ID
			soot.Value dataDefValue;
			if(leftOp.getType() instanceof ArrayType) {
				ArrayType type = (ArrayType) leftOp.getType();

				if(rightOp instanceof CastExpr) {
					CastExpr castExpr = (CastExpr) rightOp;

					// if it is a cast between arrays with the same dimensions, use the casted variable
					if(castExpr.getOp().getType() instanceof ArrayType &&
							((ArrayType)castExpr.getOp().getType()).numDimensions == type.numDimensions) {
						rightOp = castExpr.getOp();

					} else {
						// if it is a cast like this: Object[] arr = (Object []) obj;

						Local tmpObj = getTmpObject();
						Local tr = getTrackingLocal((Local) leftOp);

						newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newCastExpr(castExpr.getOp(), objectType)));
						newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newVirtualInvokeExpr(localTracker, arrayAssignmentDef.makeRef(), tmpObj, IntConstant.v(dataDef.getId()))));
						newUnits.add(Jimple.v().newAssignStmt(tr, Jimple.v().newCastExpr(tmpObj, tr.getType())));
						return;
					}
				}

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
					soot.Value base = arrayRef.getBase();
					soot.Value index = arrayRef.getIndex();

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

				} else if (rightOp instanceof NullConstant) {
					//dataDefValue = NullConstant.v(); //TBD: is better but generates bad code (Bad use of array type)
					return;

				} else {
					if(TestFul.DEBUG) TestFul.debug("Unable to handle array definition: " + rightOp + " (" + rightOp.getClass().getName() + ") in " + stmt);

					return;
				}
			} else if(leftOp instanceof Local || leftOp instanceof InstanceFieldRef || leftOp instanceof StaticFieldRef) {
				dataDefValue =  Jimple.v().newVirtualInvokeExpr(localTracker, getDataAccess.makeRef(), IntConstant.v(dataDef.getId()));
			} else if(leftOp instanceof ArrayRef) {
				dataDefValue = localDataAccessD;
				newUnits.add(Jimple.v().newAssignStmt(localDataAccessD, Jimple.v().newVirtualInvokeExpr(localTracker, getDataAccess.makeRef(), IntConstant.v(dataDef.getId()))));
			} else {
				if(TestFul.DEBUG) TestFul.debug("Unable to handle: " + leftOp + " - " + leftOp.getClass().getName());
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
				soot.Value base = arrayRef.getBase();
				soot.Value index = arrayRef.getIndex();

				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(getTrackingLocal((Local) base), index), dataDefValue));
			}
		}

		public void processPost(Chain<Unit> newUnits, Stmt stmt) {
			if(config.getDataFlowCoverage() != DataFlowCoverage.DISABLED && (stmt instanceof AssignStmt)) {
				AssignStmt u = (AssignStmt) stmt;
				if(u.getLeftOp().getType() instanceof ArrayType && u.getRightOp() instanceof InvokeExpr) {
					Local leftOp = (Local) u.getLeftOp();
					Data data = getData(leftOp);

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
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackerBasicBlock.makeRef(), Arrays.asList(new soot.Value[] { IntConstant.v(current.getId()) }))));

			done.put(stmt, current);

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
