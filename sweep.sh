#!/usr/bin/env bash
#

SUBSCRIBERS="20 30 40 50 60"
RELAYS="6 8 10 12 14"
THETA="20 30 40 50 60"
MEANQ="20000 30000 40000 50000 60000"
SIZE="20000 30000 40000 50000 60000"
ITERATIONS="1 2 3 4 5 6 7 8 9 10"

export CPLEXBIN=/opt/cplex/cplex/bin/x86-64_darwin9_gcc4.0
export JAR=target/beam-scheduling-1.0-jar-with-dependencies.jar 

for s in $SUBSCRIBERS; do
 for r in $RELAYS; do
  for t in $THETA; do
   for m in $MEANQ; do
    for b in $SIZE; do
     for i in $ITERATIONS; do
      ( java -Djava.library.path=${CPLEXBIN} -jar ${JAR} -t $t -c $s -w $b -h $b -u $m -n $r  >& output/output.$s.$r.$t.$m.$b.$i ) &
     done
     wait
     cat output/output.$s.$r.$t.$m.$b.* >> output.all
     echo "10 jobs finished..."
    done
   done
  done
 done
done

cat output* | grep -v IBM | grep -v Seed > combined.out

exit 0

