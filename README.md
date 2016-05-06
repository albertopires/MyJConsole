# MyJConsole #

The idea of MyJConsole is to collect the same data as the standard java **jconsole** program. The difference is that **jconsole** does not allow collected data to be stored, only displayed.
**MyJConsole** does the opposite, it collects and store jvm data for an external program to use it. An example would some scripts that insert it into a RRD to create some nice graphs.

## How it works ##

It's quite simple, you just run the **JConsoleM** class using a configuration file as parameter. 
The configuration file is quite simple and you can build one from the file **sample.conf**.
The mbeans will be written to a file name ipaddr_port_YYYYMMDD and the columns are:


```Fields:
0 - TimeStamp
1 - CPU Usage
2 - Heap Usage
3 - Loaded Class Count
4 - Thread Count
5 - CMS Usage
6 - Par Eden Usage
7 - Non-Heap Usage
8 - CMS Usage Threshold Count```


## Requirements ##
For the moment, this tool is tested only for jdk6 (bummer), and to collect specific beans from an specific **gc** configuration, usually used on web servers. I wrote this tool to solve a quit problem, it's not very flexible yet. But until it is here is the options you should use on your jvm.

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
