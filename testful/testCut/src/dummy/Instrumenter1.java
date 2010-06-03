package dummy;

public class Instrumenter1 {

	public int emptyCatch(Object o) {
		try {
			return o.hashCode();
		} catch (Exception e) {
			return -1;
		}
	}

	public int moreCatches(Object o) {
		int ret = 0;

		try {
			ret += o.hashCode();
		} catch (Exception e) {
			return -1;
		}

		try {
			ret += o.hashCode();
		} catch (Exception e) {
			return -1;
		}

		try {
			ret += o.hashCode();
		} catch (Exception e) {
			return -1;
		}

		return ret;
	}


}
