package testful.evolutionary;

import org.kohsuke.args4j.Option;

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
	@Option(required = false, name = "-disableBasicBlock", usage = "Removes the basic block coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
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
	@Option(required = false, name = "-disableBasicBlockCode", usage = "Removes the basic block coverage on the code from the multi-objective fitness")
	public void setDisableBasicBlockCode(boolean disable) {
		if(disable) {
			bbd = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableBasicBlockContract(boolean)
	 */
	@Override
	@Option(required = false, name = "-disableBasicBlockContract", usage = "Removes the basic block coverage on contracts from the multi-objective fitness")
	public void setDisableBasicBlockContract(boolean disable) {
		if(disable) {
			bbn = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableBranch(boolean)
	 */
	@Override
	@Option(required = false, name = "-disableBranch", usage = "Removes the branch coverage from the multi-objective fitness (shortcut for -disableBranchCode and -disableBranchContract)")
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
	@Option(required = false, name = "-disableBranchCode", usage = "Removes the branch coverage on the code from the multi-objective fitness")
	public void setDisableBranchCode(boolean disable) {
		if(disable) {
			brd = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableBranchContract(boolean)
	 */
	@Override
	@Option(required = false, name = "-disableBranchContract", usage = "Removes the branch coverage on contracts from the multi-objective fitness")
	public void setDisableBranchContract(boolean disable) {
		if(disable) {
			brn = false;
		}
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setDisableLength(boolean)
	 */
	@Override
	@Option(required = false, name = "-disableLength", usage = "Removes the length of test from the multi-objective fitness")
	public void setDisableLength(boolean disable) {
		if(disable) length = false;
	}

	/* (non-Javadoc)
	 * @see testful.evolutionary.Args4j#setEnableBug(boolean)
	 */
	@Override
	@Option(required = false, name = "-enableBug", usage = "Inserts the number of bug found in the multi-objective fitness")
	public void setEnableBug(boolean bug) {
		if(bug) this.bug = true;
	}
}
