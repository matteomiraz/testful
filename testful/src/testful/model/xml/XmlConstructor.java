package testful.model.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://home.dei.polimi.it/miraz/testful", name = "constructor", propOrder = { "parameter", "extra" })
public class XmlConstructor {

	@XmlElement(nillable = true)
	protected List<XmlParameter> parameter;

	@XmlElement
	protected List<Extra> extra;

	public List<XmlParameter> getParameter() {
		if(parameter == null) parameter = new ArrayList<XmlParameter>();
		return parameter;
	}

	public List<Extra> getExtra() {
		if(extra == null) extra = new ArrayList<Extra>();
		return extra;
	}
}
