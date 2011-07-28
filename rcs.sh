#!/usr/bin/env bash
#
#

JAR="target/beam-scheduling-1.0-jar-with-dependencies.jar"
OUTDIR="$1"

if [ ! -d $OUTDIR ]; then
    mkdir $OUTDIR
fi

# Scenario 1: vary channels => 3, 6, 9, 12, 15
for i in 1 2 3 4 5; do
    OUTFILE="${OUTDIR}/rcs-channel-${i}.csv"
    if [ ! -f $OUTFILE ]; then 
	echo "Running test for ${i} channels"
	java -Xms3072m -Xmx4096m -jar ${JAR} -c $i &> ${OUTFILE}
    fi
done

# Scenario 2: vary size, keep density constant
# Width/Height  30 40 50 60 70 
# Nodes          9 16 25 36 49 
OUTFILE="${OUTDIR}/rcs-area-30.9.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 30km x 30km with 9 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 30000.0 -h 30000.0 -n 9 &> $OUTFILE
fi

OUTFILE="${OUTDIR}/rcs-area-40.16.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 40km x 40km with 16 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 40000.0 -h 40000.0 -n 16 &> $OUTFILE
fi

OUTFILE="${OUTDIR}/rcs-area-50.25.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 50km x 50km with 25 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 50000.0 -h 50000.0 -n 25 &> $OUTFILE
fi

OUTFILE="${OUTDIR}/rcs-area-60.36.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 60km x 60km with 36 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 60000.0 -h 60000.0 -n 36 &> $OUTFILE
fi

OUTFILE="${OUTDIR}/rcs-area-70.49.csv"
if [ ! -f $OUTFILE ]; then 
    echo "Running test for area of 70km x 70km with 49 nodes"
    java -Xms3072m -Xmx4096m -jar ${JAR} -w 70000.0 -h 70000.0 -n 49 &> $OUTFILE
fi

# Scenario 3: vary node density
# Width/Height: 50
# Nodes 9 16 25 36 49 
for i in 9 16 25 36 49; do
    OUTFILE="${OUTDIR}/rcs-density-${i}.csv"
    if [ ! -f $OUTFILE ]; then 
    echo "Running test for ${i} channels"
    java -Xms3072m -Xmx4096m -jar ${JAR} -n $i &> ${OUTFILE}
    fi
done
