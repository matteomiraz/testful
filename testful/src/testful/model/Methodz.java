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

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;

import testful.model.MethodInformation.ParameterInformation;
import testful.model.xml.XmlMethod;
import testful.model.xml.XmlParameter;

/**
 * Methodz render methods of classes
 *
 * @author matteo
 */
public class Methodz implements Serializable, Comparable<Methodz> {

	private static final long serialVersionUID = -6044812551106557800L;

	private final Clazz clazz;
	private final String name;
	private final Clazz[] params;
	private final Clazz returnType;
	private final boolean isStatic;

	private final String fullMethodName;

	private transient Method method = null;

	private final MethodInformation info;

	Methodz(TestCluster cluster, Clazz clazz, Method m, XmlMethod xml) {
		this.clazz = clazz;
		name = m.getName();
		fullMethodName = m.toGenericString();
		params = cluster.getRegistry().convert(m.getParameterTypes());

		// ISSUE #1: if you need array support, vote here: http://code.google.com/p/testful/issues/detail?id=1
		if(m.getReturnType() ==  Void.TYPE || m.getReturnType().isArray())
			returnType = null;
		else
			returnType = cluster.getRegistry().getClazz(m.getReturnType());

		method = m;
		isStatic = Modifier.isStatic(method.getModifiers());

		List<XmlParameter> paramsXml = xml.getParameter();
		ParameterInformation[] paramsInfo = new ParameterInformation[paramsXml.size()];
		for(int i = 0; i < paramsXml.size(); i++) {
			XmlParameter p = paramsXml.get(i);
			paramsInfo[i] = new ParameterInformation(i);
			paramsInfo[i].setMutated(p.isMutated());
			paramsInfo[i].setCaptured(p.isCaptured());
			paramsInfo[i].setCapturedByReturn(p.isExposedByReturn());
		}

		for(int i = 0; i < paramsXml.size(); i++)
			for(int exch : paramsXml.get(i).getExchangeState())
				paramsInfo[i].addCaptureStateOf(paramsInfo[exch]);

		info = new MethodInformation(MethodInformation.Kind.convert(xml.getKind()), xml.isExposeState(), paramsInfo);
	}

	public Clazz getClazz() {
		return clazz;
	}

	public MethodInformation getMethodInformation() {
		return info;
	}

	public Method toMethod() {
		if(method == null) {
			try {
				method = clazz.toJavaClass().getMethod(name, Clazz.convert(params));
			} catch(Exception e) {
				return null; // never happens
			}
		}
		return method;
	}

	/** Clear the cache: discard the Method reference */
	public void clearCache() {
		method = null;
	}

	public String getName() {
		return name;
	}

	public String getFullMethodName() {
		return fullMethodName;
	}

	public String getShortMethodName() {
		return toMethod().getName();
	}

	public Clazz[] getParameterTypes() {
		return params;
	}

	public Clazz getReturnType() {
		return returnType;
	}

	public boolean isStatic() {
		return isStatic;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name).append("(");

		boolean first = true;
		for(Clazz p : params) {
			if(first) first = false;
			else sb.append(", ");

			sb.append(p.getClassName());
		}

		return sb.append(")").toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + clazz.hashCode();
		result = prime * result + name.hashCode();
		result = prime * result + Arrays.hashCode(params);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof Methodz)) return false;

		Methodz other = (Methodz) obj;
		return clazz.equals(other.clazz) && name.equals(other.name) && Arrays.equals(params, other.params);
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Methodz o) {
		if(name.compareTo(o.name) != 0) return name.compareTo(o.name);

		int i1 = 0, i2 = 0;
		while(i1 < params.length & i2 < o.params.length) {
			Clazz p1 = params[i1++];
			Clazz p2 = o.params[i2++];

			final int compare = p1.compareTo(p2);
			if(compare != 0) return compare;
		}

		if(i1 >= params.length) return -1;
		if(i2 >= o.params.length) return  1;
		return 0;
	}
}
