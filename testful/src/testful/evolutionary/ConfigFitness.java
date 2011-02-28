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

/**
 * Specify the information to use to drive as fitness.
 * @see IConfigFitness
 * @author matteo
 */
public class ConfigFitness implements IConfigFitness, IConfigFitness.Args4j {

	private boolean basicBlock = true;
	private boolean branch = true;
	private boolean defUse = true;
	private boolean behavioral = true;

	@Override
	public boolean isBasicBlock() {
		return basicBlock;
	}

	@Override
	public void setDisableBranch(boolean disable) {
		basicBlock = !disable;
	}

	@Override
	public boolean isBranch() {
		return branch;
	}

	@Override
	public void setDisableBasicBlock(boolean disable) {
		basicBlock = !disable;
	}

	@Override
	public boolean isDefUse() {
		return defUse;
	}

	@Override
	public void setDisableDefUse(boolean disable) {
		defUse = !disable;
	}

	@Override
	public boolean isBehavioral() {
		return behavioral;
	}

	@Override
	public void setDisableBehavioral(boolean disable) {
		behavioral = !disable;
	}
}
