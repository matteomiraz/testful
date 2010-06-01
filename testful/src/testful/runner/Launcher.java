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


package testful.runner;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import testful.TestFul;
import testful.IConfigProject.LogLevel;

public class Launcher {

	private static Logger logger = Logger.getLogger("testful.executor");

	@Option(required = false, name = "-cpu", usage = "The number of workers to allocate (default: number of available CPU cores)")
	private int cpu;

	@Option(required = false, name = "-buffer", usage = "The number of jobs to cache (must be >= 0)")
	private int bufferSize;

	@Option(required = false, name = "-register", usage = "Register the workerManager in a RMI Registry")
	private boolean register;

	@Option(required = false, name = "-stop", usage = "Stop after n minutes")
	private int stop;

	@Option(required = false, name = "-nonInteractive", usage = "Starts a non-interactive executor")
	private boolean nonInteractive;

	/** if not null, enables the logging in the specified directory */
	@Option(required = false, name = "-log", usage = "Enables logging in the specified directory")
	private File log;

	/** the logging level */
	@Option(required = false, name = "-logLevel", usage = "Sets the logging level")
	private LogLevel logLevel = LogLevel.INFO;

	/** disable all the output to the console */
	@Option(required = false, name = "-quiet", usage = "Do not print anything to the console")
	private boolean quiet;

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public static final String RMI_NAME = "worker";

	public static void main(String[] args) throws RemoteException {
		TestFul.printHeader("Worker");

		final Launcher config = new Launcher();
		config.parseArgs(args);

		TestFul.setupLogging(config.log, config.logLevel.getLoggingLevel(), config.quiet);

		logger.config(TestFul.getProperties(config));

		if(config.cpu < 0) {
			logger.info("Starting without CPUs: acting as a test repository.");
			config.cpu = 0;
		} else if(config.cpu == 0) config.cpu = -1;

		final WorkerManager wm = new WorkerManager(config.cpu, config.bufferSize);

		if(config.register) {
			logger.info("Registering workerManager");

			Registry r;
			try {
				r = LocateRegistry.getRegistry(Registry.REGISTRY_PORT);
				r.list();
			} catch(Exception e) {
				r = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
			}
			try {
				r.bind(RMI_NAME, UnicastRemoteObject.exportObject(wm, 0));

				String msg;
				try {
					InetAddress localHost = InetAddress.getLocalHost();
					msg = "Registered worker at " + "//" + localHost.getHostAddress() + "/" + RMI_NAME + " - " + "//" + localHost.getCanonicalHostName() + "/" + RMI_NAME;
				} catch(UnknownHostException e) {
					msg = "Registered worker at " + RMI_NAME;
				}

				logger.info(msg);

			} catch(AlreadyBoundException e1) {
				logger.log(Level.SEVERE, "There is already a worker registered here!", e1);
				System.exit(1);
			}
		}

		for(String arg : config.arguments) {
			arg = arg.trim();
			connect(wm, arg);
		}

		if(config.stop > 0) {
			final int stop = config.stop;
			Thread stopper = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						logger.info("WorkerManager will quit in " + stop + " minutes");
						TimeUnit.SECONDS.sleep(stop * 60);
					} catch(InterruptedException e) {
					}

					wm.stop();
				}
			});
			stopper.setDaemon(true);
			stopper.start();
		}

		if(!config.nonInteractive) {
			Scanner s = new Scanner(System.in);
			while(true) {
				System.out.println("\n" + wm.toString());
				System.out.println("\nEnter a RMI URL or type exit to quit");
				String line = s.nextLine().trim();

				if(line.length() <= 0) {
					// do nothing
				} else if(line.equalsIgnoreCase("exit")) {
					wm.stop();
					System.exit(0);
				} else if(line.endsWith("/")) try {
					String[] list = Naming.list(line);
					System.out.println("Found " + list.length + " services bound to " + line);
					for(String name : list)
						System.out.println(" * " + name);
					System.out.println("---");

				} catch(Exception e) {
					logger.log(Level.WARNING, e.getMessage(), e);
				}
				else connect(wm, line);
			}
		} else try {
			while(true) {
				TimeUnit.SECONDS.sleep(60);
				logger.info(wm.toString());
			}
		} catch(InterruptedException e) {
		}
	}

	private static void connect(WorkerManager wm, String rmiName) {
		logger.info("Connecting to " + rmiName);
		try {
			wm.addTestRepository(rmiName);
			logger.info("Connected to " + rmiName);
		} catch(Exception e) {
			logger.log(Level.WARNING, "Cannot connect to " + rmiName, e);
		}
	}

	public void parseArgs(String[] args) {
		CmdLineParser parser = new CmdLineParser(this);

		try {
			// parse the arguments.
			parser.parseArgument(args);

		} catch(CmdLineException e) {
			System.err.println(e.getMessage());
			System.err.println("java " + Launcher.class.getCanonicalName() + " [options...] arguments...");
			parser.printUsage(System.err);
			System.err.println();

			// print option sample. This is useful some time
			System.err.println("   Example: java " + Launcher.class.getCanonicalName() + parser.printExample(org.kohsuke.args4j.ExampleMode.REQUIRED));

			System.exit(1);
		}
	}

}
