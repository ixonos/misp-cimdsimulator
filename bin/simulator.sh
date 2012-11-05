#!/bin/sh

port=${CIMD_PORT:-9071}
msg_port=${CIMD_MSG_PORT:-9072} 

libs=`ls lib/*.jar|awk '{ ORS=":" } { print $1; }'`

# JVM remote debugging
#DEBUG=-agentlib:jdwp=transport=dt_socket,server=y,address=localhost:7070,suspend=n

# configuration parameters:
# -Dcimdsimulator.port=9071                         # mandatory
# -Dcimdsimulator.messagePort=9072                  # mandatory
# -Dcimdsimulator.messageInjectSleepTimeMillis=20   # optional
# -Dcimdsimulator.useCimdCheckSum=false             # optional

java $DEBUG -cp $libs -Dcimdsimulator.port=$port -Dcimdsimulator.messagePort=$msg_port com.ixonos.cimd.simulator.CIMDSimulator
