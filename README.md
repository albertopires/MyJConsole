# MyJConsole #

The idea of MyJConsole is to collect the same data as the standard java **jconsole** program. The difference is that **jconsole** does not allow collected data to be stored, only displayed.
**MyJConsole** does the opposite, it collects and store jvm data for an external program to use it. An example would some scripts that insert it into a RRD to create some nice graphs.

## How it works ##

It's quite simple, you just run:

``java -jar my-jconsole-x.y.z ./sample.conf ./sample_bean.conf ``

The configuration file is quite simple and you can build one from the template file **sample.conf**.
There are four default mbeans monitored, other can be configured using the template file **sample_bean**. There are two examples:

* sample_bean_jdk6.conf
* sample_bean_jdk8.conf

The mbeans will be written to a file name ipaddr_port_YYYYMMDD and the columns are:


```
Fields:
0 - TimeStamp
1 - CPU Usage
2 - Heap Usage
3 - Loaded Class Count
4 - Thread Count
5 - User configured bean
  .
  .
N - User configured bean
```


## Requirements ##
It uses java JDK 8. As far as I've tested it can monitor jdk's 6 to 8.

An example of jdk options to test with **sample_bean_jdk6.conf**

* -XX:ParallelGCThreads=5
* -XX:+UseConcMarkSweepGC
* -XX:+UseParNewGC
* -Dcom.sun.management.jmxremote.authenticate=false
* -Dcom.sun.management.jmxremote.ssl=false
* -Dcom.sun.management.jmxremote.port=7001

or just copy and paste:

`-XX:ParallelGCThreads=5
-XX:+UseConcMarkSweepGC
-XX:+UseParNewGC
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
-Dcom.sun.management.jmxremote.port=7001`

An example of jdk options to test with **sample_bean_jdk8.conf**

* -XX:+UseG1GC
* -Dcom.sun.management.jmxremote.port=7001
* -Dcom.sun.management.jmxremote.authenticate=false
* -Dcom.sun.management.jmxremote.ssl=false

or just copy and paste:

`-XX:+UseG1GC
-Dcom.sun.management.jmxremote.port=7001 -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false`
