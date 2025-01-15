#!/bin/bash

./gradlew publish

GRADLE_PROPS="${HOME}/.gradle/gradle.properties"

# Check if file exists
if [ ! -f "$GRADLE_PROPS" ]; then
    echo "Error: gradle.properties not found at $GRADLE_PROPS"
    exit 1
fi
SONATYPE_USER=$(grep "^sonatypeUsername=" "$GRADLE_PROPS" | cut -d'=' -f2-)
SONATYPE_PASS=$(grep "^sonatypePassword=" "$GRADLE_PROPS" | cut -d'=' -f2-)

# Check if credentials were found
if [ -z "$SONATYPE_USER" ] || [ -z "$SONATYPE_PASS" ]; then
    echo "Error: Could not find credentials in gradle.properties"
    echo "Looking for: sonatypeUsername and sonatypePassword"
    exit 1
fi

# Example curl call to Sonatype (adjust URL and parameters as needed)
curl -i -X POST https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/org.daisy.dotify \
     -H "Authorization: Bearer $(echo "${SONATYPE_USER}:${SONATYPE_PASS}" | openssl base64)"
