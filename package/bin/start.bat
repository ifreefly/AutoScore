rem @echo off
IF "%JAVA_HOME%" == "" (
    echo Enter path to JAVA_HOME:
    set /p JAVA_HOME=
) ELSE (
    echo %JAVA_HOME%
)

set workPathWithSlash=%~dp0
set workPath=%workPathWithSlash:~0,-1%

set java="%JAVA_HOME%\bin\java"
set exejar=bin\autoScore-0.7.jar
set classpath=./bin/libs/*;%JAVA_HOME%/lib/*

%java% -DworkPath="%workPath%" -Dlog4j.configurationFile=".\conf\log4j2.xml" -cp "%exejar%;%classpath%" idevcod.score.AutoScore
