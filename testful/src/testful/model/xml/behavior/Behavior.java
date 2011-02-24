package testful.model.xml.behavior;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import testful.model.xml.Extra;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "behavior", namespace = "http://home.dei.polimi.it/miraz/testful/behavior", propOrder = { "abstraction" })
public class Behavior extends Extra {

	@XmlElement(required = true)
	protected List<Abstraction> abstraction;

	public List<Abstraction> getAbstraction() {
		if(abstraction == null) abstraction = new ArrayList<Abstraction>();
		return abstraction;
	}

}
