#!/bin/sh

port=${CIMD_PORT:-9071}
msg_port=${CIMD_MSG_PORT:-9072} 

libs=`ls lib/*.jar|awk '{ ORS=":" } { print $1; }'`

# -Dcimdsimulator.messageInjectSleepTimeMillis=5
java -cp $libs -Dcimdsimulator.port=$port -Dcimdsimulator.messagePort=$msg_port com.ixonos.cimd.simulator.CIMDSimulator
