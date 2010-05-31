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


public class ConfigFitness implements IConfigFitness, IConfigFitness.Args4j {

	private boolean toMinimize;

	private boolean basicBlock = true;
	private boolean branch = true;

	private boolean length = true;

	/* (non-Javadoc)
	 * @see testful.evolutionary.IConfigFitness#isBasicBlock()
	 */
	@Override
	public boolean isBasicBlock() {
		return basicBlock;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IConfigFitness.Args4j#setDisableBranch(boolean)
	 */
	@Override
	public void setDisableBranch(boolean disable) {
		basicBlock = !disable;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isBranch()
	 */
	@Override
	public boolean isBranch() {
		return branch;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IConfigFitness.Args4j#setDisableBasicBlock(boolean)
	 */
	@Override
	public void setDisableBasicBlock(boolean disable) {
		basicBlock = !disable;
	}


	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isLength()
	 */
	@Override
	public boolean isLength() {
		return length;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableLength(boolean)
	 */
	@Override
	public void setDisableLength(boolean disable) {
		if(disable) length = false;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isToMinimize()
	 */
	@Override
	public boolean isToMinimize() {
		return toMinimize;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IConfigFitness#setToMinimize(boolean)
	 */
	@Override
	public void setToMinimize(boolean toMinimize) {
		this.toMinimize = toMinimize;
	}
}
