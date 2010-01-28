package testful.utils;

public class TimeWall implements Time {

	private long start;

	public TimeWall() {
		start = System.currentTimeMillis();
	}

	@Override
	public long getCurrentMs() {
		return System.currentTimeMillis() - start;
	}

}
