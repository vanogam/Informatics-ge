FROM debian:bookworm-slim

ENV DEBIAN_FRONTEND=noninteractive

COPY /src/main/resources/launch.sh /launch/launch.sh

RUN adduser --disabled-password --gecos '' contestant && \
    adduser --disabled-password --gecos '' checker && \
    mkdir -p /sandbox/tasks /sandbox/tasks && \
    chmod 700 /sandbox/tasks && \
    mkdir -p /sandbox/checkers /sandbox/checkers && \
    chmod 700 /sandbox/checkers && \
    mkdir -p /sandbox/submission /sandbox/checker && \
    chown contestant:contestant /sandbox/submission && \
    chmod 750 /sandbox/submission && \
    chown checker:checker /sandbox/checker && \
    chmod 770 /sandbox/checker && \
    usermod -aG contestant checker && \
    apt-get update && \
    apt-get install -y time && \
    apt-get install -y g++ make && \
    chown contestant:contestant /usr/bin/time && \
    chmod 555 /usr/bin/time
