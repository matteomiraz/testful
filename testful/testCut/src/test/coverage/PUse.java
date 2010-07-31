package test.coverage;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class PUse {

	private int a;

	public void zero() {
		this.a = 0;
	}

	public void setA(int a) {
		this.a = a;
	}

	public void ifFieldGT10() {
		if(a > 10) {
			Math.abs(1);
		} else {
			Math.abs(1);
		}
	}



}
