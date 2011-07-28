#!/usr/bin/env bash
#
#

JAR="target/beam-scheduling-1.0-jar-with-dependencies.jar"

# Scenario 1: vary channels => 3, 6, 9, 12, 15
for i in 1 2 3 4 5; do
    OUTFILE="rcs-channel-${i}.csv"
    if [ ! -f $OUTFILE ]; then 
	echo "Running test for ${i} channels"
	java -Xms3072m -Xmx4096m -jar ${JAR} -c $i &> ${OUTFILE}
    fi
done

# Scenario 2: vary size, keep density constant
# Width/Height  30 40 50 60 70 80
# Nodes          9 16 25 36 49 64
OUTFILE="rcs-area-30.9.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 30km x 30km with 9 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 30000.0 -h 30000.0 -n 9 &> $OUTFILE
fi

OUTFILE="rcs-area-40.16.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 40km x 40km with 16 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 40000.0 -h 40000.0 -n 16 &> $OUTFILE
fi

OUTFILE="rcs-area-50.25.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 50km x 50km with 25 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 50000.0 -h 50000.0 -n 25 &> $OUTFILE
fi

OUTFILE="rcs-area-60.36.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 60km x 60km with 36 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 60000.0 -h 60000.0 -n 36 &> $OUTFILE
fi

OUTFILE="rcs-area-70.49.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 70km x 70km with 49 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 70000.0 -h 70000.0 -n 49 &> $OUTFILE
fi


OUTFILE="rcs-area-80.64.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 80km x 80km with 64 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 80000.0 -h 80000.0 -n 64 &> rcs-area-80.64.csv
fi

# Scenario 3: vary node density
# Width/Height: 50
# Nodes 9 16 25 36 49 
for i in 9 16 25 36 49; do
    OUTFILE="rcs-density-${i}.csv"
    if [ ! -f $OUTFILE ]; then 
    echo "Running test for ${i} channels"
    java -Xms3072m -Xmx4096m -jar ${JAR} -n $i &> ${OUTFILE}
    fi
done
