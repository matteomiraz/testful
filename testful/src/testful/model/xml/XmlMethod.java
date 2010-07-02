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

package testful.model.xml;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://testful.sourceforge.net/schema/1.2/testful.xsd", name = "method", propOrder = { "parameter", "extra" })
public class XmlMethod implements Comparable<XmlMethod> {

	private static final Logger logger = Logger.getLogger("testful.model.xml");

	/** Default maximum execution time (in milliseconds) */
	static final Integer MAX_EXEC_TIME = 500;

	@XmlEnum
	@XmlType(namespace = "http://testful.sourceforge.net/schema/1.2/testful.xsd")
	public static enum Kind {
		/** the method is static */
		@XmlEnumValue("static")
		STATIC("static"),

		/** the method is pure and to be used in tests. <br/>
		 * <ul>
		 * <li>Pure method: does not modify the state and returns an observation of the
		 * object's state (changing the return value, the state is not changed).</li>
		 * <li>Usable for creating assertions: it has no input parameters, and the value it returns
		 * can be used for regression testing.</li>
		 * </ul>
		 */
		@XmlEnumValue("observer")
		OBSERVER("observer"),

		/** the method is pure: does not modify the state and returns an observation of the
		 * object's state (changing the return value, the state is not changed).*/
		@XmlEnumValue("pure")
		PURE("pure"),

		/** the method is a mutator: does something, may mutate the object's state */
		@XmlEnumValue("mutator")
		MUTATOR("mutator"),

		/** the method should be skipped: testful should not consider this method at all */
		@XmlEnumValue("skip")
		SKIP("skip");

		private final String value;

		Kind(String v) {
			value = v;
		}

		public String value() {
			return value;
		}

		public static Kind fromValue(String v) {
			for(Kind c : Kind.values())
				if(c.value.equals(v)) return c;
			throw new IllegalArgumentException(v.toString());
		}
	}

	@XmlAttribute(required = true)
	protected String name;

	/** Maximum execution time (in milliseconds) */
	@XmlAttribute(required=false)
	public Integer maxExecTime = XmlMethod.MAX_EXEC_TIME;

	@XmlAttribute
	protected Boolean exposeState;

	@XmlAttribute(required = true)
	protected Kind kind;

	@XmlElement(nillable = true)
	protected List<XmlParameter> parameter;

	@XmlElement
	protected List<Extra> extra;

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public Boolean isExposeState() {
		return exposeState;
	}

	public void setExposeState(Boolean value) {
		exposeState = value;
	}

	public Kind getKind() {
		if(kind == null) kind = Kind.MUTATOR;
		return kind;
	}

	public void setKind(Kind kind) {
		this.kind = kind;
	}

	public List<XmlParameter> getParameter() {
		if(parameter == null) parameter = new ArrayList<XmlParameter>();
		return parameter;
	}

	/**
	 * Returns the maximum execution time (in milliseconds)
	 * @return the maximum execution time (in milliseconds)
	 */
	public int getMaxExecTime() {
		if(maxExecTime == null) return XmlMethod.MAX_EXEC_TIME;
		return maxExecTime;
	}

	/**
	 * Sets the maximum execution time (in milliseconds)
	 * @param maxExecTime the maximum execution time (in milliseconds)
	 */
	public void setMaxExecTime(Integer maxExecTime) {
		this.maxExecTime = maxExecTime;
	}

	public List<Extra> getExtra() {
		if(extra == null) extra = new ArrayList<Extra>();
		return extra;
	}

	/**
	 * @return true if testful should ignore this method
	 */
	public boolean isSkip() {
		return kind == Kind.SKIP;
	}

	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(XmlMethod o) {
		if(name.compareTo(o.name) != 0) return name.compareTo(o.name);

		Iterator<XmlParameter> it1 = getParameter().iterator();
		Iterator<XmlParameter> it2 = o.getParameter().iterator();

		while(it1.hasNext() & it2.hasNext()) {
			XmlParameter p1 = it1.next();
			XmlParameter p2 = it2.next();

			final int compare = p1.compareTo(p2);
			if(compare != 0) return compare;
		}

		if(it1.hasNext()) return -1;
		if(it2.hasNext()) return  1;
		return 0;
	}

	/**
	 * Create the XML description for a given method.
	 * @param meth the method to describe
	 * @return null if the method should be skipped (i.e., it is a bridge, synthetic, not public, or inherited from java),
	 * 			or the XML description of the method
	 */
	public static XmlMethod create(Method meth) {
		// check if the method should be skipped

		// skip strange methods
		if(meth.isBridge() || meth.isSynthetic()) return null;

		// skip non-public methods
		if(!Modifier.isPublic(meth.getModifiers())) return null;

		// skip Object's methods
		if(meth.getDeclaringClass().getCanonicalName().equals("java.lang.Object"))
			return null;

		// skip methods with arrays
		for (Class<?> params : meth.getParameterTypes()) {
			if(params.isArray()) {
				logger.info("Skipping " + meth + ": has an array as parameter. If you are interested in testing this method, vote for issue #1: http://code.google.com/p/testful/issues/detail?id=1");
				return null;
			}
		}

		// create the XML description
		final Class<?> returnType = meth.getReturnType();

		XmlMethod xmeth = testful.model.xml.ObjectFactory.factory.createMethod();
		xmeth.setExposeState(returnType.isArray());
		xmeth.setName(meth.getName());
		if(Modifier.isStatic(meth.getModifiers())) xmeth.setKind(Kind.STATIC);
		else {
			if(meth.getName().startsWith("get") && meth.getParameterTypes().length == 0) {
				// getter without any parameter
				if(meth.getReturnType().isPrimitive()) xmeth.setKind(Kind.OBSERVER);
				else xmeth.setKind(Kind.PURE);

			} else {
				xmeth.setKind(Kind.MUTATOR);
			}
		}

		for(Class<?> p : meth.getParameterTypes())
			xmeth.getParameter().add(XmlParameter.create(p, returnType));

		return xmeth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((parameter == null) ? 0 : parameter.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		XmlMethod other = (XmlMethod) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameter == null) {
			if (other.parameter != null)
				return false;
		} else if (!parameter.equals(other.parameter))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(name + "(");

		boolean first = true;
		for (XmlParameter p : getParameter()) {
			if(first) first = false;
			else sb.append(", ");
			sb.append(p.getType());
		}

		return sb.append(")").toString();
	}
}
