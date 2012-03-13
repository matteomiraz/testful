/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2012 Matteo Miraz
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import soot.ArrayType;
import soot.IntType;
import soot.Local;
import soot.Modifier;
import soot.RefType;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.VoidType;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.InvokeExpr;
import soot.jimple.Jimple;
import soot.jimple.JimpleBody;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;
import soot.util.Chain;
import testful.model.faults.PreconditionViolationException;

/**
 * This class enables the support for arrays.
 * For each array (e.g., Foo[]), a support class (testful.FooArray) is created.
 * This class enables TestFul to create and modify the Foo array
 * @author matteo
 */
public class ArraySupport {

	private static final boolean CREATE_MERGE = false;
	private static final boolean CREATE_OBSERVERS = false;

	private static final SootClass java_lang_Object = Scene.v().getSootClass("java.lang.Object");
	private static final SootMethod java_lang_Object_init = java_lang_Object.getMethod(SootMethod.constructorName, Collections.EMPTY_LIST);

	private static final SootClass java_lang_System = Scene.v().getSootClass("java.lang.System");
	private static final SootMethod java_lang_System_arraycopy = java_lang_System.getMethodByName("arraycopy");

	static {
		Scene.v().loadClassAndSupport(PreconditionViolationException.Impl.class.getName());
	}
	private static final SootClass preconditionViolationException = Scene.v().getSootClass(PreconditionViolationException.Impl.class.getName());
	private static final SootMethod throwExceptionPreconditionViolationException = preconditionViolationException.getMethodByName("throwException");

	/**
	 * @param c the class to analyze
	 * @return the classes generated to enable the support for arrays
	 */
	public static List<String> addArraySupport(SootClass c) {
		if(c.isInterface()) return Collections.emptyList();

		List<String> supportClasses = new LinkedList<String>();
		for (SootMethod m : new LinkedList<SootMethod>(c.getMethods())) {
			if(!m.isPublic()) continue;
			if(m.isAbstract()) continue;

			/** the return type */
			final Type retOriginal = m.getReturnType();

			/** the type of the parameters */
			@SuppressWarnings("unchecked")
			final List<Type> paramsOriginal = m.getParameterTypes();

			if(!isArray(retOriginal, paramsOriginal)) continue;

			/** the return type (adapter) */
			final Type retAdapter = addArraySupportIfRequired(supportClasses, retOriginal);

			/** the type of the parameters (adapted) */
			final List<Type> paramsAdapter = new ArrayList<Type>(paramsOriginal.size());
			for (Type p : paramsOriginal) paramsAdapter.add(addArraySupportIfRequired(supportClasses, p));

			createMethodAdapter(c, m, retOriginal, paramsOriginal, retAdapter, paramsAdapter);
		}

		return supportClasses;
	}

	private static void createMethodAdapter(SootClass c, SootMethod m, final Type retOriginal, final List<Type> paramsOriginal, final Type retAdapter, final List<Type> paramsAdapter) {

		String name = m.getName();
		if(paramsAdapter.equals(paramsOriginal)) {
			name += "_testful";
		}

		SootMethod mAdapter = new SootMethod(name, paramsAdapter, retAdapter, m.getModifiers());
		c.addMethod(mAdapter);
		mAdapter.addTag(Skip.s);

		JimpleBody body = Jimple.v().newBody(mAdapter);
		Chain<Unit> units = body.getUnits();
		mAdapter.setActiveBody(body);

		/** this (if any) */
		final Local t;
		if(!m.isStatic()) {
			t = Jimple.v().newLocal("t", c.getType());
			body.getLocals().add(t);
			units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(c.getType())));
		} else t = null;

		/** Locals for original parameters. */
		final Local lParamsOriginal[] = new Local[paramsOriginal.size()];

		/** Locals for adapting parameters */
		final Local lParamsAdapter[] = new Local[paramsOriginal.size()];

		for (int i = 0; i < paramsOriginal.size(); i++) {

			Local p = Jimple.v().newLocal("ap" + i, paramsAdapter.get(i));
			body.getLocals().add(p);
			lParamsAdapter[i] = p;

			units.add(Jimple.v().newIdentityStmt(p, Jimple.v().newParameterRef(paramsAdapter.get(i), i)));

			if(paramsOriginal.get(i) instanceof ArrayType) {
				Local ap = Jimple.v().newLocal("op" + i, paramsOriginal.get(i));
				body.getLocals().add(ap);
				lParamsOriginal[i] = ap;
			} else lParamsOriginal[i] = p;
		}

		final Local lRetOriginal, lRetAdapter;
		if(m.getReturnType() instanceof VoidType) lRetAdapter = lRetOriginal = null;
		else {
			lRetAdapter = Jimple.v().newLocal("aRet", retAdapter);
			body.getLocals().add(lRetAdapter);

			if(retOriginal instanceof ArrayType) {
				lRetOriginal = Jimple.v().newLocal("oRet", retOriginal);
				body.getLocals().add(lRetOriginal);
			} else lRetOriginal = lRetAdapter;
		}

		for (int i = 0; i < lParamsOriginal.length; i++) {
			final Local lParamOriginal = lParamsOriginal[i];
			final Local lParamAdapter = lParamsAdapter[i];

			if(lParamOriginal == lParamAdapter) continue;

			SootMethodRef toArray = ((RefType)paramsAdapter.get(i)).getSootClass().getMethodByName("toArray").makeRef();

			NopStmt nop = Jimple.v().newNopStmt();
			units.add(Jimple.v().newAssignStmt(lParamOriginal, NullConstant.v()));
			units.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(lParamAdapter, NullConstant.v()), nop));
			units.add(Jimple.v().newAssignStmt(lParamOriginal, Jimple.v().newVirtualInvokeExpr(lParamAdapter, toArray)));
			units.add(nop);
		}

		final InvokeExpr invokeExpr;
		if(t == null) invokeExpr = Jimple.v().newStaticInvokeExpr(m.makeRef(), Arrays.asList(lParamsOriginal));
		else if(name.equals(SootMethod.constructorName)) invokeExpr = Jimple.v().newSpecialInvokeExpr(t, m.makeRef(), Arrays.asList(lParamsOriginal));
		else invokeExpr = Jimple.v().newVirtualInvokeExpr(t, m.makeRef(), Arrays.asList(lParamsOriginal));

		if(lRetOriginal == null) units.add(Jimple.v().newInvokeStmt(invokeExpr));
		else units.add(Jimple.v().newAssignStmt(lRetOriginal, invokeExpr));

		if(lRetAdapter == null) {
			units.add(Jimple.v().newReturnVoidStmt());
		} else {

			if(lRetOriginal != lRetAdapter) {

				RefType retType = (RefType) retAdapter;
				units.add(Jimple.v().newAssignStmt(lRetAdapter, Jimple.v().newNewExpr(retType)));

				SootMethod cns = retType.getSootClass().getMethod(SootMethod.constructorName, Arrays.asList(retOriginal));
				units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(lRetAdapter, cns.makeRef(), lRetOriginal)));
			}

			units.add(Jimple.v().newReturnStmt(lRetAdapter));
		}
	}

	private static boolean isArray(Type t1, List<Type> t2) {
		if(t1 instanceof ArrayType) return true;

		for (Type t : t2) {
			if(t instanceof ArrayType)
				return true;
		}

		return false;
	}

	private static Type addArraySupportIfRequired(List<String> supportClasses, final Type type) {
		if(!(type instanceof ArrayType)) return type;

		final String arraySupportName = getArrayName(type);
		if(!Scene.v().containsClass(arraySupportName)) {

			supportClasses.add(arraySupportName);

			final ArrayType arrayType = (ArrayType)type;
			final Type elementType = arrayType.getElementType();

			final SootClass arraySupport = new SootClass(arraySupportName, Modifier.PUBLIC);
			arraySupport.setSuperclass(java_lang_Object);
			Scene.v().addClass(arraySupport);
			final RefType supportType = arraySupport.getType();

			final SootField a = new SootField("array", arrayType, Modifier.PRIVATE);
			arraySupport.addField(a);

			SootMethod checkAccess = checkAccess(arrayType, arraySupport, supportType, a);

			constructor(arrayType, elementType, arraySupport, supportType, a);
			copyConstructor(arrayType, elementType, arraySupport, supportType, a);

			toArray(arrayType, arraySupport, supportType, a);

			if(CREATE_MERGE) merge(arrayType, elementType, arraySupport, supportType, a);

			addHead(arrayType, elementType, arraySupport, supportType, a);
			addTail(arrayType, elementType, arraySupport, supportType, a);

			if (CREATE_OBSERVERS) {
				getHead(arrayType, elementType, arraySupport, supportType, a, checkAccess);
				get(arrayType, elementType, arraySupport, supportType, a, checkAccess);
				getTail(arrayType, elementType, arraySupport, supportType, a, checkAccess);
			}

			setHead(arrayType, elementType, arraySupport, supportType, a, checkAccess);
			set(arrayType, elementType, arraySupport, supportType, a, checkAccess);
			setTail(arrayType, elementType, arraySupport, supportType, a, checkAccess);

			delHead(arrayType, elementType, arraySupport, supportType, a, checkAccess);
			delTail(arrayType, elementType, arraySupport, supportType, a, checkAccess);
		}

		return RefType.v(arraySupportName);
	}

	private static void delHead(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a, final SootMethod checkAccess) {
		SootMethod meth = new SootMethod("delHead", Collections.emptyList(), VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		/** newArray */
		Local na = Jimple.v().newLocal("na", arrayType);
		body.getLocals().add(na);

		/** t.array.length */
		Local tal = Jimple.v().newLocal("tal", IntType.v());
		body.getLocals().add(tal);

		/** newArray.length */
		Local nal = Jimple.v().newLocal("nal", IntType.v());
		body.getLocals().add(nal);

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));

		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(t, checkAccess.makeRef(), IntConstant.v(0))));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));
		units.add(Jimple.v().newAssignStmt(tal, Jimple.v().newLengthExpr(ta)));
		units.add(Jimple.v().newAssignStmt(nal, Jimple.v().newSubExpr(tal, IntConstant.v(1))));
		units.add(Jimple.v().newAssignStmt(na, Jimple.v().newNewArrayExpr(elementType, nal)));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(java_lang_System_arraycopy.makeRef(), Arrays.asList(ta, IntConstant.v(1), na, IntConstant.v(0), nal))));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(t, a.makeRef()), na));

		units.add(Jimple.v().newReturnVoidStmt());
	}

	private static void delTail(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a, final SootMethod checkAccess) {
		SootMethod meth = new SootMethod("delTail", Collections.emptyList(), VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		/** newArray */
		Local na = Jimple.v().newLocal("na", arrayType);
		body.getLocals().add(na);

		/** t.array.length */
		Local tal = Jimple.v().newLocal("tal", IntType.v());
		body.getLocals().add(tal);

		/** newArray.length */
		Local nal = Jimple.v().newLocal("nal", IntType.v());
		body.getLocals().add(nal);

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));

		final Unit op6 = Jimple.v().newAssignStmt(tal, Jimple.v().newLengthExpr(ta));
		final Unit op9 = Jimple.v().newAssignStmt(nal, Jimple.v().newSubExpr(tal, IntConstant.v(1)));

		units.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(ta, NullConstant.v()), op6));
		units.add(Jimple.v().newAssignStmt(tal, IntConstant.v(-1)));
		units.add(Jimple.v().newGotoStmt(op9));

		units.add(op6);

		units.add(op9);

		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(t, checkAccess.makeRef(), nal)));
		units.add(Jimple.v().newAssignStmt(na, Jimple.v().newNewArrayExpr(elementType, nal)));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(java_lang_System_arraycopy.makeRef(), Arrays.asList(ta, IntConstant.v(0), na, IntConstant.v(0), nal))));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(t, a.makeRef()), na));
		units.add(Jimple.v().newReturnVoidStmt());
	}

	private static void getHead(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a, final SootMethod checkAccess) {
		SootMethod meth = new SootMethod("getHead", Collections.emptyList(), elementType, Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** return value */
		Local v = Jimple.v().newLocal("v", elementType);
		body.getLocals().add(v);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));

		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(t, checkAccess.makeRef(), IntConstant.v(0))));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));
		units.add(Jimple.v().newAssignStmt(v, Jimple.v().newArrayRef(ta, IntConstant.v(0))));
		units.add(Jimple.v().newReturnStmt(v));
	}

	private static void get(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a, final SootMethod checkAccess) {
		SootMethod meth = new SootMethod("get", Arrays.asList(IntType.v()), elementType, Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** parameter */
		Local p = Jimple.v().newLocal("p", IntType.v());
		body.getLocals().add(p);

		/** return value */
		Local v = Jimple.v().newLocal("v", elementType);
		body.getLocals().add(v);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newIdentityStmt(p, Jimple.v().newParameterRef(IntType.v(), 0)));

		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(t, checkAccess.makeRef(), p)));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));
		units.add(Jimple.v().newAssignStmt(v, Jimple.v().newArrayRef(ta, p)));
		units.add(Jimple.v().newReturnStmt(v));
	}

	private static void getTail(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a, final SootMethod checkAccess) {
		SootMethod meth = new SootMethod("getTail", Collections.emptyList(), elementType, Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** return value */
		Local v = Jimple.v().newLocal("v", elementType);
		body.getLocals().add(v);

		/** position */
		Local p = Jimple.v().newLocal("p", IntType.v());
		body.getLocals().add(p);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));

		final Unit op6 = Jimple.v().newAssignStmt(p, Jimple.v().newLengthExpr(ta));
		final Unit op9 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(t, checkAccess.makeRef(), p));

		units.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(ta, NullConstant.v()), op6));
		units.add(Jimple.v().newAssignStmt(p, IntConstant.v(-1)));
		units.add(Jimple.v().newGotoStmt(op9));

		units.add(op6);
		units.add(Jimple.v().newAssignStmt(p, Jimple.v().newSubExpr(p, IntConstant.v(1))));

		units.add(op9);

		units.add(Jimple.v().newAssignStmt(v, Jimple.v().newArrayRef(ta, p)));
		units.add(Jimple.v().newReturnStmt(v));
	}

	private static void setHead(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a, final SootMethod checkAccess) {
		SootMethod meth = new SootMethod("setHead", Arrays.asList(elementType), VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** return value */
		Local v = Jimple.v().newLocal("v", elementType);
		body.getLocals().add(v);

		/** parameter */
		Local p = Jimple.v().newLocal("p", elementType);
		body.getLocals().add(p);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newIdentityStmt(p, Jimple.v().newParameterRef(elementType, 0)));

		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(t, checkAccess.makeRef(), IntConstant.v(0))));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(ta, IntConstant.v(0)), p));
		units.add(Jimple.v().newReturnVoidStmt());
	}

	private static void set(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a, final SootMethod checkAccess) {
		SootMethod meth = new SootMethod("set", Arrays.asList(elementType, IntType.v()), VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** parameter */
		Local p = Jimple.v().newLocal("p", elementType);
		body.getLocals().add(p);

		/** parameter */
		Local pos = Jimple.v().newLocal("pos", IntType.v());
		body.getLocals().add(pos);

		/** return value */
		Local v = Jimple.v().newLocal("v", elementType);
		body.getLocals().add(v);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newIdentityStmt(p, Jimple.v().newParameterRef(elementType, 0)));
		units.add(Jimple.v().newIdentityStmt(pos, Jimple.v().newParameterRef(IntType.v(), 1)));

		units.add(Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(t, checkAccess.makeRef(), pos)));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(ta, pos), p));
		units.add(Jimple.v().newReturnVoidStmt());
	}

	private static void setTail(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a, final SootMethod checkAccess) {
		SootMethod meth = new SootMethod("setTail", Arrays.asList(elementType), VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** return value */
		Local v = Jimple.v().newLocal("v", elementType);
		body.getLocals().add(v);

		/** parameter */
		Local p = Jimple.v().newLocal("p", elementType);
		body.getLocals().add(p);

		/** position */
		Local pos = Jimple.v().newLocal("pos", IntType.v());
		body.getLocals().add(pos);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newIdentityStmt(p, Jimple.v().newParameterRef(elementType, 0)));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));

		final Unit op6 = Jimple.v().newAssignStmt(pos, Jimple.v().newLengthExpr(ta));
		final Unit op9 = Jimple.v().newInvokeStmt(Jimple.v().newVirtualInvokeExpr(t, checkAccess.makeRef(), pos));

		units.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(ta, NullConstant.v()), op6));
		units.add(Jimple.v().newAssignStmt(pos, IntConstant.v(-1)));
		units.add(Jimple.v().newGotoStmt(op9));

		units.add(op6);
		units.add(Jimple.v().newAssignStmt(pos, Jimple.v().newSubExpr(pos, IntConstant.v(1))));

		units.add(op9);

		units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(ta, pos), p));
		units.add(Jimple.v().newReturnVoidStmt());
	}

	private static void addTail(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a) {

		SootMethod meth = new SootMethod("addTail", Arrays.asList(elementType), VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** parameter */
		Local p = Jimple.v().newLocal("p", elementType);
		body.getLocals().add(p);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		/** newArray */
		Local na = Jimple.v().newLocal("na", arrayType);
		body.getLocals().add(na);

		/** t.array.length */
		Local tal = Jimple.v().newLocal("tal", IntType.v());
		body.getLocals().add(tal);

		/** newArray.length */
		Local nal = Jimple.v().newLocal("nal", IntType.v());
		body.getLocals().add(nal);

		ReturnVoidStmt ret = Jimple.v().newReturnVoidStmt();

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newIdentityStmt(p, Jimple.v().newParameterRef(elementType, 0)));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));

		Unit u = Jimple.v().newAssignStmt(tal, Jimple.v().newLengthExpr(ta));

		units.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(ta, NullConstant.v()), u));
		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newNewArrayExpr(elementType, IntConstant.v(0))));

		units.add(u);

		units.add(Jimple.v().newAssignStmt(nal, Jimple.v().newAddExpr(tal, IntConstant.v(1))));
		units.add(Jimple.v().newAssignStmt(na, Jimple.v().newNewArrayExpr(elementType, nal)));

		units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(na, tal), p));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(java_lang_System_arraycopy.makeRef(), Arrays.asList(ta, IntConstant.v(0), na, IntConstant.v(0), tal))));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(t, a.makeRef()), na));

		units.add(ret);
	}

	private static void addHead(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a) {
		SootMethod meth = new SootMethod("addHead", Arrays.asList(elementType), VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** parameter */
		Local p = Jimple.v().newLocal("p", elementType);
		body.getLocals().add(p);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		/** newArray */
		Local na = Jimple.v().newLocal("na", arrayType);
		body.getLocals().add(na);

		/** t.array.length */
		Local tal = Jimple.v().newLocal("tal", IntType.v());
		body.getLocals().add(tal);

		/** newArray.length */
		Local nal = Jimple.v().newLocal("nal", IntType.v());
		body.getLocals().add(nal);

		ReturnVoidStmt ret = Jimple.v().newReturnVoidStmt();

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newIdentityStmt(p, Jimple.v().newParameterRef(elementType, 0)));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));

		Unit u = Jimple.v().newAssignStmt(tal, Jimple.v().newLengthExpr(ta));

		units.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(ta, NullConstant.v()), u));
		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newNewArrayExpr(elementType, IntConstant.v(0))));

		units.add(u);

		units.add(Jimple.v().newAssignStmt(nal, Jimple.v().newAddExpr(tal, IntConstant.v(1))));
		units.add(Jimple.v().newAssignStmt(na, Jimple.v().newNewArrayExpr(elementType, nal)));

		units.add(Jimple.v().newAssignStmt(Jimple.v().newArrayRef(na, IntConstant.v(0)), p));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(java_lang_System_arraycopy.makeRef(), Arrays.asList(ta, IntConstant.v(0), na, IntConstant.v(1), tal))));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(t, a.makeRef()), na));

		units.add(ret);
	}

	private static void merge(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a) {
		SootMethod meth = new SootMethod("merge", Arrays.asList(supportType), VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** parameter */
		Local p = Jimple.v().newLocal("p", supportType);
		body.getLocals().add(p);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		/** p.array */
		Local pa = Jimple.v().newLocal("pa", arrayType);
		body.getLocals().add(pa);

		/** newArray */
		Local na = Jimple.v().newLocal("na", arrayType);
		body.getLocals().add(na);

		/** t.array.length */
		Local tal = Jimple.v().newLocal("tal", IntType.v());
		body.getLocals().add(tal);

		/** p.array.length */
		Local pal = Jimple.v().newLocal("pal", IntType.v());
		body.getLocals().add(pal);

		/** newArray.length */
		Local nal = Jimple.v().newLocal("nal", IntType.v());
		body.getLocals().add(nal);


		ReturnVoidStmt ret = Jimple.v().newReturnVoidStmt();

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newIdentityStmt(p, Jimple.v().newParameterRef(supportType, 0)));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));
		units.add(Jimple.v().newAssignStmt(pa, Jimple.v().newInstanceFieldRef(p, a.makeRef())));

		IfStmt u = Jimple.v().newIfStmt(Jimple.v().newEqExpr(pa, NullConstant.v()), ret);

		units.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(ta, NullConstant.v()), u));
		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newNewArrayExpr(elementType, IntConstant.v(0))));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(t, a.makeRef()), ta));

		units.add(u);

		units.add(Jimple.v().newAssignStmt(tal, Jimple.v().newLengthExpr(ta)));
		units.add(Jimple.v().newAssignStmt(pal, Jimple.v().newLengthExpr(pa)));
		units.add(Jimple.v().newAssignStmt(nal, Jimple.v().newAddExpr(tal, pal)));
		units.add(Jimple.v().newAssignStmt(na, Jimple.v().newNewArrayExpr(elementType, nal)));

		units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(java_lang_System_arraycopy.makeRef(), Arrays.asList(ta, IntConstant.v(0), na, IntConstant.v(0), tal))));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(java_lang_System_arraycopy.makeRef(), Arrays.asList(pa, IntConstant.v(0), na, tal, pal))));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(t, a.makeRef()), na));

		units.add(ret);
	}

	private static void toArray(final ArrayType arrayType, final SootClass arraySupport, final RefType supportType, final SootField a) {
		SootMethod m = new SootMethod("toArray", Collections.emptyList(), arrayType, Modifier.PUBLIC);
		arraySupport.addMethod(m);

		JimpleBody body = Jimple.v().newBody(m);
		Chain<Unit> units = body.getUnits();
		m.setActiveBody(body);

		Local _this = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(_this);

		Local tmp = Jimple.v().newLocal("tmp", arrayType);
		body.getLocals().add(tmp);

		units.add(Jimple.v().newIdentityStmt(_this, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newAssignStmt(tmp, Jimple.v().newInstanceFieldRef(_this, a.makeRef())));
		units.add(Jimple.v().newReturnStmt(tmp));
	}

	private static void copyConstructor(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a) {
		SootMethod cns = new SootMethod(SootMethod.constructorName, Arrays.asList(arrayType), VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(cns);

		JimpleBody body = Jimple.v().newBody(cns);
		Chain<Unit> units = body.getUnits();
		cns.setActiveBody(body);

		Local _this = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(_this);

		Local p0 = Jimple.v().newLocal("p0", arrayType);
		body.getLocals().add(p0);

		units.add(Jimple.v().newIdentityStmt(_this, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newIdentityStmt(p0, Jimple.v().newParameterRef(arrayType, 0)));

		units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(_this, java_lang_Object_init.makeRef())));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(_this, a.makeRef()), p0));
		units.add(Jimple.v().newAssignStmt(p0, Jimple.v().newNewArrayExpr(elementType, IntConstant.v(0))));
		units.add(Jimple.v().newReturnVoidStmt());
	}

	private static void constructor(final ArrayType arrayType, final Type elementType, final SootClass arraySupport, final RefType supportType, final SootField a) {
		SootMethod cns = new SootMethod(SootMethod.constructorName, Collections.EMPTY_LIST, VoidType.v(), Modifier.PUBLIC);
		arraySupport.addMethod(cns);

		JimpleBody body = Jimple.v().newBody(cns);
		Chain<Unit> units = body.getUnits();
		cns.setActiveBody(body);

		Local _this = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(_this);

		/** newArray */
		Local na = Jimple.v().newLocal("na", arrayType);
		body.getLocals().add(na);

		units.add(Jimple.v().newIdentityStmt(_this, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newInvokeStmt(Jimple.v().newSpecialInvokeExpr(_this, java_lang_Object_init.makeRef())));

		units.add(Jimple.v().newAssignStmt(na, Jimple.v().newNewArrayExpr(elementType, IntConstant.v(0))));
		units.add(Jimple.v().newAssignStmt(Jimple.v().newInstanceFieldRef(_this, a.makeRef()), na));
		units.add(Jimple.v().newReturnVoidStmt());
	}

	private static SootMethod checkAccess(final ArrayType arrayType, final SootClass arraySupport, final RefType supportType, final SootField a) {
		SootMethod meth = new SootMethod("checkAccess", Arrays.asList(IntType.v()), VoidType.v(), Modifier.PRIVATE);
		arraySupport.addMethod(meth);

		JimpleBody body = Jimple.v().newBody(meth);
		Chain<Unit> units = body.getUnits();
		meth.setActiveBody(body);

		Local t = Jimple.v().newLocal("this", supportType);
		body.getLocals().add(t);

		/** parameter */
		Local p = Jimple.v().newLocal("p", IntType.v());
		body.getLocals().add(p);

		/** this.array */
		Local ta = Jimple.v().newLocal("ta", arrayType);
		body.getLocals().add(ta);

		/** t.array.length */
		Local tal = Jimple.v().newLocal("tal", IntType.v());
		body.getLocals().add(tal);

		Stmt ret = Jimple.v().newReturnVoidStmt();
		Stmt throwException = Jimple.v().newInvokeStmt(Jimple.v().newStaticInvokeExpr(throwExceptionPreconditionViolationException.makeRef()));

		units.add(Jimple.v().newIdentityStmt(t, Jimple.v().newThisRef(supportType)));
		units.add(Jimple.v().newIdentityStmt(p, Jimple.v().newParameterRef(IntType.v(), 0)));

		units.add(Jimple.v().newAssignStmt(ta, Jimple.v().newInstanceFieldRef(t, a.makeRef())));

		units.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(ta, NullConstant.v()), throwException));
		units.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(p, IntConstant.v(0)), throwException));

		units.add(Jimple.v().newAssignStmt(tal, Jimple.v().newLengthExpr(ta)));
		units.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(p, tal), ret));

		units.add(throwException);
		units.add(ret);

		return meth;
	}

	private static String getArrayName(Type t) {
		if(t instanceof ArrayType)
			return getArrayName(((ArrayType)t).getElementType()) + "Array";

		return "testful." + t.toString();
	}
}
