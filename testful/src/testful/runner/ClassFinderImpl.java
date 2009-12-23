package testful.runner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.UUID;
import java.util.logging.Logger;

import testful.utils.ByteReader;

/**
 * Retrieve the bytecode of classes.
 * 
 * @author matteo
 */
public class ClassFinderImpl implements ClassFinder {

	private static Logger logger = Logger.getLogger("testful.executor.classloader");

	private final File[] where;
	private final String key;

	public ClassFinderImpl(File ... where) {
		this.where = where;
		key = UUID.randomUUID().toString();

		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch(Exception e) {
			System.err.println("Unable to use the classloader " + toString() + " in a remote context: " + e);
		}
	}

	@Override
	public synchronized byte[] getClass(String name) throws ClassNotFoundException {
		String fileName = name.replace('.', File.separatorChar) + ".class";

		for(File element : where)
			try {
				File w = new File(element, fileName);
				byte[] ret = ByteReader.readBytes(w);
				if(ret != null) {
					logger.fine("(" + key + ") serving class " + name + " from " + w);
					return ret;
				}
			} catch(IOException e) {
			}

			fileName = name.replace('.', '/') + ".clazz";
			
			URL resource = ClassLoader.getSystemResource(fileName);

			if(resource != null)
				try {
					byte[] ret = ByteReader.readBytes(resource.openStream());
					if(ret != null) {
						logger.fine("(" + key + ") serving class " + name + " from " + resource);
						return ret;
					}
				} catch(IOException e) {
					String msg = "cannot load class " + name + " from " + resource;
					System.err.println(msg);
					logger.warning("(" + key + ") " + msg);

					throw new ClassNotFoundException(msg, e);
				}

			String msg = "cannot find class " + name;
			System.err.println(msg);
			logger.warning("(" + key + ") " + msg);

			throw new ClassNotFoundException(msg);
	}

	@Override
	public String getKey() throws RemoteException {
		return key;
	}
}
