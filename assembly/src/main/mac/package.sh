#!/bin/sh
#TODO set version
# -BappVersion version\
# -Bmac.CFBundleVersion filtered-from-version
#TODO see how the -srcfiles options can be generated
DIRNAME=$(dirname "$0")
RES_DIR=$(cd $DIRNAME; pwd)
BUILD_DIR=$(cd $DIRNAME/../../../target; pwd)
cd $BUILD_DIR/pipeline2-*_mac
$JAVA_HOME/bin/javapackager\
 -deploy\
 -BclassPath=system/bootstrap/felix.jar\
 -Bidentifier=org.daisy.pipeline2\
 -BjvmOptions=-Xms256M\
 -BjvmOptions=-Xmx1G\
 -BjvmOptions=-XX:PermSize=16M\
 -BjvmOptions=-XX:MaxPermSize=256M\
 -BjvmProperties=org.daisy.pipeline.main.mode="gui"\
 -BjvmProperties=org.daisy.pipeline.home="."\
 -BjvmProperties=org.daisy.pipeline.base="."\
 -BjvmProperties=org.daisy.pipeline.data="data"\
 -BjvmProperties=felix.config.properties="file:etc/config.properties"\
 -BjvmProperties=felix.system.properties="file:etc/system.properties"\
 -BjvmProperties=org.daisy.pipeline.ws.localfs="true"\
 -BjvmProperties=org.daisy.pipeline.ws.authentication="false"\
 -BmainJar=system/bootstrap/felix.jar\
 -Bmac.CFBundleIdentifier=org.daisy.pipeline2\
 -BlicensFile=NOTICE.txt\
 -Bicon=$RES_DIR/pipeline-logo.icns\
 -srcdir daisy-pipeline\
 -srcfiles bin\
 -srcfiles cli\
 -srcfiles etc\
 -srcfiles modules\
 -srcfiles samples\
 -srcfiles system\
 -srcfiles updater\
 -srcfiles NOTICE.txt\
 -srcfiles README.txt\
 -outdir $BUILD_DIR/javapackager\
 -outfile dp2\
 -appclass org.apache.felix.main.Main\
 -Bruntime=`/usr/libexec/java_home -v 1.8`\
 -name "DAISY Pipeline 2"\
 -native image\
 -native dmg\
 -vendor "DAISY Consortium"\
 -nosign
