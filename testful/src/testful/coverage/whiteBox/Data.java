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

/**
 * Renders a variable or a field, able to store data
 * 
 * @author matteo
 */
public class Data implements Serializable {

	private static final long serialVersionUID = -4042773236943535616L;

	public enum Type {
		Boolean, Character, Number, String, Array, Reference
	}

	private final int id;
	private static int idGenerator = 0;

	private final Type type;
	private final String fieldName;

	private final BitSet mask;
	private final Set<DataDef> defs;
	private final Set<DataUse> uses;

	private boolean isField;
	private final boolean isParam;

	private Data(String fieldName, Type type, boolean param) {
		id = idGenerator++;

		this.type = type;

		isParam = param;
		this.fieldName = fieldName;
		isField = fieldName != null;

		mask = new BitSet();
		defs = new HashSet<DataDef>();
		uses = new HashSet<DataUse>();

	}

	public static Data getPrimitiveData(String fieldName, Type primType, boolean param) {
		return new Data(fieldName, primType, param);
	}

	public static Data getReferenceData(String fieldName, boolean param) {
		return new Data(fieldName, Type.Reference, param);
	}

	public static Data getArrayData(String fieldName, boolean param) {
		return new Data(fieldName, Type.Array, param);
	}

	public int getId() {
		return id;
	}

	public String getFieldName() {
		return fieldName;
	}

	void setField(boolean isField) {
		this.isField = isField;
	}

	public boolean isField() {
		return isField;
	}

	public boolean isParam() {
		return isParam;
	}

	public BitSet getMask() {
		return (BitSet) mask.clone();
	}

	public Type getType() {
		return type;
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
		if(isParam) return "p" + id;
		if(isField) return "f" + id;
		return "d" + id;
	}
}
