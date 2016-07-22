@ECHO OFF
SETLOCAL ENABLEDELAYEDEXPANSION

title Pipeline 2 Web UI
cd /d "%~dp0"

set DP2DATA=%APPDATA%\DAISY Pipeline 2
set DP2DATA_SLASH=%DP2DATA:\=/%

IF EXIST !DP2DATA!\webui GOTO CONFIGURATION_DONE

echo Creating application data directory...
mkdir "%DP2DATA%\webui"

echo Creating directory for log files...
mkdir "%DP2DATA%\log"

echo Copying default database...
mkdir "%DP2DATA%\webui\dp2webui"
xcopy /E /Y dp2webui "%DP2DATA%\webui\dp2webui"

:CONFIGURATION_DONE

echo Starting Web UI...
java -Dderby.stream.error.file="%DP2DATA%\log\webui-database.log" -Dlogger.file="%~dp0\logger.xml" -Dpidfile.path="%DP2DATA%\webui\RUNNING_PID" -Dconfig.file="%~dp0\application.conf" %* -cp "%~dp0\lib\*;" play.core.server.NettyServer .
