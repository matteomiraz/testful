/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010 Matteo Miraz
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

package testful.utils;

import java.util.Comparator;

/**
 * Compare two classes, basing on their name
 * @author matteo
 */
public class ClassComparator implements Comparator<Class<?>> {

	public final static ClassComparator singleton = new ClassComparator();

	@Override
	public int compare(Class<?> o1, Class<?> o2) {
		if(o1 == o2) return 0;
		return o1.getName().compareTo(o2.getName());
	}
}
