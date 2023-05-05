@echo off
cd %~dp0
rem echo %1
rem pause
rem javac.exe -encoding utf-8 -classpath %cd%\gson.jar;%cd%\jackson-core-2.13.2.jar;%cd%\jackson-datatype-jsr310-2.13.2.jar;%cd%\* %1 
rem javac.exe -encoding utf-8 -classpath %cd%\*;%cd%\XMLHandler.java %1 
javac.exe -encoding utf-8 %1 