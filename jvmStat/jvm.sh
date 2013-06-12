#!/bin/bash

# $Revision: 24 $

rrdtool create $TMP_DIR/jvm.rrd \
	--start `./convDate $1 00:00:00`-4 \
	--step 4 \
	DS:CPU:GAUGE:60:U:U \
	DS:HeapUsage:GAUGE:60:U:U \
	DS:ClassLoad:GAUGE:60:U:U \
	DS:ThreadCount:GAUGE:60:U:U \
	DS:CMSUsage:GAUGE:60:U:U \
	DS:ParEden:GAUGE:60:U:U \
	DS:NonHeap:GAUGE:60:U:U \
	RRA:MAX:0.5:1:21600 \
	RRA:AVERAGE:0.5:900:21600

