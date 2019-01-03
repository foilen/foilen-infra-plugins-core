#!/bin/bash

set -e

if [ -d /etc/php5 ]; then
	cd /etc/php5
	for SUB in $(ls); do
		if [ -d $SUB/conf.d/ ]; then
			cp /99-fcloud.ini $SUB/conf.d/
		fi
	done
fi

if [ -d /etc/php ]; then
	cd /etc/php
	for VER in $(ls); do
		cd /etc/php/$VER
		for SUB in $(ls); do
			if [ -d $SUB/conf.d/ ]; then
				cp /99-fcloud.ini $SUB/conf.d/
			fi
		done
	done
fi
