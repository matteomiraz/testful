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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://testful.sourceforge.net/schema/1.1/testful.xsd", name = "class", propOrder = { "aux", "constructor", "method", "extra" })
@XmlRootElement(name = "class", namespace = "http://testful.sourceforge.net/schema/1.1/testful.xsd")
public class XmlClass {

	@XmlAttribute(required = true)
	protected String name;

	@XmlAttribute(required = false)
	protected boolean instrument;

	@XmlElement(nillable = true)
	protected List<XmlAux> aux;

	@XmlElement(nillable = true)
	protected List<XmlConstructor> constructor;

	@XmlElement(nillable = true)
	protected List<XmlMethod> method;

	@XmlElement
	protected List<Extra> extra;

	public String getName() {
		return name;
	}

	public void setName(String value) {
		name = value;
	}

	public boolean isInstrument() {
		return instrument;
	}

	public void setInstrument(boolean instrument) {
		this.instrument = instrument;
	}

	public List<XmlAux> getAux() {
		if(aux == null) aux = new ArrayList<XmlAux>();

		return aux;
	}

	public List<XmlMethod> getMethod() {
		if(method == null) method = new ArrayList<XmlMethod>();

		return method;
	}

	public List<XmlConstructor> getConstructor() {
		if(constructor == null) constructor = new ArrayList<XmlConstructor>();

		return constructor;
	}

	public List<Extra> getExtra() {
		if(extra == null) extra = new ArrayList<Extra>();

		return extra;
	}

	public XmlConstructor getConstructor(Constructor<?> cons) {
		Class<?>[] params = cons.getParameterTypes();
		String[] paramsString = new String[params.length];

		for(int i = 0; i < params.length; i++)
			paramsString[i] = params[i].getCanonicalName();

		return getConstructor(paramsString);
	}

	public XmlConstructor getConstructor(String params[]) {
		for(XmlConstructor xmlCons : getConstructor()) {
			List<XmlParameter> xmlParams = xmlCons.getParameter();
			if(xmlParams.size() != params.length) continue;

			boolean ok = true;
			for(int i = 0; i < params.length && ok; i++)
				if(!params[i].equals(xmlParams.get(i).getType())) ok = false;
			if(!ok) continue;

			return xmlCons;
		}

		return null;
	}

	public XmlMethod getMethod(Method meth) {
		Class<?>[] params = meth.getParameterTypes();
		String[] paramsString = new String[params.length];

		for(int i = 0; i < params.length; i++)
			paramsString[i] = params[i].getCanonicalName();

		return getMethod(meth.getName(), paramsString);
	}

	public XmlMethod getMethod(String name, String[] params) {
		for(XmlMethod xmlMeth : getMethod()) {
			if(!xmlMeth.getName().equals(name)) continue;

			List<XmlParameter> xmlParams = xmlMeth.getParameter();
			if(xmlParams.size() != params.length) continue;

			boolean ok = true;
			for(int i = 0; i < params.length && ok; i++)
				if(!params[i].equals(xmlParams.get(i).getType())) ok = false;
			if(!ok) continue;

			return xmlMeth;
		}

		return null;
	}
}
