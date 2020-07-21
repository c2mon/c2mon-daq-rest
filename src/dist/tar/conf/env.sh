#!/usr/bin/env bash
VERSION=`cat ${DAQ_HOME}/version.txt`
JVM_OTHER_OPTS="-Dapp.name=${PROCESS_NAME} -Dapp.version=${VERSION} -Dc2mon.client.conf.url=http://timweb.cern.ch/conf/c2mon-client.properties"
