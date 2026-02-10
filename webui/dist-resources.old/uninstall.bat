@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION

title Pipeline Uninstaller
cd /d "%~dp0"

set DP2DATA=%APPDATA%\DAISY Pipeline 2


IF NOT EXIST "!DP2DATA!" GOTO NOT_INSTALLED

echo There is no DAISY Pipeline data to remove.
pause

:NOT_INSTALLED


IF EXIST "!DP2DATA!" GOTO WAS_INSTALLED

echo Deleting DAISY Pipeline data...
rmdir /S /Q "%DP2DATA%"
echo DAISY Pipeline data is now removed.
pause

:WAS_INSTALLED

