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

	private boolean bbd = true;

	private boolean bbn = true;

	private boolean brd = true;

	private boolean brn = true;

	private boolean bug = false;

	private boolean length = true;

	@Override
	public void setToMinimize(boolean toMinimize) {
		this.toMinimize = toMinimize;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isBbd()
	 */
	@Override
	public boolean isBbd() {
		return bbd;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isBbn()
	 */
	@Override
	public boolean isBbn() {
		return bbn;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isBrd()
	 */
	@Override
	public boolean isBrd() {
		return brd;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isBrn()
	 */
	@Override
	public boolean isBrn() {
		return brn;
	}
	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isBug()
	 */
	@Override
	public boolean isBug() {
		return bug;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isLength()
	 */
	@Override
	public boolean isLength() {
		return length;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.IFitness#isToMinimize()
	 */
	@Override
	public boolean isToMinimize() {
		return toMinimize;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableBasicBlock(boolean)
	 */
	@Override
	public void setDisableBasicBlock(boolean disable) {
		if(disable) {
			bbd = false;
			bbn = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableBasicBlockCode(boolean)
	 */
	@Override
	public void setDisableBasicBlockCode(boolean disable) {
		if(disable) {
			bbd = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableBasicBlockContract(boolean)
	 */
	@Override
	public void setDisableBasicBlockContract(boolean disable) {
		if(disable) {
			bbn = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableBranch(boolean)
	 */
	@Override
	public void setDisableBranch(boolean disable) {
		if(disable) {
			brd = false;
			brn = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableBranchCode(boolean)
	 */
	@Override
	public void setDisableBranchCode(boolean disable) {
		if(disable) {
			brd = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableBranchContract(boolean)
	 */
	@Override
	public void setDisableBranchContract(boolean disable) {
		if(disable) {
			brn = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableLength(boolean)
	 */
	@Override
	public void setDisableLength(boolean disable) {
		if(disable) length = false;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setEnableBug(boolean)
	 */
	@Override
	public void setEnableBug(boolean bug) {
		if(bug) this.bug = true;
	}
}
