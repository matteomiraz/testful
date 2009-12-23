package testful.coverage.behavior;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import testful.coverage.TrackerDatum;
import testful.model.xml.XmlClass;
import testful.model.xml.XmlMethod.Kind;

public class BehaviorTrackerData implements TrackerDatum {
	private static Logger logger = Logger.getLogger("testful.coverage.behavior");

	private static final long serialVersionUID = 1340613023941156981L;

	public static final String KEY = BehaviorTrackerData.class.getCanonicalName();

	@Override
	public String getKey() {
		return KEY;
	}

	/** Class name => abstractor Object State */
	private final Map<String, AbstractorObjectState> abstractorClass;

	/** ClassName.methodName(IIV) => Abstractor Method */
	private final Map<String, AbstractorMethod> abstractorMethod;

	public BehaviorTrackerData(Map<String, AbstractorObjectState> abstractorClass, Map<String, AbstractorMethod> abstractorMethod) {
		this.abstractorClass = abstractorClass;
		this.abstractorMethod = abstractorMethod;
	}

	public BehaviorTrackerData(Collection<XmlClass> xmlClasses) {
		abstractorClass = new HashMap<String, AbstractorObjectState>();
		abstractorMethod = new HashMap<String, AbstractorMethod>();

		for(testful.model.xml.XmlClass xmlClass : xmlClasses) {

			{ // create state abstractor
				testful.model.xml.behavior.Behavior beh = getBehavior(xmlClass.getExtra());
				if(beh != null) {
					List<Abstractor> state = new ArrayList<Abstractor>();
					for(testful.model.xml.behavior.Abstraction abs : beh.getAbstraction()) {
						Abstractor abstractor = getAbstractor(abs);
						if(abstractor != null) state.add(abstractor);
					}
					try {
						abstractorClass.put(xmlClass.getName(), new AbstractorObjectState(state.toArray(new Abstractor[state.size()])));
					} catch(Exception e) {
						logger.warning(e.getMessage());
					}
				}
			} // end of create state abstractor

			{ // create constructor abstractor
				for(testful.model.xml.XmlConstructor xmlCns : xmlClass.getConstructor()) {
					// create state abstractor
					testful.model.xml.behavior.Behavior beh = getBehavior(xmlCns.getExtra());
					String bytecodeName = BytecodeUtils.getBytecodeName(xmlCns);
					if(beh == null) abstractorMethod.put(bytecodeName, new AbstractorMethod(bytecodeName, false));
					else {
						List<Abstractor> method = new LinkedList<Abstractor>();
						for(testful.model.xml.behavior.Abstraction abs : beh.getAbstraction()) {
							Abstractor abstractor = getAbstractor(abs);
							if(abstractor != null) method.add(abstractor);
						}
						abstractorMethod.put(bytecodeName, new AbstractorMethod(bytecodeName, false, method.toArray(new Abstractor[method.size()])));
					}
				}
			} // end of create constructor abstractor

			{ // create method abstractor
				for(testful.model.xml.XmlMethod xmlMeth : xmlClass.getMethod()) {
					// create state abstractor
					testful.model.xml.behavior.Behavior beh = getBehavior(xmlMeth.getExtra());
					String bytecodeName = BytecodeUtils.getBytecodeName(xmlMeth);
					if(beh == null) abstractorMethod.put(bytecodeName, new AbstractorMethod(bytecodeName, xmlMeth.getKind() == Kind.STATIC));
					else {
						List<Abstractor> method = new LinkedList<Abstractor>();
						for(testful.model.xml.behavior.Abstraction abs : beh.getAbstraction()) {
							Abstractor abstractor = getAbstractor(abs);
							if(abstractor != null) method.add(abstractor);
						}
						abstractorMethod.put(bytecodeName, new AbstractorMethod(bytecodeName, xmlMeth.getKind() == Kind.STATIC, method.toArray(new Abstractor[method.size()])));
					}
				}
			} // end of create method abstractor

		} // end foreach xmlClass
	}

	@SuppressWarnings("unchecked")
	private static Abstractor getAbstractor(testful.model.xml.behavior.Abstraction abs) {
		try {
			if(abs.getFunction() == null || abs.getFunction().trim().length() <= 0) return new Abstractor(abs.getValue(), abs.getRange());
			else {
				Class<? extends Abstractor> abstractor = (Class<? extends Abstractor>) Class.forName(abs.getFunction().trim());
				java.lang.reflect.Constructor<? extends Abstractor> cns = abstractor.getConstructor(String.class, String.class);
				return cns.newInstance(abs.getValue(), abs.getRange());
			}
		} catch(Exception e) {
			logger.warning(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	private static testful.model.xml.behavior.Behavior getBehavior(List<testful.model.xml.Extra> extras) {
		for(testful.model.xml.Extra extra : extras)
			if(extra instanceof testful.model.xml.behavior.Behavior) return (testful.model.xml.behavior.Behavior) extra;

		return null;
	}

	public AbstractorObjectState getAbstractorClass(String className) {
		return abstractorClass.get(className);
	}

	public AbstractorMethod getAbstractorMethod(String methodName) {
		return abstractorMethod.get(methodName);
	}

	@Override
	public BehaviorTrackerData clone() throws CloneNotSupportedException {
		return (BehaviorTrackerData) super.clone();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((abstractorClass == null) ? 0 : abstractorClass.hashCode());
		result = prime * result + ((abstractorMethod == null) ? 0 : abstractorMethod.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null) return false;
		if(!(obj instanceof BehaviorTrackerData)) return false;
		BehaviorTrackerData other = (BehaviorTrackerData) obj;
		if(abstractorClass == null) {
			if(other.abstractorClass != null) return false;
		} else if(!abstractorClass.equals(other.abstractorClass)) return false;
		if(abstractorMethod == null) {
			if(other.abstractorMethod != null) return false;
		} else if(!abstractorMethod.equals(other.abstractorMethod)) return false;
		return true;
	}
}
