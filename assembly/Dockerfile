# Use a multistage build to first build the pipeline using maven. Then
# copy the artifacts into a final image which exposes the port and
# starts the pipeline.

# Build the pipeline first
FROM maven:3.5-jdk-8 as builder
ADD . /usr/src/daisy-pipeline2
WORKDIR /usr/src/daisy-pipeline2
RUN curl -fsSL https://raw.github.com/daisy/maven-parents/travis/settings.xml > $HOME/.m2/settings.xml
RUN mvn clean package
RUN cd target && unzip assembly-*-linux.zip

# then use the build artifacts to create an image where the pipeline is installed
FROM openjdk:9-jre
LABEL maintainer="DAISY Consortium (http://www.daisy.org/)"
COPY --from=builder /usr/src/daisy-pipeline2/target/daisy-pipeline /opt/daisy-pipeline2
ENV PIPELINE2_WS_LOCALFS=false \
    PIPELINE2_WS_AUTHENTICATION=true \
    PIPELINE2_WS_AUTHENTICATION_KEY=clientid \
    PIPELINE2_WS_AUTHENTICATION_SECRET=sekret
EXPOSE 8181
# for the healthcheck use PIPELINE2_HOST if defined. Otherwise use localhost
HEALTHCHECK --interval=30s --timeout=10s --start-period=1m CMD curl --fail http://${PIPELINE2_WS_HOST-localhost}:${PIPELINE2_WS_PORT:-8181}/${PIPELINE2_WS_PATH:-ws}/alive || exit 1
ENTRYPOINT ["/opt/daisy-pipeline2/bin/pipeline2"]
