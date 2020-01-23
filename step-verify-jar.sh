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
