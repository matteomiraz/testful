/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2010  Matteo Miraz
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


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
		return true;
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

	@Override
	public String toString() {
		if(quality > 0) return String.format("Length: %d, Time: %d, Quality: %.2f", length, time, quality);
		else return String.format("Length: %d, Time: %d", length, time);
	}
}
