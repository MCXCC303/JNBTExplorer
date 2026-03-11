#!/bin/bash
if [ "$1" = "clear" ]; then
    echo "Cleaning build artifacts..."
    rm -rfv bin
    rm -rfv out
    rm -fv JNBTExplorer.jar
    rm -fv JNBTExplorer+git-*.jar
    echo "Clean completed!"
    exit 0
fi

COMMIT_HASH=$(git rev-parse --short HEAD)
JAR_FILE=JNBTExplorer+git-$COMMIT_HASH.jar
RELEASE_JAR_FILE=JNBTExplorer.jar

echo "Building JNBTExplorer JAR file..."

mkdir -p bin

echo "Compiling Java files..."
javac -encoding UTF-8 -d bin -sourcepath src src/com/MCXCC/JNBTExplorer/JNBTExplorerMain.java
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

echo "Creating JAR file..."
jar cvfm $JAR_FILE MANIFEST.MF -C bin .
if [ $? -ne 0 ]; then
    echo "JAR creation failed!"
    exit 1
fi

cp $JAR_FILE $RELEASE_JAR_FILE

echo "jar_file=$JAR_FILE" >> $GITHUB_OUTPUT
echo "commit_hash=$COMMIT_HASH" >> $GITHUB_OUTPUT
echo "release_jar_file=$RELEASE_JAR_FILE" >> $GITHUB_OUTPUT

echo ""
echo "Build successful! Created $RELEASE_JAR_FILE"
echo "You can run it with: java -jar $RELEASE_JAR_FILE"
echo ""
