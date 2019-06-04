rm -rf bin/*
javac -d bin src/com/letex/xslt/utils.java
jar cvfe bin/letex-utils.jar com.letex.xslt.utils -C bin .