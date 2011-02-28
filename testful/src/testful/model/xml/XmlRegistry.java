/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011 Matteo Miraz
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

package testful.model.xml;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;

import testful.IConfigProject;
import testful.model.Clazz;

/**
 * For each class allows one to get its XML description.
 * @author matteo
 */
public class XmlRegistry {

	private static Logger logger = Logger.getLogger("testful.model.xml.XmlRegistry");

	/** cache of XML classes */
	private final Map<String, XmlClass> xmlClasses = new HashMap<String, XmlClass>();

	/** I need to read XML files from the source directory... */
	private final IConfigProject config;

	/** I need java classes ( {@link Class} ) to auto-generate missing XML descriptors */
	private final ClassLoader classLoader;

	public XmlRegistry(ClassLoader classLoader, IConfigProject config) {
		this.classLoader = classLoader;
		this.config = config;
	}

	/**
	 * Returns the XML description for the class, either by reading it from the proper file or creating one on-the-fly.
	 * @param className the name of the class
	 * @return the XML of the class
	 */
	public XmlClass getXmlClass(String className) {

		XmlClass xmlClass = xmlClasses.get(className);
		if(xmlClass == null) {

			try {
				xmlClass = Parser.singleton.parse(config, className);
			} catch (JAXBException e) {

				// consider java.lang.String as a primitive type
				if(className.equals("java.lang.String")) return null;

				Class<?> javaClass;
				try {
					javaClass = classLoader.loadClass(className);
					if(javaClass.isPrimitive()) return null;
				} catch (ClassNotFoundException e1) {
					logger.log(Level.WARNING, "Cannot load class " + className + ": " + e1, e1);
					return null;
				}

				if(!className.startsWith("java.") && !className.startsWith("javax.") && !className.startsWith("sun."))
					logger.log(Level.WARNING, "Cannot parse XML descriptor of class " + className + ": " + e.getCause());

				xmlClass = XmlClass.create(javaClass);
			}

			xmlClasses.put(className, xmlClass);
		}

		return xmlClass;
	}

	/**
	 * Returns the XML description for the class, either by reading it from the proper file or creating one on-the-fly.
	 * @param c the clazz
	 * @return the XML of the class
	 */
	public XmlClass getXmlClass(Clazz c) {
		return getXmlClass(c.getClassName());
	}
}