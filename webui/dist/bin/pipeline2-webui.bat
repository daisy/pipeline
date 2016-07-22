@echo off
@setlocal enabledelayedexpansion
title DAISY Pipeline 2 Web UI

if "%WEBUI_HOME%"=="" set "WEBUI_HOME=%~dp0.."

set "APP_LIB_DIR=%WEBUI_HOME%\lib\"

cd /d "%WEBUI_HOME%"

set DP2DATA=%APPDATA%\DAISY Pipeline 2 Web UI
set DP2DATA_SLASH=%DP2DATA:\=/%

if exist "!DP2DATA!" goto CONFIGURATION_DONE

rem ensure xcopy is in path
set PATH=%PATH%;%SystemRoot%\System32

echo Creating application data directory...
mkdir "%DP2DATA%"

echo Copying default database...
mkdir "%DP2DATA%\dp2webui"
echo PATH=%PATH%
xcopy /E /Y "%WEBUI_HOME%\db-empty" "%DP2DATA%\dp2webui" >NUL

:CONFIGURATION_DONE

rem Detect if we were double clicked, although theoretically A user could
rem manually run cmd /c
for %%x in (!cmdcmdline!) do if %%~x==/c set DOUBLECLICKED=1

rem We use the value of the JAVACMD environment variable if defined
set _JAVACMD=%JAVACMD%

if "%_JAVACMD%"=="" (
  if not "%JAVA_HOME%"=="" (
    if exist "%JAVA_HOME%\bin\java.exe" set "_JAVACMD=%JAVA_HOME%\bin\java.exe"
  )
)
if "%_JAVACMD%"=="" set _JAVACMD=java

rem Detect if this java is ok to use.
for /F %%j in ('"%_JAVACMD%" -version  2^>^&1') do (
  if %%~j==java set JAVAINSTALLED=1
  if %%~j==openjdk set JAVAINSTALLED=1
)

rem BAT has no logical or, so we do it OLD SCHOOL! Oppan Redmond Style
set JAVAOK=true
if not defined JAVAINSTALLED set JAVAOK=false

if "%JAVAOK%"=="false" (
  echo.
  echo Java is not installed or can't be found.
  if not "%JAVA_HOME%"=="" (
    echo JAVA_HOME = "%JAVA_HOME%"
  )
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a valid Java Runtime Environment and install before
  echo running the DAISY Pipeline 2 Web UI.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if "java.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)

rem Check java version
set JAVAVER=0
for /f "tokens=3" %%g in ('java -version 2^>^&1 ^| findstr /i "version"') do (
    set JAVAVER_FULL=%%g
)
set JAVAVER_FULL=%JAVAVER_FULL:"=%
for /f "delims=. tokens=1-3" %%v in ("%JAVAVER_FULL%") do (
    set JAVAVER=%%w
)
if 8 gtr %JAVAVER% (
  echo.
  echo Java version is %JAVAVER%. Java 8 or higher is required.
  echo.
  echo Please go to
  echo   http://www.oracle.com/technetwork/java/javase/downloads/index.html
  echo and download a newer version of Java before
  echo running the DAISY Pipeline 2 Web UI.
  echo.
  echo If you think this message is in error, please check
  echo your environment variables to see if the correct "java.exe" are
  echo available via JAVA_HOME or PATH.
  echo.
  if defined DOUBLECLICKED pause
  exit /B 1
)

rem Check if log file is in use by a process
set DP2WEBUILOGFILE="%DP2DATA%\log.txt"
2>nul (
    >>%DP2WEBUILOGFILE% (call )
  ) && (
    if exist "%DP2DATA%\RUNNING_PID" (
      rem %DP2DATA%\RUNNING_PID already exists;
      rem the Web UI may have exited incorrectly.
      rem Deleting RUNNING_PID...
      del "%DP2DATA%\RUNNING_PID"
    )
  ) || (
    echo DAISY Pipeline 2 Web UI is already running
    echo (because the log file is locked by another process^)
    if defined DOUBLECLICKED pause
    exit /B 1
  )

echo %date% %time% > %DP2WEBUILOGFILE%
ver >> %DP2WEBUILOGFILE%
echo %processor_architecture% >> %DP2WEBUILOGFILE%

if not exist "%DP2DATA%\RUNNING_PID" (
  echo Starting Web UI on http://localhost:9000/ ...
  "%_JAVACMD%" -Ddb.default.url="jdbc:derby:%DP2DATA%\dp2webui" -Dderby.stream.error.file="%DP2DATA%\webui-database.log" -Dlogger.file="%WEBUI_HOME%\conf\logger.xml" -Dpidfile.path="%DP2DATA%\RUNNING_PID" -Dconfig.file="%WEBUI_HOME%\conf\application.conf" %* -cp "%WEBUI_HOME%\lib\*;" play.core.server.ProdServerStart >> %DP2WEBUILOGFILE% 2>&1
  if exist "%DP2DATA%\RUNNING_PID" (
    del "%DP2DATA%\RUNNING_PID"
  )
) else (
  echo Web UI is already running.
  echo If you are sure this is not the case, please delete %DP2DATA%\RUNNING_PID to try again
)

if defined DOUBLECLICKED pause
