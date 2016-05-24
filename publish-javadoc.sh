#!/bin/bash

repo=braille-utils.pef-tools
if [ "$TRAVIS_REPO_SLUG" == "brailleapps/$repo" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then

  echo -e "Publishing javadoc...\n"

  cp -R build/docs/javadoc $HOME/$repo

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet https://${GH_TOKEN}@github.com/brailleapps/brailleapps.github.io  > /dev/null

  cd brailleapps.github.io
  git rm -rf ./javadoc/$repo
  
  cp -Rf $HOME/$repo ./javadoc/$repo
  
  git add -f .
  git commit -m "Lastest successful travis build of $repo ($TRAVIS_BUILD_NUMBER) auto-pushed to brailleapps.github.io"
  git push -fq origin master > /dev/null

  echo -e "Published javadocs to brailleapps.github.io.\n"
  
fi
