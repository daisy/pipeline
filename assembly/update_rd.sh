#!/bin/bash
set -e
set -o pipefail

WORKING_DIR=$(pwd)
TMP_DIR=$( mktemp -t "$(basename "$0").XXXXXX" )
rm $TMP_DIR
REMOTE="git@github.com:daisy/pipeline-assembly.git"

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
