package testful.model.xml;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	public static ObjectFactory factory = new ObjectFactory();

	public ObjectFactory() {}

	public XmlAux createAux() {
		return new XmlAux();
	}

	public XmlClass createClass() {
		return new XmlClass();
	}

	public XmlConstructor createConstructor() {
		return new XmlConstructor();
	}

	public XmlMethod createMethod() {
		return new XmlMethod();
	}

	public XmlParameter createParameter() {
		return new XmlParameter();
	}

}
