@echo off
rem Checks java version via the following methods, respectively:
REM    - 1 : Check if a local JRE is present along the daisy-pipeline
REM    - 2 : Check if a java registry key exists
REM    - 3 : Check if a JAVA_HOME environment var is set
REM For all this case, if a version of java corresponding to the 
REM "REQUIRED_JAVA_VER" is found, the JAVA env var is set to the java.exe found.
rem Exit code:
rem   0 check passed and JAVA set
rem   1 check failed
rem   3 check failed fatally (something wrong with code)

rem For unit testing
REM if not [%1]==[] (
REM     call %*
REM     goto :EOF
REM )

setlocal enabledelayedexpansion

set DIRNAME=%~dp0
set PROGNAME=%~nx0
set REQUIRED_JAVA_VER=%1

shift

if not [%1]==[] (
    rem %* not affected by shift
    call %1 %2 %3 %4 %5 %6 %7 %8 %9
    goto :EOF
)

goto BEGIN

rem # # HELPERS # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:warn
REM Warning messaging
    echo %PROGNAME%: %*
goto :EOF

:parse_java_version java_exec_path
    set FOUND=
    if exist "%~1" set FOUND=yes
    if not defined FOUND (
        for %%X in (%1) do (set FOUND=%%~$PATH:X)
    )
    if not defined FOUND (
        call:warn Java executable does not exist: "%~1"
        exit /b 1
    )
    call:parse_java_version_with_cmd %1 -version
goto :EOF

:parse_java_version_with_cmd java_version_cmd
    for /f "usebackq tokens=3" %%a in (`%* 2^>^&1`) do (
        set JAVA_VER=%%~a
        call:validate_version "Version parsed from %cmd% is invalid: ""%JAVA_VER%"""
        if errorLevel 1 goto :EOF
        exit /b 0
    )
    call:warn Failed to parse java -version output from: "%~1"
    exit /b 3
goto :EOF

:check_version
    call:compare_versions %JAVA_VER% %REQUIRED_JAVA_VER%
    if errorLevel 3 (
        call:warn Failed to compare versions: "%JAVA_VER%" with "%REQUIRED_JAVA_VER%"
        goto :EOF
    )
    if "%ERRORLEVEL%" == "-1" exit /b 1
    exit /b 0
goto :EOF

:compare_versions version1  version2
rem Compares up to 4 numbers (X.X.X.X)
rem Returns via %ERRORLEVEL%... v1<v2:-1, v1=v2:0, v1>v2:1
    for /f "tokens=1,2,3,4 delims=._-" %%a in ("%~1") do (
        for /f "tokens=1,2,3,4 delims=._-" %%w in ("%~2") do (
            if %%a lss %%w exit /b -1
            if %%a gtr %%w exit /b  1
            if %%b lss %%x exit /b -1
            if %%b gtr %%x exit /b  1
            if %%c lss %%y exit /b -1
            if %%c gtr %%y exit /b  1
            if %%d lss %%z exit /b -1
            if %%d gtr %%z exit /b  1
            exit /b 0
        )
    )
    exit /b 3
goto :EOF

:search_registry
    call:search_registry_with_cmd "reg query"
goto :EOF

:search_registry_with_cmd reg_query_cmd
    call:warn Searching registry key HKLM\SOFTWARE\JavaSoft\JRE
    call:parse_regKey_value "%~1 ""HKLM\SOFTWARE\JavaSoft\JRE"" /v CurrentVersion"
    if errorLevel 1 goto javaHome_try_jdk
    set JAVA_VER=%RETURN%
    call:validate_version "CurrentVersion is not valid: ""%JAVA_VER%"""
    if errorLevel 1 goto javaHome_try_jdk
    call:parse_regKey_value "%~1 ""HKLM\SOFTWARE\JavaSoft\JRE\%JAVA_VER%"" /v JavaHome"
    if errorLevel 1 goto javaHome_try_jdk
    set JAVA_HOME=%RETURN%
    exit /b 0
    :javaHome_try_jdk
        rem some versions use the "Java Development Kit"
        call:warn Searching registry key HKLM\SOFTWARE\JavaSoft\JDK
        call:parse_regKey_value "%~1 ""HKLM\SOFTWARE\JavaSoft\JDK"" /v CurrentVersion"
        if errorLevel 1 goto :EOF
        set JAVA_VER=%RETURN%
        call:validate_version "CurrentVersion is not valid: ""%JAVA_VER%"""
        if errorLevel 1 goto :EOF
        call:parse_regKey_value "%~1 ""HKLM\SOFTWARE\JavaSoft\JDK\%JAVA_VER%"" /v JavaHome"
        if errorLevel 1 goto :EOF
        set JAVA_HOME=%RETURN%
        exit /b 0
goto :EOF

:parse_regKey_value cmd
    set "cmd=%~1"
    set "cmd=%cmd:""="%" &rem "
    set RETURN=
    for /f "usebackq skip=2 tokens=3*" %%x in (`%cmd%`) do (
      if not "%%y"=="" (
        set RETURN=%%x %%y
        exit /b 0
      )
      set RETURN=%%x
    )
    if "%RETURN%" == "" exit /b 1
    exit /b 0
goto :EOF

:validate_version errorMsg
rem fails if JAVA_VER < 3 numbers
    for /f "tokens=1,2,3* delims=._-" %%a in ("%JAVA_VER%") do (if not "%%c" == "" exit /b 0)
    set "errorMsg=%~1"
    set "errorMsg=%errorMsg:""="%" &rem "
    call:warn %errorMsg%
    exit /b 1
goto :EOF

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #


REM // -----------------------------
REM // Starting point of the script
REM // -----------------------------
:BEGIN
    call:warn Checking Java version, at least Java "%REQUIRED_JAVA_VER%" is required...


REM Step 1 
:CheckRelative
REM check if a local jre is present
    call:warn Searching for a local JRE in "%DIRNAME%..\jre" 
    call:parse_java_version "%DIRNAME%..\jre\bin\java.exe"
    if errorLevel 1 goto CheckRegistry
    call:check_version
    if errorLevel 3 goto END
    if errorLevel 1 (
        call:warn DAISY Pipeline 2 folder contains incompatible JVM: "%JAVA_VER%", trying JAVA variable...
        goto CheckJava
    )
    call:warn Found compatible JVM in DAISY Pipeline 2 installation folder: "%JAVA_VER%"
    set JAVA=%DIRNAME%..\jre\bin\java.exe
goto END

REM Step 2
:CheckRegistry
REM Check through registry if java was installed with an valid version
    call:search_registry
    if errorLevel 1 (
        call:warn Registry does not contain a compatible JVM, trying JAVA_HOME
        goto CheckJavaHome
    )
    call:check_version
    if errorLevel 3 goto END
    if errorLevel 1 (
        call:warn CurrentVersion registry key shows an incompatible version of Java: "%JAVA_VER%", trying JAVA_HOME ...
        goto CheckJavaHome
    )
    call:warn Found compatible JVM from CurrentVersion registry key: "%JAVA_VER%"
    set JAVA=%JAVA_HOME%\bin\java.exe
goto END

REM Step 3
:CheckJavaHome
REM Check if a JAVA_HOME env var is set with a valid version of java
    REM JAVA_HOME can be set by the :search_registry call as part of :CheckRegistry
    REM Or can be already defined as an environement variable
    call:parse_java_version "%JAVA_HOME%\bin\java.exe"
    if errorLevel 1 goto END
    call:check_version
    if errorLevel 3 goto END
    if errorLevel 1 (
      call:warn JavaHome registry key points to an incompatible JVM: "%JAVA_VER%"
      goto END
    )

    call:warn Found compatible JVM from JAVA_HOME : "%JAVA_VER%"
    set JAVA=%JAVA_HOME%\bin\java.exe
goto END

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:END
    endlocal & set JAVA=%JAVA%
    exit /b %ERRORLEVEL%