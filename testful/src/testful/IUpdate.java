/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
