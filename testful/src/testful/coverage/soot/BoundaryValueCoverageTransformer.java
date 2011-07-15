/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
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

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import soot.Body;
import soot.BodyTransformer;
import soot.BooleanType;
import soot.ByteType;
import soot.CharType;
import soot.DoubleType;
import soot.FloatType;
import soot.IntType;
import soot.Local;
import soot.LongType;
import soot.PrimType;
import soot.Scene;
import soot.ShortType;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.CmpExpr;
import soot.jimple.CmpgExpr;
import soot.jimple.CmplExpr;
import soot.jimple.ConditionExpr;
import soot.jimple.DoubleConstant;
import soot.jimple.FloatConstant;
import soot.jimple.IfStmt;
import soot.jimple.IntConstant;
import soot.jimple.Jimple;
import soot.jimple.LongConstant;
import soot.jimple.NopStmt;
import testful.TestFul;

/**
 * Modify the bytecode of the class under test.
 * For each condition, this transformer introduces several "dummy" conditions to stress the boundary values.
 * 
 * See Pandita, Xie, Tillman, de Halleux, Guided Test Generation for Coverage Criteria.
 * 
 * @author matteo
 */
public class BoundaryValueCoverageTransformer extends BodyTransformer {

	public static final BoundaryValueCoverageTransformer singleton = new BoundaryValueCoverageTransformer();

	private static final SootMethod floatIsNan, doubleIsNan;

	// Checking for infinite has been disabled: it reduces the effectiveness of the other boundaries
	// private static final SootMethod floatIsInfinite, doubleIsInfinite;

	static {
		Scene.v().loadClassAndSupport(Float.class.getName());
		floatIsNan = Scene.v().getSootClass(Float.class.getName()).getMethod("isNaN", Arrays.asList(new Type[] { FloatType.v() }));
		// floatIsInfinite = Scene.v().getSootClass(Float.class.getName()).getMethod("isInfinite", Arrays.asList(new Type[] { FloatType.v() }));

		Scene.v().loadClassAndSupport(Double.class.getName());
		doubleIsNan = Scene.v().getSootClass(Double.class.getName()).getMethod("isNaN", Arrays.asList(new Type[] { DoubleType.v() }));
		// doubleIsInfinite = Scene.v().getSootClass(Double.class.getName()).getMethod("isInfinite", Arrays.asList(new Type[] { DoubleType.v() }));
	}

	private BoundaryValueCoverageTransformer() {}

	@Override
	protected void internalTransform(Body body, String phaseName, @SuppressWarnings("rawtypes") Map options) {

		AtomicInteger localProgressiveNumber = new AtomicInteger();

		Iterator<Unit> it = body.getUnits().snapshotIterator();
		while(it.hasNext()) {
			Unit u = it.next();

			if (u instanceof AssignStmt) {
				soot.Value leftOp = ((AssignStmt)u).getLeftOp();
				soot.Value rightOp = ((AssignStmt)u).getRightOp();

				if(leftOp instanceof Local  && leftOp.getType() instanceof ByteType &&
						(rightOp instanceof CmplExpr || rightOp instanceof CmpExpr || rightOp instanceof CmpgExpr)) {

					Unit w = it.next();
					if(w instanceof IfStmt) {
						ConditionExpr expr = (ConditionExpr) ((IfStmt) w).getCondition();

						if(expr.getOp1() == leftOp && expr.getOp2() instanceof IntConstant && ((IntConstant)expr.getOp2()).value == 0) {

							final soot.Value op1;
							final soot.Value op2;

							if(rightOp instanceof CmplExpr) {
								op1 = ((CmplExpr) rightOp).getOp1();
								op2 = ((CmplExpr) rightOp).getOp2();
							} else if(rightOp instanceof CmpExpr) {
								op1 = ((CmpExpr) rightOp).getOp1();
								op2 = ((CmpExpr) rightOp).getOp2();
							} else if(rightOp instanceof CmpgExpr) {
								op1 = ((CmpgExpr) rightOp).getOp1();
								op2 = ((CmpgExpr) rightOp).getOp2();
							} else {
								TestFul.debug("Unexpected Comparison operator: " + rightOp + " (" + rightOp.getClass() + ")");
								continue;
							}

							Local b;
							List<Unit> toInsert = new LinkedList<Unit>();
							final NopStmt nop = Jimple.v().newNopStmt();

							// checking NaN and Infinites for floats and doubles
							if (op1.getType() instanceof FloatType || op1.getType() instanceof DoubleType) {

								if (op1 instanceof Local && op2 instanceof Local) {

									final boolean isDouble = op1.getType() instanceof DoubleType;

									// if(!Double.isNan(op1)) goto nop1;
									final NopStmt nop1 = Jimple.v().newNopStmt();
									Local is1Nan = getLocal(body, localProgressiveNumber, BooleanType.v());

									toInsert.add(Jimple.v().newAssignStmt(is1Nan, Jimple.v().newStaticInvokeExpr(isDouble ? doubleIsNan.makeRef(): floatIsNan.makeRef(), op1)));
									toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(is1Nan, IntConstant.v(0)), nop1));

									//    if(Double.isNan(op2)) goto nop;
									Local is2Nan = getLocal(body, localProgressiveNumber, BooleanType.v());
									toInsert.add(Jimple.v().newAssignStmt(is2Nan, Jimple.v().newStaticInvokeExpr( isDouble ? doubleIsNan.makeRef() : floatIsNan.makeRef(), op2)));
									toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(is2Nan, IntConstant.v(0)), nop));
									//    else goto nop;
									toInsert.add(Jimple.v().newGotoStmt(nop));

									toInsert.add(nop1);

									// if(Double.isNan(op2)) goto nop;
									Local is2NanBis = getLocal(body, localProgressiveNumber, BooleanType.v());
									toInsert.add(Jimple.v().newAssignStmt(is2NanBis, Jimple.v().newStaticInvokeExpr(isDouble ? doubleIsNan.makeRef() : floatIsNan.makeRef(), op2)));
									toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(is2NanBis, IntConstant.v(0)), nop));

									//	// ----- checking if op1 and op2 are infinite
									//
									//	// if(!Double.isInfinite(op1)) goto o1f;
									//	final NopStmt o1f = Jimple.v().newNopStmt();
									//	Local is1Inf = getLocal(body, localProgressiveNumber, BooleanType.v());
									//	toInsert.add(Jimple.v().newAssignStmt(is1Inf, Jimple.v().newStaticInvokeExpr(isDouble ? doubleIsInfinite.makeRef() : floatIsInfinite.makeRef(), op1)));
									//	toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(is1Inf, IntConstant.v(0)), o1f));
									//
									//	//   -op1 is Inf- if(Double.isInfinite(op2)) goto nop;
									//	Local is2Inf = getLocal(body, localProgressiveNumber, BooleanType.v());
									//	toInsert.add(Jimple.v().newAssignStmt(is2Inf, Jimple.v().newStaticInvokeExpr(isDouble ? doubleIsInfinite.makeRef() : floatIsInfinite.makeRef(), op2)));
									//	toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(is2Inf, IntConstant.v(1)), nop));
									//	//   -op1 is Inf- else goto nop;
									//	toInsert.add(Jimple.v().newGotoStmt(nop));
									//
									//	toInsert.add(o1f);
									//
									//	//   -op1 is Finite- if(Double.isInfinite(op2)) goto nop;
									//	Local is2Inf2 = getLocal(body, localProgressiveNumber, BooleanType.v());
									//	toInsert.add(Jimple.v().newAssignStmt(is2Inf2, Jimple.v().newStaticInvokeExpr(isDouble ? doubleIsInfinite.makeRef() : floatIsInfinite.makeRef(), op2)));
									//	toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(is2Inf2, IntConstant.v(1)), nop));

								} else if (op1 instanceof Local || op2 instanceof Local) {
									final Local op = (Local) ((op1 instanceof Local) ? op1 : op2);
									final boolean isDouble = op.getType() instanceof DoubleType;

									// if(Double.isNan(op)) goto nop;
									Local isNan = getLocal(body, localProgressiveNumber, BooleanType.v());
									toInsert.add(Jimple.v().newAssignStmt(isNan, Jimple.v().newStaticInvokeExpr(isDouble ? doubleIsNan.makeRef() : floatIsNan.makeRef(), op)));
									toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(isNan, IntConstant.v(1)), nop));

									//	// if(Double.isInfinite(op)) goto otherChecks
									//	NopStmt nopOtherChecks = Jimple.v().newNopStmt();
									//	Local isInfinite = getLocal(body, localProgressiveNumber, BooleanType.v());
									//	toInsert.add(Jimple.v().newAssignStmt(isInfinite, Jimple.v().newStaticInvokeExpr(isDouble ? doubleIsInfinite.makeRef() : floatIsInfinite.makeRef(), op)));
									//	toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(isInfinite, IntConstant.v(0)), nopOtherChecks));
									//
									//	// if(op < 0) goto nop; // -inf
									//	b = getLocal(body, localProgressiveNumber, ByteType.v());
									//	toInsert.add(Jimple.v().newAssignStmt(b, Jimple.v().newCmplExpr(op, isDouble ? DoubleConstant.v(0) : FloatConstant.v(0))));
									//	toInsert.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(b, IntConstant.v(0)), nop));
									//
									//	// goto nop; // +inf
									//	toInsert.add(Jimple.v().newGotoStmt(nop));

									// not a NaN nor an Infinite: continue with the otherChecks
									// toInsert.add(nopOtherChecks);

								}
							}
							Local tmp = getLocal(body, localProgressiveNumber, op1.getType());
							toInsert.add(mutate(tmp, op2, -1));

							// op1 <  (op2-1)
							b = getLocal(body, localProgressiveNumber, ByteType.v());
							toInsert.add(Jimple.v().newAssignStmt(b, Jimple.v().newCmplExpr(op1, tmp)));
							toInsert.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(b, IntConstant.v(0)), nop));

							// op1 == (op2-1)
							b = getLocal(body, localProgressiveNumber, ByteType.v());
							toInsert.add(Jimple.v().newAssignStmt(b, Jimple.v().newCmplExpr(op1, tmp)));
							toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(b, IntConstant.v(0)), nop));

							// op1 <  op2
							b = getLocal(body, localProgressiveNumber, ByteType.v());
							toInsert.add(Jimple.v().newAssignStmt(b, Jimple.v().newCmplExpr(op1, op2)));
							toInsert.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(b, IntConstant.v(0)), nop));

							// op1 == op2
							b = getLocal(body, localProgressiveNumber, ByteType.v());
							toInsert.add(Jimple.v().newAssignStmt(b, Jimple.v().newCmplExpr(op1, op2)));
							toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(b, IntConstant.v(0)), nop));

							toInsert.add(mutate(tmp, op2, 1));

							// op1 <  (op2+1)
							b = getLocal(body, localProgressiveNumber, ByteType.v());
							toInsert.add(Jimple.v().newAssignStmt(b, Jimple.v().newCmplExpr(op1, tmp)));
							toInsert.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(b, IntConstant.v(0)), nop));

							// op1 == (op2+1)
							b = getLocal(body, localProgressiveNumber, ByteType.v());
							toInsert.add(Jimple.v().newAssignStmt(b, Jimple.v().newCmplExpr(op1, tmp)));
							toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(b, IntConstant.v(0)), nop));

							// the condition "if (op1 > (op2+1))" is skipped because
							// (1) it is exactly what is missing (hence it is implied from the previous expressions
							// (2) it would introduce an infeasible else branch

							toInsert.add(nop);

							body.getUnits().insertBefore(toInsert, u);

						} else TestFul.debug("Unexpected IF Unit " + w + " (after " + u + ")");
					} else TestFul.debug("Unexpected Unit " + w + " (after " + u + ")");
				}

			} else if(u instanceof IfStmt) {
				final ConditionExpr expr = (ConditionExpr) ((IfStmt) u).getCondition();
				final soot.Value op1 = expr.getOp1();
				final soot.Value op2 = expr.getOp2();

				// booleans and non-primitive types do not have boundaries ;)
				final Type type = op1.getType();
				if(!(type instanceof PrimType)) continue;
				if(type instanceof BooleanType) continue;
				if(type instanceof CharType) continue;

				List<Unit> toInsert = new LinkedList<Unit>();

				NopStmt nop = Jimple.v().newNopStmt();

				Local tmp = getLocal(body, localProgressiveNumber, type);

				toInsert.add(mutate(tmp, op2, -1));

				toInsert.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(op1, tmp), nop)); // op1 <  (op2-1)
				toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(op1, tmp), nop)); // op1 == (op2-1)

				toInsert.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(op1, op2), nop)); // op1 <  op2
				toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(op1, op2), nop)); // op1 == op2

				toInsert.add(mutate(tmp, op2, 1));

				toInsert.add(Jimple.v().newIfStmt(Jimple.v().newLtExpr(op1, tmp), nop)); // op1 <  (op2+1)
				toInsert.add(Jimple.v().newIfStmt(Jimple.v().newEqExpr(op1, tmp), nop)); // op1 == (op2+1)

				// impossible to take the false branch, hence it is removed
				// toInsert.add(Jimple.v().newIfStmt(Jimple.v().newGtExpr(op1, tmp), nop));

				toInsert.add(nop);

				body.getUnits().insertBefore(toInsert, u);
			}
		}
	}

	/**
	 * @param toInsert
	 * @param out
	 * @param src
	 */
	private Unit mutate(Local out, Value src, int amount) {
		final Type type = src.getType();

		if(type instanceof ByteType) return Jimple.v().newAssignStmt(out, Jimple.v().newAddExpr(src, IntConstant.v(amount)));
		if(type instanceof DoubleType) return Jimple.v().newAssignStmt(out, Jimple.v().newAddExpr(src, DoubleConstant.v(amount)));
		if(type instanceof FloatType) return Jimple.v().newAssignStmt(out, Jimple.v().newAddExpr(src, FloatConstant.v(amount)));
		if(type instanceof IntType) return Jimple.v().newAssignStmt(out, Jimple.v().newAddExpr(src, IntConstant.v(amount)));
		if(type instanceof LongType) return Jimple.v().newAssignStmt(out, Jimple.v().newAddExpr(src, LongConstant.v(amount)));
		if(type instanceof ShortType) return Jimple.v().newAssignStmt(out, Jimple.v().newAddExpr(src, IntConstant.v(amount)));

		TestFul.debug("unknown type: " + type);
		return null;
	}

	private static Local getLocal(Body b, AtomicInteger progressive, Type t) {
		Local local = Jimple.v().newLocal("__testful__BVC_" + progressive.incrementAndGet() + "__", t);
		b.getLocals().add(local);
		return local;
	}
}
