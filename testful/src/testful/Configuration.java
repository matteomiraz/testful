package testful;

import java.io.File;

public class Configuration {

	private final String dirBase;

	public Configuration(String baseDir) {
		if(baseDir == null) this.dirBase = "cut";
		else this.dirBase = baseDir;
	}
	
	public Configuration() {
		this(null);
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
}