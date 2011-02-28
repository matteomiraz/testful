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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestfulException;
import testful.runner.ISerializable;

public class TestCluster implements ISerializable {

	private static final long serialVersionUID = 6041896902165032348L;

	public static final String ISERIALIZABLE_ID = "testful.model.TestCluster";

	@Override
	public String getISerializableIdentifier() {
		return ISERIALIZABLE_ID;
	}

	private static final Logger logger = Logger.getLogger("testful.model");

	/** Class Under Test */
	private final Clazz cut;

	/** All classes required for the test (CUT + AUX). It is sorted according to the name of the classes */
	private final Clazz[] cluster;

	/** The whole set of clazzes involved in the test. It includes the cluster, primitive types, and all return types. */
	private final Clazz[] all;

	TestCluster(Clazz cut, Clazz[] cluster, Clazz[] all) {
		this.cut = cut;
		this.cluster = cluster;
		this.all = all;

		if(logger.isLoggable(Level.CONFIG)) {
			StringBuilder sb = new StringBuilder("Test Cluster:");

			for (Clazz c : cluster) {
				sb.append("\nclass " + c.getClassName());
				for (Constructorz cns : c.getConstructors()) print(sb, cns.toString(), cns.getParameterTypes(), cns.getMethodInformation());
				for (Methodz m : c.getMethods()) print(sb, m.toString(), m.getParameterTypes(), m.getMethodInformation());
			}

			logger.config(sb.toString());
		}
	}

	private void print(StringBuilder sb, final String name, final Clazz[] params, final MethodInformation info) {

		sb.append("\n * [" + info.getType() + "] "  + name);

		for (int i = 0; i < info.getParameters().length; i++) {
			sb.append(String.format("\n     p%2d %s ", i, params[i].getClassName()));
			if(info.getParameters()[i].isMutated()) sb.append(" mutated");
			if(info.getParameters()[i].isCaptured()) sb.append(" captured");
			if(info.getParameters()[i].isCapturedByReturn()) sb.append(" capturedByReturn");
			if(info.getParameters()[i].getCaptureStateOf() != null && !info.getParameters()[i].getCaptureStateOf().isEmpty())
				sb.append(info.getParameters()[i].getCaptureStateOf());
		}
	}

	/**
	 * Checks if it is possible to obtain objects
	 * (using constructors, constants, or values returned by methods)
	 * for each class it contains.<br/>
	 * If it is not the case, the method throws an exception,
	 * listing all the missing classes.
	 *
	 * @throws MissingClassException if some class is missing
	 */
	public void isValid() throws MissingClassException {
		Set<Clazz> missing = new HashSet<Clazz>();
		for (Clazz c : cluster)
			missing.add(c);

		for (Clazz c : cluster) {
			if(!c.isAbstract())
				for (Clazz assignable : c.getAssignableTo())
					missing.remove(assignable);

			if(missing.isEmpty()) return;

			for (Methodz m : c.getMethods()) {
				Clazz ret = m.getReturnType();
				if(ret != null)
					for (Clazz assignable : ret.getAssignableTo())
						missing.remove(assignable);
			}

			if(missing.isEmpty()) return;

			for (StaticValue sv : c.getConstants())
				for (Clazz assignable : sv.getType().getAssignableTo())
					missing.remove(assignable);

			if(missing.isEmpty()) return;
		}

		// just in case...
		if(missing.isEmpty()) return;

		throw new MissingClassException(missing, cut);
	}

	public static class MissingClassException extends TestfulException {
		private static final long serialVersionUID = -7271283808569139038L;

		/** Contains the missing classes */
		public final Set<String> missing;

		public MissingClassException(Set<Clazz> missing, Clazz cut) {
			super("Some classes are missing:" + missing);

			Set<String> tmp = new HashSet<String>();
			for (Clazz c : missing) tmp.add(c.getClassName());
			this.missing = Collections.unmodifiableSet(tmp);
		}
	}

	public String[] getClasses() {
		String[] ret = new String[cluster.length];

		for(int i = 0; i < cluster.length; i++)
			ret[i] = cluster[i].getClassName();

		return ret;
	}

	public Clazz getClazz(String name) {
		for (Clazz c : cluster) {
			if(c.getClassName().equals(name))
				return c;
		}
		return null;
	}

	public int getClusterSize() {
		return cluster.length;
	}

	public Clazz getCluster(int pos) {
		return cluster[pos];
	}

	public Clazz getCut() {
		return cut;
	}

	private transient Map<Integer, Clazz> clazzCache;
	private transient Map<Integer, Constructorz> constructorzCache;
	private transient Map<Integer, Methodz> methodzCache;
	private transient Map<Integer, StaticValue> staticValueCache;

	private void updateCaches() {
		clazzCache = new HashMap<Integer, Clazz>();
		constructorzCache = new HashMap<Integer, Constructorz>();
		methodzCache = new HashMap<Integer, Methodz>();
		staticValueCache = new HashMap<Integer, StaticValue>();

		for (Clazz c : all) {
			clazzCache.put(c.getId(), c);

			for (Constructorz cns : c.getConstructors())
				constructorzCache.put(cns.getId(), cns);

			for (Methodz meth : c.getMethods())
				methodzCache.put(meth.getId(), meth);

			for (StaticValue sv : c.getConstants())
				staticValueCache.put(sv.getId(), sv);
		}
	}

	public Clazz getClazzById(int id) {
		if(clazzCache == null)
			updateCaches();

		return clazzCache.get(id);
	}

	public Constructorz getConstructorById(int id) {
		if(constructorzCache == null)
			updateCaches();

		return constructorzCache.get(id);
	}

	public Methodz getMethodById(int id) {
		if(methodzCache == null)
			updateCaches();

		return methodzCache.get(id);
	}

	public StaticValue getStaticValueById(int id) {
		if(staticValueCache == null)
			updateCaches();

		return staticValueCache.get(id);
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();

		ret.append("CUT: ").append(cut.getClassName()).append("\n");

		ret.append("Test Cluster: ");
		for(Clazz c : cluster)
			ret.append("\n  ").append(c.getClassName());

		return ret.toString();
	}

	public Clazz[] getCluster() {
		return cluster;
	}

	public boolean contains(Clazz clazz) {
		if(clazz == null) return false;

		for(Clazz c : cluster)
			if(c.equals(clazz)) return true;

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + cut.hashCode();
		result = prime * result + Arrays.hashCode(cluster);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof TestCluster)) return false;

		TestCluster other = (TestCluster) obj;
		return cut.equals(other.cut) && Arrays.equals(cluster, other.cluster);
	}

	/**
	 * Adapts a clazz belonging to another test cluster to a clazz belonging to
	 * this test cluster
	 *
	 * @param clazz the clazz
	 * @return a class belonging to this test cluster
	 */
	public Clazz adapt(Clazz clazz) {
		final int idx = Arrays.binarySearch(all, clazz);
		if(idx >= 0) return all[idx];

		logger.warning("Cannot adapt class " + clazz + " " + Arrays.toString(cluster));
		return null;
	}

	/**
	 * Adapts a method belonging to another test cluster to a method belonging to
	 * this test cluster
	 *
	 * @param method the method
	 * @return a method belonging to this test cluster
	 */
	public Methodz adapt(Methodz method) {
		if(method == null) return null;

		final Clazz thisClass = adapt(method.getClazz());
		if(thisClass == null) return null;
		final Methodz[] methods = thisClass.getMethods();

		final int idx = Arrays.binarySearch(methods, method);
		if(idx >= 0) return methods[idx];

		logger.warning("Cannot adapt Method " + method + " " + Arrays.toString(methods));
		return null;
	}

	/**
	 * Adapts a constructor belonging to another test cluster to a constructor
	 * belonging to this test cluster
	 *
	 * @param cns the constructor
	 * @return a constructor belonging to this test cluster
	 */
	public Constructorz adapt(Constructorz cns) {
		if(cns == null) return null;

		final Clazz thisClass = adapt(cns.getClazz());
		if(thisClass == null) return null;
		final Constructorz[] constructors = thisClass.getConstructors();

		final int idx = Arrays.binarySearch(constructors, cns);
		if(idx >= 0) return constructors[idx];

		logger.warning("Cannot adapt Constructor " + cns + " " + Arrays.toString(constructors));
		return null;
	}

	/**
	 * Adapts a constant belonging to another test cluster to a constant belonging
	 * to this test cluster
	 *
	 * @param sv the constant
	 * @return a constant belonging to this test cluster
	 */
	public StaticValue adapt(StaticValue sv) {
		if(sv == null) return null;

		final Clazz thisClass = adapt(sv.getType());
		if(thisClass == null) return null;
		final StaticValue[] constants = thisClass.getConstants();

		final int idx = Arrays.binarySearch(constants, sv);
		if(idx >= 0) return constants[idx];

		logger.warning("cannot adapt StaticValue " + sv + " " + Arrays.toString(constants));
		return null;
	}
}