#!/bin/bash
#version: 2018-09-26

repo=dotify.formatter.impl

if [ "$TRAVIS_REPO_SLUG" == "brailleapps/$repo" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then

  echo -e "Publishing tests...\n"

  mkdir -p $HOME/$repo/latest
  cp -R build/docs/tests $HOME/$repo/tests

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet https://${GH_TOKEN}@github.com/brailleapps/brailleapps.github.io  > /dev/null

  cd brailleapps.github.io
  git rm -rf ./$repo/tests
  
  cp -Rf $HOME/$repo/. ./$repo
  
  git add -f .
  git commit -m "Lastest successful travis build of $repo ($TRAVIS_BUILD_NUMBER) auto-pushed to brailleapps.github.io"
  git push -fq origin master > /dev/null

  echo -e "Published tests to brailleapps.github.io.\n"
  
fi
