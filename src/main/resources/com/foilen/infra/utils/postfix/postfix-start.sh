#!/bin/bash

set -e

echo Running as $(whoami)

service postfix start

sleep 5s

PID_GMGR=$(pidof qmgr)
PID_PICKUP=$(pidof pickup)

while [ -e /proc/$PID_GMGR -a -e /proc/$PID_PICKUP ]; do sleep 5; done

echo Postfix service is down
