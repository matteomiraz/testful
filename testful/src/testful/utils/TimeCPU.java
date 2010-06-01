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
