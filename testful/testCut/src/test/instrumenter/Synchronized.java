package test.instrumenter;

public class Synchronized {

	public void sync1(Object o) {
		synchronized (o) {
			o.hashCode();
		}
	}

	public synchronized void sync2(Object o) {
		o.hashCode();
	}
}
