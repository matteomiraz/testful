package testful.model.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://home.dei.polimi.it/miraz/testful", name = "aux")
public class XmlAux {

	@XmlAttribute(required = true)
	protected String name;

	public XmlAux() {
	}
	
	public XmlAux(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
}
