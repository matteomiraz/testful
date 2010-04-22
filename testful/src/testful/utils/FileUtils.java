package testful.utils;

import java.io.File;
import java.io.IOException;

public class FileUtils {
	private static final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
	private static final IOException FAIL = new IOException("Failed to create a temporary directory");

	public static File createTempDir(String prefix, String suffix) throws IOException {

		String dirName =
			(prefix != null ? prefix : "" ) +
			Long.toHexString(System.currentTimeMillis()) +
			(suffix != null ? suffix : "" );

		File newTempDir = new File(sysTempDir, dirName);

		if(newTempDir.exists()) throw FAIL;

		if(newTempDir.mkdirs())
			return newTempDir;

		throw FAIL;
	}

	public static boolean deleteRecursive(File directory) {
		if(directory == null) return false;

		if(directory.isDirectory()) {
			for (File f : directory.listFiles()) {
				if(!deleteRecursive(f))
					return false;
			}
		}

		return directory.delete();
	}
}
