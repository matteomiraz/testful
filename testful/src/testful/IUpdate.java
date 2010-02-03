package testful;


/**
 * Classes implementing this interface are able to provide updates.
 * 
 * @author matteo
 * 
 */
public interface IUpdate {

	/**
	 * Register for updates
	 * @param c the callback that will handle the updates
	 */
	public void register(Callback c);

	/**
	 * Unregister for updates
	 * @param c the callback that was handling the updates
	 */
	public void unregister(Callback c);

	/**
	 * Classes implementing this interface can receive updates
	 * @author matteo
	 */
	public static interface Callback  {

		/**
		 * When an update is made, its information is sent to all subscribers by using this method.
		 * 
		 * @param start the starting (in millisecond)
		 * @param current the current (in millisecond)
		 * @param end the end (in millisecond)
		 */
		public void update(long start, long current, long end);

	}
}
