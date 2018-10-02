# Build the webui
FROM java as builder

# Uncomment the following during development to reduce build time
#RUN apt install git && \
#    cd /tmp && git clone https://github.com/daisy/pipeline-webui && \
#    cd /tmp/pipeline-webui && ./activator publish-local

ADD . /usr/src/webui
WORKDIR /usr/src/webui
# use the sbt docker plugin to create the artifacts
# (http://www.scala-sbt.org/sbt-native-packager/formats/docker.html)
RUN ./activator clean docker:stage
# the webui expects the db under data/db
WORKDIR /usr/src/webui/target/docker/stage/opt/docker
RUN mkdir data && mv db-empty data/db


# then use the build artifacts to create an image where the pipeline is installed
FROM openjdk:8-jre
LABEL maintainer="DAISY Consortium (http://www.daisy.org/)"
COPY --from=builder /usr/src/webui/target/docker/stage/opt/docker /opt/daisy-pipeline2-webui/.
RUN mkdir /run/daisy-pipeline2-webui
EXPOSE 9000 9443
ENTRYPOINT ["/opt/daisy-pipeline2-webui/bin/pipeline2-webui"]
CMD []
