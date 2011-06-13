#!/usr/bin/env bash
#

THETA="$1"
CLIENTS="$2"
SECTORS="$3"

export CPLEXBIN=/opt/cplex/cplex/bin/x86-64_darwin9_gcc4.0
export JAR=target/beam-scheduling-1.0-jar-with-dependencies.jar 

java -Djava.library.path=${CPLEXBIN} -jar ${JAR} -t $THETA -m $SECTORS -c $CLIENTS | grep -v IBM
