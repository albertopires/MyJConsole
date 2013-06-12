#!/bin/bash

# $Revision: 24 $

echo 187.33.29.11
scp -C root@187.33.29.11:~/jvmStats/*_$1 .
