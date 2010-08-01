package test.coverage;

public class PUse {

	private int a;		// default
	private double b;	// default

	public void zero() {
		this.a = 0; // def 1
		this.b = 0; // def 2
	}

	public void setA(int a) {
		this.a = a;	// def 4
		this.b = a;	// def 5
	}

	public void ifFieldGT10() {
		if(a > 10) {
			Math.abs(1);
		} else {
			Math.abs(2);
		}

		if(b > 10) {
			Math.abs(1);
		} else {
			Math.abs(2);
		}

		switch(a) {
		case 0: Math.abs(0); break;
		case 11: Math.abs(11); break;
		default: Math.abs(-1);
		}
	}

	public static void ifParams(int p0, int p1, int p2) {
		final int a, b;
		if(p2 == 0) {
			a = p0; // defs 9, 10
			b = p1;
		} else {
			b = p0; // defs 11, 12
			a = p1;
		}

		Math.abs(a);
		Math.abs(b);

		if(a > b) {
			Math.abs(0);
		} else {
			Math.abs(1);
		}
	}
}
