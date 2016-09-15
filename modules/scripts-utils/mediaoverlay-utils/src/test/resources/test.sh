#!/bin/bash
SCRIPTPATH=`dirname $0 | xargs readlink -f`
$UTFX_HOME/utfx.sh -Dutfx.test.dir=$SCRIPTPATH/
