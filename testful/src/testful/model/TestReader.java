package testful.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import testful.coverage.CoverageInformation;
import testful.model.Tracker.DataLight;

public abstract class TestReader {

	protected abstract Logger getLogger();

	public void process(Iterable<? extends Test> tests) {

		// count the number of tests
		int tot = 1;
		{
			Iterator<? extends Test> it = tests.iterator();
			while(it.hasNext()) {
				it.next();
				tot++;
			}
		}


		int n = (int) Math.ceil(Math.log10(tot));
		String format = "%0" + n + "d";

		int i = 1;
		for (Test t : tests) {
			if(t instanceof TestCoverage)
				read(String.format(format, i++), ((TestCoverage)t));
			else
				read(String.format(format, i++), t);
		}
	}

	public void read(List<String> fileNames) {
		for(String fileName : fileNames)
			read(new File(fileName));
	}

	public void read(String[] fileNames) {
		for(String fileName : fileNames)
			read(new File(fileName));
	}

	public void read(File[] files) {
		for(File file : files)
			read(file);
	}

	public void read(File file) {
		String fileName = file.getAbsolutePath();
		if(file.isDirectory()) {
			File[] list = file.listFiles();
			getLogger().fine("Reading files inside directory " + fileName + " (" + list.length + " elements)");
			read(list);
		} else {
			long start = System.currentTimeMillis();
			Object read = readObject(file);
			long stop = System.currentTimeMillis();
			getLogger().fine("Read " + fileName + "  in " + (stop - start) / 1000.0 + " seconds");

			if(read == null) return;
			else if(read instanceof DataLight[]) read(getBaseFileName(fileName), (DataLight[]) read);
			else if(read instanceof DataLight) read(getBaseFileName(fileName), (DataLight) read);
			else if(read instanceof TestCoverage[]) read(getBaseFileName(fileName), (TestCoverage[]) read);
			else if(read instanceof TestCoverage) read(getBaseFileName(fileName), (TestCoverage) read);
			else if(read instanceof Test[]) read(getBaseFileName(fileName), (Test[]) read);
			else if(read instanceof Test) read(getBaseFileName(fileName), (Test) read);
			else getLogger().warning("Read an unknown object: " + read.getClass().getCanonicalName());
		}
	}

	protected void read(String fileName, DataLight[] datas) {
		for(int i = 0; i < datas.length; i++)
			read(fileName + "-" + i, datas[i]);
	}

	protected void read(String fileName, DataLight read) {
		getLogger().fine(fileName + " selectedCoverage: " + read.selectedCoverage);
		read(fileName, read.tests);
	}

	protected void read(String fileName, Test[] test) {
		for(int i = 0; i < test.length; i++)
			read(fileName + "-" + i, test[i]);
	}

	protected void read(String fileName, TestCoverage[] test) {
		for(int i = 0; i < test.length; i++)
			read(fileName + "-" + i, test[i]);
	}

	protected abstract void read(String fileName, Test test);

	protected void read(String fileName, TestCoverage test) {
		read(fileName, (Test) test);
	}

	private static String getBaseFileName(String fileName) {
		if(fileName.endsWith(".gz")) fileName = fileName.substring(0, fileName.length() - 3);
		if(fileName.endsWith(".ser")) fileName = fileName.substring(0, fileName.length() - 4);

		return fileName;
	}

	private Object readObject(File file) {
		InputStream is = null;
		try {
			is = new FileInputStream(file);

			if(file.getName().endsWith("gz")) is = new GZIPInputStream(is);

			ObjectInput oi = new ObjectInputStream(is);

			return oi.readObject();
		} catch(IOException e) {
			getLogger().log(Level.WARNING, "Cannot read from " + file.getAbsolutePath() + ": " + e.getMessage(), e);
			return null;
		} catch(ClassNotFoundException e) {
			getLogger().log(Level.WARNING, "Cannot read from " + file.getAbsolutePath() + ": " + e.getMessage(), e);
			return null;
		} finally {
			if(is != null) try {
				is.close();
			} catch(IOException e) {
			}
		}
	}

	public static void main(String[] args) {
		final Logger logger = Logger.getLogger("testful.model.testReader");

		TestReader r = new TestReader() {

			@Override
			protected Logger getLogger() {
				return logger;
			}

			@Override
			protected void read(String fileName, TestCoverage test) {
				super.read(fileName, test);
				for(CoverageInformation info : test.getCoverage()) {
					write(fileName + "-" + info.getKey() + ".txt", info.toString());
					getLogger().info("  " + info.getKey() + ": " + info.getQuality() + "(" + info.getClass().getName() + ")");
				}
			}

			private void write(String fileName, String value) {
				try {
					PrintWriter pw = new PrintWriter(fileName);
					pw.println(value);
					pw.close();
				} catch(IOException e) {
					getLogger().log(Level.WARNING, "Cannot write " + fileName + ": " + e.getMessage(), e);
				}
			}

			@Override
			protected void read(String fileName, Test test) {
				getLogger().info(fileName + ": " + test.getTest().length + " operations");

				try {
					test.write(new GZIPOutputStream(new FileOutputStream(fileName + ".ser.gz")));
				} catch(IOException e) {
					getLogger().log(Level.WARNING, "Cannot write " + fileName + ": " + e.getMessage(), e);
				}

				PrintWriter out = null;
				try {
					out = new PrintWriter(fileName + ".txt");
					for(Operation op : test.getTest())
						out.println(op);
				} catch(IOException e) {
					getLogger().log(Level.WARNING, "Cannot write " + fileName + ": " + e.getMessage(), e);
				} finally {
					if(out != null) out.close();
				}
			}
		};

		r.read(args);
	}
}
