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

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

import testful.TestFul;

/**
 * Renders a variable or a field, able to store data
 *
 * @author matteo
 */
public class Data implements Serializable, Value {

	private static final long serialVersionUID = -4042773236943535616L;

	private final int id;
	private static int idGenerator = 0;

	private final String fieldName;

	private final BitSet mask;
	private final Set<DataDef> defs;
	private final Set<DataUse> uses;

	private boolean field;
	private final boolean param;

	public Data(String fieldName, boolean param) {
		id = idGenerator++;

		this.param = param;
		this.fieldName = fieldName;
		field = fieldName != null;

		if(TestFul.DEBUG) {
			if(field && param)
				TestFul.debug("The data cannot be both a field and a parameter.");
		}

		mask = new BitSet();
		defs = new HashSet<DataDef>();
		uses = new HashSet<DataUse>();
	}

	public int getId() {
		return id;
	}

	public String getFieldName() {
		return fieldName;
	}

	void setField(boolean field) {
		this.field = field;
	}

	public boolean isField() {
		return field;
	}

	public boolean isParam() {
		return param;
	}

	public BitSet getMask() {
		return (BitSet) mask.clone();
	}

	void addDef(DataDef def) {
		defs.add(def);
		mask.set(def.getId());
	}

	public Set<DataDef> getDefs() {
		return defs;
	}

	public Set<DataUse> getUses() {
		return uses;
	}

	void addUse(DataUse use) {
		uses.add(use);
	}

	@Override
	public String toString() {
		if(param) return "p" + id;
		if(field) return "f" + id;
		return "d" + id;
	}
}
