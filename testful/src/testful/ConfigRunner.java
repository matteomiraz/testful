package testful;

import java.util.ArrayList;
import java.util.List;

public class ConfigRunner implements IConfigRunner.Args4j {

	private List<String> remote = new ArrayList<String>();

	private boolean localEvaluation = true;

	/* (non-Javadoc)
	 * @see testful.IConfigRunner#getRemote()
	 */
	@Override
	public List<String> getRemote() {
		return remote;
	}

	/*
	 * (non-Javadoc)
	 * @see testful.IConfigRunner.Args4j#addRemote(java.lang.String)
	 */
	@Override
	public void addRemote(String remote) {
		this.remote.add(remote);
	}

	/* (non-Javadoc)
	 * @see testful.IConfigRunner#isLocalEvaluation()
	 */
	@Override
	public boolean isLocalEvaluation() {
		return localEvaluation;
	}

	/* (non-Javadoc)
	 * @see testful.IConfigRunner#disableLocalEvaluation(boolean)
	 */
	@Override
	public void disableLocalEvaluation(boolean disableLocalEvaluation) {
		localEvaluation = !disableLocalEvaluation;
	}
}
