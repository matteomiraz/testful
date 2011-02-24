package testful.model.xml.behavior;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "abstraction", namespace = "http://home.dei.polimi.it/miraz/testful/behavior")
public class Abstraction {

	@XmlAttribute(required = false)
	protected String function;

	@XmlAttribute(required = true)
	protected String range;

	@XmlAttribute(required = true)
	protected String value;

	public String getRange() {
		return range;
	}

	public void setRange(String value) {
		range = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String value) {
		function = value;
	}
}
