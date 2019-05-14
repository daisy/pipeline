# copied from src/main/resources/bin/pipeline2
checkJavaVersion() {
    local in=$( cat )
    JAVA_VERSION=$( echo "$in" | sed 's/[^ ]* version "\([^\.-]*\).*".*/\1/; 1q' )
    if [ "$JAVA_VERSION" == "1" ]; then
        JAVA_VERSION=$( echo "$in" | sed 's/[^ ]* version "1\.\([^\._-]*\).*".*/\1/; 1q' )
    fi
    if [ "$JAVA_VERSION" -lt "8" ]; then
        echo "Java version must be at least 8" >&2
        return 1
    fi
}

checkJavaVersion <<EOF 2>/dev/null
java version "1.8.0_65"
Java(TM) SE Runtime Environment (build 1.8.0_65-b17)
Java HotSpot(TM) 64-Bit Server VM (build 25.65-b01, mixed mode)
EOF

[ $? == 0 ]                || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "8" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

checkJavaVersion <<EOF 2>/dev/null
java version "9.0.4"
Java(TM) SE Runtime Environment (build 9.0.4+11)
Java HotSpot(TM) 64-Bit Server VM (build 9.0.4+11, mixed mode)
EOF

[ $? == 0 ]                || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "9" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

checkJavaVersion <<EOF 2>/dev/null
openjdk version "9-Debian"
OpenJDK Runtime Environment (build 9-Debian+0-9b181-4bpo91)
OpenJDK 64-Bit Server VM (build 9-Debian+0-9b181-4bpo91, mixed mode)
EOF

[ $? == 0 ]                || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "9" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

checkJavaVersion <<EOF 2>/dev/null
java version "11.0.1" 2018-10-16 LTS
Java(TM) SE Runtime Environment 18.9 (build 11.0.1+13-LTS)
Java HotSpot(TM) 64-Bit Server VM 18.9 (build 11.0.1+13-LTS, mixed mode)
EOF

[ $? == 0 ]                 || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "11" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

checkJavaVersion <<EOF 2>/dev/null
java version "1.7_u12"
Java(TM) SE Runtime Environment (build 1.7.0_67-b01)
Java HotSpot(TM) Client VM (build 24.65-b04, mixed mode, sharing)
EOF

[ $? == 1 ]                || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "7" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

checkJavaVersion <<EOF 2>/dev/null
java version "1.6.12"
Java(TM) SE Runtime Environment (build 1.7.0_67-b01)
Java HotSpot(TM) Client VM (build 24.65-b04, mixed mode, sharing)
EOF

[ $? == 1 ]                || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "6" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

checkJavaVersion <<EOF 2>/dev/null
java version "1.7_12"
OpenJDK Runtime Environment (IcedTea 2.5.2) (7u65-2.5.2-3~14.04)
OpenJDK 64-Bit Server VM (build 24.65-b04, mixed mode)
EOF

[ $? == 1 ]                || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "7" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

checkJavaVersion <<EOF 2>/dev/null
java version "1.8"
OpenJDK Runtime Environment (IcedTea 2.5.2) (7u65-2.5.2-3~14.04)
OpenJDK 64-Bit Server VM (build 24.65-b04, mixed mode)
EOF

[ $? == 0 ]                || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "8" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

checkJavaVersion <<EOF 2>/dev/null
openjdk version "1.8.0_45-internal"
OpenJDK Runtime Environment (build 1.8.0_45-internal-b14)
OpenJDK 64-Bit Server VM (build 25.45-b02, mixed mode)
EOF

[ $? == 0 ]                || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "8" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

checkJavaVersion <<EOF 2>/dev/null
openjdk version "10.0.2" 2018-07-17
OpenJDK Runtime Environment (build 1.8.0_45-internal-b14)
OpenJDK 64-Bit Server VM (build 25.45-b02, mixed mode)
EOF

[ $? == 0 ]                 || echo "[FAIL] line $LINENO"
[ "$JAVA_VERSION" == "10" ] || echo "[FAIL] line $LINENO: was $JAVA_VERSION"

