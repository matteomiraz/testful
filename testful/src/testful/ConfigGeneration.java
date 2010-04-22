package testful;

import java.io.File;

public class ConfigGeneration extends ConfigCut
implements IConfigProject.Args4j, IConfigCut.Args4j, IConfigGeneration.Args4j {

	/** generated test directory */
	private File dirGeneratedTests;

	/** should I reload all classes every new test? */
	private boolean reload;

	/** How much time do I have? (in seconds) */
	private int time;

	/** Can I use the cache? */
	private boolean cache;

	/** How many variables there are for each type? */
	private int numVar;

	/** How many variables there are for the CUT type? */
	private int numVarCut;

	/** What is the maximum length of the test? */
	private int maxTestLen;


	public ConfigGeneration() {
		dirGeneratedTests = new File("genTests");
		reload = false;

		time=600;
		cache=false;
		numVar=4;
		numVarCut=4;
		maxTestLen=10000;
	}

	/* (non-Javadoc)
	 * @see testful.IConfigGeneration#setReload(boolean)
	 */
	@Override
	public void setReload(boolean reload) {
		this.reload = reload;
	}

	/* (non-Javadoc)
	 * @see testful.IConfigGeneration#isReload()
	 */
	@Override
	public boolean isReload() {
		return reload;
	}

	/* (non-Javadoc)
	 * @see testful.IConfigGeneration#setDirGeneratedTests(java.io.File)
	 */
	@Override
	public void setDirGeneratedTests(File dirGeneratedTests) {
		this.dirGeneratedTests = dirGeneratedTests;
	}

	/* (non-Javadoc)
	 * @see testful.IConfigGeneration#getDirGeneratedTests()
	 */
	@Override
	public File getDirGeneratedTests() {
		if(!dirGeneratedTests.isAbsolute()) dirGeneratedTests = new File(getDirBase(), dirGeneratedTests.getPath()).getAbsoluteFile();
		return dirGeneratedTests;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration#getTime()
	 */
	@Override
	public int getTime() {
		return time;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration.Args4j#setTime(int)
	 */
	@Override
	public void setTime(int time) {
		this.time = time;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration#isCache()
	 */
	@Override
	public boolean isCache() {
		return cache;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration.Args4j#setCache(boolean)
	 */
	@Override
	public void setCache(boolean cache) {
		this.cache = cache;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration#getNumVar()
	 */
	@Override
	public int getNumVar() {
		return numVar;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration.Args4j#setNumVar(int)
	 */
	@Override
	public void setNumVar(int numVar) {
		this.numVar = numVar;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration#getNumVarCut()
	 */
	@Override
	public int getNumVarCut() {
		return numVarCut;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration.Args4j#setNumVarCut(int)
	 */
	@Override
	public void setNumVarCut(int numVarCut) {
		this.numVarCut = numVarCut;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration#getMaxTestLen()
	 */
	@Override
	public int getMaxTestLen() {
		return maxTestLen;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigGeneration.Args4j#setMaxTestLen(int)
	 */
	@Override
	public void setMaxTestLen(int maxTestLen) {
		this.maxTestLen = maxTestLen;
	}
}
