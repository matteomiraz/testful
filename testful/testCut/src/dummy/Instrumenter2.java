package dummy;

public class Instrumenter2 {

	public void sync1(Object o) {
		synchronized (o) {
			o.hashCode();
		}
	}

	public synchronized void sync2(Object o) {
		o.hashCode();
	}
}
