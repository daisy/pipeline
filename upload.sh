#!/bin/bash


branch=$TRAVIS_BRANCH
if [ -z "$branch" ]; then
	branch=`git rev-parse --abbrev-ref HEAD`
fi
revision=`git rev-parse HEAD`
pullrequest=$TRAVIS_PULL_REQUEST
if [ -z "$pullrequest" ]; then
	pullrequest="false"
fi
echo "Branch: $branch"
echo "Revision: $revision"
echo "Pull request: $pullrequest"

if [ "$pullrequest" = "false" ]; then
	if [ $branch = "master" ]; then
		echo "On master branch."
		if [ -n "$SONATYPE_USER" ]; then
			if [ -n "$SONATYPE_PASSWORD" ]; then
				echo "Starting upload..."
				./gradlew uploadArchives -PsonatypeUsername=$SONATYPE_USER -PsonatypePassword=$SONATYPE_PASSWORD -PrepositoryRevision=$revision
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



