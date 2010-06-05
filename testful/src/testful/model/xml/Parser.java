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
import java.util.Collection;
import java.util.TreeSet;
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
import testful.runner.ClassFinderImpl;
import testful.runner.TestfulClassLoader;

public class Parser {

	private static final Logger logger = Logger.getLogger("testful.model.xml");

	private static final Class<?>[] CLASSES = { XmlClass.class };

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

	XmlClass parse(IConfigProject config, String fullQualifiedClassName) throws JAXBException {
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
		testful.TestFul.setupLogging(config);

		final TestfulClassLoader loader = new TestfulClassLoader(new ClassFinderImpl(config));

		try {
			Class<?> clazz = loader.loadClass(config.getCut());
			XmlClass xmlClass = XmlClass.create(clazz);

			xmlClass.getInstrument().addAll(getProjectClasses(loader, config));

			singleton.encode(config.getCut(), xmlClass, config);
		} catch(ClassNotFoundException e) {
			logger.log(Level.SEVERE, "Class not found: " + e.getMessage(), e);
			System.exit(1);
		}

		System.exit(0);
	}

	/**
	 * Builds the list of classes in the current project
	 * @param config the configuration of the current project
	 * @return the collection with all the names of the classes in the current project
	 */
	private static Collection<String> getProjectClasses(ClassLoader loader, IConfigProject config) {
		Collection<String> ret = new TreeSet<String>();

		getProjectClasses(loader, ret, config.getDirCompiled(), config.getDirCompiled().getAbsolutePath());

		return ret;
	}

	/**
	 * Recursive method to get the list of classes in the current project
	 * @param ret the list of classes being built
	 * @param dir the directory to analyze
	 * @param base the base directory
	 */
	private static void getProjectClasses(ClassLoader loader, Collection<String> ret, File dir, String base) {
		for (File f : dir.listFiles()) {
			if(f.isDirectory()) getProjectClasses(loader, ret, f, base);
			else if(f.isFile() && f.getName().endsWith(".class")) {
				final String fullName = f.getAbsolutePath();
				final String className = fullName.substring(base.length()+1, fullName.length() - 6).replace(File.separatorChar, '.');

				try {
					Class<?> c = loader.loadClass(className);

					if(!c.isInterface()) {
						ret.add(className);
					}

				} catch (Throwable e) {
					logger.warning("Cannot load class " + className + ": " + e);
				}
			}
		}
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
