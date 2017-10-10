#!/usr/bin/env bash

# This test starts the pipeline inside a Docker container and then
# starts the dtbook-to-epub3 script using the cli inside a second
# container

set -e
cd $(dirname "$0")

# download test DTBook from https://github.com/daisy/pipeline-samples
DATA_DIR=../../../target/test-docker/data
mkdir -p $DATA_DIR
cd $DATA_DIR
DTBOOK=hauy_valid.xml
DATA=$(basename $DTBOOK).zip
rm -f $DATA
for f in \
    $DTBOOK \
    dtbook.2005.basic.css \
    valentin.jpg
do
    curl https://raw.githubusercontent.com/daisy/pipeline-samples/10bbb8e/dtbook/$f 2>/dev/null >$f
    zip $DATA $f
done

CLIENTKEY=clientid
CLIENTSECRET=sekret
MOUNT_POINT=/mnt

# run the pipeline
docker run --name pipeline --detach \
       -e PIPELINE2_WS_HOST=0.0.0.0 \
       -e PIPELINE2_AUTH=true \
       -e PIPELINE2_WS_AUTHENTICATION_KEY=$CLIENTKEY \
       -e PIPELINE2_WS_AUTHENTICATION_SECRET=$CLIENTSECRET \
       -p 8181:8181 daisyorg/pipeline2

# wait for the pipeline to start
sleep 5
while ! curl localhost:8181/ws/alive >/dev/null 2>/dev/null; do
    echo "Waiting for web service to be up..." >&2
    sleep 2
done

# run the cli
docker run --name cli --rm -it --link pipeline \
       --entrypoint /opt/daisy-pipeline2/cli/dp2 \
       --volume="$(pwd):$MOUNT_POINT:rw" \
       daisyorg/pipeline2 \
       --host http://pipeline \
       --starting false \
       --client_key $CLIENTKEY \
       --client_secret $CLIENTSECRET \
       dtbook-to-epub3 --source $DTBOOK --output $MOUNT_POINT --data $MOUNT_POINT/$DATA --persistent
#       help dtbook-to-epub3
#       dtbook-validator --input-dtbook $DTBOOK --output $MOUNT_POINT --data $MOUNT_POINT/$DATA

docker stop pipeline
docker rm pipeline

