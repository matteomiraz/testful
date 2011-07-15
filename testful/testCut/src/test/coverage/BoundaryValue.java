package test.coverage;

public class BoundaryValue {

	public void bools(boolean a, boolean b) {
		if(a == b) System.out.println("ok");
		if(a != b) System.out.println("ok");
		if(a == true) System.out.println("ok");
		if(a != true) System.out.println("ok");
		if(b == false) System.out.println("ok");
		if(b != false) System.out.println("ok");
	}


	public void bytes(byte a, byte b) {
		if(a <= b) System.out.println("ok");
		if(a <= b) System.out.println("ok");
		if(a == b) System.out.println("ok");
		if(a != b) System.out.println("ok");
		if(a >= b) System.out.println("ok");
		if(a >  b) System.out.println("ok");

		if(a <  1   ) System.out.println("ok");
		if(a <= 2.0f) System.out.println("ok");
		if(a == 3.0) System.out.println("ok");
		if(a != (short) 4) System.out.println("ok");
		if(a >= 5) System.out.println("ok");
		if(a >= 6) System.out.println("ok");
	}


	public void shorts(short a, short b) {
		if(a <= b) System.out.println("ok");
		if(a <= b) System.out.println("ok");
		if(a == b) System.out.println("ok");
		if(a != b) System.out.println("ok");
		if(a >= b) System.out.println("ok");
		if(a >  b) System.out.println("ok");

		if(a <  1   ) System.out.println("ok");
		if(a <= 2.0f) System.out.println("ok");
		if(a == 3.0) System.out.println("ok");
		if(a != (short) 4) System.out.println("ok");
		if(a >= 5) System.out.println("ok");
		if(a >= 6) System.out.println("ok");
	}

	public void ints(int a, int b) {
		if(a <= b) System.out.println("ok");
		if(a <= b) System.out.println("ok");
		if(a == b) System.out.println("ok");
		if(a != b) System.out.println("ok");
		if(a >= b) System.out.println("ok");
		if(a >  b) System.out.println("ok");

		if(a <  1   ) System.out.println("ok");
		if(a <= 2.0f) System.out.println("ok");
		if(a == 3.0) System.out.println("ok");
		if(a != (short) 4) System.out.println("ok");
		if(a >= 5) System.out.println("ok");
		if(a >= 6) System.out.println("ok");
	}

	public void longs(long a, long b) {
		if(a <= b) System.out.println("ok");
		if(a <= b) System.out.println("ok");
		if(a == b) System.out.println("ok");
		if(a != b) System.out.println("ok");
		if(a >= b) System.out.println("ok");
		if(a >  b) System.out.println("ok");

		if(a <  1   ) System.out.println("ok");
		if(a <= 2.0f) System.out.println("ok");
		if(a == 3.0) System.out.println("ok");
		if(a != (short) 4) System.out.println("ok");
		if(a >= 5) System.out.println("ok");
		if(a >= 6) System.out.println("ok");
	}

	public void floats(float a, float b) {
		if(a <  b) System.out.println("ok");
		if(a <= b) System.out.println("ok");
		if(a == b) System.out.println("ok");
		if(a != b) System.out.println("ok");
		if(a >= b) System.out.println("ok");
		if(a >  b) System.out.println("ok");

		if(a <  1   ) System.out.println("ok");
		if(a <= 2.0f) System.out.println("ok");
		if(a == 3.0) System.out.println("ok");
		if(a != (short) 4) System.out.println("ok");
		if(a >= 5) System.out.println("ok");
		if(a >  6) System.out.println("ok");
	}

	public void doubles(double a, double b) {
		if(a <  b) System.out.println("a <  b");
		if(a <= b) System.out.println("a <= b");
		if(a == b) System.out.println("a == b");
		if(a != b) System.out.println("a != b");
		if(a >= b) System.out.println("a >= b");
		if(a >  b) System.out.println("a >  b");

		if(a <  1   ) System.out.println("ok");
		if(a <= 2.0f) System.out.println("ok");
		if(a == 3.0) System.out.println("ok");
		if(a != (short) 4) System.out.println("ok");
		if(a >= 5) System.out.println("ok");
		if(a >  6) System.out.println("ok");
		if(a > 7 & b < 8) System.out.println("ok");
	}
}
