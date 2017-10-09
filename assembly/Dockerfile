# Use a multistage build to first build the pipeline using maven. Then
# copy the artifacts into a final image which exposes the port and
# starts the pipeline.

# Build the pipeline first
FROM maven:3.5-jdk-8 as builder
ADD . /usr/src/daisy-pipeline2
WORKDIR /usr/src/daisy-pipeline2
RUN mvn clean package

# then use the build artifacts to create an image where the pipeline is installed
FROM openjdk:8-jre
LABEL maintainer="DAISY Consortium (http://www.daisy.org/)"
COPY --from=builder /usr/src/daisy-pipeline2/target/pipeline2-*_linux/daisy-pipeline /opt/daisy-pipeline2
ENV PIPELINE2_LOCAL=false \
    PIPELINE2_AUTH=true \
    PIPELINE2_AUTH_CLIENTKEY=clientid \
    PIPELINE2_AUTH_CLIENTSECRET=sekret
EXPOSE 8181
ENTRYPOINT ["/opt/daisy-pipeline2/bin/pipeline2"]
