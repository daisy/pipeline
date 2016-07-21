#!/bin/bash

CURDIR=$( cd $( dirname "$0" ) && pwd )
SAXON=${HOME}/.m2/repository/net/sf/saxon/Saxon-HE/9.5.1-5/Saxon-HE-9.5.1-5.jar

for f in $CURDIR/src/test/xprocspec/test_dotify.formatter.impl.xprocspec; do
    java -jar "$SAXON" \
         -s:$f \
         -o:$CURDIR/target/dotify-tests/dummy \
         -xsl:$CURDIR/generate-dotify-tests.xsl \
        || exit
done
echo 'Now run `rsync -avm target/dotify-tests/ ~/path/to/dotify` to move the generated tests to Dotify'
