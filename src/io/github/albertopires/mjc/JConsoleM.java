/*
 * Copyright (C) 2016 Alberto Pires de Oliveira Neto
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.albertopires.mjc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.openmbean.CompositeDataSupport;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 *
 * @author Alberto Pires de Oliveira Neto
 */
public class JConsoleM {

	private MBeanServerConnection mbsc;
	private int ncpu = 1;
	private long sample;
	private String host;
	private String port;
	private String dir;
	public String[] stats;

	public static void main(String[] args) throws Exception {
		System.err.println("Length " + args.length);
		if ((args.length != 1)) {
			System.err.println("JMX Monitor v1.0.0 - 03/May/2016");
			System.err.println("Parameters: <config_file>\n");
			System.err.println("Fields:");
			System.err.println("0 - TimeStamp");
			System.err.println("1 - CPU Usage");
			System.err.println("2 - Heap Usage");
			System.err.println("3 - Loaded Class Count");
			System.err.println("4 - Thread Count");
			System.err.println("5 - CMS Usage");
			System.err.println("6 - Par Eden Usage");
			System.err.println("7 - Non-Heap Usage");
			System.err.println("8 - CMS Usage Threshold Count\n");
			System.exit(1);
		}

		Properties p = loadConfig(args[0]);
		int i = 0;
		String host, port, logDir;
		LogInvoker li;
		while (true) {
			host = p.getProperty("host." + i);
			port = p.getProperty("port." + i);
			logDir = p.getProperty("dir." + i);
			i++;
			System.err.println("Host " + host +":"+port);
			if (host == null)
				break;
			li = new LogInvoker(host, port, logDir, p);
			new Thread(li).start();
		}
	}

	private static Properties loadConfig(String confFile) {
		File f = new File(confFile);

		Properties p = new Properties();

		try {
			FileInputStream fi = new FileInputStream(f);
			p.load(fi);
			return p;
		} catch (IOException e) {
			System.err.println(e.getMessage());
			return null;
		}
	}

	public static void jvmLog(String[] args, Properties conf) throws Exception {
		JConsoleM jc;
		StringBuffer line;

		try {
			jc = getInstance(args[0], args[1], conf);
			jc.setDir(args[2]);
			jc.setSample(4000);

			for (;;) {
				try {
					jc.runStats();
					line = new StringBuffer();
					Date d = new Date();
					line.append("" + d.getTime() + " ");
					for (int s = 0; s < jc.stats.length; s++) {
						line.append(jc.stats[s] + " ");
					}
					line.append("\n");
					jc.logToFile(line.toString());
				} catch (Exception e) {
					System.err.println("Loop Exception : " + e.getMessage());
					jc = getInstance(args[0], args[1], conf);
					jc.setDir(args[2]);
					jc.setSample(4000);
				}
			}
		} catch (Exception e) {
			System.err.println("Exception : " + e.getMessage());
		}
	}

	private JConsoleM(String host, String port, Properties conf)
			throws Exception {
		this.host = host;
		this.port = port;
		String urlStr = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
		JMXServiceURL url = new JMXServiceURL(urlStr);
		JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
		mbsc = jmxc.getMBeanServerConnection();
		ncpu = getAvailableProcessors();
		stats = new String[8];
	}

	public static JConsoleM getInstance(String host, String port,
			Properties conf) {
		JConsoleM jc = null;

		while (jc == null) {
			try {
				jc = new JConsoleM(host, port, conf);
			} catch (Exception ex) {
				jc = null;
			}

			try {
				Thread.sleep(10000);
			} catch (InterruptedException ex) {
				System.err.println("InterruptedException : " + ex.getMessage());
			}
		}
		return jc;
	}

	public void logToFile(String line) {
		Calendar c = Calendar.getInstance();
		String name = dir + "/" + host + "_" + port + "_"
				+ c.get(Calendar.YEAR);
		name += String.format("%02d", c.get(Calendar.MONTH) + 1);
		name += String.format("%02d", c.get(Calendar.DAY_OF_MONTH));
		try {
			FileWriter fw = new FileWriter(name, true);
			fw.append(line);
			fw.close();
		} catch (IOException ex) {
			System.err.println("IOException : " + ex.getMessage());
		}
	}

	public void runStats() throws Exception {
		stats[0] = String.format("%.2f", getCpuUsage());
		stats[1] = "" + getHeapUsage();
		stats[2] = "" + getLoadedClassCount();
		stats[3] = "" + getThreadCount();
		stats[4] = "" + getCMSUsage();
		stats[5] = "" + getEdenUsage();
		stats[6] = "" + getNonHeapUsage();
		stats[7] = "" + getCMSUsageThresholdCount();
		getDeadLockedThreads();
	}

	public double getCpuUsage() throws Exception {
		long c, u;
		double ec, eu;

		c = getOSProcessCpuTime();
		u = getUpTime();
		Thread.sleep(sample);
		ec = (getOSProcessCpuTime() - c) / 1000000;
		eu = (getUpTime() - u);
		// System.err.println("C :" + ec + " U : " + eu);
		return (ec / (eu * ncpu)) * 100;
	}

	public final int getAvailableProcessors() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=OperatingSystem");
		Integer ut;
		ut = (Integer) mbsc.getAttribute(mbeanName, "AvailableProcessors");
		return ut.intValue();
	}

	public long getUpTime() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=Runtime");
		Long ut;
		ut = (Long) mbsc.getAttribute(mbeanName, "Uptime");
		return ut.longValue();
	}

	public long getOSProcessCpuTime() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=OperatingSystem");
		Long ut;
		ut = (Long) mbsc.getAttribute(mbeanName, "ProcessCpuTime");
		return ut.longValue();
	}

	public long getHeapUsage() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=Memory");
		CompositeDataSupport o;
		o = (CompositeDataSupport) mbsc.getAttribute(mbeanName,
				"HeapMemoryUsage");
		return ((Long) o.get("used")).longValue();
	}

	public long getNonHeapUsage() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=Memory");
		CompositeDataSupport o;
		o = (CompositeDataSupport) mbsc.getAttribute(mbeanName,
				"NonHeapMemoryUsage");
		return ((Long) o.get("used")).longValue();
	}

	public int getThreadCount() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=Threading");
		Integer ut;
		ut = (Integer) mbsc.getAttribute(mbeanName, "ThreadCount");
		return ut.intValue();
	}

	public int getLoadedClassCount() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=ClassLoading");
		Integer ut;
		ut = (Integer) mbsc.getAttribute(mbeanName, "LoadedClassCount");
		return ut.intValue();
	}

	public long getCMSUsageThresholdCount() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=MemoryPool,name=CMS Old Gen");
		Long ut;
		ut = (Long) mbsc.getAttribute(mbeanName, "UsageThresholdCount");
		return ut.longValue();
	}

	public long getCMSUsage() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=MemoryPool,name=CMS Old Gen");
		CompositeDataSupport o;
		o = (CompositeDataSupport) mbsc.getAttribute(mbeanName, "Usage");
		return ((Long) o.get("used")).longValue();
	}

	public long getEdenUsage() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName(
				"java.lang:type=MemoryPool,name=Par Eden Space");
		CompositeDataSupport o;
		o = (CompositeDataSupport) mbsc.getAttribute(mbeanName, "Usage");
		return ((Long) o.get("used")).longValue();
	}

	public final void getDeadLockedThreads() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=Threading");
		long[] dl = (long[]) mbsc.invoke(mbeanName, "findDeadlockedThreads",
				null, null);
		StringBuilder sb;
		if (dl != null) {
			sb = new StringBuilder();
			sb.append("Dead Lock Detected - Host:");
			sb.append(host);
			sb.append("\n");
			for (int i = 0; i < dl.length; i++) {
				sb.append("Thread " + dl[i] + "\n");
			}
		}
	}

	public String[] getRcptList(Properties conf) {
		int i = 0;
		String addr;
		ArrayList<String> addrList = new ArrayList<String>();
		while (true) {
			addr = conf.getProperty("mail.rcpto." + i);
			i++;
			if (addr != null)
				addrList.add(addr);
			if (addr == null)
				break;
		}
		return addrList.toArray(new String[0]);
	}

	public void showInfo() throws Exception {
		String domains[] = mbsc.getDomains();
		Arrays.sort(domains);
		for (String domain : domains) {
			echo("\tDomain = " + domain);
		}
		echo("\nMBeanServer default domain = " + mbsc.getDefaultDomain());
		echo("\nMBean count = " + mbsc.getMBeanCount());

		echo("\nQuery MBeanServer MBeans:");
		Set<ObjectName> names = new TreeSet<ObjectName>(mbsc.queryNames(null,
				null));
		for (ObjectName name : names) {
			echo("\tObjectName = " + name);
		}
	}

	private static void echo(String msg) {
		System.out.println(msg);
	}

	public long getSample() {
		return sample;
	}

	public void setSample(long sample) {
		this.sample = sample;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}
}

class LogInvoker implements Runnable {

	private String host;
	private String port;
	private String logDir;
	private Properties conf;

	public LogInvoker(String host, String port, String logDir, Properties conf) {
		this.host = host;
		this.port = port;
		this.logDir = logDir;
		this.conf = conf;
	}

	@Override
	public void run() {
		String[] params = new String[3];
		params[0] = host;
		params[1] = port;
		params[2] = logDir;
		try {
			JConsoleM.jvmLog(params, conf);
		} catch (Exception ex) {
			System.err.println("Thread Exception " + ex.getMessage());
		}
	}

	/**
	 * @return the host
	 */
	public String getHost() {
		return host;
	}

	/**
	 * @param host
	 *            the host to set
	 */
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * @return the port
	 */
	public String getPort() {
		return port;
	}

	/**
	 * @param port
	 *            the port to set
	 */
	public void setPort(String port) {
		this.port = port;
	}

	/**
	 * @return the logDir
	 */
	public String getLogDir() {
		return logDir;
	}

	/**
	 * @param logDir
	 *            the logDir to set
	 */
	public void setLogDir(String logDir) {
		this.logDir = logDir;
	}
}
