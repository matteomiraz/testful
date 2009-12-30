package testful.model.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;

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
import testful.model.Methodz;
import testful.model.xml.XmlMethod.Kind;

public class Parser {

	/** Should I create the behavior element? */
	private static final boolean ADD_EMPTY_ELEMENTS = false;

	private static final Class<?>[] CLASSES = { XmlClass.class };

	public static final Parser singleton;
	static {
		Parser tmp = null;
		try {
			tmp = new Parser();
		} catch(JAXBException e) {
			System.err.println("FATAL ERROR: " + e);
			e.printStackTrace();
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

	public void encode(XmlClass xml, IConfigProject config) throws JAXBException {
		FileOutputStream out = null;

		try {
			out = new FileOutputStream(new File(config.getDirSource(), xml.getName().replace('.', File.separatorChar) + ".xml"));
			marshaller.marshal(xml, out);
		} catch(IOException e) {
			System.err.println("Cannot write to file: " + e);
		} finally {
			if(out != null) {
				try {
					out.close();
				} catch(IOException e) {
					System.err.println("Cannot close the xml file: " + e);
				}
			}
		}

	}

	public XmlClass createClassModel(Class<?> c) {
		XmlClass xmlClass = testful.model.xml.ObjectFactory.factory.createClass();
		xmlClass.setName(c.getCanonicalName());

		if(ADD_EMPTY_ELEMENTS) xmlClass.getAux().add(new XmlAux());

		for(Constructor<?> cns : c.getConstructors()) {
			XmlConstructor xcns = testful.model.xml.ObjectFactory.factory.createConstructor();

			for(Class<?> p : cns.getParameterTypes()) {
				XmlParameter xmlParam = testful.model.xml.ObjectFactory.factory.createParameter();
				xmlParam.setType(p.getCanonicalName());
				xmlParam.setCaptured(p.isArray());
				xmlParam.setMutated(p.isArray());
				xmlParam.setExposedByReturn(false);
				xmlParam.setExchangeStateWith("");
				xcns.getParameter().add(xmlParam);
			}

			xmlClass.getConstructor().add(xcns);
		}

		for(Method meth : c.getMethods()) {
			if(Methodz.toSkip(meth)) {
				System.out.println("Skipping " + meth.getName());
				continue;
			}

			XmlMethod xmeth = testful.model.xml.ObjectFactory.factory.createMethod();

			xmeth.setExposeState(meth.getReturnType().isArray());
			xmeth.setName(meth.getName());
			if(Modifier.isStatic(meth.getModifiers())) xmeth.setKind(Kind.STATIC);
			else xmeth.setKind(Kind.MUTATOR);

			for(Class<?> p : meth.getParameterTypes()) {
				XmlParameter xmlParam = testful.model.xml.ObjectFactory.factory.createParameter();
				xmlParam.setType(p.getCanonicalName());
				xmlParam.setCaptured(p.isArray());
				xmlParam.setMutated(p.isArray());
				xmlParam.setExposedByReturn(p.isArray() && meth.getReturnType().isArray());
				xmlParam.setExchangeStateWith("");
				xmeth.getParameter().add(xmlParam);
			}

			xmlClass.getMethod().add(xmeth);
		}

		return xmlClass;
	}

	public static void main(String[] args) throws JAXBException, IOException {
		IConfigCut config = new ConfigCut();

		TestFul.parseCommandLine(config, args, Parser.class);

		final URLClassLoader loader = new URLClassLoader(new URL[] { config.getDirCompiled().toURI().toURL() });

		try {
			Class<?> clazz = loader.loadClass(config.getCut());
			XmlClass xmlClass = singleton.createClassModel(clazz);
			singleton.encode(xmlClass, config);
		} catch(ClassNotFoundException e) {
			System.err.println("Class not found: " + e);
		}
	}

	private static class TestfulValidationEventHandler implements ValidationEventHandler {

		@Override
		public boolean handleEvent(ValidationEvent ve) {
			if(ve.getSeverity() == ValidationEvent.FATAL_ERROR || ve.getSeverity() == ValidationEvent.ERROR) {
				ValidationEventLocator locator = ve.getLocator();
				//Print message from valdation event
				System.out.println("Invalid booking document: " + locator.getURL());
				System.out.println("Error: " + ve.getMessage());
				//Output line and column number
				System.out.println("Error at column " + locator.getColumnNumber() + ", line " + locator.getLineNumber());
			}
			return true;
		}
	}
}
