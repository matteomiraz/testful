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


package testful.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ByteReader {

	public static byte[] readBytes(File classFile) throws IOException {
		return readBytes(new FileInputStream(classFile));
	}

	public static byte[] readBytes(InputStream inputStream) throws IOException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();

			while(inputStream.available() > 0) {
				byte[] tmp = new byte[1024];
				int n = inputStream.read(tmp);
				baos.write(tmp, 0, n);
			}

			return baos.toByteArray();
		} finally {
			inputStream.close();
		}
	}

}
