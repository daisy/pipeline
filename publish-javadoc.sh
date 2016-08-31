#!/bin/bash
#version: 2016-08-31

repo=dotify.api
version=`grep 'version='  gradle.properties | grep -E -o '[0-9\.]+.+$'`
if [[ $version == *SNAPSHOT ]]; then
	is_release=false
else
	is_release=true
fi
if [ `git diff --name-only HEAD HEAD~ | grep gradle.properties -c` = 1 ]; then	
	prop_change="true"
else 
	prop_change="false"
fi

echo "Version: $version"
echo "Is release: $is_release"
echo "Properties changed: $prop_change"

if [ "$prop_change" = "false" ]; then
	if [ "$is_release" = "true" ]; then
		echo "No version change since last commit. Is this really a release?"
		exit 1
	fi
fi

if [ "$TRAVIS_REPO_SLUG" == "brailleapps/$repo" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ] && [ "$TRAVIS_BRANCH" == "master" ]; then

  echo -e "Publishing javadoc...\n"

  if [ "$is_release" = true ]; then
  	mkdir -p $HOME/$repo/$version
	cp -R build/docs/javadoc $HOME/$repo/$version
  fi
  mkdir -p $HOME/$repo/latest
  cp -R build/docs/javadoc $HOME/$repo/latest

  cd $HOME
  git config --global user.email "travis@travis-ci.org"
  git config --global user.name "travis-ci"
  git clone --quiet https://${GH_TOKEN}@github.com/brailleapps/brailleapps.github.io  > /dev/null

  cd brailleapps.github.io
  if [ "$is_release" = true ]; then
  	git rm -rf ./$repo/$version
  fi
  git rm -rf ./$repo/latest
  
  cp -Rf $HOME/$repo/. ./$repo
  
  git add -f .
  git commit -m "Lastest successful travis build of $repo ($TRAVIS_BUILD_NUMBER) auto-pushed to brailleapps.github.io"
  git push -fq origin master > /dev/null

  echo -e "Published javadocs to brailleapps.github.io.\n"
  
fi
