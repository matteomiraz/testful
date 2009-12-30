package testful;

import java.util.List;

import org.kohsuke.args4j.Option;

public interface IConfigRunner {

	/**
	 * Get the list of remote runners
	 * @return the list of remote runners
	 */
	public List<String> getRemote();

	/**
	 * Check if the local evaluation is enabled
	 * @return true if it is possible to execute tests locally
	 */
	public boolean isLocalEvaluation();

	public interface Args4j extends IConfigRunner {
		@Option(required = false, name = "-remote", multiValued = true, usage = "Use the specified remote evaluator")
		public void addRemote(String remote);

		@Option(required = false, name = "-noLocal", usage = "Do not use local evaluators")
		public void disableLocalEvaluation(boolean disableLocalEvaluation);
	}
}