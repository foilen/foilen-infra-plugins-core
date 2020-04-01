#!/bin/bash

set -e

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

echo ----[ Verify Jar ]----
if unzip -l build/libs/foilen-infra-plugins-core-$VERSION.jar | grep org/shredzone/acme4j --quiet; then
	echo OK Found the Acme classes
else
	echo FAIL Could not find the Acme classes
	exit 1
fi

if unzip -l build/libs/foilen-infra-plugins-core-$VERSION.jar | grep org.kohsuke.args4j --quiet; then
	echo FAIL Found args4j classes
	exit 1
else
	echo "OK Didn't find the args4j classes"
fi

if unzip -l build/libs/foilen-infra-plugins-core-$VERSION.jar | grep com.foilen.smalltools --quiet; then
	echo FAIL Found smalltools classes
	exit 1
else
	echo "OK Didn't find the smalltools classes"
fi

if unzip -l build/libs/foilen-infra-plugins-core-$VERSION.jar | grep org.springframework --quiet; then
	echo FAIL Found spring classes
	exit 1
else
	echo "OK Didn't find the spring classes"
fi
