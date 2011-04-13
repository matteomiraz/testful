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

package testful.evolutionary;

import testful.IConfigGeneration;

/**
 * Contains the information required to run Testful
 * @author matteo
 */
public interface IConfigEvolutionary extends IConfigGeneration, IConfigFitness {

	public static enum FitnessInheritance {
		DISABLED, UNIFORM, FRONTEER;
	}

	public int getLocalSearchPeriod();

	public int getLocalSearchElements();

	public int getPopSize();

	public FitnessInheritance getFitnessInheritance();

	public int getRandomSeeding();

	/**
	 * Returns the seed to use in the random number generator
	 * @return the seed to use in the random number generator
	 */
	public long getSeed();
}