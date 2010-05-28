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

import java.io.File;
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

	public static final long runId = System.currentTimeMillis();

	public static final boolean DEBUG = false;

	private static final String VERSION = "1.1.2";

	public static void parseCommandLine(Object config, String[] args, Class<?> launcher, String name) {

		CmdLineParser parser = new CmdLineParser(config);
		try {
			// parse the arguments.
			parser.parseArgument(args);
		} catch(CmdLineException e) {
			testful.TestFul.printHeader(name);

			System.err.println(e.getMessage());

			System.err.println();
			System.err.println("Usage: java " + launcher.getCanonicalName() + " [options...] arguments...");
			parser.setUsageWidth(120);
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("   Example: java " + launcher.getCanonicalName() + parser.printExample(org.kohsuke.args4j.ExampleMode.REQUIRED));

			System.exit(1);
		}
	}

	public static void printHeader(String module) {

		System.out.println("Testful v. " + VERSION + (module != null ? " - " + module : "") + " - http://code.google.com/p/testful");
		System.out.println("Copyright (c) 2010 - Matteo Miraz");
		System.out.println("This program comes with ABSOLUTELY NO WARRANTY.");
		System.out.println("This is free software, and you are welcome to redistribute it under certain conditions.");
		System.out.println("For more information, read http://www.gnu.org/licenses/gpl-3.0.txt");
		System.out.println();
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

	public static void setupLogging(IConfigProject config ) {
		setupLogging(config.getLog(), config.getLogLevel().getLoggingLevel(), config.isQuiet());
	}

	public static void setupLogging(File logDir, Level loggingLevel, boolean quiet) {
		Logger logger = Logger.getLogger("testful");
		logger.setUseParentHandlers(false);
		logger.setLevel(loggingLevel);

		if(!quiet) {
			ConsoleHandler ch = new ConsoleHandler();
			ch.setFormatter(consoleFormatter);
			ch.setLevel(Level.INFO);
			logger.addHandler(ch);
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

		}
	}

	public static String getProperties(Object o) {
		StringBuilder sb = new StringBuilder();

		for (Method m : o.getClass().getMethods()) {
			final String name = m.getName();
			if(!name.startsWith("get") || name.equals("getClass")) continue;
			if(m.getParameterTypes().length > 0) continue;
			if(!Modifier.isPublic(m.getModifiers())) continue;

			try {
				sb.append(name.substring(3) + " = " + m.invoke(o) + "\n");
			} catch (Exception e) {
				e.printStackTrace();
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

}
