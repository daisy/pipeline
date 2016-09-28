#!/bin/bash

if [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
  make cover-deploy
  make dist
  mvn deploy -DskipTests -Dinvoker.skip=true --settings .build/settings.xml
else
  echo "Skipping deploy tasks."
fi
