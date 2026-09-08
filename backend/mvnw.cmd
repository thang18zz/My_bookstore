@echo off
set "MAVEN_BIN=C:\Users\Admin\.m2\wrapper\dists\apache-maven-3.8.4-bin\52ccbt68d252mdldqsfsn03jlf\apache-maven-3.8.4\bin\mvn.cmd"
if exist "%MAVEN_BIN%" (
    "%MAVEN_BIN%" %*
) else (
    mvn %*
)
