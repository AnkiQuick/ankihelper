#!/bin/bash
# Build script for AnkiHelper (Kotlin version)
# Updated for package com.lmyby.ankihelper v0.1.0

eval "$(jenv init -)"

# Build the project (Kotlin compilation is handled by Gradle automatically)
echo "Building AnkiHelper v0.1.0..."
./gradlew clean build -x lint

if [[ $? -eq 0 ]]
then
    echo "Build successful! Signing APK..."
    ~/development/android-sdk/build-tools/34.0.0/apksigner sign \
        --ks ankihelper-jennings.keystore \
        --out ankihelper-0.1.0-release-signed.apk \
        app/build/outputs/apk/release/ankihelper-0.1.0-release-unsigned.apk

    if [[ $? -eq 0 ]]
    then
        echo "✓ APK signed successfully: ankihelper-0.1.0-release-signed.apk"
    else
        echo "✗ APK signing failed"
        exit 1
    fi
else
    echo "✗ Build failed"
    exit 1
fi
