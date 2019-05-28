#!/bin/bash

set -e 

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

USER_ID=$(id -u)

# Prepare folders
FOLDER_PLUGINS_JARS=$(pwd)/_plugins-jars
mkdir -p $FOLDER_PLUGINS_JARS

# Create release
./create-local-release-no-tests.sh
cp build/libs/foilen-infra-plugins-core-master-SNAPSHOT.jar $FOLDER_PLUGINS_JARS

# Start webapp
docker run -ti \
  --rm \
  --env PLUGINS_JARS=/plugins \
  --env FOILEN_PLUGIN_SKIP_UPDATE_EVENTS=true \
  --user $USER_ID \
  --volume $FOLDER_PLUGINS_JARS:/plugins \
  --publish 8080:8080 \
  foilen-infra-system-app-test-docker:master-SNAPSHOT \
  web --debug
