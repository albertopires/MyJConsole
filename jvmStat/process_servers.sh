#!/bin/bash

# $Revision: 174 $

./create_all.sh $1 187.33.29.11 7001 &
./create_all.sh $1 187.33.29.12 7001 &
./create_all.sh $1 187.33.29.13 7001 &
./create_all.sh $1 187.33.29.14 7001 &
wait
echo "End of Batch 1"
./create_all.sh $1 187.33.29.15 7001 &
./create_all.sh $1 187.33.29.16 7001 &
./create_all.sh $1 187.33.29.17 7001 &
./create_all.sh $1 187.33.29.18 7001 &
wait
echo "End of Batch 2"
./create_all.sh $1 187.33.29.19 7001 &
./create_all.sh $1 187.33.29.20 7001 &
./create_all.sh $1 187.33.29.21 7001 &
./create_all.sh $1 187.33.29.22 7001 &
wait
echo "End of Batch 3"
./create_all.sh $1 187.45.202.70 7001 &
./create_all.sh $1 201.76.53.21 7001 &
./create_all.sh $1 201.76.53.61 7001 &
./create_all.sh $1 201.76.53.63 7001 &
./create_all.sh $1 201.76.53.65 7001 &

wait 

date
