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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import testful.TestFul;
import testful.coverage.CoverageInformation;

public class BehaviorCoverage implements CoverageInformation {

	private static final long serialVersionUID = -7544654531090656602L;

	public static final String KEY = "beh";
	public static final String NAME = "behavioral coverage";

	private static transient int stateNumberGenerator = 1;
	private static transient Map<Abstraction, Integer> stateNumber = new LinkedHashMap<Abstraction, Integer>();

	public static void resetLabels() {
		stateNumberGenerator = 1;
		stateNumber = new LinkedHashMap<Abstraction, Integer>();
	}

	private static int getNumber(Abstraction state) {
		if(state == null || (state instanceof AbstractionObjectReference && ((AbstractionObjectReference) state).isNull())) return 0;

		Integer num = stateNumber.get(state);
		if(num == null) {
			num = stateNumberGenerator++;
			stateNumber.put(state, num);
		}
		return num;
	}

	private Map<Abstraction, Set<Operation>> stateMachine;

	private BehaviorCoverage() {
		stateMachine = new LinkedHashMap<Abstraction, Set<Operation>>();
		stateMachine.put(new AbstractionObjectReference("", true), new LinkedHashSet<Operation>());
	}

	public static BehaviorCoverage getEmpty() {
		return new BehaviorCoverage();
	}

	@Override
	public BehaviorCoverage createEmpty() {
		return new BehaviorCoverage();
	}

	// constructors are called with pre = null
	public boolean add(Abstraction pre, AbstractionMethod partition, Abstraction post) {
		if(partition == null) {
			TestFul.debug("null partition");
			return false;
		}

		if(partition.isStatic()) {
			pre = new AbstractionObjectReference("", true);
			post = new AbstractionObjectReference("", true);
		} else {
			if(pre == null) pre = new AbstractionObjectReference("", true);
			if(post == null) {
				TestFul.debug("post is NULL");
				return false;
			}
		}

		Set<Operation> state = stateMachine.get(pre);
		if(state == null) {
			state = new LinkedHashSet<Operation>();
			stateMachine.put(pre, state);
		}

		return state.add(new Operation(partition, post));
	}

	@Override
	public float getQuality() {
		int nTransitions = 0;
		for(Set<Operation> ops : stateMachine.values())
			nTransitions += ops.size();

		return nTransitions;
	}

	@Override
	public void merge(CoverageInformation o) {
		if(o instanceof BehaviorCoverage)
			for(Abstraction a : ((BehaviorCoverage) o).stateMachine.keySet()) {
				Set<Operation> thisState = stateMachine.get(a);
				if(thisState == null) {
					thisState = new LinkedHashSet<Operation>();
					stateMachine.put(a, thisState);
				}
				thisState.addAll(((BehaviorCoverage) o).stateMachine.get(a));
			}
	}

	@Override
	public boolean contains(CoverageInformation other) {
		if(!(other instanceof BehaviorCoverage)) return false;

		BehaviorCoverage o = (BehaviorCoverage) other;

		for(Abstraction abs : o.stateMachine.keySet()) {
			Set<Operation> ops = stateMachine.get(abs);

			if(ops == null) return false;
			if(!ops.containsAll(o.stateMachine.get(abs))) return false;
		}

		return true;
	}

	public static boolean DOT = true;
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if(DOT) sb.append("digraph Behavioral {\n");

		for(Abstraction state : stateMachine.keySet()) {
			int n = getNumber(state);
			if(!DOT) sb.append("S").append(n).append("\n");

			Set<Operation> opsSet = stateMachine.get(state);
			Operation[] ops = opsSet.toArray(new Operation[opsSet.size()]);
			Arrays.sort(ops);

			for(Operation op : ops)
				if(DOT)
					sb.append("  ").append("S").append(n).append(" -> ").append("S").append(getNumber(op.getTarget())).append(" [label=\"").append(op.getPartition()).append("\"];\n");
				else
					sb.append("  ").append(op.toString()).append(" -> ").append("S").append(getNumber(op.getTarget())).append("\n");
		}

		if(DOT) {
			sb.append("\n\n");
			sb.append("  S0 [label=\"NULL\" shape=\"box\"];\n");
			for (Abstraction a : stateNumber.keySet())
				sb.append("  S").append(getNumber(a)).append(" [ label=\"S").append(getNumber(a)).append("\"];\n");


			sb.append("  L [ shape=\"note\" label=\"Legend\\n");
			for (Abstraction a : stateNumber.keySet())
				sb.append("  S").append(getNumber(a)).append(" = ").append(a).append("\\n");
			sb.append("\"];\n");

			sb.append("}");
		} else {
			sb.append("--- Legend ---\n");
			sb.append("S0: Null Object\n");
			for(Abstraction state : stateNumber.keySet())
				sb.append("S").append(getNumber(state)).append(": ").append(state).append("\n");
		}

		return sb.toString();
	}

	public String toDot() {
		StringBuilder sb = new StringBuilder();

		for (Abstraction k : stateMachine.keySet())
			for (Operation op : stateMachine.get(k))
				sb.append("  ").append("S").append(getNumber(k)).append(" -> ").append("S").append(getNumber(op.getTarget())).append(" [label=\"").append(op.getPartition()).append("\"];\n");

		return sb.toString();
	}


	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public BehaviorCoverage clone() {
		BehaviorCoverage ret = new BehaviorCoverage();

		for(Entry<Abstraction, Set<Operation>> e : stateMachine.entrySet()) {
			Set<Operation> set = new HashSet<Operation>();
			for(Operation operation : e.getValue())
				set.add(operation);
			ret.stateMachine.put(e.getKey(), set);
		}

		return ret;
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		// TODO Auto-generated method stub
		out.writeObject(stateMachine);
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		// TODO Auto-generated method stub
		stateMachine = (Map<Abstraction, Set<Operation>>) in.readObject();
	}
}

class Operation implements Serializable, Comparable<Operation> {

	private static final long serialVersionUID = 3731090816752599353L;

	private AbstractionMethod partition;
	private Abstraction target;

	public Operation(AbstractionMethod partition, Abstraction target) {
		this.partition = partition;
		this.target = target;
	}

	public AbstractionMethod getPartition() {
		return partition;
	}

	public Abstraction getTarget() {
		return target;
	}

	@Override
	public String toString() {
		return partition.toString();
	}

	@Override
	public int compareTo(Operation o) {
		// compare the name of methods
		int result = partition.getExpression().compareTo(o.partition.getExpression());

		if(result == 0)
			result = target.hashCode() - o.partition.hashCode();

		return result;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((partition == null) ? 0 : partition.hashCode());
		result = prime * result + ((target == null) ? 0 : target.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;

		if(!(obj instanceof Operation)) return false;
		Operation other = (Operation) obj;

		if(partition == null) {
			if(other.partition != null) return false;
		} else if(!partition.equals(other.partition)) return false;

		if(target == null) {
			if(other.target != null) return false;
		} else if(!target.equals(other.target)) return false;

		return true;
	}
}
