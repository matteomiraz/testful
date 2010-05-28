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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://testful.sourceforge.net/schema/1.1/testful.xsd", name = "method", propOrder = { "parameter", "extra" })
public class XmlMethod {

	@XmlEnum
	@XmlType(namespace = "http://testful.sourceforge.net/schema/1.1/testful.xsd")
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
		MUTATOR("mutator");

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

	public List<Extra> getExtra() {
		if(extra == null) extra = new ArrayList<Extra>();
		return extra;
	}
}
