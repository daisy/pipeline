FROM debian:bookworm
LABEL maintainer="DAISY Consortium (http://www.daisy.org/)"
ADD daisy-pipeline /opt/daisy-pipeline2
ADD logback.xml /opt/daisy-pipeline2/etc/logback.xml
ARG TARGETARCH
ADD jre/$TARGETARCH /opt/daisy-pipeline2/jre
ENV JAVA_HOME=/opt/daisy-pipeline2/jre
ENV PIPELINE2_WS_LOCALFS=false \
    PIPELINE2_WS_AUTHENTICATION=true \
    PIPELINE2_WS_AUTHENTICATION_KEY=clientid \
    PIPELINE2_WS_AUTHENTICATION_SECRET=sekret
EXPOSE 8181
ENTRYPOINT ["/opt/daisy-pipeline2/bin/pipeline2"]
