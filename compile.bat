@echo off
echo Compiling Music Mixer App...
if not exist bin mkdir bin
javac -d bin src\mixer\*.java
if %ERRORLEVEL% == 0 (
    echo Compilation Successful.
) else (
    echo Compilation Failed.
)
pause
