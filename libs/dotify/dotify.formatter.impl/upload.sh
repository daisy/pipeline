#!/bin/bash
#version: 2019-03-13 (use maven publish)

branch=$TRAVIS_BRANCH
if [ -z "$branch" ]; then
	branch=`git rev-parse --abbrev-ref HEAD`
fi
revision=`git rev-parse HEAD`
pullrequest=$TRAVIS_PULL_REQUEST
if [ -z "$pullrequest" ]; then
	pullrequest="false"
fi

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
echo "Branch: $branch"
echo "Revision: $revision"
echo "Pull request: $pullrequest"
echo "Is release: $is_release"
echo "Properties changed: $prop_change"

if [ "$prop_change" = "false" ]; then
	if [ "$is_release" = "true" ]; then
		echo "No version change since last commit. Is this really a release?"
		exit 1
	fi
fi

if [ "$pullrequest" = "false" ]; then
	if [ $branch = "master" -o "$is_release" = "false" ]; then
		echo "On master branch or snapshot version."
		if [ -n "$SONATYPE_USER" ]; then
			if [ -n "$SONATYPE_PASSWORD" ]; then
				echo "Starting upload..."
				./gradlew publishMavenPublicationToMavenRepository -PsonatypeUsername=$SONATYPE_USER -PsonatypePassword=$SONATYPE_PASSWORD -PrepositoryRevision=$revision -Psigning.keyId=$SIGNING_KEY -Psigning.password=$SIGNING_PASSWORD -Psigning.secretKeyRingFile=secring.gpg
			else
				echo "SONATYPE_PASSWORD not set. Skipping upload."
			fi
		else
			echo "SONATYPE_USER not set. Skipping upload."
		fi
	else
		echo "Not on master branch. Skipping upload."
	fi
else
	echo "Pull request. Skipping upload."
fi