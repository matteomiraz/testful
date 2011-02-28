/*
 * TestFul - http://code.google.com/p/testful/
 * Copyright (C) 2011  Matteo Miraz
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

package testful.model.xml.behavior;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import testful.model.xml.Extra;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "behavior", namespace = "http://testful.sourceforge.net/schema/2.0/behavioral.xsd", propOrder = { "abstraction" })
public class Behavior extends Extra {

	@XmlElement(required = true)
	protected List<Abstraction> abstraction;

	public List<Abstraction> getAbstraction() {
		if(abstraction == null) abstraction = new ArrayList<Abstraction>();
		return abstraction;
	}

}
