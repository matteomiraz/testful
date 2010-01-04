package testful.coverage;

import java.io.Serializable;

import testful.utils.ElementWithKey;

/**
 * TrackerDatum represents a quantum of data usable by trackers.<br/>
 * Instances of this class should be given to the execution manager, which will
 * setup properly the execution environment, making available all the tracker
 * data.<br/>
 * Trackers can retrive the information they need by using the Tracker's static
 * method <code>getDatum</code> and specifying the key of the information.
 * 
 * @author matteo
 */
public interface TrackerDatum extends ElementWithKey<String>, Serializable {

	/**
	 * Each Tracker Datum must have a unique key.
	 */
	@Override
	public abstract String getKey();
}
