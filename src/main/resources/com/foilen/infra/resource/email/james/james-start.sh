#!/bin/bash

set -e

cp /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunjce_provider.jar /james-server-app/lib/

cd /james-server-app
./bin/james start


PIDFILE="var/james.pid"

until [ -f $PIDFILE ]
do
	echo Waiting for $PIDFILE
	sleep 1
done

APP_PID=$(cat $PIDFILE)
while [ -e /proc/$APP_PID ]; do sleep 5; done

echo Service is down
