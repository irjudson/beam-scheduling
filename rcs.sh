#!/usr/bin/env bash
#
#


# Scenario 1: vary channels => 3, 6, 9, 12, 15

for i in "3 6 9 12 15"; do
    java -jar target/beam-scheduling-1.0-jar-with-dependencies.jar -c $i >& rcs-channel-${i}.out
done


# Scenario 2: vary 
