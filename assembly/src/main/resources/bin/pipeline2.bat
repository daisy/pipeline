@echo off
rem
rem
rem    Licensed to the Apache Software Foundation (ASF) under one or more
rem    contributor license agreements.  See the NOTICE file distributed with
rem    this work for additional information regarding copyright ownership.
rem    The ASF licenses this file to You under the Apache License, Version 2.0
rem    (the "License"); you may not use this file except in compliance with
rem    the License.  You may obtain a copy of the License at
rem
rem       http://www.apache.org/licenses/LICENSE-2.0
rem
rem    Unless required by applicable law or agreed to in writing, software
rem    distributed under the License is distributed on an "AS IS" BASIS,
rem    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
rem    See the License for the specific language governing permissions and
rem    limitations under the License.
rem
rem    ------------------------------------------------------------------------
rem
rem    This script is adapted from the launcher script of the Karaf runtime:
rem    http://karaf.apache.org/
rem

if not "%ECHO%" == "" echo %ECHO%

setlocal enabledelayedexpansion
set DIRNAME=%~dp0
set PROGNAME=%~nx0
rem Code to return to launcher on failure
rem 0:success, 1:unhandled, 2:user-fixable, 3:fatal(we must fix)
set exitCode=0


title Pipeline2

if "%PIPELINE2_DATA%" == "" (
    set PIPELINE2_DATA=%appdata%/DAISY Pipeline 2
    if not exist "%PIPELINE2_DATA%" (
      mkdir "%PIPELINE2_DATA%"
    )
)

if not exist "%PIPELINE2_DATA%/log" mkdir "%PIPELINE2_DATA%/log"

goto BEGIN

rem # # SUBROUTINES # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:warn
    echo %PROGNAME%: %*
goto :EOF

:append_to_classpath
    set filename=%~1
    set suffix=%filename:~-4%
    if %suffix% equ .jar set CLASSPATH=!CLASSPATH!;%PIPELINE2_HOME%\%BOOTSTRAP:/=\%\%filename%
goto :EOF

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:BEGIN
    call:warn %DATE:~10,4%-%DATE:~4,2%-%DATE:~7,2% %TIME:~0,2%:%TIME:~3,2%:%TIME:~6,2%

    rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

    if not "%PIPELINE2_HOME%" == "" call:warn Ignoring predefined value for PIPELINE2_HOME

    set PIPELINE2_HOME=%DIRNAME%..
    if not exist "%PIPELINE2_HOME%" (
        call:warn PIPELINE2_HOME is not valid: %PIPELINE2_HOME%
        rem fatal
        set exitCode=3
        goto END
    )

    if not "%PIPELINE2_BASE%" == "" (
        if not exist "%PIPELINE2_BASE%" (
            call:warn PIPELINE2_BASE is not valid: %PIPELINE2_BASE%
            rem fatal
            set exitCode=3
            goto END
        )
    )

    if "%PIPELINE2_BASE%" == "" set PIPELINE2_BASE=%PIPELINE2_HOME%

    if not "%PIPELINE2_DATA%" == "" (
        if not exist "%PIPELINE2_DATA%" (
            mkdir "%PIPELINE2_DATA%"
        )
    )

    rem Setup the Java Virtual Machine
    call "%~dp0\checkJavaVersion.bat" 11
    if errorLevel 1 (
        rem Fall back to Java 8 (or 9 or 10) because web server does not work with Java 11
        call:warn Java 11 not found; Trying Java 8
        call "%~dp0\checkJavaVersion.bat" 1.8
        if errorLevel 1 (
            if errorLevel 2 (
                rem fatal
                set exitCode=3
            ) else (
                rem user-fixable
                set exitCode=2
            )
            goto END
        )
    )

    set DEFAULT_JAVA_OPTS=-Dcom.sun.management.jmxremote
    if "%JAVA_OPTS%" == "" set JAVA_OPTS=%DEFAULT_JAVA_OPTS%

    set DEFAULT_JAVA_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005
    if "%PIPELINE2_DEBUG%" == "" goto :PIPELINE2_DEBUG_END
    rem Use the defaults if JAVA_DEBUG_OPTS was not set
    if "%JAVA_DEBUG_OPTS%" == "" set JAVA_DEBUG_OPTS=%DEFAULT_JAVA_DEBUG_OPTS%
    set "JAVA_OPTS=%JAVA_DEBUG_OPTS% %JAVA_OPTS%"
    call:warn Enabling Java debug options: %JAVA_DEBUG_OPTS%
:PIPELINE2_DEBUG_END

    set BOOTSTRAP=system/bootstrap
    rem Setup the classpath
    pushd "%PIPELINE2_HOME%\%BOOTSTRAP:/=\%"
    for %%G in (*.jar) do call:append_to_classpath %%G
    popd

    SET MAIN=org.apache.felix.main.Main
    SET SHIFT=false
    SET MODE=-Dorg.daisy.pipeline.main.mode=webservice

:RUN_LOOP
    if [%1]==[] goto :EXECUTE
    if "%1" == "remote" goto :EXECUTE_REMOTE
    if "%1" == "local" goto :EXECUTE_LOCAL
    if "%1" == "clean" goto :EXECUTE_CLEAN
    if "%1" == "gui" goto :EXECUTE_GUI
    if "%1" == "debug" goto :EXECUTE_DEBUG
    if "%1" == "shell" goto :EXECUTE_SHELL
    call:warn Unexpected argument: "%1"
    rem user-fixable
    set exitCode=2
    goto END
goto :EXECUTE

:EXECUTE_REMOTE
    set PIPELINE2_WS_LOCALFS=false
    set PIPELINE2_WS_AUTHENTICATION=true
    shift
goto :RUN_LOOP

:EXECUTE_LOCAL
    set PIPELINE2_WS_LOCALFS=true
    set PIPELINE2_WS_AUTHENTICATION=false
    shift
goto :RUN_LOOP

:EXECUTE_CLEAN
    rmdir /S /Q "%PIPELINE2_DATA%"
    shift
goto :RUN_LOOP

:EXECUTE_GUI
    SET MODE=-Dorg.daisy.pipeline.main.mode=gui
    shift
goto :RUN_LOOP

:EXECUTE_DEBUG
    if "%JAVA_DEBUG_OPTS%" == "" set JAVA_DEBUG_OPTS=%DEFAULT_JAVA_DEBUG_OPTS%
    set "JAVA_OPTS=%JAVA_DEBUG_OPTS% %JAVA_OPTS%"
    shift
goto :RUN_LOOP

:EXECUTE_SHELL
    for /f %%F in ('dir /b "%PIPELINE2_BASE%\system\felix\gogo\*.jar"') do (
         set GOGO_BUNDLES=!GOGO_BUNDLES! file:system\felix\gogo\%%F
    )
    set FELIX_OPTS=%FELIX_OPTS% -Dfelix.auto.start.1="%GOGO_BUNDLES%"
    shift
goto :RUN_LOOP

:EXECUTE
    rem Execute the Java Virtual Machine
    cd "%PIPELINE2_BASE%"

    call "%~dp0\checkJavaVersion.bat" _ :compare_versions %JAVA_VER% 9
    if %ERRORLEVEL% geq 0 (
        if errorLevel 3 (
            rem unexpected error
            call:warn Failed to compare versions: "%JAVA_VER%" with "9"
            set exitCode=3
            goto END
        )
        rem at least version 9
        SET COMMAND="%JAVA%" %JAVA_OPTS% ^
            --add-opens java.base/java.security=ALL-UNNAMED ^
            --add-opens java.base/java.net=ALL-UNNAMED ^
            --add-opens java.base/java.lang=ALL-UNNAMED ^
            --add-opens java.base/java.util=ALL-UNNAMED ^
            --add-opens java.naming/javax.naming.spi=ALL-UNNAMED ^
            --add-opens java.rmi/sun.rmi.transport.tcp=ALL-UNNAMED ^
            --add-exports=java.base/sun.net.www.protocol.http=ALL-UNNAMED ^
            --add-exports=java.base/sun.net.www.protocol.https=ALL-UNNAMED ^
            --add-exports=java.base/sun.net.www.protocol.jar=ALL-UNNAMED ^
            --add-exports=jdk.xml.dom/org.w3c.dom.html=ALL-UNNAMED ^
            --add-exports=jdk.naming.rmi/com.sun.jndi.url.rmi=ALL-UNNAMED ^
            -Dorg.daisy.pipeline.home="%PIPELINE2_HOME%" ^
            -Dorg.daisy.pipeline.data="%PIPELINE2_DATA%" ^
            -Dfelix.config.properties="file:%PIPELINE2_HOME:\=/%/etc/config.properties" ^
            -Dfelix.system.properties="file:%PIPELINE2_HOME:\=/%/etc/system.properties" ^
            %FELIX_OPTS% %MODE% -classpath "%CLASSPATH%" %MAIN%
    ) else (
        rem version 8
        SET COMMAND="%JAVA%" %JAVA_OPTS% ^
            -Dorg.daisy.pipeline.home="%PIPELINE2_HOME%" ^
            -Dorg.daisy.pipeline.data="%PIPELINE2_DATA%" ^
            -Dfelix.config.properties="file:%PIPELINE2_HOME:\=/%/etc/config.properties" ^
            -Dfelix.system.properties="file:%PIPELINE2_HOME:\=/%/etc/system.properties" ^
            %FELIX_OPTS% %MODE% -classpath "%CLASSPATH%" %MAIN%
            rem skipping java.endorsed.dirs and java.ext.dirs because this requires JAVA_HOME which is not always available
            rem -Djava.endorsed.dirs="%JAVA_HOME%\jre\lib\endorsed;%JAVA_HOME%\lib\endorsed;%PIPELINE2_HOME%\lib\endorsed" ^
            rem -Djava.ext.dirs="%JAVA_HOME%\jre\lib\ext;%JAVA_HOME%\lib\ext;%PIPELINE2_HOME%\lib\ext" ^
    )
    call:warn Starting java: %COMMAND%

    if not "%GOGO_BUNDLES%" == "" (
        endlocal & (
            set "PIPELINE2_HOME=%PIPELINE2_HOME%"
            set "PIPELINE2_BASE=%PIPELINE2_BASE%"
            set "PIPELINE2_DATA=%PIPELINE2_DATA%"
            set "PIPELINE2_WS_LOCALFS=%PIPELINE2_WS_LOCALFS%"
            set "PIPELINE2_WS_AUTHENTICATION=%PIPELINE2_WS_AUTHENTICATION%"
            %COMMAND%
        )
    ) else (
        call:warn Output is written to daisy-pipeline-java.log
        endlocal & (
            set "PIPELINE2_HOME=%PIPELINE2_HOME%"
            set "PIPELINE2_BASE=%PIPELINE2_BASE%"
            set "PIPELINE2_DATA=%PIPELINE2_DATA%"
            set "PIPELINE2_WS_LOCALFS=%PIPELINE2_WS_LOCALFS%"
            set "PIPELINE2_WS_AUTHENTICATION=%PIPELINE2_WS_AUTHENTICATION%"
            %COMMAND% > "%PIPELINE2_DATA%/log/daisy-pipeline-java.log"
        )
    )

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:END
    call:warn Exiting with value %exitCode%
    if not "%PAUSE%" == "" pause
    exit /b %exitCode%
