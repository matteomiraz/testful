package dummy;

public class WhiteSample {

	public int intif(int i) {
		int res = 0;
		
		if(i >= 0) res++;	// bb: 9		br: 1
		else false_(res--);	// bb: 10,11	br: 0
		
		if(i > 1) res++; 	// bb: 13		br: 3
		else false_(res--);	// bb: 14,15	br: 2
		
		if(i <= 2) res++;	// bb: 17		br: 5
		else false_(res--);	// bb: 18,19	br: 4
		
		if(i < 3) res++;	// bb: 21		br: 7
		else false_(res--);	// bb: 22,23	br: 6		
		
		if(i == 4) res++;	// bb: 25		br: 9
		else false_(res--);	// bb: 26,27	br: 8
		
		if(i != 5) res++;	// bb: 29		br: 11
		else false_(res--);	// bb: 30,31	br: 10
		
		return res;
	}

	public int doubleif(double i) {
		int res = 0;
		
		if(i >= 0.0) res++;	// bb: 37		br: 13
		else false_(res--);	// bb: 38,39	br: 12
		
		if(i > 1.0) res++;	// bb: 41		br: 15
		else false_(res--);	// bb: 42,43	br: 14
		
		if(i <= 2.0) res++;	// bb: 45		br: 17
		else false_(res--);	// bb: 46,47	br: 16
		
		if(i < 3.0) res++;	// bb: 49		br: 19
		else false_(res--);	// bb: 50,51	br: 18
		
		if(i == 4.0) res++;	// bb: 53		br: 21
		else false_(res--);	// bb: 54,55	br: 20
		
		if(i != 5.0) res++;	// bb: 57		br: 23
		else false_(res--);	// bb: 58,59	br: 22
		
		return res;
	}
	
	private void false_(int i) { }
	
	public int boolif(boolean i) {
		int res = 0;

		if(i == true) res++;// bb: 69		br: 25
		else false_(res--);	// bb: 70,71	br: 24
		
		if(i != true) res++;// bb: 73		br: 27
		else false_(res--);	// bb: 74,75	br: 26
		
		return res;
	}

	public int objif(WhiteSample o) {
		int res = 0;

		if(o == this) res++;// bb: 81		br: 29
		else false_(res--);	// bb: 82,83	br: 28
		
		if(o != this) res++;// bb: 85		br: 31
		else false_(res--);	// bb: 86,87	br: 30
		
		return res;
	}

	public int tSwitch(int i) {
		int res = 0;
		
		switch(i) {
		case 0: res++; // br: 32	bb: 93,94,95,96
		case 1: res++; // br: 33	bb: 94,95,96
		case 2: res++; // br: 34	bb: 95,96
		case 3: res++; // br: 35	bb: 96
			break;
		case 4: res++; // br: 36	bb: 97
			break;
		case 5: res++; // br: 37	bb: 98
			break;
		default: res--; // br: 38	bb: 99
		}
		return res;
	}

	public int lSwitch(int i) {
		int res = 0;
		switch(i) {
		case 0: res++;	// br: 39	bb: 105,106,107,108
		case 10: res++;	// br: 40	bb: 106,107,108
		case 20: res++;	// br: 41	bb: 107,108
		case 30: res++;	// br: 42	bb: 108
			break;
		case 40: res++;	// br: 43	bb: 109
			break;
		case 50: res++;	// br: 44	bb: 110
			break;
		default: res--;	// br: 45	bb: 111
		}
		return res;
	}
}