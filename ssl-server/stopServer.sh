#!/bin/sh
SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
cd $SCRIPTPATH
kill $(pgrep -a java | grep my_server_app | cut -d" " -f1)

