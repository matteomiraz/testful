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
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import testful.IConfigProject;
import testful.utils.ByteReader;

/**
 * Retrieve the bytecode of classes.
 *
 * @author matteo
 */
public class ClassFinderImpl implements ClassFinder {

	private static Logger logger = Logger.getLogger("testful.executor.classloader");
	private static final ClassLoader classLoader = ClassFinderImpl.class.getClassLoader();

	private final File[] where;
	private final String key;

	public ClassFinderImpl(IConfigProject config) {
		this(config.getDirInstrumented(), config.getDirCompiled());
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

	@Override
	public String getKey() throws RemoteException {
		return key;
	}

	@Override
	public synchronized byte[] getClass(String name) throws ClassNotFoundException {
		String fileName = name.replace('.', File.separatorChar) + ".class";

		// try looking in the where class directories
		{
			try {
				File w = searchClassFile(name);
				byte[] ret = ByteReader.readBytes(w);
				if(ret != null) {
					logger.finer("(" + key + ") serving class " + name + " from " + w);
					return ret;
				}
			} catch(IOException e) {
				// logger.warning("(" + key + ") " + "cannot load class " + name + " from " + w + ": " + e.getMessage());
			}
		}

		// try using the classLoader
		fileName = name.replace('.', '/') + ".clazz";
		{
			URL resource = classLoader.getResource(fileName);
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

		logger.warning("(" + key + ") " + "cannot find class " + name);
		throw new ClassNotFoundException("cannot find class " + name);
	}

	/**
	 * Looks for the class with the specified name in the paths specified by the user, and
	 * returns the corresponding file. The returned value does exist and it is readable.
	 * @param name the full.qualified.Class$Name
	 * @return the file that contains the bytecode
	 * @throws FileNotFoundException if the file is not found
	 */
	private File searchClassFile(String name) throws FileNotFoundException {
		final String fileName = name.replace('.', File.separatorChar) + ".class";

		for(File element : where) {
			File w = new File(element, fileName);
			if(w.exists()) {
				if(w.canRead()) return w;
				else logger.warning("(" + key + ") found " + name + " in " + w.getAbsolutePath() + ", but cannot read it");
			}
		}

		throw new FileNotFoundException("cannot find class " + name);
	}
}
