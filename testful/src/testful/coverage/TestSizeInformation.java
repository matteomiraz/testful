package testful.coverage;


public class TestSizeInformation implements CoverageInformation {

	private static final long serialVersionUID = -357803584845798269L;
	
	public static final String KEY = "LEN"; 
	public static final String NAME = "Test size"; 
	
	private static final float TIME_FACTOR = 1.0f / 100;
	
	private final long time;
	private final int length;
	
	private final float quality;
	
	/**
	 * Create a test length information
	 * @param time the time required for executing the test (in milliseconds)
	 * @param length the lenght of the test (# of operations)
	 */
	public TestSizeInformation(long time, int length) {
		this.time = time;
		this.length = length;
		
		if(this.time <= 0 && this.length <= 0) 
			this.quality =  Float.POSITIVE_INFINITY;
		else
			this.quality = 
				time <= 0   ? 0 : time * TIME_FACTOR + 
				length <= 0 ? 0 : length;
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
		throw new IllegalStateException("Cannot create an empty TestSizeInformation");
	}
	
	@Override
	public TestSizeInformation clone() throws CloneNotSupportedException {
		return new TestSizeInformation(time, length);
	}
}
