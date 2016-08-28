#!/bin/sh
# Run like: ./fetch.sh --username=<username>,--password=<password>,--total=<total>
mvn spring-boot:run -Drun.arguments="$@"
