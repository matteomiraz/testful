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

package testful;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Calendar;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

public class TestFul {

	//  ---------- testful's system properties (settable with -Dprop=value) ----------

	/** Enables the debug mode (boolean; default: false) */
	public static final String PROPERTY_DEBUG = "testful.debug";

	/** Do not print anything on the console (boolean; default: false) */
	public static final String PROPERTY_QUIET = "testful.quiet";

	/** Directory that holds the logs (String; default: "" (do not log to file) ) */
	public static final String PROPERTY_LOG_DIR = "testful.log.dir";

	/** Logging level (String, one of {SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST}; default: INFO) */
	public static final String PROPERTY_LOG_LEVEL = "testful.log.level";

	/** Measure the effective CPU time used, and not the wall clock (boolean; default: false) */
	public static final String PROPERTY_TIME_CPU = "testful.cpuTime";

	/** Enables the testful's fault detection feature (boolean; default: enabled) */
	public static final String PROPERTY_FAULT_DETECT = "testful.fault";

	/** Set the maximum execution time for methods and constructors (long; default: 500) */
	public static final String PROPERTY_MAX_EXEC_TIME = "testful.maxExecTime";

	/** Monitor TestFul to collect internal execution performances (boolean; default: false) */
	public static final String PROPERTY_MONITOR_PERFORMANCE = "testful.monitorPerformance";

	/** Probability to remove an operation during mutation (float, between 0 and 1, default: 0.75) */
	public static final String PROPERTY_MUTATION_REMOVE = "testful.mutation.probRemove";

	/** Probability to remove all the useless operations of a test during mutation (float, between 0 and 1, default: disabled (0) ) */
	public static final String PROPERTY_MUTATION_SIMPLIFY = "testful.mutation.probRemove";

	/** The number of threads to use on the local machine (integer, 0 disables the evaluation, -1 uses as many threads as the number of CPUs; default: -1 (the number of CPUs) ) */
	public static final String PROPERTY_RUNNER_LOCAL_WORKERS = "testful.runner.local";

	/** Enables the remote evaluation infrastructure (Boolean, default: false) */
	public static final String PROPERTY_RUNNER_REMOTE = "testful.runner.remote";

	/** The list of remote workers, separated by commas (String, default: "") */
	public static final String PROPERTY_RUNNER_REMOTE_ADDR = "testful.runner.remoteAddr";

	/** Number of jobs to store on the client side (TestFul and jAutoTest put jobs here) (integer, default: 1000)  */
	public static final String PROPERTY_RUNNER_TESTREPOSITORY_JOBS = "testful.runner.nLocalJobs";

	/** Number of jobs to store on the worker side (integer, default: 50)  */
	public static final String PROPERTY_RUNNER_WORKER_JOBS = "testful.runner.nWorkerJobs";

	// --------------------- end of testful's system properties ----------------------

	//  ------------------------- Shared values --------------------------------------

	/** The identification of this run */
	public static final long runId = System.currentTimeMillis();

	/** if the user doesn't want to be annoyed */
	public static final boolean quiet = TestFul.getProperty(PROPERTY_QUIET, false);

	/** If not null, is the directory that contains all the logs. */
	public static final File logDir;
	static {
		String property = TestFul.getProperty(PROPERTY_LOG_DIR, "").trim();
		if(property.isEmpty()) logDir = null;
		else logDir = new File(property);
	}

	//  --------------------- end of Shared values -----------------------------------


	// ------------------- debug -----------------------
	public static final boolean DEBUG;
	private static final Logger loggerDebug;
	static {
		DEBUG = TestFul.getProperty(PROPERTY_DEBUG, false);

		if(DEBUG) loggerDebug = Logger.getLogger("debug");
		else loggerDebug = null;
	}

	public static void debug(String message) {
		if(DEBUG)
			loggerDebug.log(Level.WARNING, message, new Exception("Stack Trace"));
	}

	public static void debug(Throwable exc) {
		if(DEBUG) {
			if(exc == null) exc = new NullPointerException("The exception is null");
			loggerDebug.log(Level.WARNING, exc.getMessage(), exc);
		}
	}

	public static void debug(String message, Throwable e) {
		if(DEBUG)
			loggerDebug.log(Level.WARNING, message, e);
	}

	// ----------- command-line handling ---------------

	private static final String VERSION = "2.0 (alpha)";

	private static final String REVISION = readRevision();

	public static void parseCommandLine(IConfig config, String[] args, Class<?> launcher, String name) {

		CmdLineParser parser = new CmdLineParser(config);
		try {
			// parse the arguments.
			parser.parseArgument(args);

			config.validate();

		} catch(CmdLineException e) {
			testful.TestFul.printHeader(name);

			System.err.println(e.getMessage());

			System.err.println();
			System.err.println("Usage: java " + launcher.getName() + " [options...] arguments...");
			parser.setUsageWidth(120);
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("   Example: java " + launcher.getName() + parser.printExample(org.kohsuke.args4j.ExampleMode.REQUIRED));

			System.exit(1);
		}
	}

	public static void printHeader(String module) {
		if(!quiet) {
			System.out.println("Testful v. " + VERSION + REVISION + (DEBUG ? " - debug mode" : "" ) + (module != null ? " - " + module : ""));
			System.out.println("Copyright (c) 2011 Matteo Miraz - http://code.google.com/p/testful");
			System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
			System.out.println("This is free software, and you are welcome to redistribute it under certain conditions.");
			System.out.println("For more information, read http://www.gnu.org/licenses/gpl-3.0.txt");
			System.out.println();
		}
	}

	private static String readRevision() {
		final InputStream stream = TestFul.class.getResourceAsStream("/revision.txt");
		if(stream == null) return "";

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(stream));
			return " (" + reader.readLine() + ")";
		} catch (Exception e) {
			return "";
		} finally {
			try {
				if(reader != null) reader.close();
			} catch (IOException e) {
			}
		}
	}

	private static final Formatter consoleFormatter = new Formatter() {
		private final Calendar cal = Calendar.getInstance();

		@Override
		public String format(LogRecord record) {
			cal.setTimeInMillis(record.getMillis());

			StringBuilder sb = new StringBuilder();

			sb.append(String.format("%2d:%02d:%02d ",
					cal.get(Calendar.HOUR_OF_DAY),
					cal.get(Calendar.MINUTE),
					cal.get(Calendar.SECOND)));

			if(record.getLevel().intValue() >= Level.WARNING.intValue())
				sb.append(record.getLevel().getLocalizedName()).append(" ");

			sb.append(record.getMessage());
			sb.append("\n");

			return sb.toString();
		}
	};

	private static final Formatter debugFormatter = new Formatter() {
		private final Calendar cal = Calendar.getInstance();

		@Override
		public String format(LogRecord record) {
			cal.setTimeInMillis(record.getMillis());

			StringBuilder sb = new StringBuilder();

			sb.append(" ----- start DEBUG -----\n");

			sb.append(String.format("%2d:%02d:%02d ",
					cal.get(Calendar.HOUR_OF_DAY),
					cal.get(Calendar.MINUTE),
					cal.get(Calendar.SECOND)));

			sb.append(record.getLevel().getLocalizedName()).append(" ");

			sb.append(record.getMessage()).append("\n");

			if(record.getThrown() != null) {
				sb.append("-- ").append(record.getThrown().toString()).append("\n");
				for (StackTraceElement ste : record.getThrown().getStackTrace())
					sb.append("    ").append(ste).append("\n");
			}

			sb.append(" ------ end  DEBUG -----\n");

			return sb.toString();
		}
	};

	private static final Formatter fileFormatter = new Formatter() {

		@Override
		public String format(LogRecord record) {
			StringBuilder sb = new StringBuilder("# ");

			sb.append(record.getMillis()).append(" ");
			sb.append(record.getLevel().getName()).append(" ");
			sb.append(record.getLoggerName()).append(" ");

			sb.append(record.getMessage());
			sb.append("\n");

			if (record.getThrown() != null) {
				try {
					StringWriter sw = new StringWriter();
					PrintWriter pw = new PrintWriter(sw);
					record.getThrown().printStackTrace(pw);
					pw.close();
					sb.append(sw.toString());
				} catch (Exception ex) {
				}
			}

			return sb.toString();
		}
	};

	static {
		Logger logger = Logger.getLogger("testful");
		logger.setUseParentHandlers(false);

		Level loggingLevel;
		try {
			loggingLevel = Level.parse(getProperty(PROPERTY_LOG_LEVEL, "INFO"));
		} catch (IllegalArgumentException e) {
			System.err.println("Invalid Logging Level: " + e.getMessage());
			loggingLevel = Level.INFO;
		}
		logger.setLevel(loggingLevel);

		if(!quiet) {
			ConsoleHandler ch = new ConsoleHandler();
			ch.setFormatter(consoleFormatter);
			ch.setLevel(Level.INFO);
			logger.addHandler(ch);
		}

		if(DEBUG) {
			Logger debug = Logger.getLogger("debug");
			debug.setUseParentHandlers(false);
			debug.setLevel(Level.ALL);

			ConsoleHandler ch = new ConsoleHandler();
			ch.setFormatter(debugFormatter);
			ch.setLevel(Level.ALL);
			debug.addHandler(ch);
		}

		if(logDir != null) {
			try {
				logDir.mkdirs();

				final String logFile = logDir.getAbsolutePath() + File.separator + "testful.log";
				Handler fh = new FileHandler(logFile);
				fh.setLevel(Level.ALL);
				fh.setFormatter(fileFormatter);
				logger.addHandler(fh);

				if(!quiet) System.out.println("Logging to " + logFile);
			} catch(Exception e) {
				System.err.println("Cannot log: " + e);
			}

			logger.config("Testful v. " + VERSION + REVISION);
		}
	}

	public static String printGetters(Object o) {
		StringBuilder sb = new StringBuilder();

		for (Method m : o.getClass().getMethods()) {
			final String name = m.getName();
			if(!name.startsWith("get") || name.equals("getClass")) continue;
			if(m.getParameterTypes().length > 0) continue;
			if(!Modifier.isPublic(m.getModifiers())) continue;

			try {
				sb.append(name.substring(3) + " = " + m.invoke(o) + "\n");
			} catch (Exception e) {
				debug(e);
			}

		}

		return sb.toString();
	}

	public static File createFileWithBackup(File baseDir, String fileName) {
		baseDir.mkdirs();

		File cur = new File(baseDir, fileName);
		File old = new File(baseDir, fileName + ".bak");

		if(old.exists()) old.delete();
		if(cur.exists()) cur.renameTo(old);

		return cur;
	}

	public static boolean getProperty(String key, boolean defaultValue) {
		try {
			final String value = System.getProperty(key);

			if(value != null)
				return Boolean.parseBoolean(value);

		} catch (NumberFormatException e) {
			Logger.getLogger("testful").log(Level.FINE, "Cannot use " + key + "property: " + e.getMessage(), e);
		}
		return defaultValue;
	}

	public static int getProperty(String key, int defaultValue) {
		try {
			final String value = System.getProperty(key);

			if(value != null)
				return Integer.parseInt(value);

		} catch (NumberFormatException e) {
			Logger.getLogger("testful").log(Level.FINE, "Cannot use " + key + "property: " + e.getMessage(), e);
		}
		return defaultValue;
	}

	public static String getProperty(String key, String defaultValue) {
		return System.getProperty(key, defaultValue);
	}

	public static float getProperty(String key, float min, float defaultValue, float max) {
		try {
			final String value = System.getProperty(key);

			if(value != null) {
				float v = Float.parseFloat(value);

				if(v < min) v = min;
				if(v > max) v = max;
				return v;
			}

		} catch (NumberFormatException e) {
			Logger.getLogger("testful").log(Level.FINE, "Cannot use " + key + "property: " + e.getMessage(), e);
		}
		return defaultValue;
	}
}
