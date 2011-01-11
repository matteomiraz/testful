package testful.utils;
import java.lang.management.ManagementFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;

public class TimeCPU implements Time {
	private static Logger logger = Logger.getLogger("testful.utils");

	private static final MBeanServer mbs;
	private static final ObjectName opSystem;

	static {
		mbs = ManagementFactory.getPlatformMBeanServer();

		ObjectName tmp = null;
		try {
			tmp = new ObjectName("java.lang:type=OperatingSystem");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Cannot access to the operating system JMX: " + e.getMessage(), e);
		}
		opSystem = tmp;
	}

	private final long start;

	public TimeCPU() throws Exception {
		start = getProcessCpuTime();
		if(opSystem == null || start < 0)
			throw new Exception("Cannot measure the CPU time!");
	}

	@Override
	public long getCurrentMs() {
		return getProcessCpuTime() - start;
	}

	public static long getProcessCpuTime() {
		if(opSystem != null) {
			try {
				Long time = (Long) mbs.getAttribute(opSystem, "ProcessCpuTime");
				if(time != null) return time / 1000000;
			} catch (Exception e) {
				logger.log(Level.WARNING, "Cannot read the used process CPU time: " + e.getMessage(), e);
			}
		}
		return -1;
	}

}
