#!/usr/bin/env bash

# This test starts the pipeline inside a Docker container and then
# starts the dtbook-to-epub script using the cli inside a second
# container

# take any old DTBook file for testing for example from
# https://github.com/daisy/pipeline-samples
DOCUMENT=test.xml
DATA_DIR=$(pwd)
DATA=$(basename $DOCUMENT).zip
CLIENTKEY=clientid
CLIENTSECRET=sekret
MOUNT_POINT=/mnt

zip $DATA $DOCUMENT

# run the pipeline
docker run --name pipeline --rm --detach \
       -e PIPELINE2_HOST=0.0.0.0 \
       -e PIPELINE2_AUTH=true \
       -e PIPELINE2_AUTH_CLIENTKEY=$CLIENTKEY \
       -e PIPELINE2_AUTH_CLIENTSECRET=$CLIENTSECRET \
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
       --volume="$DATA_DIR:$MOUNT_POINT:rw" \
       daisyorg/pipeline2 \
       --host http://pipeline \
       --starting false \
       --client_key $CLIENTKEY \
       --client_secret $CLIENTSECRET \
       dtbook-to-epub3 --source $DOCUMENT --output $MOUNT_POINT --data $MOUNT_POINT/$DATA --persistent 
#       help dtbook-to-epub3
#       dtbook-validator --input-dtbook $DOCUMENT --output $MOUNT_POINT --data $MOUNT_POINT/$DATA

docker stop pipeline

