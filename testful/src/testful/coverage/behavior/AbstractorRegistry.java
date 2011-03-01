/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011  Matteo Miraz
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

package testful.coverage.behavior;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.TestFul;
import testful.model.TestCluster;
import testful.model.xml.XmlClass;
import testful.model.xml.XmlMethod.Kind;
import testful.model.xml.XmlRegistry;
import testful.runner.ISerializable;

/**
 * Creates abstractors for a test cluster,
 * store and transfer them towards the test executor,
 * and allows the {@link BehaviorTracker} to efficiently retrieve them.
 * @author matteo
 */
public class AbstractorRegistry implements ISerializable {

	private static Logger logger = Logger.getLogger("testful.coverage.behavior");

	private static final long serialVersionUID = 1340613023941156981L;

	public static final String ISERIALIZABLE_ID = "testful.coverage.behavior.AbstractorRegistry";

	@Override
	public String getISerializableIdentifier() {
		// The AbstractorRegistry is a singleton for each Test
		return ISERIALIZABLE_ID;
	}

	/** Class name => abstractor Object State */
	private final Map<String, AbstractorObjectState> abstractorClass;

	/** ClassName.methodName(IIV) => Abstractor Method */
	private final Map<String, AbstractorMethod> abstractorMethod;

	public AbstractorRegistry(TestCluster cluster, XmlRegistry xml) {
		abstractorClass = new HashMap<String, AbstractorObjectState>();
		abstractorMethod = new HashMap<String, AbstractorMethod>();

		for (String cName : cluster.getClasses()) {
			XmlClass cXml = xml.getXmlClass(cName);

			if (cXml != null) {
				{ // create state abstractor
					testful.model.xml.behavior.Behavior beh = getBehavior(cXml.getExtra());
					if (beh != null) {
						List<Abstractor> state = new ArrayList<Abstractor>();
						for (testful.model.xml.behavior.Abstraction abs : beh
								.getAbstraction()) {
							Abstractor abstractor = getAbstractor(abs);
							if (abstractor != null)
								state.add(abstractor);
						}
						try {
							abstractorClass.put(cName, new AbstractorObjectState(state.toArray(new Abstractor[state.size()])));
						} catch (Exception ex) {
							logger.warning(ex.getMessage());
						}
					}
				} // end of create state abstractor
				{ // create constructor abstractor
					for (testful.model.xml.XmlConstructor xmlCns : cXml.getConstructors()) {
						// create state abstractor
						testful.model.xml.behavior.Behavior beh = getBehavior(xmlCns.getExtra());
						String bytecodeName = BytecodeUtils.getBytecodeName(xmlCns);
						if (beh == null) abstractorMethod.put(cName + "." + bytecodeName, new AbstractorMethod(bytecodeName, false, null));
						else {
							List<Abstractor> method = new LinkedList<Abstractor>();
							for (testful.model.xml.behavior.Abstraction abs : beh
									.getAbstraction()) {
								Abstractor abstractor = getAbstractor(abs);
								if (abstractor != null)
									method.add(abstractor);
							}
							abstractorMethod.put(cName + "." + bytecodeName, new AbstractorMethod(bytecodeName, false, method.toArray(new Abstractor[method.size()])));
						}
					}
				} // end of create constructor abstractor
				{ // create method abstractor
					for (testful.model.xml.XmlMethod xmlMeth : cXml.getMethods()) {
						// create state abstractor
						testful.model.xml.behavior.Behavior beh = getBehavior(xmlMeth.getExtra());
						String bytecodeName = BytecodeUtils.getBytecodeName(xmlMeth);

						if (beh == null) abstractorMethod.put(cName + "." + bytecodeName, new AbstractorMethod(bytecodeName, xmlMeth.getKind() == Kind.STATIC, null));
						else {
							List<Abstractor> method = new LinkedList<Abstractor>();
							for (testful.model.xml.behavior.Abstraction abs : beh.getAbstraction()) {
								Abstractor abstractor = getAbstractor(abs);
								if (abstractor != null)
									method.add(abstractor);
							}
							abstractorMethod.put(cName + "." + bytecodeName, new AbstractorMethod(bytecodeName, xmlMeth.getKind() == Kind.STATIC, method.toArray(new Abstractor[method.size()])));
						}
					}
				} // end of create method abstractor
			}

		} // end foreach class in the test cluster
	}

	@SuppressWarnings("unchecked")
	private static Abstractor getAbstractor(testful.model.xml.behavior.Abstraction abs) {
		try {
			if(abs.getFunction() == null || abs.getFunction().trim().length() <= 0) return new Abstractor(abs.getExpression(), abs.getParameters());
			else {
				Class<? extends Abstractor> abstractor = (Class<? extends Abstractor>) Class.forName(abs.getFunction().trim());
				java.lang.reflect.Constructor<? extends Abstractor> cns = abstractor.getConstructor(String.class, String.class);
				return cns.newInstance(abs.getExpression(), abs.getParameters());
			}
		} catch(Exception e) {
			logger.log(Level.WARNING, e.getMessage(), e.getMessage());
			TestFul.debug(e);
			return null;
		}
	}

	private static testful.model.xml.behavior.Behavior getBehavior(List<testful.model.xml.Extra> extras) {
		for(testful.model.xml.Extra extra : extras)
			if(extra instanceof testful.model.xml.behavior.Behavior)
				return (testful.model.xml.behavior.Behavior) extra;

		return null;
	}

	/**
	 * Returns the object-state abstractor for a certain type of object
	 * @param className the type of the object (i.e., its class name)
	 * @return the abstractor to be used for the object state
	 */
	public AbstractorObjectState getAbstractorClass(String className) {
		return abstractorClass.get(className);
	}

	/**
	 * Returns the abstractor to be used for a particular method
	 * @param methodName the name of the method
	 * @return the abstractor to be used for the method
	 */
	public AbstractorMethod getAbstractorMethod(String methodName) {
		return abstractorMethod.get(methodName);
	}
}
