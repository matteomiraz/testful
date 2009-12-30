package testful.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import testful.coverage.CoverageInformation;
import testful.model.Test;
import testful.model.TestCoverage;

public class TestfulLogger {

	public static final TestfulLogger singleton = new TestfulLogger();

	private final File baseDir;

	public final long runId;

	private TestfulLogger() {
		runId = System.currentTimeMillis();
		baseDir = new File("run" + File.separator + runId);

		baseDir.mkdirs();

		System.err.println("Logging data to " + baseDir.getAbsolutePath());
	}

	public String getBaseDir() {
		return baseDir.getAbsolutePath();
	}

	public String writeParameters(Set<String> params) throws IOException {
		File propFile = new File(baseDir, "parameters.txt");

		PrintWriter wr = new PrintWriter(propFile);
		for(String p : params)
			wr.println(p);
		wr.close();

		return propFile.getAbsolutePath();
	}

	public String writeParameters(Properties parameters) throws IOException {
		File propFile = new File(baseDir, "parameters.txt");
		if(!propFile.exists()) {
			PrintWriter wr = new PrintWriter(propFile);
			parameters.list(wr);
			wr.close();
		}

		return propFile.getAbsolutePath();
	}

	public File getFile(String filename) {
		File ret = new File(baseDir, filename);
		ret.getParentFile().mkdirs();
		return ret;
	}

	public OutputStream getOutputStreamWithBackup(String filename) throws FileNotFoundException {
		File cur = getFile(filename);
		File old = getFile(filename + ".bak");

		if(old.exists()) old.delete();
		if(cur.exists()) cur.renameTo(old);

		return new FileOutputStream(cur);
	}

	public OutputStream getOutputStream(String filename) throws FileNotFoundException {
		return new FileOutputStream(getFile(filename));
	}

	public PrintWriter getWriter(String filename) throws FileNotFoundException {
		return new PrintWriter(getFile(filename));
	}

	public PrintWriter getWriterWithBackup(String filename) throws FileNotFoundException {
		File cur = getFile(filename);
		File old = getFile(filename + ".bak");

		if(old.exists()) old.delete();
		if(cur.exists()) cur.renameTo(old);

		return new PrintWriter(cur);
	}

	public CoverageWriter getCoverageWriter(String filename) throws FileNotFoundException {
		return new CoverageWriter(getWriter(filename));
	}

	public CombinedCoverageWriter getCombinedCoverageWriter(String filename) throws FileNotFoundException {
		return new CombinedCoverageWriter(getWriter(filename));
	}

	public static class CoverageWriter {

		private final PrintWriter wr;
		private String[] keys = null;

		CoverageWriter(PrintWriter wr) {
			this.wr = wr;
		}

		public void write(long gen, long len, ElementManager<String, CoverageInformation> infos) {
			if(keys == null) {

				if(infos.size() <= 1)
					return;

				int n = 0;
				StringBuilder sb = new StringBuilder("gen;len");
				keys = new String[infos.size()];
				for(CoverageInformation cov : infos) {
					keys[n++] = cov.getKey();
					sb.append(";").append(cov.getKey());
				}
				wr.println(sb.toString());
			}

			StringBuilder sb = new StringBuilder();

			sb.append(gen).append(";").append(len);
			for(String key : keys) {
				CoverageInformation info = infos.get(key);
				float quality = -1;
				if(info != null) quality = info.getQuality();
				sb.append(";").append(quality);
			}
			wr.println(sb.toString());
			wr.flush();
		}
	}

	public static class CombinedCoverageWriter {

		private final PrintWriter wr;
		private String[] keys = null;
		private final long init;

		CombinedCoverageWriter(PrintWriter wr) {
			this.wr = wr;
			init = System.currentTimeMillis();
		}

		public void write(int gen, long inv, ElementManager<String, CoverageInformation> infos, Map<String, Set<TestCoverage>> optimal) {
			if(keys == null) {

				if(infos.isEmpty()) return;

				int n = 0;
				StringBuilder sb = new StringBuilder("gen;time;inv");
				keys = new String[infos.size()];
				for(CoverageInformation cov : infos) {
					keys[n++] = cov.getKey();
					sb.append(";").append(cov.getKey()).append(";").append(cov.getKey()).append("-tests").append(";").append(cov.getKey()).append("-length");
				}
				wr.println(sb.toString());
			}

			StringBuilder sb = new StringBuilder();

			sb.append(gen).append(";").append(System.currentTimeMillis() - init).append(";").append(inv);
			for(String key : keys) {
				int tot = 0;
				int nSolutions = 0;

				if(optimal != null) {
					Set<TestCoverage> solutions = optimal.get(key);
					nSolutions = solutions.size();
					for(Test s : solutions)
						tot += s.getTest().length;
				}

				sb.append(";").append(infos.get(key).getQuality()).append(";").append(nSolutions).append(";").append(tot);
			}
			wr.println(sb.toString());
			wr.flush();
		}
	}

	private static final long MAX_NUM = 1000000000000l;

	public static String formatNumber(long num) {
		StringBuilder sb = new StringBuilder();

		for(long i = num; i < MAX_NUM; i *= 10)
			sb.append('0');

		sb.append(num);

		return sb.toString();
	}
}
