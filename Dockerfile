FROM maven:3.6-jdk-11 as builder

ADD . /opt/pipeline
WORKDIR /opt/pipeline

RUN apt-get update && \
    apt-get install -y git build-essential make libxml2-utils

RUN make dist-zip-linux

RUN git clone -b guidelines-revision https://github.com/nlbdev/nordic-epub3-dtbook-migrator.git /opt/nordic-epub3-dtbook-migrator
WORKDIR /opt/nordic-epub3-dtbook-migrator

RUN mvn --settings /opt/pipeline/settings.xml clean package -Dmaven.test.skip=true

RUN rm /opt/nordic-epub3-dtbook-migrator/target/nordic-epub3-dtbook-migrator-*-doc.jar
RUN rm /opt/nordic-epub3-dtbook-migrator/target/nordic-epub3-dtbook-migrator-*-xprocdoc.jar

# then use the build artifacts to create an image where the pipeline is installed
FROM debian:stretch
LABEL maintainer="MTM (https://mtm.se)"
RUN apt-get update && \
    apt-get install -y curl wget unzip && \
    rm -rf /var/lib/apt/lists/*

RUN wget "https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11%2B28/OpenJDK11-jdk_x64_linux_hotspot_11_28.tar.gz" -O /tmp/openjdk.tar.gz --no-verbose \
    && tar -zxvf /tmp/openjdk.tar.gz -C /opt \
    && rm /tmp/openjdk.tar.gz
ENV JAVA_HOME=/opt/jdk-11+28

COPY --from=builder /opt/pipeline/*.zip /opt/
WORKDIR /opt
RUN unzip *.zip
RUN mv daisy-pipeline daisy-pipeline2

COPY --from=builder /opt/nordic-epub3-dtbook-migrator/target/nordic-epub3-dtbook-migrator-*.jar /opt/daisy-pipeline2/system/felix/
ENV PIPELINE2_WS_LOCALFS=false \
    PIPELINE2_WS_AUTHENTICATION=false \
    PIPELINE2_WS_AUTHENTICATION_KEY=clientid \
    PIPELINE2_WS_AUTHENTICATION_SECRET=sekret
EXPOSE 8181

# for the healthcheck use PIPELINE2_HOST if defined. Otherwise use localhost
HEALTHCHECK --interval=30s --timeout=10s --start-period=1m CMD http_proxy="" https_proxy="" HTTP_PROXY="" HTTPS_PROXY="" curl --fail http://${PIPELINE2_WS_HOST-localhost}:${PIPELINE2_WS_PORT:-8181}/${PIPELINE2_WS_PATH:-ws}/alive || exit 1

COPY --from=builder /opt/nordic-epub3-dtbook-migrator/docker-entrypoint.sh /opt/daisy-pipeline2/docker-entrypoint.sh
ENTRYPOINT ["/opt/daisy-pipeline2/docker-entrypoint.sh"]