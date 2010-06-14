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

package testful.model;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;

public class Test implements Serializable {

	private static final long serialVersionUID = 1591209932563881988L;

	/** the test cluster */
	private final TestCluster cluster;
	/** the reference factory */
	private final ReferenceFactory refFactory;
	/** the test as sequence of operation */
	private final Operation[] test;

	private final int hashCode;

	public Test(TestCluster cluster, ReferenceFactory refFactory, Operation[] test) {
		this.cluster = cluster;
		this.refFactory = refFactory;
		this.test = test;
		hashCode = Arrays.hashCode(test);
	}

	public Test(Test ... parts) throws Exception {
		if(parts == null) throw new Exception("Cannot join tests: null");
		if(parts.length == 0) throw new Exception("Cannot join tests: 0 test provided!");

		cluster = parts[0].cluster;

		// TBD: it is likely that different tests have different reference factory!
		refFactory = parts[0].refFactory;

		int len = 0;
		for(Test other : parts) {
			if(!cluster.equals(other.cluster) || !refFactory.equals(other.refFactory)) throw new Exception("Incompatible tests");

			len += 1 + other.test.length;
		}

		int i = 0;
		test = new Operation[len - 1];
		for(Test other : parts) {
			if(i > 0) test[i++] = ResetRepository.singleton;
			for(Operation op : other.test)
				test[i++] = op.adapt(cluster, refFactory);
		}
		hashCode = Arrays.hashCode(test);
	}

	public TestCluster getCluster() {
		return cluster;
	}

	public Operation[] getTest() {
		return test;
	}

	public ReferenceFactory getReferenceFactory() {
		return refFactory;
	}

	public Test join(Test ... others) throws Exception {
		Test[] tmp = new Test[others.length + 1];

		int i = 0;
		tmp[i++] = this;
		for(Test t : others)
			tmp[i++] = t;

		return new Test(tmp);
	}

	public void write(OutputStream outputStream) throws IOException {
		ObjectOutput write = null;
		try {
			write = new ObjectOutputStream(outputStream);
			write.writeObject(this);
		} finally {
			if(write != null) write.close();
			else outputStream.close();
		}
	}

	/**
	 * Ensures that there are no duplicate operations.<br>
	 * after this method holds: (\forall int i; 0 <= i && i < test.length;
	 * (\forall int j; i < j && j < test.length; test[i] != test[j] ))
	 */
	public void ensureNoDuplicateOps() {
		ensureNoDuplicateOps(test);
	}


	/**
	 * Ensures that there are no duplicate operations.<br>
	 * after this method holds: (\forall int i; 0 <= i && i < test.length;
	 * (\forall int j; i < j && j < test.length; test[i] != test[j] ))
	 */
	public static void ensureNoDuplicateOps(Operation[] test) {
		IdentityHashMap<Operation, Operation> map = new IdentityHashMap<Operation, Operation>();

		for(int i = 0; i < test.length; i++) {
			if(map.containsKey(test[i])) test[i] = test[i].clone();

			map.put(test[i], test[i]);
		}
	}

	/**
	 * Calculate the hash of the current test.
	 * Other parts of Testful requires that the hash code calculus is deterministic
	 * (i.e., it does not depend on random variation such as the location in memory, like the
	 * Object.hashCode).
	 */
	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Test)) return false;

		return Arrays.equals(test, ((Test) obj).test);
	}

	public static final Comparator<Test> sizeComparator = new SizeComparator();

	private static class SizeComparator implements Comparator<Test>, Serializable {

		private static final long serialVersionUID = 1L;

		@Override
		public int compare(Test o1, Test o2) {
			int ret = o1.getTest().length - o2.getTest().length;
			if(ret == 0 && o1 != o2) return 1;
			return ret;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Test for class ").append(cluster.getCut().getClassName()).append("\n");

		for(Operation op : test) {

			Iterator<OperationInformation> infos = op.getInfos();
			if(infos.hasNext()) sb.append("\n");
			while(infos.hasNext())
				sb.append("  //").append(infos.next().toString()).append("\n");

			sb.append("  ").append(op.toString()).append(";\n");
		}

		return sb.toString();
	}
}
