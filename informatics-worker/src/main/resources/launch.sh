#!/bin/sh

## Create users
#adduser --disabled-password --gecos '' contestant
#adduser --disabled-password --gecos '' checker
#
## Create directories
#mkdir -p /sandbox/submission /sandbox/checker
#
## Set permissions
#chown contestant:contestant /sandbox/submission
#chmod 700 /sandbox/submission
#chown checker:checker /sandbox/checker
#chmod 770 /sandbox/checker
#usermod -aG checker contestant
#
#apt-get update && apt-get install -y time
#chmod 755 /usr/bin/time
#
## Keep the container running for further commands
tail -f /dev/null