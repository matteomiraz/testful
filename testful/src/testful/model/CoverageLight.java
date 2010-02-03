package testful.model;

import testful.coverage.CoverageInformation;
import testful.coverage.TestSizeInformation;
import testful.utils.ElementManager;

public class CoverageLight implements CoverageInformation {

	private static final long serialVersionUID = 7899041916672179935L;

	private float quality;
	private final String key;
	private final String name;

	public static TestCoverage convert(TestCoverage test) {
		ElementManager<String, CoverageInformation> coverage = new ElementManager<String, CoverageInformation>();
		for(CoverageInformation info : test.getCoverage())
			if(!(info instanceof TestSizeInformation))
				coverage.put(new CoverageLight(info));

		return new TestCoverage(test.getCluster(), test.getReferenceFactory(), test.getTest(), coverage);
	}

	public CoverageLight(String key, String name, float quality) {
		this.quality = quality;
		this.name = name;
		this.key = key;
	}

	public CoverageLight(CoverageInformation info) {
		this(info.getKey(), info.getName(), info.getQuality());
	}

	@Override
	public CoverageInformation createEmpty() {
		return new CoverageLight(key, name, 0.0f);
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public float getQuality() {
		return quality;
	}

	@Override
	public boolean contains(CoverageInformation other) {
		throw new UnsupportedOperationException("Not supported in coverage light");
	}

	@Override
	public void merge(CoverageInformation other) {
		throw new UnsupportedOperationException("Not supported in coverage light");
	}

	@Override
	public CoverageLight clone() throws CloneNotSupportedException {
		return (CoverageLight) super.clone();
	}
}
