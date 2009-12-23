package testful.coverage;

import java.util.HashSet;
import java.util.Set;

import testful.utils.ElementManager;

/**
 * The <code>Tracker</code> collects (i.e. tracks) coverage information out from
 * the running system. The Tracker class manage the set of running trackers, and
 * subclasses of the Tracker class are automatically added to this set (if you
 * want that multiple instances of your tracker are added to this set, please
 * take care of the hashCode and equals method). It is possible to set the
 * initial coverage information on all trackers by invoking the method
 * <code>setInitialCoverage</code>, and their coverage reports are gathered
 * using the method <code>getAllCoverage()</code>.
 * 
 * @author matteo
 */
public abstract class Tracker {


	private static ElementManager<String, TrackerDatum> data = new ElementManager<String, TrackerDatum>();

	/**
	 * <b>DO NOT USE THIS METHOD!</b><br/>
	 * only the execution manager can setup properly the execution environment<br/>
	 * <br/>
	 * Set up the tracker data
	 * 
	 * @param data the data
	 */
	public static void setup(TrackerDatum[] data) {
		Tracker.data = new ElementManager<String, TrackerDatum>(data);
	}

	/**
	 * Retrieve the information with the specified key
	 * 
	 * @param key the key of the interested information
	 * @return the interested information
	 */
	public static TrackerDatum getDatum(String key) {
		return data.get(key);
	}


	private static final Set<Tracker> trackers = new HashSet<Tracker>();

	/**
	 * Initialize the base tracker, adding it to the set of running trackers.
	 */
	protected Tracker() {
		Tracker.trackers.add(this);
	}

	/**
	 * Reset the coverage on this tracker.
	 */
	public abstract void reset();

	/**
	 * Get the collected coverage information
	 * 
	 * @return the collected coverage information
	 */
	public abstract ElementManager<String, CoverageInformation> getCoverage();

	/**
	 * Reset the coverage on this tracker on all the registered trackers.
	 */
	public static void resetAll() {
		for(Tracker t : trackers)
			t.reset();
	}

	/**
	 * Gather the collected coverage information from all trackers.
	 * 
	 * @return the collected coverage information.
	 */
	public static ElementManager<String, CoverageInformation> getAllCoverage() {
		ElementManager<String, CoverageInformation> ret = new ElementManager<String, CoverageInformation>();

		for(Tracker t : trackers) {

			ElementManager<String, CoverageInformation> covs = t.getCoverage();
			for(CoverageInformation cov : covs) {

				CoverageInformation retCov = ret.get(cov.getKey());
				if(retCov == null) {
					retCov = cov.createEmpty();
					ret.put(retCov);
				}
				retCov.merge(cov);
			}
		}

		return ret;
	}
}
