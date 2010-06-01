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
import testful.utils.ElementManager;

public class TestCoverage extends Test {

	private static final long serialVersionUID = 1420569092643662448L;

	/** the coverage */
	private final ElementManager<String, CoverageInformation> coverage;

	public TestCoverage(TestCluster cluster, ReferenceFactory refFactory, Operation[] test, ElementManager<String, CoverageInformation> coverage) {
		super(cluster, refFactory, test);
		this.coverage = coverage;
	}

	public TestCoverage(Test test, ElementManager<String, CoverageInformation> coverage) {
		super(test.getCluster(), test.getReferenceFactory(), test.getTest());
		this.coverage = coverage;
	}

	public ElementManager<String, CoverageInformation> getCoverage() {
		return coverage;
	}
	/**
	 * @author Tudor
	 * @return a score for the existing Test (the higher, the better)
	 */
	public float getRating(){
		float ret = 0;
		for (CoverageInformation i : getCoverage()){
			ret += i.getQuality();
		}
		return ret;
	}
}