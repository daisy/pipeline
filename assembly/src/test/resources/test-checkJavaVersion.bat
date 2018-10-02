@echo off
setlocal enabledelayedexpansion
set checkJavaVersion="..\..\main\resources\bin\checkJavaVersion.bat"

if "%1" == ":mock-reg-query-1"    goto %1
if "%1" == ":mock-java-version-1" goto %1

goto BEGIN

rem # # HELPERS # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:info msg
    echo Test %test% of %testing%: %~1
goto :EOF

:fail msg
    set /A failures=failures+1
    set "msg=%~1"
    set "msg=%msg:""="%" &rem "
    set line=    [FAILURE] Test %test%: %testing% %msg%
    echo [91m%line%[0m
goto :EOF

:pass
    set line=Test %test% passed
    echo [92m%line%[0m
goto :EOF

rem # # MOCKS # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:mock-reg-query-1
    shift
    if not "%~2" == "/v" exit /b 1
    if not "%4" == "" exit /b 1
    if "%~1" == "HKLM\SOFTWARE\JavaSoft\JRE"        goto :mock-reg-query-1-jre
    if "%~1" == "HKLM\SOFTWARE\JavaSoft\JRE\10.0.1" goto :mock-reg-query-1-jre-10-0-1
    exit /b 1
:mock-reg-query-1-jre
    if "%~3" == "CurrentVersion" (
        echo.
        echo HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\JRE
        echo     CurrentVersion    REG_SZ    10.0.1
        echo.
        echo.
        exit /b 0
    )
    exit /b 1
:mock-reg-query-1-jre-10-0-1
    if "%~3" == "JavaHome" (
        echo.
        echo HKEY_LOCAL_MACHINE\SOFTWARE\JavaSoft\JRE\10.0.1
        echo     JavaHome    REG_SZ    C:\Program Files\Java\jre-10.0.1
        echo.
        echo.
        exit /b 0
    )
    exit /b 1
goto :EOF

:mock-java-version-1
    echo java version "10.0.1" 2018-04-17
    echo Java(TM) SE Runtime Environment 18.3 (build 10.0.1+10)
    echo Java HotSpot(TM) 64-Bit Server VM 18.3 (build 10.0.1+10, mixed mode)
    echo.
goto :EOF

rem # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # # #

:BEGIN

set failures=0

:TEST1
    set testing=validate_version
    set test=1a
    set JAVA_VER=1.1.1
    call:info "%JAVA_VER%"
    call %checkJavaVersion% :%testing% "" >nul
    if %ERRORLEVEL% == 0 (
        call:pass
    ) else if %ERRORLEVEL% == 1 (
        call:fail "incorrectly processed "%JAVA_VER%" as invalid"
        goto TEST2
    ) else (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST2
    )

    set test=1b
    set JAVA_VER=1.1
    call:info "%JAVA_VER%"
    call %checkJavaVersion% :%testing% "" >nul
    if %ERRORLEVEL% == 0 (
        call:fail "incorrectly processed "%JAVA_VER%" as valid"
        goto TEST2
    ) else if %ERRORLEVEL% == 1 (
        call:pass
    ) else (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST2
    )

:TEST2
    set testing=compare_versions
    set test=2a
    set ver1=1
    set ver2=1
    call:info "%ver1% vs. %ver2%"
    call %checkJavaVersion% :%testing% %ver1% %ver2%
    if %ERRORLEVEL% == 0 (
        call:pass
    ) else if %ERRORLEVEL% == -1 (
        call:fail "incorrectly compared %ver1% as < %ver2%"
        goto TEST3
    ) else if %ERRORLEVEL% == 1 (
        call:fail "incorrectly compared %ver1% as > %ver2%"
        goto TEST3
    ) else (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST3
    )

    set test=2b
    set ver1=1.0.1
    set ver2=1.0.0
    call:info "%ver1% vs. %ver2%"
    call %checkJavaVersion% :%testing% %ver1% %ver2%
    if %ERRORLEVEL% == 0 (
        call:fail "incorrectly compared %ver1% as == %ver2%"
        goto TEST3
    ) else if %ERRORLEVEL% == -1 (
        call:fail "incorrectly compared %ver1% as < %ver2%"
        goto TEST3
    ) else if %ERRORLEVEL% == 1 (
        call:pass
    ) else (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST3
    )

    set test=2c
    set ver1=1.0.0
    set ver2=1.0.1
    call:info "%ver1% vs. %ver2%"
    call %checkJavaVersion% :%testing% %ver1% %ver2%
    if %ERRORLEVEL% == 0 (
        call:fail "incorrectly compared %ver1% as == %ver2%"
        goto TEST3
    ) else if %ERRORLEVEL% == -1 (
        call:pass
    ) else if %ERRORLEVEL% == 1 (
        call:fail "incorrectly compared %ver1% as > %ver2%"
        goto TEST3
    ) else (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST3
    )

    set test=2d
    set ver1=1.0
    set ver2=1.0.0
    call:info "%ver1% vs. %ver2%"
    call %checkJavaVersion% :%testing% %ver1% %ver2%
    if %ERRORLEVEL% == 0 (
        call:pass
    ) else if %ERRORLEVEL% == -1 (
        call:fail "incorrectly compared %ver1% as < %ver2%"
        goto TEST3
    ) else if %ERRORLEVEL% == 1 (
        call:fail "incorrectly compared %ver1% as > %ver2%"
        goto TEST3
    ) else (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST3
    )

    set test=2e
    set ver1=1.0
    set ver2=1.0.1
    call:info "%ver1% vs. %ver2%"
    call %checkJavaVersion% :%testing% %ver1% %ver2%
    if %ERRORLEVEL% == 0 (
        call:fail "incorrectly compared %ver1% as == %ver2%"
        goto TEST3
    ) else if %ERRORLEVEL% == -1 (
        call:pass
    ) else if %ERRORLEVEL% == 1 (
        call:fail "incorrectly compared %ver1% as > %ver2%"
        goto TEST3
    ) else (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST3
    )

:TEST3
    set testing=parse_java_version_with_cmd
    set test=3
    set "cmd=:mock-java-version-1"
    call:info "%cmd%"
    call %checkJavaVersion% :%testing% "call %~dpnx0 %cmd%"
    if errorLevel 1 (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST4
    )
    if not "%JAVA_VER%" == "10.0.1" (
        call:fail "failed to parse java -version, output: ""%JAVA_VER%"""
        goto TEST4
    )
    call:pass

:TEST4
    set testing=parse_regKey_value
    set test=4a
    set "cmd=:mock-reg-query-1 HKLM\SOFTWARE\JavaSoft\JRE /v CurrentVersion"
    call:info "%cmd%"
    call %checkJavaVersion% :%testing% "call %~dpnx0 %cmd%"
    if errorLevel 1 (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST5
    )
    if not "%RETURN%" == "10.0.1" (
        call:fail "failed to parse CurrentVersion, output: ""%RETURN%"""
        goto TEST5
    )
    call:pass

    set test=4b
    set "cmd=:mock-reg-query-1 HKLM\SOFTWARE\JavaSoft\JRE\10.0.1 /v JavaHome"
    call:info "%cmd%"
    call %checkJavaVersion% :%testing% "call %~dpnx0 %cmd%"
    if errorLevel 1 (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST5
    )
    if not "%RETURN%" == "C:\Program Files\Java\jre-10.0.1" (
        call:fail "failed to parse mock JavaHome, output: ""%RETURN%"""
        goto TEST5
    )
    call:pass

:TEST5
    set testing=search_registry_with_cmd
    set test=5
    set "cmd=:mock-reg-query-1"
    call:info "%cmd%"
    call %checkJavaVersion% :%testing% "call %~dpnx0 %cmd%"
    if errorLevel 1 (
        call:fail "failed with errorLevel %ERRORLEVEL%"
        goto TEST6
    )
    if not "%JAVA_HOME%" == "C:\Program Files\Java\jre-10.0.1" (
        call:fail "failed to get compatible java from mock registry"
        goto TEST6
    )
    call:pass

:TEST6
    set testing=parse_java_version
    set test=6a
    set cmd=foo.exe
    call:info "%cmd%"
    call %checkJavaVersion% :%testing% "%cmd%"
    if errorLevel 1 (
        call:pass
    ) else (
        call:fail "foo.exe is not expected to be a Java executable"
        goto TEST7
    )

    set test=6b
    set cmd=java.exe
    call:info "%cmd%"
    call %checkJavaVersion% :%testing% "%cmd%"
    if errorLevel 1 (
        call:fail "failed to parse output from java.exe -version"
        goto TEST7
    ) else (
        call:pass
    )

:TEST7
    set testing=CheckJAVA_HOME
    set test=7
    call:info
    set REQUIRED_JAVA_VER=1.8
    set PATH=
    rem Either this points to an unexisting file, or it exists in which case the returned version should be 1.8.0_102
    set "JAVA_HOME=C:\Program Files (x86)\Java\jre1.8.0_102"
    call %checkJavaVersion% :%testing%
    if exist "%JAVA_HOME%" (
        if errorLevel 1 (
            call:fail "failed with errorLevel %ERRORLEVEL%"
            goto EXIT
        )
        if not "%JAVA_VER%" == "1.8.0_102" (
            call:fail "failed to get correct java version, output: ""%JAVA_VER%"""
            goto EXIT
        )
        call:pass
    ) else (
        if errorLevel 1 (
            call:pass
        ) else (
            call:fail "no compatible version of Java was expected to be found, output: ""%JAVA_VER%"""
            goto EXIT
        )
    )

:EXIT
    exit /b 1
