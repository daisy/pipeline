FROM ubuntu:16.04

LABEL maintainer="jostein@nlb.no"

WORKDIR /root/

# Install dependencies
RUN sed -i.bak 's/main$/main universe/' /etc/apt/sources.list
RUN apt-get update && apt-get install -y locales && apt-get clean
RUN locale-gen en_US en_US.UTF-8

# Install Java
RUN apt-get update && apt-get install -y openjdk-8-jdk && apt-get clean

# Install other dependencies
RUN apt-get update && apt-get install -y software-properties-common build-essential wget zip unzip git maven gradle golang libxml2-utils pcregrep && apt-get clean
RUN apt-get update && apt-get install -y ruby ruby-dev zlib1g-dev && apt-get clean
RUN gem install jekyll sparql rdf-turtle rdf-rdfa commaparty nokogiri mustache github-markup coderay

# Initialize git repository and store hash of current git commit as an environment variable
RUN mkdir -p /tmp/pipeline/.git/objects
ADD .git/refs /tmp/pipeline/.git/refs
ADD .git/HEAD /tmp/pipeline/.git/HEAD
ADD .git/config /tmp/pipeline/.git/config
RUN git --git-dir=/tmp/pipeline/.git rev-parse HEAD | sed 's/^/export SOURCE_COMMIT=/' | tee -a .bashrc >> .profile
RUN mkdir -p pipeline && cd pipeline && git init
RUN cp /tmp/pipeline/.git/config pipeline/.git/
RUN cd pipeline && git remote | grep '/' | while read remote; do git remote remove $remote ; done
RUN rm /tmp/pipeline -rf

# Fetch, build and install Pipeline 2
# (and run build commands with bash)
RUN echo '#!/bin/bash -e -x\n\
    cd ~/pipeline \n\
    git fetch --all \n\
    . /root/.profile \n\
    git checkout $SOURCE_COMMIT \n\
    make dist-deb \n\
    tail -f maven.log & \n\
    make dist-webui-deb \n\
    DEBIAN_FRONTEND=noninteractive dpkg -i *.deb \n\
    apt-get update && apt-get -f install && apt-get clean \n\
    service daisy-pipeline2 stop \n\
    service daisy-pipeline2-webui stop \n\
    cd ~ \n\
    rm pipeline -rf\n'\
    >> /tmp/run.sh && \
    sh /tmp/run.sh && \
    rm /tmp/run.sh
# in case the webui service didn't shut down properly:
RUN rm /run/daisy-pipeline2-webui/play.pid

EXPOSE 8181
EXPOSE 9000

# By default, start the Pipeline 2 Engine and Web UI when the container is started
CMD service daisy-pipeline2 start && \
    service daisy-pipeline2-webui start && \
    bash
