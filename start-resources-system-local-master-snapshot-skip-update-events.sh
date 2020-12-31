#!/bin/bash

set -e 

if [ $# -ne 1 ]
  then
    echo Usage: $0 file or folder to import;
    exit 1;
fi

TO_IMPORT=$(readlink -f $1)

RUN_PATH="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd $RUN_PATH

# Prepare folders
FOLDER_PLUGINS_JARS=$(pwd)/_plugins-jars
mkdir -p $FOLDER_PLUGINS_JARS

# Create release
./create-local-release-no-tests.sh
cp build/libs/foilen-infra-plugins-core-*.jar $FOLDER_PLUGINS_JARS

# Start resources
docker run -ti \
  --rm \
  --env HOSTFS=/hostfs/ \
  --env PLUGINS_JARS=/plugins \
  --env FOILEN_PLUGIN_SKIP_UPDATE_EVENTS=true \
  --volume $FOLDER_PLUGINS_JARS:/plugins \
  --volume $TO_IMPORT:/to_import \
  --volume /etc:/hostfs/etc \
  --volume /home:/hostfs/home \
  --volume /usr/bin/docker:/usr/bin/docker \
  --volume /usr/lib/x86_64-linux-gnu/libltdl.so.7.3.1:/usr/lib/x86_64-linux-gnu/libltdl.so.7 \
  --volume /var/run/docker.sock:/var/run/docker.sock \
  foilen-infra-system-app-test-docker:master-SNAPSHOT \
  start-resources --debug \
  /to_import
