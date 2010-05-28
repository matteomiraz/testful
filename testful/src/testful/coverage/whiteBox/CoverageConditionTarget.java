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


package testful.coverage.whiteBox;

import testful.coverage.CoverageInformation;

public class CoverageConditionTarget implements CoverageInformation {
	private static final long serialVersionUID = 7837300371834856584L;

	private static String NAME = "Distance to branch ";
	
	private static String KEY = "br-";
	public static String getKEY(int branchId) {
		return KEY + branchId;
	}

	private final String name;
	private final String key; 
	private final int branchId;
	private double distance;
	
	CoverageConditionTarget(int branchId) {
		this.key = KEY + branchId;
		this.name = NAME + branchId;
		this.branchId = branchId;
		this.distance = Float.POSITIVE_INFINITY;
	}
	
	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public int getBranchId() {
		return branchId;
	}

	void setDistance(double d) {
		if(d < 0) {
			new NullPointerException("Negative distance!").printStackTrace();
		} else if(this.distance > d)
			this.distance = d;
	}
	
	/**
	 * Returns the quality: higher is better
	 */
	@Override
	public float getQuality() {
		if(distance == Float.POSITIVE_INFINITY) return Float.NEGATIVE_INFINITY;
		if(distance < 0) return 0;
		
		if(distance == 0) return Float.POSITIVE_INFINITY;
		return (float) (1.0f/distance);
	}
	
	@Override
	public boolean contains(CoverageInformation other) {
		if(!other.getKey().equals(this.getKey())) return false;
		
		CoverageConditionTarget o = (CoverageConditionTarget) other;
		if(this.distance > o.distance) return false; 
		
		return true;
	}
	

	@Override
	public void merge(CoverageInformation other) {
		if(!other.getKey().equals(this.getKey())) return;
		
		CoverageConditionTarget o = (CoverageConditionTarget) other;
		if(this.distance <= o.distance) return; 

		this.distance = o.distance;
	}

	@Override
	public CoverageConditionTarget createEmpty() {
		return new CoverageConditionTarget(branchId);
	}

	@Override
	public CoverageConditionTarget clone() {
		CoverageConditionTarget ret = new CoverageConditionTarget(branchId);
		ret.distance = distance;
		return ret;
	}
	
	@Override
	public String toString() {
		return Double.toString(this.distance);
	}
}