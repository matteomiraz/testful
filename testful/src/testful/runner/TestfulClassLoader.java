package testful.runner;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Set;

import testful.utils.ElementWithKey;

/**
 * @testful.classloader System class loader
 * @author matteo
 *
 */
public class TestfulClassLoader extends ClassLoader implements ElementWithKey<String> {

	private static final ClassLoader superClassLoader = TestfulClassLoader.class.getClassLoader();

	/** for these classes, use always the system classloader */
	private static final String[] SYSTEM_CLASSES = {
		"testful.coverage.TrackerDatum"
	};

	/** for these classes force to use the testful classloader */
	private static final String[] TESTFUL_CLASSES = {
		"testful.utils.Cloner",
		"testful.mutation.MutationExecutionManager",
		"testful.mutation.MutationExecutionManagerSingle",
		"testful.mutation.MutationExecutionManagerSingle$TestThread",
		"testful.mutation.MutationExecutionData",
		"testful.mutation.MutationCoverage",
		"testful.mutation.MutationCoverageSingle"
	};

	/** for these packages force to use the testful classloader */
	private static final String[] TESTFUL_PACKAGES = {
		"testful.coverage",
		"testful.model.executor",
		"org.apache.commons.jexl"
	};

	public static boolean canUseSystemClassLoader(String name) {
		for(String allow : SYSTEM_CLASSES)
			if(name.equals(allow)) return true;

		for(String forbid : TESTFUL_PACKAGES)
			if(name.startsWith(forbid)) return false;

		for(String forbid : TESTFUL_CLASSES)
			if(name.equals(forbid)) return false;

		return true;

	}

	private static long idGenerator = 0;

	private final long id;
	private final ClassFinder finder;
	private final Set<String> loaded;
	private boolean isWarmedUp = false;
	private final String key;

	public TestfulClassLoader(ClassFinder finder) throws RemoteException {
		super(superClassLoader);

		id = idGenerator++;
		loaded = new HashSet<String>();

		this.finder = finder;
		key = finder.getKey();
	}

	public TestfulClassLoader getNew() throws RemoteException {
		return new TestfulClassLoader(finder);
	}

	public ClassFinder getFinder() {
		return finder;
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		if(canUseSystemClassLoader(name) || loaded.contains(name)) return super.loadClass(name, resolve);

		return findClass(name);
	}

	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		try {
			byte[] b = finder.getClass(name);
			Class<?> c = defineClass(name, b, 0, b.length);
			loaded.add(name);
			return c;
		} catch(RemoteException e) {
			throw new ClassNotFoundException("Cannot retrieve the class " + name, e);
		}
	}

	public boolean isWarmedUp() {
		return isWarmedUp;
	}

	public void setWarmedUp(boolean isWarmedUp) {
		this.isWarmedUp = isWarmedUp;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + ":" + id;
	}

	@Override
	public TestfulClassLoader clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException("Clone not supported in TestfulClassLoader");
	}
}
