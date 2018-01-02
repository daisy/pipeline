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
set DIRNAME=%~dp0%
set PROGNAME=%~nx0%
set ARGS=%*

title Pipeline2

goto BEGIN

:warn
    echo %PROGNAME%: %*
goto :EOF

:BEGIN

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

if not "%PIPELINE2_HOME%" == "" (
    call :warn Ignoring predefined value for PIPELINE2_HOME
)
set PIPELINE2_HOME=%DIRNAME%..
if not exist "%PIPELINE2_HOME%" (
    call :warn PIPELINE2_HOME is not valid: !PIPELINE2_HOME!
    goto END
)

if not "%PIPELINE2_BASE%" == "" (
    if not exist "%PIPELINE2_BASE%" (
       call :warn PIPELINE2_BASE is not valid: !PIPELINE2_BASE!
       goto END
    )
)

if "%PIPELINE2_BASE%" == "" (
  set PIPELINE2_BASE=!PIPELINE2_HOME!
)

if not "%PIPELINE2_DATA%" == "" (
    if not exist "%PIPELINE2_DATA%" (
        mkdir "!PIPELINE2_DATA!"
    )
)

if "%PIPELINE2_DATA%" == "" (
    set PIPELINE2_DATA=%appdata%/DAISY Pipeline 2
    if not exist "!PIPELINE2_DATA!" (
      mkdir "!PIPELINE2_DATA!"
    )
)

set LOCAL_CLASSPATH=%CLASSPATH%
set DEFAULT_JAVA_OPTS=-Xmx1G -XX:MaxPermSize=256M -Dcom.sun.management.jmxremote
set CLASSPATH=%LOCAL_CLASSPATH%;%PIPELINE2_BASE%\conf
set DEFAULT_JAVA_DEBUG_OPTS=-Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005

if "%LOCAL_CLASSPATH%" == "" goto :PIPELINE2_CLASSPATH_EMPTY
    set CLASSPATH=%LOCAL_CLASSPATH%;%PIPELINE2_BASE%\conf
    goto :PIPELINE2_CLASSPATH_END
:PIPELINE2_CLASSPATH_EMPTY
    set CLASSPATH=%PIPELINE2_BASE%\conf
:PIPELINE2_CLASSPATH_END

rem Support for loading native libraries
set PATH=%PATH%;%PIPELINE2_BASE%\lib;%PIPELINE2_HOME%\lib

rem Setup the Java Virtual Machine
if not "%JAVA%" == "" goto :Check_JAVA_END
    if not "%JAVA_HOME%" == "" goto :TryJDKEnd
        call :warn JAVA_HOME not set; results may vary
:TryJRE
    start /w regedit /e __reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment"
    if not exist __reg1.txt goto :TryJDK
    type __reg1.txt | find "CurrentVersion" > __reg2.txt
    if errorlevel 1 goto :TryJDK
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JavaTemp=%%~x
    if errorlevel 1 goto :TryJDK
    set JavaTemp=%JavaTemp%##
    set JavaTemp=%JavaTemp:                ##=##%
    set JavaTemp=%JavaTemp:        ##=##%
    set JavaTemp=%JavaTemp:    ##=##%
    set JavaTemp=%JavaTemp:  ##=##%
    set JavaTemp=%JavaTemp: ##=##%
    set JavaTemp=%JavaTemp:##=%
    del __reg1.txt
    del __reg2.txt
    start /w regedit /e __reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Runtime Environment\%JavaTemp%"
    if not exist __reg1.txt goto :TryJDK
    type __reg1.txt | find "JavaHome" > __reg2.txt
    if errorlevel 1 goto :TryJDK
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JAVA_HOME=%%~x
    if errorlevel 1 goto :TryJDK
    del __reg1.txt
    del __reg2.txt
    goto TryJDKEnd
:TryJDK
    start /w regedit /e __reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit"
    if not exist __reg1.txt (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    type __reg1.txt | find "CurrentVersion" > __reg2.txt
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JavaTemp=%%~x
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    set JavaTemp=%JavaTemp%##
    set JavaTemp=%JavaTemp:                ##=##%
    set JavaTemp=%JavaTemp:        ##=##%
    set JavaTemp=%JavaTemp:    ##=##%
    set JavaTemp=%JavaTemp:  ##=##%
    set JavaTemp=%JavaTemp: ##=##%
    set JavaTemp=%JavaTemp:##=%
    del __reg1.txt
    del __reg2.txt
    start /w regedit /e __reg1.txt "HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\Java Development Kit\%JavaTemp%"
    if not exist __reg1.txt (
        call :warn Unable to retrieve JAVA_HOME from JDK
        goto END
    )
    type __reg1.txt | find "JavaHome" > __reg2.txt
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    for /f "tokens=2 delims==" %%x in (__reg2.txt) do set JAVA_HOME=%%~x
    if errorlevel 1 (
        call :warn Unable to retrieve JAVA_HOME
        goto END
    )
    del __reg1.txt
    del __reg2.txt
:TryJDKEnd
    if not exist "%JAVA_HOME%" (
        call :warn JAVA_HOME is not valid: "%JAVA_HOME%"
        goto END
    )
    set JAVA=%JAVA_HOME%\bin\java
:Check_JAVA_END

if "%JAVA_OPTS%" == "" set JAVA_OPTS=%DEFAULT_JAVA_OPTS%

if "%PIPELINE2_DEBUG%" == "" goto :PIPELINE2_DEBUG_END
    rem Use the defaults if JAVA_DEBUG_OPTS was not set
    if "%JAVA_DEBUG_OPTS%" == "" set JAVA_DEBUG_OPTS=%DEFAULT_JAVA_DEBUG_OPTS%

    set "JAVA_OPTS=%JAVA_DEBUG_OPTS% %JAVA_OPTS%"
    call :warn Enabling Java debug options: %JAVA_DEBUG_OPTS%
:PIPELINE2_DEBUG_END

if "%PIPELINE2_PROFILER%" == "" goto :PIPELINE2_PROFILER_END
    set PIPELINE2_PROFILER_SCRIPT=%PIPELINE2_HOME%\conf\profiler\%PIPELINE2_PROFILER%.cmd

    if exist "%PIPELINE2_PROFILER_SCRIPT%" goto :PIPELINE2_PROFILER_END
    call :warn Missing configuration for profiler '%PIPELINE2_PROFILER%': %PIPELINE2_PROFILER_SCRIPT%
    goto END
:PIPELINE2_PROFILER_END
set BOOTSTRAP=${bundles.bootstrap}
rem Setup the classpath
pushd "%PIPELINE2_HOME%\%BOOTSTRAP:/=\%"
for %%G in (*.jar) do call:APPEND_TO_CLASSPATH %%G
popd
goto CLASSPATH_END

: APPEND_TO_CLASSPATH
set filename=%~1
set suffix=%filename:~-4%
if %suffix% equ .jar set CLASSPATH=%CLASSPATH%;%PIPELINE2_HOME%\%BOOTSTRAP:/=\%\%filename%
goto :EOF

:CLASSPATH_END



rem Execute the JVM or the load the profiler
if "%PIPELINE2_PROFILER%" == "" goto :RUN
    rem Execute the profiler if it has been configured
    call :warn Loading profiler script: %PIPELINE2_PROFILER_SCRIPT%
    call %PIPELINE2_PROFILER_SCRIPT%

:RUN
    if "%PIPELINE2_WS_AUTHENTICATION%" == "" (
	SET AUTH=-Dorg.daisy.pipeline.ws.authentication=false
    ) else (
	SET AUTH=-Dorg.daisy.pipeline.ws.authentication=%PIPELINE2_WS_AUTHENTICATION%
    )
    if "%PIPELINE2_WS_LOCALFS%" == "" (
	SET LOCAL=-Dorg.daisy.pipeline.ws.localfs=true
    ) else (
	SET LOCAL=-Dorg.daisy.pipeline.ws.localfs=%PIPELINE2_WS_LOCALFS%
    )
    
    SET OPTS=%LOCAL% %AUTH%
    SET MAIN=org.apache.felix.main.Main
    SET SHIFT=false
    SET MODE=-Dorg.daisy.pipeline.main.mode=webservice

:RUN_LOOP
    if "%1" == "remote" goto :EXECUTE_REMOTE
    if "%1" == "local" goto :EXECUTE_LOCAL
    if "%1" == "clean" goto :EXECUTE_CLEAN
    if "%1" == "gui" goto :EXECUTE_GUI
    if "%1" == "debug" goto :EXECUTE_DEBUG
    goto :EXECUTE

:EXECUTE_REMOTE
    SET OPTS=-Dorg.daisy.pipeline.ws.localfs=false -Dorg.daisy.pipeline.ws.authentication=true
    shift
    goto :RUN_LOOP

:EXECUTE_LOCAL
    SET OPTS=-Dorg.daisy.pipeline.ws.localfs=true -Dorg.daisy.pipeline.ws.authentication=false
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

:EXECUTE
    SET ARGS=%1 %2 %3 %4 %5 %6 %7 %8
    rem Execute the Java Virtual Machine
    cd "%PIPELINE2_BASE%"
    "%JAVA%" %JAVA_OPTS% %OPTS% -classpath "%CLASSPATH%" -Djava.endorsed.dirs="%JAVA_HOME%\jre\lib\endorsed;%JAVA_HOME%\lib\endorsed;%PIPELINE2_HOME%\lib\endorsed" -Djava.ext.dirs="%JAVA_HOME%\jre\lib\ext;%JAVA_HOME%\lib\ext;%PIPELINE2_HOME%\lib\ext" -Dorg.daisy.pipeline.home="%PIPELINE2_HOME%" -Dorg.daisy.pipeline.base="%PIPELINE2_BASE%" -Dorg.daisy.pipeline.data="%PIPELINE2_DATA%" -Dfelix.config.properties="file:%PIPELINE2_HOME:\=/%/etc/config.properties" -Dfelix.system.properties="file:%PIPELINE2_HOME:\=/%/etc/system.properties" %MODE% %PIPELINE2_OPTS% %MAIN% %ARGS%

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:END

endlocal

if not "%PAUSE%" == "" pause

:END_NO_PAUSE

