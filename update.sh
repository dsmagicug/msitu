#!/bin/bash

# Define the base paths
SOURCE_DIR="/Users/ekeeya/Documents/work/msitu/node_modules/rtn-msitu/android"
DEST_DIR="/Users/ekeeya/Documents/work/msitu/RTNMsitu/android"

# Copy specific files and directories
cp -rf "$SOURCE_DIR/build.gradle" "$SOURCE_DIR/src" "$DEST_DIR"

echo "Files copied from $SOURCE_DIR to $DEST_DIR"

echo "Adding custom module RTNMsitu..."

yarn add ./RTNMsitu

echo "Done adding custom module RTNMsitu."

# echo "Codegen artifacts"

cd android

./gradlew generateCodegenArtifactsFromSchema

