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

public interface IConfigFitness {

	public void setToMinimize(boolean toMinimize);

	public boolean isBbd();

	public boolean isBbn();

	public boolean isBrd();

	public boolean isBrn();

	public boolean isBug();

	public boolean isLength();

	public boolean isToMinimize();

	public interface Args4j extends IConfigFitness {

		@Option(required = false, name = "-disableBasicBlock", usage = "Removes the basic block coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
		public void setDisableBasicBlock(boolean disable);

		@Option(required = false, name = "-disableBasicBlockCode", usage = "Removes the basic block coverage on the code from the multi-objective fitness")
		public void setDisableBasicBlockCode(boolean disable);

		@Option(required = false, name = "-disableBasicBlockContract", usage = "Removes the basic block coverage on contracts from the multi-objective fitness")
		public void setDisableBasicBlockContract(boolean disable);

		@Option(required = false, name = "-disableBranch", usage = "Removes the branch coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
		public void setDisableBranch(boolean disable);

		@Option(required = false, name = "-disableBranchCode", usage = "Removes the branch coverage on the code from the multi-objective fitness")
		public void setDisableBranchCode(boolean disable);

		@Option(required = false, name = "-disableBranchContract", usage = "Removes the branch coverage on contracts from the multi-objective fitness")
		public void setDisableBranchContract(boolean disable);

		@Option(required = false, name = "-disableLength", usage = "Removes the length of test from the multi-objective fitness")
		public void setDisableLength(boolean disable);

		@Option(required = false, name = "-enableBug", usage = "Inserts the number of bug found in the multi-objective fitness")
		public void setEnableBug(boolean bug);

	}
}