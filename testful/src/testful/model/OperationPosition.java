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

package testful.model;

import java.util.Comparator;

import testful.TestFul;

/**
 * Memorize the ordinal position of an operation in a test
 * @author matteo
 */
public class OperationPosition extends OperationInformation {
	private static final long serialVersionUID = 3664462416048405563L;

	public static String KEY = "OperationPosition";
	public final int position;

	public OperationPosition(int position) {
		super(KEY);
		this.position = position;
	}

	@Override
	public String toString() {
		return "Operation #" + Integer.toString(position);
	}

	@Override
	public OperationInformation clone() {
		return this;
	}

	public static final Comparator<Operation> orderComparator = new Comparator<Operation>() {

		@Override
		public int compare(Operation o1, Operation o2) {
			if(o1 == o2) return 0;

			if(TestFul.DEBUG) {
				if(o1 == null) {
					NullPointerException exc = new NullPointerException("o1 is null");
					TestFul.debug(exc);
					throw exc;
				}

				if(((OperationPosition)o1.getInfo(OperationPosition.KEY)) == null) {
					NullPointerException exc = new NullPointerException("o1 does not have the position information " + o1);
					TestFul.debug(exc);
					throw exc;
				}

				if(o2 == null) {
					NullPointerException exc = new NullPointerException("o2 is null");
					TestFul.debug(exc);
					throw exc;
				}

				if(((OperationPosition)o2.getInfo(OperationPosition.KEY)) == null) {
					NullPointerException exc = new NullPointerException("o2 does not have the position information " + o2);
					TestFul.debug(exc);
					throw exc;
				}
			}

			int p1 = ((OperationPosition)o1.getInfo(OperationPosition.KEY)).position;
			int p2 = ((OperationPosition)o2.getInfo(OperationPosition.KEY)).position;

			if(p1 < p2) return -1;
			if(p1 > p2) return 1;

			// the two operations are the same, but one has target = null. Put before the one with target != null
			if(o1 instanceof CreateObject && ((CreateObject)o1).getTarget() != null) return  -1;
			if(o1 instanceof Invoke && ((Invoke)o1).getTarget() != null) return -1;
			return 1;
		}
	};
}