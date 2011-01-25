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

	private static final long serialVersionUID = -6215844840936041441L;

	private final Clazz clazz;
	private final String name;
	private final Clazz[] params;
	private final Clazz returnType;
	private final boolean isStatic;

	/** The maximum execution time (in milliseconds) */
	private final int maxExecutionTime;

	private final MethodInformation info;

	Methodz(boolean _static, Clazz returnType, Clazz clazz, String name, Clazz[] params, XmlMethod xml) {
		this.clazz = clazz;
		this.name = name;
		this.params = params;
		this.returnType = returnType;

		isStatic = _static;
		maxExecutionTime = xml.getMaxExecTime();

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

	public String getName() {
		return name;
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

	/**
	 * Returns the maximum execution time (in milliseconds)
	 * @return the maximum execution time (in milliseconds)
	 */
	public int getMaxExecutionTime() {
		return maxExecutionTime;
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

		final int compareClazz = clazz.compareTo(o.clazz);
		if(compareClazz != 0) return compareClazz;

		final int compareName = name.compareTo(o.name);
		if(compareName != 0) return compareName;

		if(params.length != o.params.length) return params.length - o.params.length;

		for (int i = 0; i < params.length; i++) {
			final int compareParam = params[i].compareTo(o.params[i]);
			if(compareParam != 0) return compareParam;
		}

		return 0;
	}
}
