MAIN_CLASS := SETSOrderBookExercise
WORKLOAD ?= workloads/canonical_replay.csv

.PHONY: test package replay benchmark

test:
	mvn test

package:
	mvn package

replay:
	mvn -q exec:java -Dexec.mainClass=$(MAIN_CLASS) -Dexec.args="--input $(WORKLOAD)"

benchmark:
	mvn -q exec:java -Dexec.mainClass=$(MAIN_CLASS) -Dexec.args="--input $(WORKLOAD) --benchmark"
