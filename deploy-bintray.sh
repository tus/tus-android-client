#!/bin/bash

set -e

base_url="https://acconut:$BINTRAY_TOKEN@api.bintray.com/"
version="$TRAVIS_TAG"

# Generate files
./gradlew createPom javadocJar sourcesJar -PpomVersion=$version

# Create new version
curl \
    -X POST \
    $base_url/packages/tus/maven/tus-android-client/versions \
    -H "Content-Type: application/json" \
    -d "{\"name\":\"${version}\",\"vcs_tag\":\"${version}\"}"

function upload {
    local src=$1
    local dst=$2

    curl \
        -X PUT \
        "$base_url/content/tus/maven/tus-android-client/$version/io/tus/android/client/tus-android-client/$version/$dst?publish=1" \
        -T ./tus-android-client/build/$src
}

# Upload files
upload "libs/tus-android-client-javadoc.jar" "tus-android-client-$version-javadoc.jar"
upload "libs/tus-android-client-sources.jar" "tus-android-client-$version-sources.jar"
upload "outputs/aar/tus-android-client-release.aar" "tus-android-client-$version.aar"
upload "libs/pom.xml" "tus-android-client-$version.pom"
