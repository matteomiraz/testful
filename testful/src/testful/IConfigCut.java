package testful;

import org.kohsuke.args4j.Option;

public interface IConfigCut extends IConfigProject {

	/**
	 * Returns the class under test
	 * @return the class under test
	 */
	public String getCut();

	public interface Args4j extends IConfigCut, IConfigProject.Args4j {

		/**
		 * Sets the class under test
		 * @param cut the class under test
		 * @throws TestfulException if the name is null or empty
		 */
		@Option(required = true, name = "-cut", usage = "The class to test", metaVar = "full.qualified.ClassName")
		public void setCut(String cut) throws TestfulException;
	}

}