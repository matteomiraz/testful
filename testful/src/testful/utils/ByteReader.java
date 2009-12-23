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
