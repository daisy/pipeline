#!/usr/bin/env bash
export SAXON_CP=saxon/saxon9he.jar
FAIL=0
for testfile in xspec/*.xspec; do
    xspec-master/bin/xspec.sh $testfile
    grep 'class="failed"' xspec/xspec/*-result.html >/dev/null 2>&1
    test ! $? -eq 0
    FAIL=$(expr $FAIL + $?)
done
exit $FAIL
