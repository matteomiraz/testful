package testful;

import java.io.File;

/**
 * Stores the configuration of the project being tested
 * 
 * @author matteo
 */
public class ConfigProject implements IConfigProject.Args4j {

	/** the base directory (e.g. $HOME/workspace/project/ ) */
	private File dirBase;

	/** source directory */
	private File dirSource;

	/** directory with classes compiled by the user */
	private File dirCompiled;

	/** directory with classes with contracts, not instrumented */
	private File dirContracts;

	/** directory with classes instrumented */
	private File dirInstrumented;

	/** if not null, enables the logging in the specified directory */
	private File log;

	/** the logging level */
	private LogLevel logLevel = LogLevel.INFO;

	/** disable all the output to the console */
	private boolean quiet;

	public ConfigProject() {
		dirBase = new File(".");

		dirSource = new File("src");
		dirCompiled = new File("bin");
		dirContracts = new File("jml-compiled");
		dirInstrumented = new File("instrumented");
	}

	public ConfigProject(IConfigProject config) {
		dirBase = config.getDirBase();

		dirSource = config.getDirSource();
		dirCompiled = config.getDirCompiled();
		dirContracts = config.getDirContracts();
		dirInstrumented = config.getDirInstrumented();
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#getDirBase()
	 */
	@Override
	public File getDirBase() {
		return dirBase;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#setDirBase(java.io.File)
	 */
	@Override
	public void setDirBase(File dirBase) {
		this.dirBase = dirBase;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.ProjectConfig#getDirSource()
	 */
	@Override
	public File getDirSource() {
		if(!dirSource.isAbsolute()) dirSource = new File(dirBase, dirSource.getPath()).getAbsoluteFile();
		return dirSource;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.ProjectConfig#setDirSource(java.io.File)
	 */
	@Override
	public void setDirSource(File dirSource) {
		this.dirSource = dirSource;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#getDirCompiled()
	 */
	@Override
	public File getDirCompiled() {
		if(!dirCompiled.isAbsolute()) dirCompiled = new File(dirBase, dirCompiled.getPath()).getAbsoluteFile();
		return dirCompiled;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#setDirCompiled(java.io.File)
	 */
	@Override
	public void setDirCompiled(File dirVanilla) {
		dirCompiled = dirVanilla;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#getDirContracts()
	 */
	@Override
	public File getDirContracts() {
		if(!dirContracts.isAbsolute()) dirContracts = new File(dirBase, dirContracts.getPath()).getAbsoluteFile();
		return dirContracts;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#setDirContracts(java.io.File)
	 */
	@Override
	public void setDirContracts(File dirJml) {
		dirContracts = dirJml;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#getDirInstrumented()
	 */
	@Override
	public File getDirInstrumented() {
		if(!dirInstrumented.isAbsolute()) dirInstrumented = new File(dirBase, dirInstrumented.getPath()).getAbsoluteFile();
		return dirInstrumented;
	}

	/* (non-Javadoc)
	 * @see testful.ProjectConfig#setDirInstrumented(java.io.File)
	 */
	@Override
	public void setDirInstrumented(File dirInstrumented) {
		this.dirInstrumented = dirInstrumented;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigProject#getLog()
	 */
	@Override
	public File getLog() {
		if(log == null) return null;

		if(!log.isAbsolute()) log = new File(getDirBase(), log.getPath()).getAbsoluteFile();
		return log;
	}

	@Override
	public void setLog(File log) {
		this.log = log;
	}

	@Override
	public void setQuiet(boolean quiet) {
		this.quiet = quiet;
	}

	@Override
	public boolean isQuiet() {
		return quiet;
	}

	@Override
	public void setLogLevel(LogLevel logLevel) {
		this.logLevel = logLevel;
	}

	@Override
	public LogLevel getLogLevel() {
		return logLevel;
	}
}