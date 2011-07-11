#!/usr/bin/env bash
#

SUBSCRIBERS="20 30 40 50 60"
RELAYS="6 8 10 12 14"
RELAYS="2 3 4 5 6"
THETA="20 30 40 50 60"
MEANQ="20000 30000 40000 50000 60000"
SIZE="20000 30000 40000 50000 60000"
CHANNELS="2 3 4 5 6"
ITERATIONS="1 2 3 4 5 6 7 8 9 10"

MSUB="40"
MREL="10"
MREL="4"
MTHE="40"
MMEA="40000"
MSIZ="40000"
MCHA="4"

export CPLEXBIN=/opt/cplex/cplex/bin/x86-64_darwin9_gcc4.0
export JAR=target/beam-scheduling-1.0-jar-with-dependencies.jar 

for s in $SUBSCRIBERS; do
    for i in $ITERATIONS; do
      ( java -Djava.library.path=${CPLEXBIN} -jar ${JAR} -t $MTHE -c $s -w $MSIZ -h $MSIZ -u $MMEA -n $MREL -k $MCHA  >& output/output.$s.$MREL.$MTHE.$MMEA.$MSIZ.$MCHA.$i) &    
      wait
    done
done

for r in $RELAYS; do
    for i in $ITERATIONS; do
      ( java -Djava.library.path=${CPLEXBIN} -jar ${JAR} -t $MTHE -c $MSUB -w $MSIZ -h $MSIZ -u $MMEA -n $r  >& output/output.$MSUB.$r.$MTHE.$MMEA.$MSIZ.$MCHA.$i) &    
      wait
    done
done

for t in $THETA; do
    for i in $ITERATIONS; do
      ( java -Djava.library.path=${CPLEXBIN} -jar ${JAR} -t $t -c $MSUB -w $MSIZ -h $MSIZ -u $MMEA -n $MREL -k $MCHA  >& output/output.$MSUB.$MREL.$t.$MMEA.$MSIZ.$MCHA.$i) &    
      wait
    done
done

for m in $MEANQ; do
    for i in $ITERATIONS; do
      ( java -Djava.library.path=${CPLEXBIN} -jar ${JAR} -t $MTHE -c $MSUB -w $MSIZ -h $MSIZ -u $m -n $MREL -k $MCHA  >& output/output.$MSUB.$MREL.$MTHE.$m.$MSIZ.$MCHA.$i) &    
      wait
    done
done

for b in $SIZE; do
    for i in $ITERATIONS; do
      ( java -Djava.library.path=${CPLEXBIN} -jar ${JAR} -t $MTHE -c $MSUB -w $b -h $b -u $MMEA -n $MREL -k $MCHA >& output/output.$MSUB.$MREL.$MTHE.$MMEA.$b.$MCHA.$i ) &    
      wait
    done
done

for k in $CHANNELS; do
    for i in $ITERATIONS; do
	( java -Djava.library.path=${CPLEXBIN} -jar ${JAR} -t $MTHE -c $MSUB -w $MSIZ -h $MSIZ -u $MMEA -n $MREL -k $k >& output/output.$MSUB.$MREL.$MTHE.$MMEA.$MSIZ.$k.$i ) &    
	wait
    done
done

cat output/output* | grep -v IBM | grep -v Seed > combined.out

exit 0

