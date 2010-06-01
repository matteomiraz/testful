package dummy;

public class Instrumenter1 {

	public int emptyCatch(Object o) {
		try {
			return o.hashCode();
		} catch (Exception e) {
			return -1;
		}
	}
}
