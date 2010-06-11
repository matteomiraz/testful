package test.coverage;

public class Stopped {

	private static final int TIME = 5000;

	public Stopped() { }

	private void dontDoSomething() { System.err.println("This code is never executed"); }

	public void longMethod1() {
		try {
			Thread.sleep(TIME);
		} catch (InterruptedException e) {
		}
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
}
