package test.coverage;

public class Stopped {

	private static final int TIME = 20000;

	public Stopped() { }

	// this is executed!
	public void execute() {
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			// never happens!
			e.printStackTrace();
		}

	}

	private void dontDoSomething() { new Exception("This code is never executed").printStackTrace(); }

	public void longMethod1() {
		long i = 0;
		while(i < i+1) i++;
		dontDoSomething();
	}

	public void longMethod2() {
		try {
			Thread.sleep(TIME);
		} catch (InterruptedException e) {
		}
		dontDoSomething();
	}

	public void longMethod3() {
		try {
			Thread.sleep(TIME);
		} catch (InterruptedException e) {
			dontDoSomething();
		}
		dontDoSomething();
	}

	public void longMethod4() {
		while (true) {
			try {
				Thread.sleep(TIME);
			} catch (Exception e) {
				dontDoSomething();
			}
		}
	}

	public void longMethod5() {
		try {

			try {
				Thread.sleep(TIME);
				dontDoSomething();
			} catch (Exception e) {
				dontDoSomething();
			}

			Thread.sleep(TIME);

			dontDoSomething();
		} catch (Exception e) {
			dontDoSomething();
		}
		dontDoSomething();
	}

	public void infLoop(int n, int delay) {
		try {
			if(delay > 0) Thread.sleep(delay);
		} catch (Exception e) {
			dontDoSomething();
		}

		switch(n) {
		case 1:  infLoop (1, delay); break;
		case 2:  infLoop2(2, delay); break;
		case 3:  infLoop3(3, delay); break;
		default: infLoop4(4, delay);
		}

		dontDoSomething();
	}

	private void infLoop2(int n, int delay) {
		try {
			if(delay > 0) Thread.sleep(delay);
		} catch (Exception e) {
			dontDoSomething();
		}

		infLoop(n, delay);
	}

	private void infLoop3(int n, int delay) {
		try {
			if(delay > 0) Thread.sleep(delay);
		} catch (Exception e) {
			dontDoSomething();
		}

		infLoop2(n, delay);
	}

	private void infLoop4(int n, int delay) {
		try {
			if(delay > 0) Thread.sleep(delay);
		} catch (Exception e) {
			dontDoSomething();
		}

		infLoop3(n, delay);
	}
}
