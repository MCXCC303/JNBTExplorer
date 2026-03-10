#!/bin/bash

echo "Building JNBTExplorer JAR file..."

# Create bin directory if not exists
mkdir -p bin

# Compile all Java files
echo "Compiling Java files..."
javac -encoding UTF-8 -d bin -sourcepath src src/com/example/main/Main.java
if [ $? -ne 0 ]; then
    echo "Compilation failed!"
    exit 1
fi

# Create JAR file
echo "Creating JAR file..."
jar cvfm JNBTExplorer.jar MANIFEST.MF -C bin .
if [ $? -ne 0 ]; then
    echo "JAR creation failed!"
    exit 1
fi

echo ""
echo "Build successful! Created JNBTExplorer.jar"
echo "You can run it with: java -jar JNBTExplorer.jar"
