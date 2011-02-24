package testful.model.xml.behavior;

import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	public static ObjectFactory factory = new ObjectFactory();

	public ObjectFactory() {}

	public Abstraction createAbstraction() {
		return new Abstraction();
	}

	public Behavior createBehavior() {
		return new Behavior();
	}
}
