/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package music.penguin.jtools;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.openmbean.CompositeDataSupport;

/**
 *
 * @author alberto
 */
public class JConsole {

    private MBeanServerConnection mbsc;
    private int ncpu = 1;
    private long sample;
    private String host;
    private String port;
    private String dir;
    public String[] stats;

    public static void main(String[] args) throws Exception {
        JConsole jc;
        StringBuffer line;

        if (args.length != 3) {
            System.err.println("JMX Monitor v1.0a - 23/Jun/2008");
            System.err.println("Parameters: <host> <port> <dir>\n");
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

        try {
            jc = getInstance(args[0], args[1]);
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
                    jc = getInstance(args[0], args[1]);
                    jc.setDir(args[2]);
                    jc.setSample(4000);
                }
            }
        } catch (Exception e) {
            System.err.println("Exception : " + e.getMessage());
        }
    }

    private JConsole(String host, String port) throws Exception {
        this.host = host;
        this.port = port;
        String urlStr = "service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi";
        JMXServiceURL url = new JMXServiceURL(urlStr);
        JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
        mbsc = jmxc.getMBeanServerConnection();
        ncpu = getAvailableProcessors();
        stats = new String[8];
    }

    public static JConsole getInstance(String host, String port) {
        JConsole jc = null;

        while (jc == null) {
            try {
                jc = new JConsole(host, port);
            } catch (Exception ex) {
                jc = null;
            }

            try {
                Thread.sleep(20000);
            } catch (InterruptedException ex) {
                System.err.println("InterruptedException : " + ex.getMessage());
            }
        }
        return jc;
    }

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
        stats[0] = String.format("%.2f", getCpuUsage());
        stats[1] = "" + getHeapUsage();
        stats[2] = "" + getLoadedClassCount();
        stats[3] = "" + getThreadCount();
        stats[4] = "" + getCMSUsage();
        stats[5] = "" + getEdenUsage();
        stats[6] = "" + getNonHeapUsage();
        stats[7] = "" + getCMSUsageThresholdCount();
    }

    public double getCpuUsage() throws Exception {
        long c, u;
        double ec, eu;

        c = getOSProcessCpuTime();
        u = getUpTime();
        Thread.sleep(sample);
        ec = (getOSProcessCpuTime() - c) / 1000000;
        eu = (getUpTime() - u);
        //System.err.println("C :" + ec + " U : " + eu);
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

    public long getNonHeapUsage() throws Exception {
        ObjectName mbeanName;
        mbeanName = new ObjectName("java.lang:type=Memory");
        CompositeDataSupport o;
        o = (CompositeDataSupport) mbsc.getAttribute(mbeanName, "NonHeapMemoryUsage");
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
        mbeanName = new ObjectName("java.lang:type=MemoryPool,name=Par Eden Space");
        CompositeDataSupport o;
        o = (CompositeDataSupport) mbsc.getAttribute(mbeanName, "Usage");
        return ((Long) o.get("used")).longValue();
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
        Set<ObjectName> names =
                new TreeSet<ObjectName>(mbsc.queryNames(null, null));
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
