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
import soot.jimple.DoubleConstant;
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
import soot.util.Chain;
import testful.coverage.Instrumenter;
import testful.utils.Skip;
import testful.utils.SootUtils;

public class WhiteInstrumenter implements Instrumenter.UnifiedInstrumentator {

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
	private static final SootMethod setConditionTargetDistance1;
	/** SootMethod representation of TrackerWhiteBox.setConditionTargetDistance(double, double) */
	private static final SootMethod setConditionTargetDistance2;
	/** SootMethod representation of TrackerWhiteBox.setConditionTargetDistance(double, double) */
	private static final SootMethod setConditionTargetDistance3;

	/** SootMethod representation of TrackerWhiteBox.trackCall(int) */
	private static final SootMethod trackCall;
	/** SootMethod representation of TrackerWhiteBox.trackReturn(int) */
	private static final SootMethod trackReturn;
	
	/** SootMethod representation of TrackerWhiteBox.getDataAccess(int, boolean) */
	private static final SootMethod getDataAccess;
	
	/** SootMethod representation of TrackerWhiteBox.manageDefUse(DataAccess, int, boolean) */
	private static final SootMethod manageDefUse;

	static {
		Scene.v().loadClassAndSupport(Object.class.getCanonicalName());
		objectClass  = Scene.v().getSootClass(Object.class.getCanonicalName());
		objectType = objectClass.getType();
		
		Scene.v().loadClassAndSupport(Exception.class.getCanonicalName());
		exceptionClass = Scene.v().getSootClass(Exception.class.getCanonicalName());
		Scene.v().loadClassAndSupport(RuntimeException.class.getCanonicalName());
		runtimeExceptionClass = Scene.v().getSootClass(RuntimeException.class.getCanonicalName());

		Scene.v().loadClassAndSupport(DefExposer.class.getCanonicalName());
		defExposerClass = Scene.v().getSootClass(DefExposer.class.getCanonicalName());

		Scene.v().loadClassAndSupport(DataAccess.class.getCanonicalName());
		dataAccess = Scene.v().getSootClass(DataAccess.class.getCanonicalName());
		
		final String TRACKER = TrackerWhiteBox.class.getCanonicalName();
		Scene.v().loadClassAndSupport(TRACKER);
		trackerClass = Scene.v().getSootClass(TRACKER);
		trackerSingleton = trackerClass.getMethodByName("getTracker");

		trackerBasicBlock = trackerClass.getMethodByName("trackBasicBlock");

		trackBranch = trackerClass.getMethodByName("trackBranch");
		getConditionTargetId = trackerClass.getMethodByName("getConditionTargetId");
		setConditionTargetDistance1 = trackerClass.getMethodByName("setConditionTargetDistance1");
		setConditionTargetDistance2 = trackerClass.getMethodByName("setConditionTargetDistance2");
		setConditionTargetDistance3 = trackerClass.getMethodByName("setConditionTargetDistance3");

		trackCall = trackerClass.getMethodByName("trackCall");
		trackReturn = trackerClass.getMethodByName("trackReturn");
		
		getDataAccess = trackerClass.getMethodByName("getDataAccess");
		manageDefUse = trackerClass.getMethodByName("manageDefUse");
		
	}

	public static final WhiteInstrumenter singleton = new WhiteInstrumenter();
	private WhiteInstrumenter() { }

	private Analyzer analyzer;

	private Local localTracker;
	private Local localConditionTarget;
	private Local localTmpDouble1;
	private Local localTmpDouble2;
	private Local localDataAccessD;
	private Local localDataAccessU;

	/** key: original variable; value: tracking variable */
	private Map<Local, Local> trackingLocals;
	
	@Override
	public void init(Chain<Unit> newUnits, Body newBody, Body oldBody, boolean classWithContracts, boolean contractMethod) {
		// generate tracking locals
		trackingLocals = new HashMap<Local, Local>();
		for(Local l : newBody.getLocals())
			if((newBody.getMethod().isStatic() || newBody.getThisLocal() != l) && 
					!l.getName().equals("__throwable_exc__")) 
				trackingLocals.put(l, Jimple.v().newLocal(getTracker(l.getName()), dataAccess.getType()));
		
		for(Local l : trackingLocals.values())
			newBody.getLocals().add(l);

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

		localDataAccessD = Jimple.v().newLocal("__testful_white_data_access_1__", dataAccess.getType());
		newBody.getLocals().add(localDataAccessD);

		localDataAccessU = Jimple.v().newLocal("__testful_white_data_access_2__", dataAccess.getType());
		newBody.getLocals().add(localDataAccessU);

		analyzer = new Analyzer(newUnits, clazz, newBody, contractMethod, newBody.getTraps(), oldBody.getTraps());

		//TBD: gli array!!!!

		//TODO: configurazioni ( defs e nulls)
	}

	@Override
	public void preprocess(SootClass sClass) {
		// def-use preprocessing: adding tracking fields and __testful_get_defs__ method
		
		if(sClass.implementsInterface(DefExposer.class.getCanonicalName())) return;
		
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
			
			List<SootField> fields = new ArrayList<SootField>();
			for(SootField f : sClass.getFields())
				if(SootUtils.isReference(f.getType()))
					fields.add(f);

			units.add(Jimple.v().newAssignStmt(ret, Jimple.v().newNewArrayExpr(objectType, IntConstant.v(fields.size()))));

			int i = 0;
			for(SootField f : fields) {
				units.add(Jimple.v().newAssignStmt(tmp, Jimple.v().newInstanceFieldRef(_this, f.makeRef())));
				units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(ret, IntConstant.v(i++)), tmp));
			}
			
			units.add(Jimple.v().newReturnStmt(ret));
		}

		
		// generate GET_DEFS
		{
			SootMethod getDefs = new SootMethod(DefExposer.GET_DEFS, Arrays.asList(new Type[] {}), ArrayType.v(dataAccess.getType(), 1), Modifier.PUBLIC );
			sClass.addMethod(getDefs);
			getDefs.addTag(Skip.s);
			
			JimpleBody body = Jimple.v().newBody(getDefs);
			PatchingChain<Unit> units = body.getUnits();
			getDefs.setActiveBody(body);
	
			Local _this = Jimple.v().newLocal("_this", sClass.getType()); 
			body.getLocals().add(_this);
	
			Local ret = Jimple.v().newLocal("ret", ArrayType.v(dataAccess.getType(), 1));
			body.getLocals().add(ret);
	
			Local tmp = Jimple.v().newLocal("tmp", dataAccess.getType());
			body.getLocals().add(tmp);
	
			units.add(Jimple.v().newIdentityStmt(_this, new ThisRef(sClass.getType())));
	
			List<SootField> fields = new ArrayList<SootField>();
			for(SootField f : sClass.getFields())
				fields.add(f);

			units.add(Jimple.v().newAssignStmt(ret, Jimple.v().newNewArrayExpr(dataAccess.getType(), IntConstant.v(fields.size()))));
			
			int i = 0;
			for(SootField f : fields) {
				
				int modifiers = f.getModifiers();
				if(Modifier.isFinal(modifiers)) modifiers -= Modifier.FINAL;

				SootField trackField = new SootField(getTracker(f.getName()), dataAccess.getType(), modifiers);
				sClass.addField(trackField);

				if(Modifier.isStatic(modifiers)) {
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
	public void processPostExc(Chain<Unit> newUnits, Stmt op, Local exception) {
		analyzer.processPostExc(newUnits, op);
	}

	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) {
		analyzer.exceptional(newUnits, exc);
	}

	@Override
	public void done(File baseDir, String cutName) {
		AnalysisWhiteBox sa = new AnalysisWhiteBox();

		for(BlockClass c : Factory.singleton.getClasses())
			try {
				c.performDataFlowAnalysis();
				sa.addClass(c);

				PrintWriter writer = new PrintWriter(new File(baseDir, c.getName().replace('.', File.separatorChar) + ".dot"));
				writer.println(c.getDot());
				writer.close();
			} catch(FileNotFoundException e) {
				System.err.println("Cannot create the class diagram: " + e);
			}

			sa.write(baseDir, cutName);
	}

	class Analyzer {

		private final Factory factory;

		/** the local that stores this */
		private final Local localThis;


		/** the begin of the current method */
		private final BlockFunctionEntry start;

		/** the end of the current method */
		private final BlockFunctionExit end;

		/** mark blocks belonging to the method being analyzed */
		private final BitSet blocks;

		/** mark conditions belonging to the method being analyzed */
		private final BitSet conditions;

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

		/** traps for the current block (unchecked excpetions) */
		private final Set<Trap> uncheckedExceptionHandlers;

		public Analyzer(Chain<Unit> newUnits, BlockClass clazz, Body newBody, boolean contract, Collection<Trap> newTraps, Collection<Trap> oldTraps) {
			final SootMethod method = newBody.getMethod();
			newBody.getTraps();
			final String methodName = method.getName();
			final boolean methodStatic = method.isStatic();
			final boolean methodPublic = method.isPublic();
			final boolean methodPrivate = method.isPrivate();

			factory = Factory.singleton;
			localRepository = new HashMap<Local, Data>();
			blocks = contract ? clazz.blocksContract : clazz.blocksCode;
			conditions = contract ? clazz.conditionsContract : clazz.conditionsCode;
			done = new HashMap<Unit, Block>();
			toLinkMap = new HashMap<Unit, Set<Edge>>();
			deadCode = new HashSet<Unit>();
			defs = new HashSet<DataDef>();
			uses = new HashSet<DataUse>();

			this.oldTraps = oldTraps;
			activeTraps = new LinkedList<Trap>();
			uncheckedExceptionHandlers = new HashSet<Trap>();

			start = new BlockFunctionEntry(clazz, methodName, methodPublic, contract);
			Block paramInit = new BlockBasic(defs, uses);
			new EdgeDirect(start).setTo(paramInit);
			toLink = new EdgeDirect(paramInit);
			end = new BlockFunctionExit(methodName, clazz);
			if(!methodStatic && !methodPrivate) {
				EdgeDirect.create(clazz, start);
				EdgeDirect.create(end, clazz);
			}

			if(methodStatic) localThis = null;
			else localThis = newBody.getThisLocal();
			
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackCall.makeRef(), Arrays.asList(new Value[] { IntConstant.v(start.getId()) }))));
			
			int nParams = method.getParameterCount();
			for(int i = 0; i < nParams; i++) {
				final Local p = newBody.getParameterLocal(i);
				manageDefs(new DataDef(start, get(p, true), null));
				addTracking(newUnits, p);
			}
		}

		private void addTracking(Chain<Unit> newUnits, final Local p) {
			newUnits.add(Jimple.v().newAssignStmt(
					trackingLocals.get(p), 
					Jimple.v().newVirtualInvokeExpr(localTracker, getDataAccess.makeRef(), IntConstant.v(0), IntConstant.v(1))));
		}

		public void exceptional(Chain<Unit> newUnits, Local exc) {
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
				; // do nothing
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
			else
				System.err.println("WARNING: cannot analyze " + op + " (" + op.getClass().getCanonicalName() + ")");
		}

		public void process(Chain<Unit> newUnits, AssignStmt u) {
			Value leftOp = u.getLeftOp();
			Data data = get(leftOp);

			if(data != null) {
				Serializable value = null;
				if(u.getRightOp() instanceof IntConstant) value = ((IntConstant) u.getRightOp()).value;
				else if(u.getRightOp() instanceof LongConstant) value = ((LongConstant) u.getRightOp()).value;
				else if(u.getRightOp() instanceof FloatConstant) value = ((FloatConstant) u.getRightOp()).value;
				else if(u.getRightOp() instanceof DoubleConstant) value = ((DoubleConstant) u.getRightOp()).value;

				final DataDef def = new DataDef(current, data, value);
				manageDefs(def);
				handleDef(newUnits, leftOp, def);
			}
		}

		public void process(Chain<Unit> newUnits, GotoStmt u) {
			add(new EdgeDirect(current), u.getTarget());
			current = null;
		}

		public void process(Chain<Unit> newUnits, IdentityStmt u) {
			// this method is invoked to store an exception (first statement in catch blocks)
			final DataDef def = new DataDef(current, get(u.getLeftOp()), null);
			manageDefs(def);
			handleDef(newUnits, u.getLeftOp(), def);
		}

		public void process(Chain<Unit> newUnits, IfStmt u) {
			DataUse use1 = null;
			DataUse use2 = null;

			if(!(u.getCondition() instanceof ConditionExpr)) {
				System.err.println("Unknown condition: " + u.getCondition() + " (" + u.getCondition().getClass().getCanonicalName() + ")");
				current = null;
				return;
			}

			ConditionExpr expr = (ConditionExpr) u.getCondition();

			Value op1 = expr.getOp1();
			Value op2 = expr.getOp2();
			Type type = op1.getType();

			{
				Data dop1 = get(op1);
				if(dop1 != null) {
					use1 = new DataUse(current, dop1, defs);
					uses.add(use1);
				}

				Data dop2 = get(op2);
				if(dop2 != null) {
					use2 = new DataUse(current, dop2, defs);
					uses.add(use2);
				}
			}

			ConditionIf c = new ConditionIf(use1, use2, expr.toString());
			current.setCondition(c);

			EdgeConditional trueBranch = new EdgeConditional(current, c);
			c.setTrueBranch(trueBranch);
			add(trueBranch, u.getTarget());
			conditions.set(trueBranch.getId());

			EdgeConditional falseBranch = new EdgeConditional(current, c);
			c.setFalseBranch(falseBranch);
			toLink = falseBranch;
			conditions.set(falseBranch.getId());

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
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance1.makeRef(), DoubleConstant.v(1))));

				} else if(type instanceof IntegerType || type instanceof LongType || type instanceof FloatType || type instanceof DoubleType) {
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, Jimple.v().newCastExpr(op1, DoubleType.v())));
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2, Jimple.v().newCastExpr(op2, DoubleType.v())));
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance3.makeRef(), localTmpDouble1, localTmpDouble2)));

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

					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance3.makeRef(), localTmpDouble1, localTmpDouble2)));

				} else System.err.println("Unknown operand type: " + type + " (" + type.getClass().getCanonicalName() + ") / " + op2.getType() + " (" + op2.getType().getClass().getCanonicalName() + ")");


				newUnits.add(Jimple.v().newGotoStmt(after));

				// calculate distance (false)
				newUnits.add(nop);
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(falseBranch.getId())), after));
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance1.makeRef(), DoubleConstant.v(0))));
				newUnits.add(Jimple.v().newGotoStmt(after));
			}

			newUnits.add(handleTrue);

			{ // handle true
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackBranch.makeRef(), IntConstant.v(trueBranch.getId()))));

				// calculate distance (true)
				Unit nop = Jimple.v().newNopStmt();
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(trueBranch.getId())), nop));
				newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance1.makeRef(), DoubleConstant.v(0))));
				newUnits.add(Jimple.v().newGotoStmt(after));

				// calculate distance (false)
				newUnits.add(nop);
				newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(localConditionTarget, IntConstant.v(falseBranch.getId())), after));

				if(type instanceof BooleanType) {
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance1.makeRef(), DoubleConstant.v(1))));

				} else if(type instanceof IntegerType || type instanceof LongType || type instanceof FloatType || type instanceof DoubleType) {
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble1, Jimple.v().newCastExpr(op1, DoubleType.v())));
					newUnits.add(Jimple.v().newAssignStmt(localTmpDouble2, Jimple.v().newCastExpr(op2, DoubleType.v())));
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance3.makeRef(), localTmpDouble1, localTmpDouble2)));

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

					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance3.makeRef(), localTmpDouble1, localTmpDouble2)));

				} else System.err.println("Unknown operand type: " + type + " (" + type.getClass().getCanonicalName() + ") / " + op2.getType() + " (" + op2.getType().getClass().getCanonicalName() + ")");

				newUnits.add(Jimple.v().newGotoStmt(after));
			}

			// done
			newUnits.add(after);


			current = null;
		}


		public void process(Chain<Unit> newUnits, LookupSwitchStmt u) {
			final Value key = u.getKey();

			DataUse use = new DataUse(current, get(key), defs);
			handleUse(newUnits, key, use);
			uses.add(use);
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
				conditions.set(edge.getId());
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
				conditions.set(edge.getId());
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

			DataUse use = new DataUse(current, get(key), defs);
			handleUse(newUnits, key, use);
			uses.add(use);
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
				conditions.set(edge.getId());
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
				conditions.set(edge.getId());
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
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance1.makeRef(), DoubleConstant.v(Math.abs(ctKey - key)))));
					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}

				{ // handle default target
					newUnits.add(ctTargets[keys.length]);
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance1.makeRef(), DoubleConstant.v(switchDistanceToDefault(key, keyBranchId.keySet())))));
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
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance2.makeRef(), DoubleConstant.v(ctKey), localTmpDouble1)));
					newUnits.add(Jimple.v().newGotoStmt(lastNop));
				}

				{ // handle default target
					newUnits.add(ctTargets[keys.length]);
					newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, setConditionTargetDistance1.makeRef(), DoubleConstant.v(0))));
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
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new Value[] { IntConstant.v(start.getId()) }))));

			EdgeDirect.create(current, end);
			current = null;
		}

		public void process(Chain<Unit> newUnits, ReturnStmt u) {
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackReturn.makeRef(), Arrays.asList(new Value[] { IntConstant.v(start.getId()) }))));

			EdgeDirect.create(current, end);
			current = null;
		}

		public void process(Chain<Unit> newUnits, ReturnVoidStmt u) {
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
				System.err.println("ERROR: block starting from " + w + " seems dead!");
		}

		private Data get(Local l, boolean param) {
			if(l == localThis) return null;

			Data ret = localRepository.get(l);

			if(ret == null) {
				ret = Factory.getData(null, l.getType(), param);
				localRepository.put(l, ret);
			}

			return ret;
		}

		private Data get(Value value) {
			if(value instanceof Local) return get((Local) value, false);

			if(value instanceof FieldRef) return factory.get(((FieldRef) value).getField());

			if(value instanceof ArrayRef) return get(((ArrayRef) value).getBase());

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

		private void manageDefs(DataDef def) {
			Data data = def.getData();

			Iterator<DataDef> iter = defs.iterator();
			while(iter.hasNext())
				if(iter.next().getData() == data) iter.remove();

			defs.add(def);
		}

		/**
		 * Performs a preliminary analysis on the statement.
		 */
		private void preProcess(Chain<Unit> newUnits, Stmt stmt) {
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
					Value v = use.getValue();
					Data d = get(v);
					if(d != null) {
						final DataUse dataUse = new DataUse(current, d, defs);
						uses.add(dataUse);

						handleUse(newUnits, v, dataUse);
					}
				}
			}

			// update oldTraps: add new oldTraps
			for(Trap t : oldTraps)
				if(t.getBeginUnit() == stmt) activeTraps.addLast(t);

			// update unchecked oldTraps
			for(Trap t : activeTraps) {
				SootClass exc = t.getException();
				if(!SootUtils.isAssignable(exceptionClass, exc) || SootUtils.isAssignable(runtimeExceptionClass, exc))
					uncheckedExceptionHandlers.add(t);
			}
		}

		private void handleUse(Chain<Unit> newUnits, Value v, final DataUse dataUse) {
			
			if(v instanceof Local) {
				
				newUnits.add(Jimple.v().newAssignStmt(
						localDataAccessU, 
						Jimple.v().newVirtualInvokeExpr(
								localTracker, 
								getDataAccess.makeRef(), 
								IntConstant.v(dataUse.getId()), 
								IntConstant.v(0))));

				newUnits.add(Jimple.v().newInvokeStmt(
						Jimple.v().newVirtualInvokeExpr(
								localTracker, 
								manageDefUse.makeRef(), 
								trackingLocals.get(v), 
								localDataAccessU)));
				
			} else if(v instanceof InstanceFieldRef) {
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
								IntConstant.v(dataUse.getId()), 
								IntConstant.v(1))));

				newUnits.add(Jimple.v().newInvokeStmt(
						Jimple.v().newVirtualInvokeExpr(
								localTracker, 
								manageDefUse.makeRef(), 
								localDataAccessD, 
								localDataAccessU)));
				
			} else if (v instanceof StaticFieldRef) {
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
								IntConstant.v(dataUse.getId()), 
								IntConstant.v(1))));

				newUnits.add(Jimple.v().newInvokeStmt(
						Jimple.v().newVirtualInvokeExpr(
								localTracker, 
								manageDefUse.makeRef(), 
								localDataAccessD, 
								localDataAccessU)));
			}
		}

		private void handleDef(Chain<Unit> newUnits, Value v, final DataDef dataDef) {
			
			if(v instanceof Local) {
				
				newUnits.add(Jimple.v().newAssignStmt(
						trackingLocals.get(v), 
						Jimple.v().newVirtualInvokeExpr(
								localTracker, 
								getDataAccess.makeRef(), 
								IntConstant.v(dataDef.getId()), 
								IntConstant.v(0))));
				
			} else if(v instanceof InstanceFieldRef) {
				InstanceFieldRef fr = (InstanceFieldRef) v;
				SootField field = fr.getField();
				SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

				newUnits.add(Jimple.v().newAssignStmt(
						localDataAccessD, 
						Jimple.v().newVirtualInvokeExpr(
								localTracker, 
								getDataAccess.makeRef(), 
								IntConstant.v(dataDef.getId()), 
								IntConstant.v(1))));

				newUnits.add(Jimple.v().newAssignStmt(
						Jimple.v().newInstanceFieldRef(fr.getBase(), tracker.makeRef()),
						localDataAccessD
						));

				
			} else if (v instanceof StaticFieldRef) {
				StaticFieldRef fr = (StaticFieldRef) v;
				SootField field = fr.getField();
				SootField tracker = field.getDeclaringClass().getFieldByName(getTracker(field.getName()));

				newUnits.add(Jimple.v().newAssignStmt(
						localDataAccessD, 
						Jimple.v().newVirtualInvokeExpr(
								localTracker, 
								getDataAccess.makeRef(), 
								IntConstant.v(dataDef.getId()), 
								IntConstant.v(1))));

				newUnits.add(Jimple.v().newAssignStmt(
						Jimple.v().newStaticFieldRef(tracker.makeRef()),
						localDataAccessD
						));
			}
		}

		public void processPostExc(Chain<Unit> newUnits, Stmt stmt) {
		}

		public void processPost(Chain<Unit> newUnits, Stmt stmt) {
			if(stmt.containsInvokeExpr()) {
				toLink = new EdgeDirect(current);
				current = null;
			}

			// update oldTraps: remove expired oldTraps
			Iterator<Trap> iterTraps = activeTraps.iterator();
			while(iterTraps.hasNext())
				if(iterTraps.next().getEndUnit() == stmt) iterTraps.remove();
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

			// for each unchecked trap, create an edge!
			for(Trap t : uncheckedExceptionHandlers)
				add(new EdgeExceptional(current, t.getException().getJavaStyleName()), t.getHandlerUnit());
			uncheckedExceptionHandlers.clear();

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
