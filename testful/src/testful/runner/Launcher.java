package testful.runner;

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
import java.util.logging.Logger;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import testful.TestFul;

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

	@Argument
	private List<String> arguments = new ArrayList<String>();

	public static final String RMI_NAME = "worker";

	public static void main(String[] args) throws RemoteException {
		TestFul.printHeader("Worker");

		final Launcher opt = new Launcher();
		opt.parseArgs(args);

		if(opt.cpu < 0) {
			System.out.println("Starting without CPUs: acting as a test repository.");
			opt.cpu = 0;
		} else if(opt.cpu == 0) opt.cpu = -1;

		final WorkerManager wm = new WorkerManager(opt.cpu, opt.bufferSize);

		if(opt.register) {
			System.out.println("Registering workerManager");

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
					System.err.println("Cannot retrieve the host name: " + e);
					msg = "Registered worker at " + RMI_NAME;
				}

				logger.info(msg);
				System.out.println(msg);

			} catch(AlreadyBoundException e1) {
				System.err.println("There is already a worker registered here!");
				System.exit(1);
			}
		}

		for(String arg : opt.arguments) {
			arg = arg.trim();
			connect(wm, arg);
		}

		if(opt.stop > 0) {
			final int stop = opt.stop;
			Thread stopper = new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						System.out.println("WorkerManager will quit in " + stop + " minutes");
						TimeUnit.SECONDS.sleep(stop * 60);
					} catch(InterruptedException e) {
					}

					wm.stop();
				}
			});
			stopper.setDaemon(true);
			stopper.start();
		}

		if(!opt.nonInteractive) {
			Scanner s = new Scanner(System.in);
			while(true) {
				System.out.println("\n" + wm.toString());
				System.out.println("\nEnter a RMI URL or type exit to quit");
				String line = s.nextLine().trim();

				if(line.length() <= 0) {
					// do nothing
				} else if(line.equalsIgnoreCase("exit")) wm.stop();
				else if(line.endsWith("/")) try {
					String[] list = Naming.list(line);
					System.out.println("Found " + list.length + " services bound to " + line);
					for(String name : list)
						System.out.println(" * " + name);
					System.out.println("---");

				} catch(Exception e) {
					System.out.println("Error: " + e.getMessage());
				}
				else connect(wm, line);
			}
		} else try {
			while(true) {
				TimeUnit.SECONDS.sleep(60);
				System.out.println("\n" + wm.toString());
			}
		} catch(InterruptedException e) {
		}
	}

	private static void connect(WorkerManager wm, String rmiName) {
		System.out.print("\nConnecting to " + rmiName);
		try {
			wm.addTestRepository(rmiName);
			System.out.println(" done.");
		} catch(Exception e) {
			System.out.println("  error: " + e);
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
