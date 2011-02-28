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

package testful.coverage.behavior;

import java.util.List;

import testful.model.xml.XmlConstructor;
import testful.model.xml.XmlMethod;
import testful.model.xml.XmlParameter;

/**
 * Some utilities to deal with the bytecode
 * @author matteo
 */
public class BytecodeUtils {

	//result: <init>(DDII)
	public static String getBytecodeName(XmlConstructor cns) {
		return "<init>" + BytecodeUtils.getBytecodeName(cns.getParameter());
	}

	//result: MethodName(DDII)
	public static String getBytecodeName(final XmlMethod method) {
		return method.getName() + getBytecodeName(method.getParameter());
	}

	//result: (DDII)
	public static String getBytecodeName(List<XmlParameter> params) {
		StringBuilder ret = new StringBuilder();

		ret.append('(');

		for(XmlParameter p : params)
			ret.append(getBytecodeName(p));

		ret.append(')');

		return ret.toString();
	}

	public static String getBytecodeName(final XmlParameter type) {
		if(type.getType().equals("void"))	return "V";
		if(type.getType().equals("boolean")) return "Z";
		if(type.getType().equals("char")) return "C";
		if(type.getType().equals("byte")) return "B";
		if(type.getType().equals("short")) return "S";
		if(type.getType().equals("int")) return "I";
		if(type.getType().equals("float")) return "F";
		if(type.getType().equals("long")) return "J";
		if(type.getType().equals("double")) return "D";

		return "L" + type.getType() + ";";
	}
}
