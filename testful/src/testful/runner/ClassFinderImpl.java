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

package testful.runner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.IConfigProject;
import testful.utils.ByteReader;
import testful.utils.JavaUtils;

/**
 * Retrieve the bytecode of classes.
 *
 * @author matteo
 */
public class ClassFinderImpl implements ClassFinder {

	private static Logger logger = Logger.getLogger("testful.executor.classloader");
	private static final ClassLoader classLoader = ClassFinderImpl.class.getClassLoader();

	private final String key;
	private final File[] where;
	private final Collection<ClassData> data = new ArrayList<ClassData>();

	public ClassFinderImpl(IConfigProject config) {
		this(JavaUtils.merge(config.getDirInstrumented(), config.getDirCompiled(), config.getLibraries()));
	}

	public ClassFinderImpl(File ... where) {
		this.where = where;

		key = UUID.randomUUID().toString();

		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch(Exception e) {
			logger.warning("Unable to use the classloader " + toString() + " in a remote context: " + e);
		}
	}

	public void addClassData(ClassData d) {
		data.add(d);
	}

	@Override
	public String getKey() throws RemoteException {
		return key;
	}

	@Override
	public synchronized byte[] getClass(String name) throws ClassNotFoundException {
		// try looking in the where class directories
		{
			try {
				URL classURL = searchClassFile(name);
				byte[] ret = ByteReader.readBytes(classURL.openStream());
				logger.finer("(" + key + ") serving class " + name + " from " + classURL);

				for (ClassData datum : data)
					datum.load(name, classURL);

				return ret;
			} catch(FileNotFoundException e) {
				// not found
			} catch(IOException e) {
				// this should not happen!
				logger.log(Level.WARNING, "I/O error: " + e, e);
			}
		}

		// try using the classLoader
		{
			URL resource = classLoader.getResource(name.replace('.', '/') + ".clazz");
			if(resource != null) {
				try {
					byte[] ret = ByteReader.readBytes(resource.openStream());
					if(ret != null) {
						logger.finer("(" + key + ") serving class " + name + " from " + resource);
						return ret;
					}
				} catch(IOException e) {
					logger.log(Level.WARNING, "(" + key + ") " + "cannot load class " + name + " from " + resource + ": " + e.getMessage(), e);
				}
			}
		}

		final String msg = "cannot find class " + name;
		logger.warning("(" + key + ") " + msg);
		throw new ClassNotFoundException(msg);
	}

	/**
	 * Looks for the class with the specified name in the paths specified by the user, and
	 * returns the corresponding file. The returned value does exist and it is readable.
	 * @param name the full.qualified.Class$Name
	 * @return the file that contains the bytecode
	 * @throws FileNotFoundException if the file is not found
	 */
	private URL searchClassFile(String name) throws FileNotFoundException {
		final String fileName = name.replace('.', File.separatorChar) + ".class";
		final String urlName = name.replace('.', '/') + ".class";

		for(File element : where) {

			// if it is a class directory, look for a .class file
			if(element.isDirectory()) {
				File w = new File(element, fileName);
				if(w.exists()) {
					if(w.canRead()) {
						try {
							return w.toURI().toURL();
						} catch (MalformedURLException e) {
							logger.log(Level.WARNING, "(" + key + ") found " + name + " in " + w.getAbsolutePath() + ", but cannot transform to a valid URL", e);
						}
					} else {
						logger.warning("(" + key + ") found " + name + " in " + w.getAbsolutePath() + ", but cannot read it");
					}
				}
			}

			// if it is a jar, create the jar:<element>!/urlClass URL
			if(element.isFile()) {
				if(!element.getName().endsWith(".jar"))
					logger.warning("(" + key + ") classpath entry " + element.getAbsolutePath() + " is a file, but it does not ends with .jar");
				else if(!element.canRead())
					logger.warning("(" + key + ") but cannot read classpath entry " + element.getAbsolutePath());
				else {
					try {
						final URL url = new URL("jar:" + element.toURI().toURL().toString() + "!/" + urlName);

						// try to access the file
						url.openStream().close();

						return url;
					} catch (MalformedURLException e) {
						logger.log(Level.WARNING, "(" + key + ") found " + name + " in " + element.getAbsolutePath() + ", but cannot transform to a valid URL", e);
					} catch (IOException e) {
					}
				}
			}
		}

		throw new FileNotFoundException();
	}
}
