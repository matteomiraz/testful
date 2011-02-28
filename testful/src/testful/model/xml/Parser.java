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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationEventLocator;

import testful.ConfigCut;
import testful.IConfigCut;
import testful.IConfigProject;
import testful.TestFul;
import testful.model.xml.behavior.Abstraction;
import testful.model.xml.behavior.Behavior;
import testful.runner.ClassType;
import testful.runner.DataFinderImpl;
import testful.runner.RemoteClassLoader;

public class Parser {

	/**
	 * TODO describe me!
	 */
	private static final boolean CREATE_BEHAVIORAL_ENTRIES = true;

	private static final Logger logger = Logger.getLogger("testful.model.xml");

	private static final Class<?>[] CLASSES = { XmlClass.class, Behavior.class };

	public static final Parser singleton;
	static {
		Parser tmp = null;
		try {
			tmp = new Parser();
		} catch(JAXBException e) {
			logger.log(Level.SEVERE, "Problem creating the XML parser: " + e.getMessage(), e);
		}
		singleton = tmp;
	}

	private final JAXBContext jaxbContext;
	private final Unmarshaller unmarshaller;
	private final Marshaller marshaller;

	private Parser() throws JAXBException {
		jaxbContext = JAXBContext.newInstance(CLASSES);

		unmarshaller = jaxbContext.createUnmarshaller();
		unmarshaller.setEventHandler(new TestfulValidationEventHandler());

		marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
	}

	public XmlClass parse(IConfigProject config, String fullQualifiedClassName) throws JAXBException {
		File file = new File(config.getDirSource(), fullQualifiedClassName.replace('.', File.separatorChar) + ".xml");
		return (XmlClass) unmarshaller.unmarshal(file);
	}

	public void encode(String className, XmlClass xml, IConfigProject config) throws JAXBException {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(new File(config.getDirSource(), className.replace('.', File.separatorChar) + ".xml"));
			marshaller.marshal(xml, out);
		} catch(IOException e) {
			logger.log(Level.WARNING, "Cannot write to file: " + e.getMessage(), e);
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(IOException e) {
					logger.log(Level.WARNING, "Cannot close the file: " + e.getMessage(), e);
				}
			}
		}
	}

	public static void main(String[] args) throws JAXBException, IOException {
		IConfigCut config = new ConfigCut();

		TestFul.parseCommandLine(config, args, Parser.class, "XML Creator");

		final RemoteClassLoader loader = new RemoteClassLoader(new DataFinderImpl(new ClassType(config)));

		try {
			Class<?> clazz = loader.loadClass(config.getCut());
			XmlClass xmlClass = XmlClass.create(clazz);

			if(CREATE_BEHAVIORAL_ENTRIES) {
				final Abstraction abs = new Abstraction();
				abs.setFunction("testful.coverage.behavior.AbstractorNumber");
				abs.setExpression("this.size()");
				abs.setParameters("0:10");

				Behavior beh = new Behavior();
				beh.getAbstraction().add(abs);

				xmlClass.getExtra().add(beh);

				for (XmlConstructor xCns : xmlClass.getConstructors())
					xCns.getExtra().add(new Behavior());

				for (XmlMethod xMet : xmlClass.getMethods())
					xMet.getExtra().add(new Behavior());
			}

			singleton.encode(config.getCut(), xmlClass, config);
		} catch(ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Class not found: " + e.getMessage(), e);
			System.exit(1);
		}

		System.exit(0);
	}

	private static class TestfulValidationEventHandler implements ValidationEventHandler {

		@Override
		public boolean handleEvent(ValidationEvent ve) {
			if(ve.getSeverity() == ValidationEvent.FATAL_ERROR || ve.getSeverity() == ValidationEvent.ERROR) {
				ValidationEventLocator locator = ve.getLocator();
				//Print message from valdation event

				logger.warning(
						"Invalid xml document: " + locator.getURL() + "\n" +
						"Error: " + ve.getMessage() + "\n" +
						"Error at column " + locator.getColumnNumber() + ", line " + locator.getLineNumber()
				);
			}
			return true;
		}
	}
}
