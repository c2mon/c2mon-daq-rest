#!/usr/bin/env bash
VERSION=`cat ${DAQ_HOME}/version.txt`
JVM_OTHER_OPTS="-Dapp.name=${PROCESS_NAME} -Dapp.version=${VERSION}"
