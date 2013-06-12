#!/bin/bash

# $Revision: 24 $

cat $1 | awk '
{
times=$1/1000; 
printf( "rrdtool update jvm.rrd %d:%f:%s:%s:%s:%s:%s:%s\n" ,times ,$2,$3,$4,$5,$6,$7,$8) 
}'



#0 - TimeStamp
#1 - CPU Usage
#2 - Heap Usage
#3 - Loaded Class Count
#4 - Thread Count
#5 - CMS Usage
#6 - Par Eden Usage
#7 - Non-Heap Usage
#8 - CMS Usage Threshold Count

