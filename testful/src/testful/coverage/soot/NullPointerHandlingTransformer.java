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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import soot.Body;
import soot.BodyTransformer;
import soot.Local;
import soot.RefLikeType;
import soot.Unit;
import soot.Value;
import soot.jimple.AssignStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.Jimple;
import soot.jimple.NopStmt;
import soot.jimple.NullConstant;

/**
 * @author matteo
 */
public class NullPointerHandlingTransformer extends BodyTransformer {

	public static final NullPointerHandlingTransformer singleton = new NullPointerHandlingTransformer();

	private NullPointerHandlingTransformer() {}

	@Override
	protected void internalTransform(Body body, String phaseName, @SuppressWarnings("rawtypes") Map options) {

		final Local thisLocal = body.getMethod().isStatic() ? null : body.getThisLocal();
		List<Unit> toInsertForParams = new LinkedList<Unit>();

		Iterator<Unit> it = body.getUnits().snapshotIterator();
		while(it.hasNext()) {
			Unit u = it.next();

			if(u instanceof IdentityStmt) {
				if(toInsertForParams != null && ((IdentityStmt)u).getLeftOp() != thisLocal)
					toInsertForParams.addAll(analyzeDef((DefinitionStmt) (u)));

			} else {
				if(toInsertForParams != null) {

					if(!toInsertForParams.isEmpty())
						body.getUnits().insertBefore(toInsertForParams, u);

					toInsertForParams = null;
				}

				if(u instanceof AssignStmt) {
					body.getUnits().insertAfter(analyzeDef((DefinitionStmt) u), u);
				}
			}
		}
	}

	/**
	 * @param def
	 * @return
	 */
	private List<Unit> analyzeDef(DefinitionStmt def) {
		Value v = def.getLeftOp();
		if(v instanceof Local && v.getType() instanceof RefLikeType) {

			List<Unit> toInsert = new LinkedList<Unit>();
			final NopStmt nop = Jimple.v().newNopStmt();

			toInsert.add(Jimple.v().newIfStmt(Jimple.v().newNeExpr(v, NullConstant.v()), nop));
			toInsert.add(nop);

			return toInsert;
		}

		return Collections.emptyList();
	}
}
