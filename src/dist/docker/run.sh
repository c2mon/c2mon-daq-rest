#!/usr/bin/env bash
docker run --rm --name daq-rest -ti --net=host -e "C2MON_PORT_61616_TCP=tcp://localhost:61616" docker.cern.ch/c2mon-project/daq-rest bin/C2MON-DAQ-STARTUP.jvm -f $@
