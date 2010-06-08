package test;

public class Stopped {

	private static final int TIME = 2000;

	public Stopped() {
	}

	private void doSomething() { /* This code is never executed */ }

	public void longMethod1() {
		try {
			Thread.sleep(TIME);
		} catch (InterruptedException e) {
			doSomething();
		}
	}

	public void longMethod2() {
		try {
			Thread.sleep(TIME);
		} catch (InterruptedException e) {
		}
		doSomething();
	}

	public void longMethod3() {
		try {
			Thread.sleep(TIME);
		} catch (InterruptedException e) {
			doSomething();
		}
		doSomething();
	}

	public void longMethod4() {
		while (true) {
			try {
				Thread.sleep(TIME);
			} catch (Exception e) {
				doSomething();
			}
		}
	}

	public void longMethod5() {
		try {

			try {
				Thread.sleep(TIME);
				doSomething();
			} catch (Exception e) {
				doSomething();
			}

			Thread.sleep(TIME);

			doSomething();
		} catch (Exception e) {
			doSomething();
		}
		doSomething();
	}
}
