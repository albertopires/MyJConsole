rrdtool graphv heap_a.png   \
        --start `./convDate $1 00:00:00` \
        --end   `./convDate $1 23:59:59` \
        --step  4                                \
        --title "Heap Usage" \
		-w 600 -h 150                    \
        DEF:AAA=jvm.rrd:HeapUsage:AVERAGE		 \
        AREA:AAA#00AA00			 \
