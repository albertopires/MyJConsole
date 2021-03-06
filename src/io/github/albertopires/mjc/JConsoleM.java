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
	private BeanConf beanConf;
	public ArrayList<String> stats;

	/**
	 * Create a monitor instance (JConsoleM) for each pair host:port and save collected data to it's respective log file.
	 * @param args
	 * @param conf
	 * @throws Exception
	 */
	public static void jvmLog(String[] args, Properties conf, BeanConf beanConf) throws Exception {
		JConsoleM jc;
		StringBuffer line;

		try {
			jc = getInstance(args[0], args[1], conf, beanConf);
			jc.setDir(args[2]);
			jc.setSample(4000);

			for (;;) {
				try {
					jc.runStats();
					line = new StringBuffer();
					Date d = new Date();
					line.append("" + d.getTime() + " ");
					for (String stat : jc.stats) {
						line.append(stat + " ");
					}
					line.append("\n");
					jc.logToFile(line.toString());
				} catch (Exception e) {
					System.err.println("Loop Exception : " + e.getMessage());
					jc = getInstance(args[0], args[1], conf, beanConf);
					jc.setDir(args[2]);
					jc.setSample(4000);
				}
			}
		} catch (Exception e) {
			System.err.println("Exception : " + e.getMessage());
		}
	}

	private JConsoleM(String host, String port, Properties conf, BeanConf beanConf) throws Exception {
		this.host = host;
		this.port = port;
		this.beanConf = beanConf;
		this.stats = new ArrayList<String>();

		String urlStr = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
		JMXServiceURL url = new JMXServiceURL(urlStr);
		JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
		mbsc = jmxc.getMBeanServerConnection();
		ncpu = getAvailableProcessors();
	}

	public static JConsoleM getInstance(String host, String port, Properties conf, BeanConf beanConf) {
		JConsoleM jc = null;

		// At the time the instance is created, it is possible that the jvm to monitored is offline.
		// In that case the connection will fail, if that happens wait 10 seconds and try again until
		// a successful connection is obtained.
		while (jc == null) {
			try {
				jc = new JConsoleM(host, port, conf, beanConf);
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

	/**
	 * Write a line with the data columns to host_port_date file. It creates one file per day.
	 *
	 * @param line
	 */
	public void logToFile(String line) {
		Calendar c = Calendar.getInstance();
		String name = dir + "/" + host + "_" + port + "_" + c.get(Calendar.YEAR);
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
		stats.clear();
		stats.add(String.format("%.2f", getCpuUsage())); // 1
		stats.add("" + getHeapUsage());                  // 2
		stats.add("" + getLoadedClassCount());           // 3
		stats.add("" + getThreadCount());                // 4

		for (String[] bean : beanConf.getBeans()) {
			String stat = getBean(bean[BeanConf.NAME], bean[BeanConf.ATTR0], bean[BeanConf.ATTR1]);
			stats.add(stat);
		}

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
		o = (CompositeDataSupport) mbsc.getAttribute(mbeanName, "HeapMemoryUsage");
		return ((Long) o.get("used")).longValue();
	}

	public String getNonHeapUsage() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=Memory");
		CompositeDataSupport o;
		o = (CompositeDataSupport) mbsc.getAttribute(mbeanName, "NonHeapMemoryUsage");
		return o.get("used").toString();
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

	public String getBean(String objectName, String attribute0, String attribute1) throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName(objectName);
		CompositeDataSupport o;
		if (attribute1 == null) {
			Object obj = mbsc.getAttribute(mbeanName, attribute0);
			return obj.toString();
		}
		o = (CompositeDataSupport) mbsc.getAttribute(mbeanName, attribute0);
		return o.get(attribute1).toString();
	}

	public final void getDeadLockedThreads() throws Exception {
		ObjectName mbeanName;
		mbeanName = new ObjectName("java.lang:type=Threading");
		long[] dl = (long[]) mbsc.invoke(mbeanName, "findDeadlockedThreads", null, null);
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

	public void showInfo() throws Exception {
		String domains[] = mbsc.getDomains();
		Arrays.sort(domains);
		for (String domain : domains) {
			echo("\tDomain = " + domain);
		}
		echo("\nMBeanServer default domain = " + mbsc.getDefaultDomain());
		echo("\nMBean count = " + mbsc.getMBeanCount());

		echo("\nQuery MBeanServer MBeans:");
		Set<ObjectName> names = new TreeSet<ObjectName>(mbsc.queryNames(null, null));
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
