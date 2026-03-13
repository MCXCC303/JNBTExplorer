#!/bin/bash

clear() {
    echo "Cleaning build artifacts..."
    rm -rfv bin
    rm -rfv out
    rm -fv *.jar
    echo "Clean completed!"
}

build() {
    COMMIT_HASH=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
    JAR_FILE=JNBTExplorer+git-$COMMIT_HASH.jar
    RELEASE_JAR_FILE=JNBTExplorer.jar

    echo "Building JNBTExplorer JAR file..."

    mkdir -p bin

    echo "Compiling Java files..."
    javac -verbose -encoding UTF-8 -d bin -sourcepath src src/com/MCXCC/JNBTExplorer/JNBTExplorerMain.java
    if [ $? -ne 0 ]; then
        echo "Compilation failed!"
        exit 1
    fi

    echo "Copying resources..."
    if [ -d "src/com/MCXCC/JNBTExplorer/resources" ]; then
        cp -r src/com/MCXCC/JNBTExplorer/resources bin/com/MCXCC/JNBTExplorer/
        if [ $? -ne 0 ]; then
            echo "Resource copy failed!"
            exit 1
        fi
    fi

    echo "Creating JAR file..."
    jar cvfm $JAR_FILE MANIFEST.MF -C bin .
    if [ $? -ne 0 ]; then
        echo "JAR creation failed!"
        exit 1
    fi

    cp $JAR_FILE $RELEASE_JAR_FILE

    if [ -n "$GITHUB_OUTPUT" ]; then
        echo "jar_file=$JAR_FILE" >> $GITHUB_OUTPUT
        echo "commit_hash=$COMMIT_HASH" >> $GITHUB_OUTPUT
        echo "release_jar_file=$RELEASE_JAR_FILE" >> $GITHUB_OUTPUT
    fi

    echo ""
    echo "Build successful! Created $RELEASE_JAR_FILE"
    echo "You can run it with: java -jar $RELEASE_JAR_FILE"
    echo ""
}

all() {
    clear
    build
}

help() {
    echo "Usage: bash build.sh <target>"
    echo ""
    echo "Targets:"
    echo "  all   - Clean and build the project (default)"
    echo "  clear - Clean build artifacts only"
    echo "  help  - Show this help message"
}

TARGET="$1"

case "$TARGET" in
    clear)
        clear
        ;;
    all)
        all
        ;;
    help)
        help
        ;;
    "")
        all
        ;;
    *)
        echo "Error: Unknown target '$TARGET'"
        echo "Valid targets: all, clear, help"
        exit 1
        ;;
esac
