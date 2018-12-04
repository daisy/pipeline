#!/usr/bin/env bash

set -e
cd $(dirname "$0")

ROOT_DIR=$(pwd)
DATA_DIR=target/test-docker/data
mkdir -p $DATA_DIR
cd $DATA_DIR
rm -rf notes.epub
cp -r $ROOT_DIR/src/test/resources/notes.epub .
DATA=notes.epub.zip
rm -f $DATA
cd notes.epub && zip $DATA * && mv $DATA ../ && cd ..
rm -f pef-output-dir/123456.pef

CLIENTKEY=clientid
CLIENTSECRET=sekret
MOUNT_POINT=/mnt

# run the pipeline
docker run --name pipeline --detach \
       -e PIPELINE2_WS_HOST=0.0.0.0 \
       -e PIPELINE2_WS_AUTHENTICATION=true \
       -e PIPELINE2_WS_AUTHENTICATION_KEY=$CLIENTKEY \
       -e PIPELINE2_WS_AUTHENTICATION_SECRET=$CLIENTSECRET \
       -p 8181:8181 notalib/pipeline-assembly

# wait for the pipeline to start
sleep 5
tries=10
while ! curl localhost:8181/ws/alive >/dev/null 2>/dev/null; do
    if [[ $tries > 0 ]]; then
        echo "Waiting for web service to be up..." >&2
        sleep 2
        (( tries-- ))
    else
        docker logs pipeline
        docker stop pipeline
        docker rm pipeline
        exit 1
    fi
done

RESULT=0

# run the cli
if docker run --name cli --rm -it --link pipeline \
              --entrypoint /opt/daisy-pipeline2/cli/dp2 \
              --volume="$(pwd):$MOUNT_POINT:rw" \
              daisyorg/pipeline-assembly \
              --host http://pipeline \
              --starting false \
              --client_key $CLIENTKEY \
              --client_secret $CLIENTSECRET \
              nota:epub3-to-pef --epub package.opf --output $MOUNT_POINT --data $MOUNT_POINT/$DATA --persistent;
then
    if test -e pef-output-dir/123456.pef; then
       :
    else
       echo "no PEF was created" >&2
       RESULT=1
    fi
else
    docker logs server > server.log 2> server.log
    open .
    RESULT=1
fi

docker stop pipeline
docker rm pipeline

exit $RESULT
