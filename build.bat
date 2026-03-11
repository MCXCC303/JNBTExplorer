@echo off
chcp 65001 >nul
echo Building JNBTExplorer JAR file...

REM Create bin directory if not exists
if not exist bin mkdir bin

REM Compile all Java files
echo Compiling Java files...
javac -encoding UTF-8 -d bin -sourcepath src src/com/MCXCC/JNBTExplorer/JNBTExplorerMain.java
if errorlevel 1 (
    echo Compilation failed!
    pause
    exit /b 1
)

REM Create JAR file
echo Creating JAR file...
jar cvfm JNBTExplorer.jar MANIFEST.MF -C bin .
if errorlevel 1 (
    echo JAR creation failed!
    pause
    exit /b 1
)

echo.
echo Build successful! Created JNBTExplorer.jar
echo You can run it with: java -jar JNBTExplorer.jar
pause
