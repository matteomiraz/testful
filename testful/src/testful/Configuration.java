package testful;

import java.io.File;

/**
 * Stores the configuration of the project being tested
 * 
 * @author matteo
 */
public class Configuration {

	/** the base directory (e.g. $HOME/workspace/project/ ) */
	private final String dirBase;

	/**
	 * Create the description for a project
	 * @param baseDir the base directory (e.g. $HOME/workspace/project/ )
	 */
	public Configuration(String baseDir) {
		if(baseDir == null) dirBase = "cut";
		else dirBase = baseDir;
	}

	protected Configuration(Configuration c) {
		dirBase = c.dirBase;
		cut = c.cut;
	}

	private String cut;
	public void setCut(String cut) throws TestfulException {
		if(cut == null || cut.trim().length() <= 0) throw new TestfulException("You must specify the class name");
		this.cut = cut.trim();
	}

	public String getCut() {
		return cut;
	}

	public String getDirBase() {
		return dirBase;
	}

	/** source directory */
	public String getDirSource() {
		return dirBase + File.separator + "src";
	}

	/** classes compiled by the user */
	public String getDirVanilla() {
		return dirBase + File.separator + "bin";
	}

	/** classes compiled by JML (with contracts, without annotations) */
	public String getDirJml() {
		return dirBase + File.separator + "jml-compiled";
	}

	/** classes instrumented */
	public String getDirInstrumented() {
		return dirBase + File.separator + "instrumented";
	}

	/** generated tests */
	public String getDirGeneratedTests() {
		return dirBase + File.separator + "genTests";
	}
}