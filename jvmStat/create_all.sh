#!/bin/bash

# $Revision: 24 $

if [ $# -ne 3 ] ; then
   echo "<YYYY-MM-DD> <host> <port>"
   exit
fi

DATE=$1
HOST=$2
PORT=$3
FNAME="$HOST"_"$PORT"_"$(echo $DATE | sed 's/-//g')"
export TMP_DIR=$FNAME"_$(date +%s)"
mkdir $TMP_DIR

export IMG_DIR=`echo $DATE | sed 's/-/\//g'`
#IMG_DIR="stats/"$HOST"_$PORT/"$IMG_DIR
IMG_DIR="/home/alberto/rrd/jvm/stats/"$HOST"_$PORT/"$IMG_DIR
echo $IMG_DIR

./jvm.sh $DATE
./gen_script.sh $FNAME > $TMP_DIR/x.sh
chmod 755 $TMP_DIR/x.sh
cd $TMP_DIR
./x.sh
cd ..

./cpu.sh $DATE
./heap.sh $DATE
./classes.sh $DATE
./threadc.sh $DATE
./pareden.sh $DATE
./cms.sh $DATE
./nonheap.sh $DATE

#mv -vi *.png $IMG_DIR
mv $TMP_DIR/*.png $IMG_DIR
rm -rf $TMP_DIR

#1 - CPU Usage
#2 - Heap Usage
#3 - Loaded Class Count
#4 - Thread Count
#5 - CMS Usage
#6 - Par Eden Usage
#7 - Non-Heap Usage
#8 - CMS Usage Threshold Count
