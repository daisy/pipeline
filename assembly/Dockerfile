# Use a multistage build to first build the pipeline using maven. Then
# copy the artifacts into a final image which exposes the port and
# starts the pipeline.

# Build Pipeline
FROM maven:3.8.4-jdk-11 as builder
RUN apt-get update && apt-get install -y make libxml2-utils
ADD src/ /usr/src/pipeline-assembly/src/
ADD pom.xml /usr/src/pipeline-assembly/
ADD Makefile /usr/src/pipeline-assembly/
ADD deps.mk /usr/src/pipeline-assembly/
WORKDIR /usr/src/pipeline-assembly
RUN make DOCKER=: docker

# Use the build artifacts to create an image with Pipeline installed
FROM debian:stretch
LABEL maintainer="DAISY Consortium (http://www.daisy.org/)"
# curl is needed for health check
RUN apt-get update && apt-get install -y curl
COPY --from=builder /usr/src/pipeline-assembly/target/docker/daisy-pipeline /opt/daisy-pipeline2
COPY --from=builder /usr/src/pipeline-assembly/target/docker/jre /opt/daisy-pipeline2/jre
ENV JAVA_HOME=/opt/daisy-pipeline2/jre
ENV PIPELINE2_WS_LOCALFS=false \
    PIPELINE2_WS_AUTHENTICATION=true \
    PIPELINE2_WS_AUTHENTICATION_KEY=clientid \
    PIPELINE2_WS_AUTHENTICATION_SECRET=sekret
EXPOSE 8181
# for the health check use PIPELINE2_WS_HOST if defined. Otherwise use localhost
HEALTHCHECK --interval=30s --timeout=10s --start-period=1m CMD curl --fail http://${PIPELINE2_WS_HOST-localhost}:${PIPELINE2_WS_PORT:-8181}/${PIPELINE2_WS_PATH:-ws}/alive || exit 1
ENTRYPOINT ["/opt/daisy-pipeline2/bin/pipeline2"]
