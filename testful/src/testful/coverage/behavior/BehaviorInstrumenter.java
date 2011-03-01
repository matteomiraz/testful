/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011  Matteo Miraz
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

package testful.coverage.behavior;

import java.util.Arrays;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

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
import soot.jimple.IdentityStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.NullConstant;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.jimple.StringConstant;
import soot.util.Chain;
import testful.IConfigProject;
import testful.coverage.Launcher.ConfigInstrumenter;
import testful.coverage.soot.Instrumenter.UnifiedInstrumentator;
import testful.model.xml.Parser;
import testful.model.xml.XmlClass;
import testful.model.xml.XmlMethod;
import testful.model.xml.XmlMethod.Kind;

public class BehaviorInstrumenter implements UnifiedInstrumentator {

	private static final Logger logger = Logger.getLogger("testful.coverage.instrumenter.behavioral");

	/** this local contains a copy of the tracker */
	private static final String LOCAL_TRACKER = "__testful_behavioral_tracker__";

	private static final SootClass object;
	/** java.lang.Boolean.valueOf() */
	private static final SootMethod boxBoolean;
	/** java.lang.Byte.valueOf() */
	private static final SootMethod boxByte;
	/** java.lang.Character.valueOf() */
	private static final SootMethod boxChar;
	/** java.lang.Short.valueOf() */
	private static final SootMethod boxShort;
	/** java.lang.Integer.valueOf() */
	private static final SootMethod boxInt;
	/** java.lang.Long.valueOf() */
	private static final SootMethod boxLong;
	/** java.lang.Float.valueOf() */
	private static final SootMethod boxFloat;
	/** java.lang.Double.valueOf() */
	private static final SootMethod boxDouble;

	private final static SootClass abstractionState;

	private final static SootClass abstractionMethod;

	/** testful.coverage.behavior.BehaviorTracker */
	private final static SootClass trackerClass;

	/** public static BehaviorTracker getTracker() */
	private final static SootMethod trackerSingleton;

	/** public void add(Abstraction pre, AbstractionMethod partition, Abstraction post) */
	private final static SootMethod trackerAdd;

	/** public Abstraction abstractState(Object _this); */
	private final static SootMethod abstractState;

	/** public AbstractionMethod abstractMethod(String className, Object _this, String methodName, Object[] params); */
	private final static SootMethod abstractMethod;

	static {
		Scene.v().loadClassAndSupport(Object.class.getName());
		object = Scene.v().getSootClass(Object.class.getName());

		Scene.v().loadClassAndSupport(Boolean.class.getName());
		boxBoolean = Scene.v().getSootClass(Boolean.class.getName()).getMethod("valueOf", Arrays.asList(new Type[] { BooleanType.v() }));
		Scene.v().loadClassAndSupport(Character.class.getName());
		boxChar = Scene.v().getSootClass(Character.class.getName()).getMethod("valueOf", Arrays.asList(new Type[] { CharType.v() }));
		Scene.v().loadClassAndSupport(Byte.class.getName());
		boxByte = Scene.v().getSootClass(Byte.class.getName()).getMethod("valueOf", Arrays.asList(new Type[] { ByteType.v() }));
		Scene.v().loadClassAndSupport(Short.class.getName());
		boxShort = Scene.v().getSootClass(Short.class.getName()).getMethod("valueOf", Arrays.asList(new Type[] { ShortType.v() }));
		Scene.v().loadClassAndSupport(Integer.class.getName());
		boxInt = Scene.v().getSootClass(Integer.class.getName()).getMethod("valueOf", Arrays.asList(new Type[] { IntType.v() }));
		Scene.v().loadClassAndSupport(Long.class.getName());
		boxLong = Scene.v().getSootClass(Long.class.getName()).getMethod("valueOf", Arrays.asList(new Type[] { LongType.v() }));
		Scene.v().loadClassAndSupport(Float.class.getName());
		boxFloat = Scene.v().getSootClass(Float.class.getName()).getMethod("valueOf", Arrays.asList(new Type[] { FloatType.v() }));
		Scene.v().loadClassAndSupport(Double.class.getName());
		boxDouble = Scene.v().getSootClass(Double.class.getName()).getMethod("valueOf", Arrays.asList(new Type[] { DoubleType.v() }));

		Scene.v().loadClassAndSupport(BehaviorTracker.class.getName());
		trackerClass = Scene.v().getSootClass(BehaviorTracker.class.getName());
		trackerSingleton = trackerClass.getMethodByName("getTracker");
		trackerAdd = trackerClass.getMethodByName("add");
		abstractState = trackerClass.getMethodByName("abstractState");
		abstractMethod = trackerClass.getMethodByName("abstractMethod");

		Scene.v().loadClassAndSupport(Abstraction.class.getName());
		abstractionState = Scene.v().getSootClass(Abstraction.class.getName());
		Scene.v().loadClassAndSupport(AbstractionMethod.class.getName());
		abstractionMethod = Scene.v().getSootClass(AbstractionMethod.class.getName());
	}

	private final ConfigInstrumenter config;
	public BehaviorInstrumenter(ConfigInstrumenter config) {
		logger.config("White-Box instrumenter loaded");
		this.config = config;
	}

	private static enum MethodType {
		SKIP, STATIC, CONSTRUCTOR, METHOD
	}

	private MethodType methodType;

	private Local thisLocal;
	private Local localTracker;
	private Local pre;
	private Local post;
	private Local tmpObj;
	private Local partition;
	private Local params;

	@Override
	public void preprocess(SootClass sClass) { }

	/* (non-Javadoc)
	 * @see testful.coverage.soot.Instrumenter.UnifiedInstrumentator#init(soot.Body, soot.Body, soot.util.Chain, soot.jimple.IdentityStmt[])
	 */
	@Override
	public void init(Body oldBody, Body newBody, Chain<Unit> newUnits, IdentityStmt[] paramDefs) {
		final SootMethod method = newBody.getMethod();
		final String className = method.getDeclaringClass().getName();

		// check if the method must be skipped
		methodType = MethodType.SKIP;

		// If the behavioral coverage has been disabled, skip all the methods
		if(!config.isBehavioralCoverage()) return;

		// skip non-public methods
		if(!method.isPublic()) return;

		// for methods, read the xml descriptor
		if(!SootMethod.constructorName.equals(method.getName())) {
			// Check xml annotations
			XmlClass xmlClass = getXmlClass(className);
			if(xmlClass == null) {
				System.out.println("WARN: no xml description for class " + className);
				return;
			}

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
						System.err.println("ERROR: unexpected primitive type: " + t + " (" + t.getClass().getName() + ")");
						params[i] = null;
					}
				} else if(t instanceof RefType) params[i] = ((RefType) t).getClassName();
				else {
					System.err.println("ERROR: unexpected type: " + t + " (" + t.getClass().getName() + ")");
					params[i] = null;
				}
			}

			XmlMethod xmlMeth = xmlClass.getMethod(method.getName(), params);
			if(xmlMeth == null) System.out.println("WARN: no xml description for method " + className + "." + method.getName() + " " + Arrays.toString(params));
			else {
				if(xmlMeth.getKind() == Kind.SKIP) return;
				if(xmlMeth.getKind() == Kind.OBSERVER) return;
				if(xmlMeth.getKind() == Kind.PURE) return;
			}
		}

		// if the method must not be skipped
		methodType = method.isStatic() ?
				MethodType.STATIC :
					SootMethod.constructorName.equals(method.getName()) ? MethodType.CONSTRUCTOR : MethodType.METHOD;

		// some useful locals
		thisLocal = methodType == MethodType.STATIC ? null : newBody.getThisLocal();

		localTracker = Jimple.v().newLocal(LOCAL_TRACKER, trackerClass.getType());
		newBody.getLocals().add(localTracker);
		newUnits.add(Jimple.v().newAssignStmt(localTracker, Jimple.v().newStaticInvokeExpr(trackerSingleton.makeRef())));

		// track initial abstraction
		if(methodType != MethodType.STATIC) {
			pre = Jimple.v().newLocal("__testful_behavior_pre_abstraction__", abstractionState.getType());
			newBody.getLocals().add(pre);

			if(methodType == MethodType.METHOD)
				newUnits.add(Jimple.v().newAssignStmt(pre, Jimple.v().newVirtualInvokeExpr(localTracker, abstractState.makeRef(), thisLocal)));
			else // constructor
				newUnits.add(Jimple.v().newAssignStmt(pre, NullConstant.v()));

		} else pre = null;

		if(methodType != MethodType.STATIC) {
			post = Jimple.v().newLocal("__testful_behavior_post_abstraction__", abstractionState.getType());
			newBody.getLocals().add(post);
		} else post = null;

		// create params array
		params = Jimple.v().newLocal("__testful_behavior_params__", ArrayType.v(object.getType(), 1));
		newBody.getLocals().add(params);

		tmpObj = Jimple.v().newLocal("__testful_behavior_tmp_obj__", object.getType());
		newBody.getLocals().add(tmpObj);

		int nParams = method.getParameterCount();
		newUnits.add(Jimple.v().newAssignStmt(params, Jimple.v().newNewArrayExpr(object.getType(), IntConstant.v(nParams))));

		// fill params array
		for(int i = 0; i < nParams; i++) {
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
		}

		// call method abstraction
		partition = Jimple.v().newLocal("__testful_behavior_partition__", abstractionMethod.getType());
		newBody.getLocals().add(partition);

		newUnits.add(Jimple.v().newAssignStmt(
				partition,
				Jimple.v().newVirtualInvokeExpr(localTracker, abstractMethod.makeRef(),
						Arrays.asList(new Value[] { StringConstant.v(className), methodType == MethodType.METHOD ? thisLocal : NullConstant.v(), StringConstant.v(method.getName() + "(" + method.getBytecodeParms() + ")"), params }))));

		// if static, there is no pre and post state
		if(methodType == MethodType.STATIC) {
			newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackerAdd.makeRef(), Arrays.asList(new Value[] { NullConstant.v(), partition, NullConstant.v() }))));
		}
	}

	@Override
	public void processPre(Chain<Unit> newUnits, Stmt op) {
		if(methodType == MethodType.SKIP || methodType == MethodType.STATIC) return;

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
	public void exceptional(Chain<Unit> newUnits, Local exc) {
		if(methodType == MethodType.SKIP || methodType == MethodType.STATIC || methodType == MethodType.CONSTRUCTOR) return;

		// calculates post abstraction
		newUnits.add(Jimple.v().newAssignStmt(post, Jimple.v().newVirtualInvokeExpr(localTracker, abstractState.makeRef(), thisLocal)));

		// updates the behavioral coverage
		newUnits.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(localTracker, trackerAdd.makeRef(), Arrays.asList(new Value[] { pre, partition, post }))));
	}

	private XmlClass getXmlClass(String className) {
		try {
			return Parser.singleton.parse(config, className);
		} catch(JAXBException e) {
			System.err.println("ERROR: cannot read xml descriptor: " + e.getMessage());
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see testful.coverage.soot.Instrumenter.UnifiedInstrumentator#done(testful.IConfigProject)
	 */
	@Override
	public void done(IConfigProject config) { }
}
