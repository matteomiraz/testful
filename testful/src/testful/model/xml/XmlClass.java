/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
/**
 * Contains the following information on a class:
 * <ul>
 * 	<li>The list of classes to instrument (when the current class is being tested)</li>
 * 	<li>The list of classes to include in the test cluster (when the current class is included in the test cluster)</li>
 *  <li>The list of constructors and methods of the class</li>
 * </ul>
 * @author matteo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://testful.sourceforge.net/schema/1.2/testful.xsd", name = "class", propOrder = { "cluster", "constructor", "method", "extra" })
@XmlRootElement(name = "class", namespace = "http://testful.sourceforge.net/schema/1.2/testful.xsd")
public class XmlClass {

	private static final Logger logger = Logger.getLogger("testful.model.xml");
	private static final boolean LOG_FINE = logger.isLoggable(Level.FINE);

	/** when the current class is included in the test cluster, include also these classes */
	@XmlElementWrapper(name="cluster", required=false, nillable=false)
	@XmlElement(name="class")
	protected SortedSet<String> cluster;

	@XmlElement(required = false)
	protected SortedSet<XmlConstructor> constructor;

	@XmlElement(required = false)
	protected SortedSet<XmlMethod> method;

	@XmlElement
	protected List<Extra> extra;

	/**
	 * Creates the default description of a class
	 * @param c the class to describe
	 * @return the XML description of the class
	 */
	public static XmlClass create(Class<?> c) {
		XmlClass xmlClass = testful.model.xml.ObjectFactory.factory.createClass();

		for(Constructor<?> cns : c.getConstructors())
			xmlClass.addConstructor(XmlConstructor.create(cns));

		for (Method meth : c.getMethods()) {
			if(!Modifier.isPublic(meth.getModifiers()) ||
					meth.isBridge() ||
					meth.isSynthetic()) continue;

			final Class<?> returnType = meth.getReturnType();
			final XmlMethod xmlMeth = xmlClass.getMethod(meth);

			// add the return type to the test cluster
			if(xmlMeth != null && !xmlMeth.isSkip()
					&& !returnType.isArray() // ISSUE #1: if you need array support, vote here: http://code.google.com/p/testful/issues/detail?id=1
					&& !returnType.isEnum()  // another bug: enum support!
					&& !returnType.equals(c)
					&& returnType != Void.TYPE && !returnType.isPrimitive()) {

				if(LOG_FINE) logger.fine("Including " + returnType.getName() + " in the test cluster");
				xmlClass.getCluster().add(returnType.getName());
			}

			xmlClass.addMethod(xmlMeth);
		}

		return xmlClass;
	}

	/**
	 * Returns the list of classes to add to the test cluster
	 * @return the list of classes to add to the test cluster
	 */
	public Collection<String> getCluster() {
		if(cluster == null) {
			// the cluster was not specified
			cluster = new TreeSet<String>();
		}
		return cluster;
	}

	/**
	 * Add a class to the test cluster
	 * @param className a class to be included in the test cluster
	 */
	public void addCluster(String className) {
		if(cluster == null) cluster = new TreeSet<String>();
		cluster.add(className);
	}

	/**
	 * Remove a class from the test cluster
	 * @param className the class to remove
	 */
	public void removeCluster(String className) {
		if(cluster != null)
			cluster.remove(className);
	}

	public Collection<XmlConstructor> getConstructors() {
		if(constructor == null) return Collections.emptySet();
		return constructor;
	}

	/**
	 * Get the XML descriptor for a constructor.
	 * If it is not in the XML file, a default descriptor is created on-the-fly.
	 * @param cns the constructor
	 * @return the descriptor of the constructor, or null if it should be skipped
	 */
	public XmlConstructor getConstructor(Constructor<?> cns) {

		if(constructor != null) {
			Class<?>[] params = cns.getParameterTypes();
			String[] paramsString = new String[params.length];

			for(int i = 0; i < params.length; i++)
				paramsString[i] = params[i].getName();

			for(XmlConstructor xmlCons : constructor) {
				List<XmlParameter> xmlParams = xmlCons.getParameter();
				if(xmlParams.size() != params.length) continue;

				boolean ok = true;
				for(int i = 0; i < params.length && ok; i++)
					if(!params[i].getName().equals(xmlParams.get(i).getType())) ok = false;
				if(ok) return xmlCons;
			}
		}

		final XmlConstructor xmlCns = XmlConstructor.create(cns);

		if(LOG_FINE) {
			if(xmlCns != null)
				logger.fine("Using default description for constructor " + cns);
			else
				logger.fine("Ignoring constructor " + cns);
		}

		return xmlCns;
	}

	/**
	 * Add the XML descriptor of a constructor.
	 * @param cns the description to add. If it is null, it is discarded.
	 */
	public void addConstructor(XmlConstructor cns) {
		if(cns == null) return;

		if(constructor == null) constructor = new TreeSet<XmlConstructor>();
		constructor.add(cns);
	}

	public Collection<XmlMethod> getMethods() {
		if(method == null) return Collections.emptySet();
		return method;
	}

	/**
	 * Get the XML descriptor for a method.
	 * If it is not in the XML file, a default descriptor is created on-the-fly.
	 * @param meth the constructor
	 * @return the descriptor of the method, or null if it should be skipped
	 */
	public XmlMethod getMethod(Method meth) {
		if (method != null) {
			final Class<?>[] params = meth.getParameterTypes();
			final String[] paramsString = new String[params.length];
			for (int i = 0; i < params.length; i++)
				paramsString[i] = params[i].getName();

			XmlMethod xmlMeth = getMethod(meth.getName(), paramsString);
			if(xmlMeth != null) return xmlMeth;
		}

		final XmlMethod xmlMeth = XmlMethod.create(meth);

		if(LOG_FINE) {
			if(xmlMeth != null)
				logger.fine("Using default description for method " + meth);
			else
				logger.fine("Ignoring method " + meth);
		}

		return xmlMeth;
	}

	public XmlMethod getMethod(final String name, final String[] params) {
		for (XmlMethod xmlMeth : method) {
			if (xmlMeth.getName().equals(name)) {
				final List<XmlParameter> xmlParams = xmlMeth.getParameter();
				if (params.length == xmlParams.size()) {

					boolean ok = true;
					for (int i = 0; i < params.length && ok; i++)
						ok = params[i].equals(xmlParams.get(i).getType());

					if (ok) return xmlMeth;
				}
			}
		}

		return null;
	}

	/**
	 * Add the XML descriptor of a method.
	 * @param meth the description to add. If it is null, it is discarded.
	 */
	public void addMethod(XmlMethod meth) {
		if(meth == null) return;

		if(method == null) method = new TreeSet<XmlMethod>();
		method.add(meth);
	}

	public List<Extra> getExtra() {
		if(extra == null) extra = new ArrayList<Extra>();
		return extra;
	}
}
