package testful;

import java.io.File;

import org.kohsuke.args4j.Option;

public interface IConfigProject {

	/**
	 * Returns the base directory (e.g. $HOME/workspace/project/ )
	 * @return the base directory
	 */
	public File getDirBase();

	/**
	 * Returns the source directory (e.g. $HOME/workspace/project/src/ )
	 * @return the source directory
	 */
	public File getDirSource();

	/**
	 * Returns the compiled directory (e.g. $HOME/workspace/project/bin/ )
	 * @return the compiled directory
	 */
	public File getDirCompiled();

	/**
	 * Returns the directory containing contract-enabled binaries (e.g. $HOME/workspace/project/jml-compiled/ )
	 * @return the directory containing contract-enabled binaries
	 */
	public File getDirContracts();

	/**
	 * Returns the directory containing instrumented binaries (e.g. $HOME/workspace/project/instrumented/ )
	 * @return the directory containing instrumented binaries
	 */
	public File getDirInstrumented();

	/**
	 * Stores the configuration of the project being tested.
	 * This interface is usable with args4j.
	 * @author matteo
	 */
	public interface Args4j extends IConfigProject {

		/**
		 * Sets the project's base directory (e.g. $HOME/workspace/project/ )
		 * @param dirBase the project's base directory
		 */
		@Option(required = false, name = "-dir", usage = "Specify the project's base directory (default: the current directory)")
		public void setDirBase(File dirBase);

		/**
		 * Sets the directory containing source files.
		 * It can be both a path relative to the base directory (e.g., "bar" or "../foo/bar")
		 * or an absolute path (e.g., $HOME/workspace/foo/bar")
		 * @param dirSource the directory containing source files.
		 */
		@Option(required = false, name = "-dirSource", usage = "Specify the source directory (default: src)")
		public void setDirSource(File dirSource);

		/**
		 * Sets the directory containing compiled files.
		 * It can be both a path relative to the base directory (e.g., "bar" or "../foo/bar")
		 * or an absolute path (e.g., $HOME/workspace/foo/bar")
		 * @param dirCompiled the directory containing compiled files.
		 */
		@Option(required = false, name = "-dirCompiled", usage = "Specify the directory containing compiled files (default: bin)")
		public void setDirCompiled(File dirCompiled);

		/**
		 * Sets the directory containing contract-enabled compiled files.
		 * It can be both a path relative to the base directory (e.g., "bar" or "../foo/bar")
		 * or an absolute path (e.g., $HOME/workspace/foo/bar")
		 * @param dirContracts the directory containing contract-enabled compiled files.
		 */
		@Option(required = false, name = "-dirContracts", usage = "Specify the directory with contract-enabled compiled files (default: jml-compiled)")
		public void setDirContracts(File dirContracts);

		/**
		 * Sets the directory containing instrumented files.
		 * It can be both a path relative to the base directory (e.g., "bar" or "../foo/bar")
		 * or an absolute path (e.g., $HOME/workspace/foo/bar")
		 * @param dirInstrumented the directory containing instrumented files.
		 */
		@Option(required = false, name = "-dirInstrumented", usage = "Specify the directory with instrumented files (default: instrumented)")
		public void setDirInstrumented(File dirInstrumented);
	}
}