package testful.coverage;


public class TestSizeInformation implements CoverageInformation {

	private static final long serialVersionUID = -357803584845798269L;

	public static final String KEY = "LEN";
	public static final String NAME = "Test size";

	private static final float TIME_FACTOR = 0.1f;

	private final long time;
	private final int length;

	private float quality = Float.NEGATIVE_INFINITY;

	/**
	 * Create a test length information
	 * @param time the time required for executing the test (in milliseconds)
	 * @param length the length of the test (# of operations)
	 */
	public TestSizeInformation(final long time, final int length) {
		this.time = time > 0 ? time : 0;
		this.length = length > 0 ? length : 0;
	}

	@Override
	public float getQuality() {
		return quality;
	}

	public long getTime() {
		return time;
	}

	public int getLength() {
		return length;
	}

	@Override
	public String getKey() {
		return KEY;
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void merge(CoverageInformation other) {
		return;
	}

	@Override
	public boolean contains(CoverageInformation other) {
		return false;
	}

	@Override
	public CoverageInformation createEmpty() {
		return new TestSizeInformation(0, 0);
	}

	@Override
	public TestSizeInformation clone() throws CloneNotSupportedException {
		return new TestSizeInformation(time, length);
	}

	public void setOtherCovs(float covTot) {
		float len = time * TIME_FACTOR + length;

		if(len > 1)
			quality = 1.0f*covTot*covTot / (float) Math.log(len);
	}
}
