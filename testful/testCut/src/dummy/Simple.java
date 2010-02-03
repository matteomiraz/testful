package dummy;

public class Simple {
	private int status;
	
	public Simple() {
		status = 0;
	}
	
	/** Mutator: increment the internal status by 1 unit */
	public void mInc() {
		status ++;
	}

	/** Mutator: decrement the internal status by 1 unit */
	public void mDec() {
		status ++;
	}

	/** Observer: returns the internal status */
	public int oStatus() {
		return status;
	}
	
	/** Worker: returns a calculus involving the internal status and the parameter */
	public int wModulo(int n) {
		return n % status;
	}
	
	/** Observer: returns the absolute value of the internal status */
	public int oAbs() {
		if(status < 0) return -status;
		else return status;
	}
}
