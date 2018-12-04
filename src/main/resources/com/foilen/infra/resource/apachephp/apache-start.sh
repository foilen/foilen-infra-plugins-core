#!/bin/bash

set -e
source /etc/apache2/envvars
/usr/sbin/apache2ctl start

until [ -f /var/run/apache2/apache2.pid ]
do
	echo Waiting for /var/run/apache2/apache2.pid
	sleep 1
done

APP_PID=$(cat /var/run/apache2/apache2.pid)
while [ -e /proc/$APP_PID ]; do sleep 5; done

echo Apache service is down
