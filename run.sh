#!/usr/bin/env bash

export VISALLO_DIR=./work

if [ ! -d "$VISALLO_DIR/war" ]; then
    mvn package
fi
mvn jetty:run-war
