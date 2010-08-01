package test.coverage;

public class ControlFlow {

	public ControlFlow() {
		super(); 			// bb: 4
							// bb: 5
	}

	public int intif(int i) {
		int res = 0;		// bb: 9

		if(i >= 0) res++;	// bb: 10		br: 1
		else false_(res--);	// bb: 11,12	br: 0
							// bb: 13
		if(i > 1) res++; 	// bb: 14		br: 3
		else false_(res--);	// bb: 15,16	br: 2
							// bb: 17
		if(i <= 2) res++;	// bb: 18		br: 5
		else false_(res--);	// bb: 19,20	br: 4
							// bb: 21
		if(i < 3) res++;	// bb: 22		br: 7
		else false_(res--);	// bb: 23,24	br: 6
							// bb: 25
		if(i == 4) res++;	// bb: 26		br: 9
		else false_(res--);	// bb: 27,28	br: 8
							// bb: 29
		if(i != 5) res++;	// bb: 30		br: 11
		else false_(res--);	// bb: 31,32	br: 10
							// bb: 33
		return res;
	}

	public int doubleif(double i) {
		int res = 0;

		if(i >= 0.0) res++;	// bb: 38		br: 13
		else false_(res--);	// bb: 39,40	br: 12

		if(i > 1.0) res++;	// bb: 42		br: 15
		else false_(res--);	// bb: 43,44	br: 14

		if(i <= 2.0) res++;	// bb: 46		br: 17
		else false_(res--);	// bb: 47,48	br: 16

		if(i < 3.0) res++;	// bb: 50		br: 19
		else false_(res--);	// bb: 51,52	br: 18

		if(i == 4.0) res++;	// bb: 54		br: 21
		else false_(res--);	// bb: 55,56	br: 20

		if(i != 5.0) res++;	// bb: 58		br: 23
		else false_(res--);	// bb: 59,60	br: 22

		return res;
	}

	private void false_(int i) { }

	public int boolif(boolean i) {
		int res = 0;

		if(i == true) res++;// bb: 70		br: 25
		else false_(res--);	// bb: 71,72	br: 24

		if(i != true) res++;// bb: 74		br: 27
		else false_(res--);	// bb: 75,76	br: 26

		return res;
	}

	public int objif(ControlFlow o) {
		int res = 0;

		if(o == this) res++;// bb: 82		br: 29
		else false_(res--);	// bb: 83,84	br: 28

		if(o != this) res++;// bb: 86		br: 31
		else false_(res--);	// bb: 87,88	br: 30

		return res;
	}

	public int tSwitch(int i) {
		int res = 0;

		switch(i) {
		case 0: res++; // br: 32	bb: 94,95,96,97
		case 1: res++; // br: 33	bb: 95,96,97
		case 2: res++; // br: 34	bb: 96,97
		case 3: res++; // br: 35	bb: 97
			break;
		case 4: res++; // br: 36	bb: 98
			break;
		case 5: res++; // br: 37	bb: 99
			break;
		default: res--; // br: 38	bb: 100
		}

		return res;
	}

	public int lSwitch(int i) {
		int res = 0;

		switch(i) {
		case 0: res++;	// br: 39	bb: 106,107,108,109
		case 10: res++;	// br: 40	bb: 107,108,109
		case 20: res++;	// br: 41	bb: 108,109
		case 30: res++;	// br: 42	bb: 109
			break;
		case 40: res++;	// br: 43	bb: 110
			break;
		case 50: res++;	// br: 44	bb: 111
			break;
		default: res--;	// br: 45	bb: 112
		}

		return res;
	}
}