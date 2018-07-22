#!/bin/sh
# Run like: ./fetch.sh --username=<username>,--password=<password>,--total=<total>
mvn spring-boot:run -Dspring-boot.run.arguments=$@
