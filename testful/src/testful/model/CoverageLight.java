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
