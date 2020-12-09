#!/bin/bash
set -e
set -o pipefail

WORKING_DIR=$(pwd)
TMP_DIR=$( mktemp -t "$(basename "$0").XXXXXX" )
rm $TMP_DIR
REMOTE="git@github.com:daisy/pipeline-assembly.git"

if [ $# != 0 ]; then
    TAG=$1
    if ! git rev-parse $TAG >/dev/null 2>&1; then
        echo "Tag $TAG does not exist" >&2
        exit 1
    fi
    if ! test -z "$(git status . --porcelain)"; then
        echo "You have uncommitted changed" >&2
        exit 1
    fi
    echo "Checking out $TAG"
    CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD)
    git checkout $TAG
    trap "cd $WORKING_DIR && git checkout $CURRENT_BRANCH" EXIT
fi

echo "Generating release descriptor"
make release-descriptor

descriptor=target/release-descriptor/releaseDescriptor.xml
version=$(xmllint --pretty 2 $descriptor | grep version | sed -n '2p' | sed  's/ //g'| sed -n 's/version="\(.*\)"/\1/p')
branch=rd-$version

echo "Checking out gh-pages"
git clone --branch gh-pages --depth 1 $REMOTE $TMP_DIR
cd $TMP_DIR
if [ "x$(echo $version | grep SNAPSHOT)" == "x"  ]; then
        echo "Creating release descriptor"
        cp $WORKING_DIR/$descriptor ./releases/current
else
        echo "Creating snapshot descriptor"
        cp $WORKING_DIR/$descriptor ./releases/snapshot
fi
cp $WORKING_DIR/$descriptor releases/$version
echo "Commiting changes"
git add .
git commit -m "Add release descriptors for $version"
git push $REMOTE HEAD:refs/heads/$branch
echo "Please, remember to merge $branch into gh-pages"
cd $WORKING_DIR
rm -rf $TMP_DIR
