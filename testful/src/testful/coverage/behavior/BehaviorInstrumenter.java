package testful.coverage.behavior;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.jmlspecs.jmlrac.runtime.JMLPreconditionError;

import soot.ArrayType;
import soot.Body;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.RefType;
import soot.Scene;
import soot.ShortType;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.util.Chain;
import testful.IConfigProject;
import testful.model.xml.Parser;
import testful.model.xml.XmlClass;
import testful.model.xml.XmlMethod;
import testful.model.xml.XmlMethod.Kind;
import testful.utils.Instrumenter;

public class BehaviorInstrumenter implements Instrumenter.UnifiedInstrumentator {

	private static BehaviorInstrumenter singleton;

	public static BehaviorInstrumenter getSingleton(IConfigProject config) {
		if(singleton == null)
			singleton = new BehaviorInstrumenter(config);

		return singleton;
	}

	/** this local contains a copy of the tracker */
	private static final String LOCAL_TRACKER = "__testful_behavioral_tracker__";

	private final SootClass jmlErrorClass;
	private final SootClass object;
	/** java.lang.Boolean.valueOf() */
	private final SootMethod boxBoolean;
	/** java.lang.Byte.valueOf() */
	private final SootMethod boxByte;
	/** java.lang.Character.valueOf() */
	private final SootMethod boxChar;
	/** java.lang.Short.valueOf() */
	private final SootMethod boxShort;
	/** java.lang.Integer.valueOf() */
	private final SootMethod boxInt;
	/** java.lang.Long.valueOf() */
	private final SootMethod boxLong;
	/** java.lang.Float.valueOf() */
	private final SootMethod boxFloat;
	/** java.lang.Double.valueOf() */
	private final SootMethod boxDouble;

	/** public Abstraction abstractState(Object _this); */
	private final SootMethod abstractState;

	/** public AbstractionMethod abstractMethod(String className, Object _this, String methodName, Object[] params); */
	private final SootMethod abstractMethod;

	private final SootClass abstractionState;
	private final SootClass abstractionMethod;

	private final String COVERAGE_TRACKER;
	private final SootClass trackerClass;
	private final SootMethod trackerSingleton;
	private final SootMethod trackerAdd;

	private final Map<String, XmlClass> xml;

	private final IConfigProject config;

	private BehaviorInstrumenter(IConfigProject config) {
		Scene.v().loadClassAndSupport(Object.class.getCanonicalName());
		object = Scene.v().getSootClass(Object.class.getCanonicalName());

		Scene.v().loadClassAndSupport(JMLPreconditionError.class.getCanonicalName());
		jmlErrorClass = Scene.v().getSootClass(JMLPreconditionError.class.getCanonicalName());

		Scene.v().loadClassAndSupport(Boolean.class.getCanonicalName());
		boxBoolean = Scene.v().getSootClass(Boolean.class.getCanonicalName()).getMethod("valueOf", Arrays.asList(new Type[] { BooleanType.v() }));
		Scene.v().loadClassAndSupport(Character.class.getCanonicalName());
		boxChar = Scene.v().getSootClass(Character.class.getCanonicalName()).getMethod("valueOf", Arrays.asList(new Type[] { CharType.v() }));
		Scene.v().loadClassAndSupport(Byte.class.getCanonicalName());
		boxByte = Scene.v().getSootClass(Byte.class.getCanonicalName()).getMethod("valueOf", Arrays.asList(new Type[] { ByteType.v() }));
		Scene.v().loadClassAndSupport(Short.class.getCanonicalName());
		boxShort = Scene.v().getSootClass(Short.class.getCanonicalName()).getMethod("valueOf", Arrays.asList(new Type[] { ShortType.v() }));
		Scene.v().loadClassAndSupport(Integer.class.getCanonicalName());
		boxInt = Scene.v().getSootClass(Integer.class.getCanonicalName()).getMethod("valueOf", Arrays.asList(new Type[] { IntType.v() }));
		Scene.v().loadClassAndSupport(Long.class.getCanonicalName());
		boxLong = Scene.v().getSootClass(Long.class.getCanonicalName()).getMethod("valueOf", Arrays.asList(new Type[] { LongType.v() }));
		Scene.v().loadClassAndSupport(Float.class.getCanonicalName());
		boxFloat = Scene.v().getSootClass(Float.class.getCanonicalName()).getMethod("valueOf", Arrays.asList(new Type[] { FloatType.v() }));
		Scene.v().loadClassAndSupport(Double.class.getCanonicalName());
		boxDouble = Scene.v().getSootClass(Double.class.getCanonicalName()).getMethod("valueOf", Arrays.asList(new Type[] { DoubleType.v() }));

		COVERAGE_TRACKER = BehaviorTracker.class.getCanonicalName();
		Scene.v().loadClassAndSupport(COVERAGE_TRACKER);

		trackerClass = Scene.v().getSootClass(COVERAGE_TRACKER);
		trackerSingleton = trackerClass.getMethodByName("getTracker");
		trackerAdd = trackerClass.getMethodByName("add");
		abstractState = trackerClass.getMethodByName("abstractState");
		abstractMethod = trackerClass.getMethodByName("abstractMethod");

		Scene.v().loadClassAndSupport(Abstraction.class.getCanonicalName());
		abstractionState = Scene.v().getSootClass(Abstraction.class.getCanonicalName());
		Scene.v().loadClassAndSupport(AbstractionMethod.class.getCanonicalName());
		abstractionMethod = Scene.v().getSootClass(AbstractionMethod.class.getCanonicalName());

		xml = new HashMap<String, XmlClass>();
		this.config = config;
	}


	private boolean toSkip;
	private boolean isStatic;

	private Local thisLocal;
	private Local localTracker;
	private Local pre;
	private Local post;
	private Local bool;
	private Local tmpObj;
	private Local partition;
	private Local params;

	@Override
	public void preprocess(SootClass sClass) { }

	@Override
	public void init(Chain<Unit> newUnits, Body newBody, Body oldBody, boolean classWithContracts, boolean contractMethod) {
		SootMethod method = newBody.getMethod();
		boolean cns = SootMethod.constructorName.equals(method.getName());
		String className = method.getDeclaringClass().getName();

		toSkip = checkSkip(method, cns, className);
		if(toSkip) return;

		isStatic = method.isStatic();

		thisLocal = (isStatic ? null : newBody.getThisLocal());

		// some useful locals
		localTracker = Jimple.v().newLocal(LOCAL_TRACKER, trackerClass.getType());
		newBody.getLocals().add(localTracker);
		newUnits.add(Jimple.v().newAssignStmt(localTracker, Jimple.v().newStaticInvokeExpr(trackerSingleton.makeRef())));

		pre = Jimple.v().newLocal("__testful_behavior_pre_abstraction__", abstractionState.getType());
		newBody.getLocals().add(pre);

		post = Jimple.v().newLocal("__testful_behavior_post_abstraction__", abstractionState.getType());
		newBody.getLocals().add(post);

		bool = Jimple.v().newLocal("__testful_behavior_bool_tmp__", BooleanType.v());
		newBody.getLocals().add(bool);

		tmpObj = Jimple.v().newLocal("__testful_behavior_obj_tmp__", object.getType());
		newBody.getLocals().add(tmpObj);

		partition = Jimple.v().newLocal("__testful_behavior_partition__", abstractionMethod.getType());
		newBody.getLocals().add(partition);

		params = Jimple.v().newLocal("__testful_behavior_params__", ArrayType.v(object.getType(), 1));
		newBody.getLocals().add(params);


		// track initial abstraction
		if(!isStatic) {
			if(cns) newUnits.add(Jimple.v().newAssignStmt(pre, NullConstant.v()));
			else newUnits.add(Jimple.v().newAssignStmt(pre, Jimple.v().newVirtualInvokeExpr(localTracker, abstractState.makeRef(), newBody.getThisLocal())));
		}

		// create params array
		int nParams = method.getParameterCount();
		newUnits.add(Jimple.v().newAssignStmt(params, Jimple.v().newNewArrayExpr(object.getType(), IntConstant.v(nParams))));

		// fill params array
		for(int i = 0; i < nParams; i++)
			if(BooleanType.v().equals(newBody.getParameterLocal(i).getType())) {
				newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newStaticInvokeExpr(boxBoolean.makeRef(), newBody.getParameterLocal(i))));
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(params, IntConstant.v(i)), tmpObj));
			} else if(CharType.v().equals(newBody.getParameterLocal(i).getType())) {
				newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newStaticInvokeExpr(boxChar.makeRef(), newBody.getParameterLocal(i))));
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(params, IntConstant.v(i)), tmpObj));
			} else if(ByteType.v().equals(newBody.getParameterLocal(i).getType())) {
				newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newStaticInvokeExpr(boxByte.makeRef(), newBody.getParameterLocal(i))));
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(params, IntConstant.v(i)), tmpObj));
			} else if(ShortType.v().equals(newBody.getParameterLocal(i).getType())) {
				newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newStaticInvokeExpr(boxShort.makeRef(), newBody.getParameterLocal(i))));
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(params, IntConstant.v(i)), tmpObj));
			} else if(IntType.v().equals(newBody.getParameterLocal(i).getType())) {
				newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newStaticInvokeExpr(boxInt.makeRef(), newBody.getParameterLocal(i))));
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(params, IntConstant.v(i)), tmpObj));
			} else if(LongType.v().equals(newBody.getParameterLocal(i).getType())) {
				newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newStaticInvokeExpr(boxLong.makeRef(), newBody.getParameterLocal(i))));
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(params, IntConstant.v(i)), tmpObj));
			} else if(FloatType.v().equals(newBody.getParameterLocal(i).getType())) {
				newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newStaticInvokeExpr(boxFloat.makeRef(), newBody.getParameterLocal(i))));
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(params, IntConstant.v(i)), tmpObj));
			} else if(DoubleType.v().equals(newBody.getParameterLocal(i).getType())) {
				newUnits.add(Jimple.v().newAssignStmt(tmpObj, Jimple.v().newStaticInvokeExpr(boxDouble.makeRef(), newBody.getParameterLocal(i))));
				newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(params, IntConstant.v(i)), tmpObj));
			} else newUnits.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(params, IntConstant.v(i)), newBody.getParameterLocal(i)));

		// call method abstraction
		newUnits.add(Jimple.v().newAssignStmt(
				partition,
				Jimple.v().newVirtualInvokeExpr(localTracker, abstractMethod.makeRef(),
						Arrays.asList(new Value[] { StringConstant.v(className), newBody.getThisLocal(), StringConstant.v(method.getName() + "(" + method.getBytecodeParms() + ")"), params }))));

		if(isStatic) {
			newUnits.add(Jimple.v().newAssignStmt(pre, NullConstant.v()));
			newUnits.add(Jimple.v().newAssignStmt(post, NullConstant.v()));
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackerAdd.makeRef(), Arrays.asList(new Value[] { pre, partition, post }))));
			toSkip = true;
		}
	}

	@Override
	public void processPre(Chain<Unit> newUnits, Stmt op) {
		if(toSkip) return;

		if((op instanceof ReturnStmt) || (op instanceof ReturnVoidStmt)) {
			// calculates post abstraction
			newUnits.add(Jimple.v().newAssignStmt(post, Jimple.v().newVirtualInvokeExpr(localTracker, abstractState.makeRef(), thisLocal)));

			// updates the behavioral coverage
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackerAdd.makeRef(), Arrays.asList(new Value[] { pre, partition, post }))));
		}
	}

	@Override
	public void processPost(Chain<Unit> newUnits, Stmt op) { }

	@Override
	public void processPostExc(Chain<Unit> newUnits, Stmt op, Local exception) { }

	@Override
	public void exceptional(Chain<Unit> newUnits, Local exc) {
		if(toSkip) return;

		// if(exc instanceof JML) skip behavioral coverage!
		Unit skip = Jimple.v().newNopStmt();

		newUnits.add(Jimple.v().newAssignStmt(bool, Jimple.v().newInstanceOfExpr(exc, jmlErrorClass.getType())));
		newUnits.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(bool, IntConstant.v(1)), skip));

		// calculates post abstraction
		newUnits.add(Jimple.v().newAssignStmt(post, Jimple.v().newVirtualInvokeExpr(localTracker, abstractState.makeRef(), thisLocal)));

		// updates the behavioral coverage
		newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackerAdd.makeRef(), Arrays.asList(new Value[] { pre, partition, post }))));

		// throw catched exception
		newUnits.add(skip);
	}

	private XmlClass getXmlClass(String className) {
		XmlClass xmlClass = xml.get(className);

		if(xmlClass == null) {
			try {
				xmlClass = Parser.singleton.parse(config, className);
				xml.put(className, xmlClass);
			} catch(JAXBException e) {
				System.err.println("ERROR: cannot read xml descriptor: " + e.getMessage());
			}
		}

		return xmlClass;
	}

	private boolean checkSkip(SootMethod method, boolean cns, String className) {
		// skip non-public methods
		if(!method.isPublic()) return true;

		// skip JML-related methods
		if(method.getDeclaringClass().implementsInterface(org.jmlspecs.jmlrac.runtime.JMLCheckable.class.getCanonicalName()) && method.getName().contains("$"))
			return true;

		// for methods, read the xml descriptor
		if(!cns) {
			// Check xml annotations
			XmlClass xmlClass = getXmlClass(className);
			if(xmlClass == null) System.out.println("WARN: no xml description for class " + className);
			else {

				// converts the params into an array of strings
				String[] params = new String[method.getParameterCount()];
				for(int i = 0; i < params.length; i++) {
					Type t = method.getParameterType(i);
					if(t instanceof PrimType) {
						if(t instanceof BooleanType) params[i] = "boolean";
						else if(t instanceof ByteType) params[i] = "byte";
						else if(t instanceof CharType) params[i] = "char";
						else if(t instanceof DoubleType) params[i] = "double";
						else if(t instanceof FloatType) params[i] = "float";
						else if(t instanceof IntType) params[i] = "int";
						else if(t instanceof LongType) params[i] = "long";
						else if(t instanceof ShortType) params[i] = "short";
						else {
							System.err.println("ERROR: unexpected primitive type: " + t + " (" + t.getClass().getCanonicalName() + ")");
							params[i] = null;
						}
					} else if(t instanceof RefType) params[i] = ((RefType) t).getClassName();
					else {
						System.err.println("ERROR: unexpected type: " + t + " (" + t.getClass().getCanonicalName() + ")");
						params[i] = null;
					}
				}

				XmlMethod xmlMeth = xmlClass.getMethod(method.getName(), params);
				if(xmlMeth == null) System.out.println("WARN: no xml description for method " + className + "." + method.getName() + " " + Arrays.toString(params));
				else
					if(xmlMeth.getKind() == Kind.OBSERVER) return true;
			}
		}

		return false;
	}


	@Override
	public void done(IConfigProject config, String cutName) {
	}
}
