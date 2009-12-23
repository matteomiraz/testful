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
@XmlType(namespace = "http://home.dei.polimi.it/miraz/testful", name = "class", propOrder = { "aux", "constructor", "method", "extra" })
@XmlRootElement(name = "class", namespace = "http://home.dei.polimi.it/miraz/testful")
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
