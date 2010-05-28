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


package testful.model.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(namespace = "http://testful.sourceforge.net/schema/1.1/testful.xsd", name = "parameter")
public class XmlParameter {

	@XmlAttribute(required = true)
	protected String type;

	@XmlAttribute
	protected Boolean mutated;

	@XmlAttribute
	protected Boolean captured;

	@XmlAttribute
	protected Boolean exposedByReturn;

	@XmlAttribute
	protected String exchangeStateWith;

	@XmlElement
	protected List<Extra> extra;

	public String getExchangeStateWith() {
		if(exchangeStateWith == null) return "";
		return exchangeStateWith.trim();
	}

	public void setExchangeStateWith(String value) {
		exchangeStateWith = value;
	}

	public int[] getExchangeState() {
		String exc = getExchangeStateWith();
		if(exc.length() <= 0) return new int[0];

		String[] split = exc.split(",");
		int[] ret = new int[split.length];

		for(int i = 0; i < ret.length; i++)
			ret[i] = Integer.parseInt(split[i]);

		return ret;
	}

	public Boolean isExposedByReturn() {
		return exposedByReturn;
	}

	public void setExposedByReturn(Boolean value) {
		exposedByReturn = value;
	}

	public Boolean isCaptured() {
		return captured;
	}

	public void setCaptured(Boolean value) {
		captured = value;
	}

	public Boolean isMutated() {
		return mutated;
	}

	public void setMutated(Boolean value) {
		mutated = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String value) {
		type = value;
	}

	public List<Extra> getExtra() {
		if(extra == null) extra = new ArrayList<Extra>();
		return extra;
	}
}
