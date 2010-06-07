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

import org.kohsuke.args4j.Option;

/**
 * Specify the information to use to drive as fitness.
 * @author matteo
 */
public interface IConfigFitness {

	public boolean isBasicBlock();

	public boolean isBranch();
	public boolean isDefUse();

	public interface Args4j extends IConfigFitness {

		@Option(required = false, name = "-disableBasicBlock", usage = "Removes the basic block coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
		public void setDisableBasicBlock(boolean disable);

		@Option(required = false, name = "-disableDefUse", usage = "Removes the def-use pairs coverage from the multi-objective fitness.")
		public void setDisableDefUse(boolean disable);

		@Option(required = false, name = "-disableBranch", usage = "Removes the branch coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
		public void setDisableBranch(boolean disable);
	}
}
