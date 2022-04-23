@echo off
cd %~dp0
rem echo %1
rem pause
javac.exe -encoding utf-8 -classpath %cd%\gson.jar %1 