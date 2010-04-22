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
@XmlType(namespace = "http://home.dei.polimi.it/miraz/testful", name = "method", propOrder = { "parameter", "extra" })
public class XmlMethod {

	@XmlEnum
	@XmlType(namespace = "http://home.dei.polimi.it/miraz/testful")
	public static enum Kind {
		/** the method is static */
		@XmlEnumValue("static")
		STATIC("static"),
		
		/** the method is an observer: it has no input parameters, does not modify
		 * the state, and returns an observation of the object's state (changing the return
		 * value, the state is not changed) */
		@XmlEnumValue("observer")
		OBSERVER("observer"),
		
		/** the method is a worker: does something without modifying the the state */
		@XmlEnumValue("worker")
		WORKER("worker"),

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
