#!/usr/bin/env bash
set -e
set -x
test -z "$(git status . --porcelain)"
GIT_HASH=$( git rev-parse HEAD )
WORKING_DIR=$(pwd)
SITE_DIR=$1
if [ "x$GH_REMOTE" = "x" ]; then
	GH_REMOTE="git@github.com:daisy/pipeline.git"
fi
GH_BRANCH=gh-pages
TMP_DIR=$( mktemp -t "$(basename "$0").XXXXXX" )
rm $TMP_DIR
git clone --branch $GH_BRANCH --depth 1 $GH_REMOTE $TMP_DIR
cd $TMP_DIR
git rm -r *
cp -r $WORKING_DIR/$SITE_DIR/* .
git add .
if [ "x$GH_USER_NAME" != "x" ]; then
	git config user.name "$GH_USER_NAME"
fi
if [ "x$GH_USER_EMAIL" != "x" ]; then
	git config user.email "$GH_USER_EMAIL"
fi
git commit -m "publish site [ commit ${GIT_HASH} ]"
git push $GH_REMOTE $GH_BRANCH:$GH_BRANCH
cd $WORKING_DIR
rm -rf $TMP_DIR
