package testful.runner;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Map.Entry;
import java.util.logging.Logger;

import testful.utils.CachingMap;
import testful.utils.CachingMap.Cacheable;

public class ClassFinderCaching implements ClassFinder {

	private static Logger logger = Logger.getLogger("testful.executor.classloader");

	private final static int MAX_ELEMS = 1000;
	private final static long MIN_AGE = 15 * 60 * 1000; // 15 min
	private final static long MIN_UNUSED = 5 * 60 * 1000; //  5 min

	private final String key;
	private final CachingMap<String, byte[]> cache;
	private final ClassFinder finder;

	public ClassFinderCaching(ClassFinder finder) throws RemoteException {

		cache = new CachingMap<String, byte[]>(MAX_ELEMS, MIN_AGE, MIN_UNUSED);
		this.finder = finder;
		key = finder.getKey();

		try {
			UnicastRemoteObject.exportObject(this, 0);
		} catch(Exception e) {
			System.err.println("Unable to use the classloader " + toString() + " in a remote context: " + e);
		}

		logger.fine("Created classFinder with key: " + key);
	}

	@Override
	public byte[] getClass(String name) throws ClassNotFoundException, RemoteException {
		if(name == null) {
			new Exception("WARN: ClassFinderCaching.getClass(name=null)").printStackTrace();
			return new byte[0];
		}

		CachingMap.Cacheable<byte[]> tmp = cache.get(name);
		if(tmp != null) {
			logger.fine("(" + key + ") serving cached class " + name);
			return tmp.getElement();
		}

		try {
			byte[] buff = finder.getClass(name);
			cache.put(name, new Cacheable<byte[]>(buff));
			logger.fine("(" + key + ") serving retrieved class " + name);
			return buff;
		} catch(RemoteException e) {
			logger.warning("(" + key + ") cannot retrieve class " + name);
			throw new ClassNotFoundException("Cannot retrieve the class " + name, e);
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.getClass().getCanonicalName()).append("\n");
		for(Entry<String, Cacheable<byte[]>> entry : cache.entrySet())
			sb.append("  ").append(entry.getKey()).append(" (").append(entry.getValue().getElement().length).append(" byte)\n");

		return sb.toString();
	}

	@Override
	public String getKey() throws RemoteException {
		return key;
	}
}
