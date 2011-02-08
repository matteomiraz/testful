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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import testful.TestFul;
import testful.coverage.CoverageInformation;

/**
 * Measures the distance to execute a given branch, optionally
 * using a precise definition.
 *
 * Suppose that the target is
 * <br><code>if (a > b) { // target }</code><br>
 * the distance to execute the target is
 * infinite if the conditional statement is never executed.
 * Otherwise, the distance is the difference between the values assumed
 * by a and b (their minimum distance if they are executed more than once).
 *
 * @author matteo
 */
public class CoverageBranchTarget implements CoverageInformation {
	private static final long serialVersionUID = 7837300371834856584L;

	private static String NAME = "Distance to branch ";
	public static String KEY = "tbr";

	/** the ID of the targeted branch. Do not assign values to this field (it is final) */
	private int branchId;

	/** whether the target is a p-use or not. Do not assign values to this field (it is final) */
	private boolean pUse;

	/** the definition to use (null means default). Do not assign values to this field (it is final) */
	private ContextualId defId;

	/** The distance to reach the target */
	private double distance;

	@Deprecated
	public CoverageBranchTarget() { }

	public CoverageBranchTarget(int branchId, boolean pUse, ContextualId defId) {
		this.branchId = branchId;
		this.pUse = pUse;
		this.defId = defId;
		distance = Float.POSITIVE_INFINITY;
	}

	public CoverageBranchTarget(ConditionTargetDatum datum) {
		this(datum.getBranchId(), datum.isPUse(), datum.getDefinitionId());
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return NAME + branchId;
	}

	public int getBranchId() {
		return branchId;
	}

	public ContextualId getDefinitionId() {
		return defId;
	}

	void setDistance(double d) {
		if(TestFul.DEBUG)
			if(d < 0 && d != -1)
				TestFul.debug("Invalid distance: " + d);

		if(distance > d)
			distance = d;
	}

	/**
	 * Returns the quality: higher is better
	 */
	@Override
	public float getQuality() {
		if(distance == Float.POSITIVE_INFINITY) return Float.NEGATIVE_INFINITY;
		if(distance < 0) return Float.POSITIVE_INFINITY;

		return (float) (1.0/(distance+0.125));
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(!other.getKey().equals(getKey())) return false;

		CoverageBranchTarget o = (CoverageBranchTarget) other;
		if(distance > o.distance) return false;

		return true;
	}

	@Override
	public void merge(CoverageInformation other) {
		if(!other.getKey().equals(getKey())) return;

		CoverageBranchTarget o = (CoverageBranchTarget) other;
		if(distance <= o.distance) return;

		distance = o.distance;
	}

	@Override
	public CoverageBranchTarget createEmpty() {
		return new CoverageBranchTarget(branchId, pUse, defId);
	}

	@Override
	public CoverageBranchTarget clone() {
		CoverageBranchTarget ret = new CoverageBranchTarget(branchId, pUse, defId);
		ret.distance = distance;
		return ret;
	}

	@Override
	public String toString() {
		return Double.toString(distance);
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(branchId);
		out.writeBoolean(pUse);
		out.writeDouble(distance);
		if(defId != null) {
			out.writeBoolean(true);
			out.writeInt(defId.getId());
			Stack.write(defId.getContext(), out);

		} else {
			out.writeBoolean(false);
		}
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		branchId = in.readInt();
		pUse = in.readBoolean();
		distance = in.readDouble();

		// if defId != null
		if(in.readBoolean()) {
			int id = in.readInt();
			Stack context = Stack.read(in);
			defId = new ContextualId(id, context);
		} else {
			defId = null;
		}
	}
}