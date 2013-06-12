#!/bin/bash

# $Revision: 23 $

rrdtool graphv $TMP_DIR/thread.png   \
        --start `./convDate $1 00:00:00` \
        --end   `./convDate $1 23:59:59` \
        --step  4                                \
        --title "Thread Count - $1" \
		-w 600 -h 150                    \
        DEF:AAA=$TMP_DIR/jvm.rrd:ThreadCount:MAX \
        AREA:AAA#5555BBBB	 \
