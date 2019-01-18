checkJavaVersion() {
    local VERSION
    VERSION=$( sed 's/[^ ]* version "\([^\.-]*\).*".*/\1/; 1q' )
    if [ "$VERSION" -lt "11" ]; then
        echo "Java version must be at least 11"
        return 1
    fi
}

checkJavaVersion <<EOF
java version "1.8.0_65"
Java(TM) SE Runtime Environment (build 1.8.0_65-b17)
Java HotSpot(TM) 64-Bit Server VM (build 25.65-b01, mixed mode)
EOF

[ $? == 1 ] || echo "[FAIL] line $LINENO"

checkJavaVersion <<EOF
java version "9.0.4"
Java(TM) SE Runtime Environment (build 9.0.4+11)
Java HotSpot(TM) 64-Bit Server VM (build 9.0.4+11, mixed mode)
EOF

[ $? == 1 ] || echo "[FAIL] line $LINENO"

checkJavaVersion <<EOF
openjdk version "9-Debian"
OpenJDK Runtime Environment (build 9-Debian+0-9b181-4bpo91)
OpenJDK 64-Bit Server VM (build 9-Debian+0-9b181-4bpo91, mixed mode)
EOF

[ $? == 1 ] || echo "[FAIL] line $LINENO"

checkJavaVersion <<EOF
java version "11.0.1" 2018-10-16 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.1+13-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.1+13-LTS, mixed mode)
EOF

[ $? == 0 ] || echo "[FAIL] line $LINENO"
