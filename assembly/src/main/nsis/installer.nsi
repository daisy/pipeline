# This installs two files, app.exe and logo.ico, creates a start menu shortcut, builds an uninstaller, and
# adds uninstall information to the registry for Add/Remove Programs

;----------------------------------------------------------
;   General Defines
;----------------------------------------------------------
!define APPNAME "DAISY Pipeline 2"
!define VERSION "${project.version}"
!define COMPANYNAME "DAISY Consortium"
!define DESCRIPTION "DAISY Pipeline 2 windows distribution"
!define PRODUCT_WEB_SITE "http://www.daisy.org/pipeline2"
!define PRODUCT_REG_ROOT HKLM
!define PRODUCT_REG_KEY "SOFTWARE\${APPNAME}"
!define PRODUCT_REG_VALUENAME_INSTDIR "InstallDir"
!define PRODUCT_REG_VALUENAME_HOMEDIR "Pipeline2Home"
!define PRODUCT_REG_VALUENAME_STARTMENU "StartMenuGroup"
!define PRODUCT_REG_KEY_UNINST "Software\Microsoft\Windows\CurrentVersion\Uninstall\${APPNAME}"
!define UNINSTALLER_NAME "Uninstall ${APPNAME}"
!define REQUIRED_JAVA_VER "11" ; this must also be set in checkJavaVersion.bat

RequestExecutionLevel admin ;Require admin rights on NT6+ (When UAC is turned on)

;----------------------------------------------------------
;   Installer General Settings
;----------------------------------------------------------
Name "${APPNAME}"
;OutFile "..\debug-installer.exe"
ShowInstDetails show
ShowUnInstDetails show
SetCompressor zlib
InstallDir "$PROGRAMFILES\${APPNAME}"
;----------------------------------------------------------
; Maven properties
;----------------------------------------------------------
!include ..\project.nsh

;----------------------------------------------------------
;   Multi-User settings
;----------------------------------------------------------
!define MULTIUSER_EXECUTIONLEVEL Highest
!define MULTIUSER_INSTALLMODE_INSTDIR "${APPNAME}"
!define MULTIUSER_INSTALLMODE_INSTDIR_REGISTRY_KEY "${PRODUCT_REG_KEY}"
!define MULTIUSER_INSTALLMODE_INSTDIR_REGISTRY_VALUENAME "${PRODUCT_REG_VALUENAME_INSTDIR}"
!include MultiUser.nsh

;----------------------------------------------------------
;   MUI Settings
;----------------------------------------------------------
; --- Includes Modern UI 2 ---
!include "MUI2.nsh"
!define MUI_ABORTWARNING
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"
; --- Registry storage of selected language ---
!define MUI_LANGDLL_ALWAYSSHOW
!define MUI_LANGDLL_REGISTRY_ROOT ${PRODUCT_REG_ROOT}
!define MUI_LANGDLL_REGISTRY_KEY "${PRODUCT_REG_KEY}"
!define MUI_LANGDLL_REGISTRY_VALUENAME "NSIS:Language"
; ---- StartMenu Page Configuration ---
var SMGROUP
!define MUI_STARTMENUPAGE_DEFAULTFOLDER "${APPNAME}"
!define MUI_STARTMENUPAGE_REGISTRY_ROOT ${PRODUCT_REG_ROOT}
!define MUI_STARTMENUPAGE_REGISTRY_KEY "${PRODUCT_REG_KEY}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "${PRODUCT_REG_VALUENAME_STARTMENU}"

;----------------------------------------------------------
;   Headers and Macros
;----------------------------------------------------------
;JRECheck
!include MultiDetailPrint.nsh
;other
!addplugindir .
!addplugindir /x86-ansi ./x86-ansi
!addplugindir /x86-unicode ./x86-unicode
!addplugindir /amd64-unicode ./amd64-unicode
!include LogicLib.nsh
!include "Sections.nsh"
!include "winmessages.nsh"
!include EnvVarUpdate.nsh
!include "x64.nsh"
!include "zipdll.nsh"

;----------------------------------------------------------
;   Installer Pages
;----------------------------------------------------------
; ---- Installer Pages ----
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "license.txt"
!define MUI_PAGE_CUSTOMFUNCTION_SHOW CheckInstDirReg
!insertmacro MUI_PAGE_DIRECTORY
!define MUI_PAGE_CUSTOMFUNCTION_SHOW CheckSMDirReg
!insertmacro MUI_PAGE_STARTMENU Application $SMGROUP
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_LANGUAGE "English"
; ---- Uninstaller Pages ----
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES

Function CheckInstDirReg
  ; Disable the directory chooser if it's an upgrade
  ReadRegStr $R0 ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY}" "${PRODUCT_REG_VALUENAME_INSTDIR}"
  StrCmp $R0 "" donothing
    FindWindow $R0 "#32770" "" $HWNDPARENT
    GetDlgItem $R1 $R0 1019
    EnableWindow $R1 0
    GetDlgItem $R1 $R0 1001
    EnableWindow $R1 0
    GetDlgItem $R0 $HWNDPARENT 1
    System::Call "user32::SetFocus(i R0)"
  donothing:
FunctionEnd

Function CheckSMDirReg
  ; Disable the start menu chooser if it's an upgrade
  ReadRegStr $R0 ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY}" "${PRODUCT_REG_VALUENAME_STARTMENU}"
  StrCmp $R0 "" donothing
    FindWindow $R0 "#32770" "" $HWNDPARENT
    GetDlgItem $R1 $R0 1002
    EnableWindow $R1 0
    GetDlgItem $R1 $R0 1004
    EnableWindow $R1 0
    GetDlgItem $R1 $R0 1005
    EnableWindow $R1 0
    GetDlgItem $R0 $HWNDPARENT 1
    System::Call "user32::SetFocus(i R0)"
  donothing:
FunctionEnd


;----------------------------------------------------------
;  Admin check
;----------------------------------------------------------
!macro VerifyUserIsAdmin
UserInfo::GetAccountType
pop $0
${If} $0 != "admin" ;Require admin rights on NT4+
        messageBox mb_iconstop "Administrator rights required!"
        setErrorLevel 740 ;ERROR_ELEVATION_REQUIRED
        quit
${EndIf}
!macroend

###########################################################
###               Installer Sections                    ###
###########################################################

InstType "Default"
InstType /COMPONENTSONLYONCUSTOM
;----------------------------------------------------------
;   Initialization Callback
;----------------------------------------------------------



function .onInit
        Var /GLOBAL CHECK_JRE
	setShellVarContext all
        ${GetParameters} $R0
        ${GetOptions} $R0 "--check-jre=" $R1
        StrCpy $CHECK_JRE $R1

	!insertmacro VerifyUserIsAdmin
	; check the user priviledges
	!insertmacro MULTIUSER_INIT
functionEnd

;----------------------------------------------------------
;   Main Section
;----------------------------------------------------------

section -Main SEC01

	# Remove previous versions
	DetailPrint "Removing old files..."
	rmDir /r "$INSTDIR\daisy-pipeline"
	ReadEnvStr $0 APPDATA
	IfFileExists "$0\DAISY Pipeline 2" +1 +2
	rmDir /r "$0\DAISY Pipeline 2"

	setOutPath $INSTDIR
	SetOverwrite on
	file .\logo.ico

	writeUninstaller "$INSTDIR\uninstall.exe"
	#setOutPath "$INSTDIR\${PROJECT_ARTIFACT_ID}"

	# Copy the whole daisy-pipeline dir
	file /r "${PROJECT_BUILD_DIR}\assembly-${VERSION}-win\daisy-pipeline"
	file .\pipeline2-gui.vbs
	file .\pipeline2-webservice.vbs
	file .\errorPrompt.vbs

	###############
	# Registry information for add/remove programs
	###############

	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "DisplayName" "${APPNAME}"
	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "UninstallString" "$\"$INSTDIR\uninstall.exe$\""
	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "QuietUninstallString" "$\"$INSTDIR\uninstall.exe$\" /S"
	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "InstallLocation" "$\"$INSTDIR$\""
	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "DisplayIcon" "$\"$INSTDIR\logo.ico$\""
	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "Publisher" "${COMPANYNAME}"
	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "DisplayVersion" "${VERSION}"
	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "URLInfoAbout" "${PRODUCT_WEB_SITE}"
	# There is no option for modifying or repairing the install
	WriteRegDWORD ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "NoModify" 1
	WriteRegDWORD ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}" "NoRepair" 1
	#pipelinehome registry entry
	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY}" "${PRODUCT_REG_VALUENAME_INSTDIR}" $INSTDIR
	WriteRegStr ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY}" "${PRODUCT_REG_VALUENAME_HOMEDIR}" "$INSTDIR\daisy-pipeline"
	#Update path env variable

	${EnvVarUpdate} $0 "PATH" "A" "HKLM" "$INSTDIR\daisy-pipeline\cli"
	${EnvVarUpdate} $0 "PATH" "A" "HKLM" "$INSTDIR\daisy-pipeline\bin"
	; make sure windows knows about the change
	SendMessage ${HWND_BROADCAST} ${WM_WININICHANGE} 0 "STR:Environment" /TIMEOUT=5000
sectionEnd

;----------------------------------------------------------
;   JRE Check
;----------------------------------------------------------

Section -JRECheck SEC00-1
  CheckJavaVersion:
    ${if} $CHECK_JRE == "FALSE"
        goto Exit
    ${endIf}

    nsExec::ExecToStack '"$INSTDIR\daisy-pipeline\bin\checkJavaVersion.bat" ${REQUIRED_JAVA_VER}'
    pop $0 ;exitCode
    pop $1 ;output
    ;print output
    push $1
    Call MultiDetailPrint

    IntCmp $0 0 Exit InstallJava InstallJava

    InstallJava:
          ClearErrors

          Var /GLOBAL BITS
          ${If} ${RunningX64}
          StrCpy $BITS "64"
          ${Else}
          StrCpy $BITS "32"
          ${EndIf}

          messageBox MB_YESNO "Java was not found, or the versions found are outdated. $\n$\nDAISY Pipeline 2 needs at least Java ${REQUIRED_JAVA_VER}. Would you like DAISY Pipeline 2 to install the latest version of Java?" IDNO NoAutoInstall
          messageBox MB_OKCANCEL "DAISY Pipeline 2 will now download and install Java ${REQUIRED_JAVA_VER}." IDCANCEL NoAutoInstall
          ${If} ${RunningX64}
          inetc::get "http://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.2%2B9/OpenJDK11U-jre_x64_windows_hotspot_11.0.2_9.zip" "$INSTDIR\jdk-11.0.2+9-jre.zip"
          ${Else}
          inetc::get "http://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.2%2B9/OpenJDK11U-jre_x86-32_windows_hotspot_11.0.2_9.zip" "$INSTDIR\jdk-11.0.2+9-jre.zip"
          ${EndIf}
          !insertmacro ZIPDLL_EXTRACT "$INSTDIR\jdk-11.0.2+9-jre.zip" "$INSTDIR\daisy-pipeline" "<ALL>"
          rename $INSTDIR\daisy-pipeline\jdk-11.0.2+9-jre $INSTDIR\daisy-pipeline\jre
          delete "$INSTDIR\jdk-11.0.2+9-jre.zip"
          goto TryAgain

      NoAutoInstall:
          messageBox MB_YESNO "Would you like to get instructions on how to install Java manually?" IDNO NoJava
          MessageBox MB_OK "You will now be redirected to the Java ${REQUIRED_JAVA_VER} downloads page. $\n$\nPlease choose the version $\"OpenJDK 11 (LTS)$\", choose the VM $\"HotSpot$\", choose the platform $\"Windows x$BITS$\", and click $\"Download JRE$\". Then extract the downloaded ZIP file to a directory that will not move or be deleted. Finally make sure your system can find this version of Java by modifying your $\"PATH$\" or $\"JAVA_HOME$\" environment variable."
          ExecShell "open" "https://adoptopenjdk.net/releases.html?variant=openjdk11&jvmVariant=hotspot#x$BITS_win"
          MessageBox MB_YESNO "Please choose the version $\"OpenJDK 11 (LTS)$\", choose the VM $\"HotSpot$\", choose the platform $\"Windows x$BITS$\", and click $\"Download JRE$\". Then extract the downloaded ZIP file to a directory that will not move or be deleted. Finally make sure your system can find this version of Java by modifying your $\"PATH$\" or $\"JAVA_HOME$\" environment variable. $\n$\nWould you like additional instructions? " IDNO Wait
          ExecShell "open" "https://adoptopenjdk.net/installation.html?variant=openjdk11&jvmVariant=hotspot#x$BITS_win-jre"

      Wait:
          MessageBox MB_OK "Once Java ${REQUIRED_JAVA_VER} has been installed, click OK to resume DAISY Pipeline 2 installation. " IDOK TryAgain
          IfErrors 0 Exit
          messageBox mb_iconstop "Java installation returned an error. Please contact the DAISY Pipeline 2 developing team."
          setErrorLevel 740 ;ERROR_ELEVATION_REQUIRED
          quit

      TryAgain:
          goto CheckJavaVersion

      NoJava:
          MessageBox MB_OK "Not installing Java. DAISY Pipeline 2 will fail to launch. $\n$\nPlease install Java version ≥ ${REQUIRED_JAVA_VER}. It is possible that Java ${REQUIRED_JAVA_VER} is already installed, but that DAISY Pipeline 2 can not locate it. In order to fix this, set the JAVA_HOME environment variable. For more information go to http://daisy.github.io/pipeline/Get-Help/Troubleshooting/Common-Errors-Windows/#setting-java_home."

      Exit:
SectionEnd

;----------------------------------------------------------
;   Start Menu
;----------------------------------------------------------
Section -StartMenu
	SetOutPath $INSTDIR
	!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
	createDirectory "$SMPROGRAMS\${APPNAME}"
	createShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk" "$INSTDIR\pipeline2-gui.vbs" "" "$INSTDIR\logo.ico"
	createShortCut "$SMPROGRAMS\${APPNAME}\Pipeline Updater.lnk" "$INSTDIR\daisy-pipeline\bin\pipeline-updater-gui.exe" "" "$INSTDIR\logo.ico"
	#createShortCut "$SMPROGRAMS\${APPNAME}\${APPNAME}-webservice.lnk" "$INSTDIR\pipeline2-webservice.vbs" "" "$INSTDIR\logo.ico"
	CreateShortCut "$SMPROGRAMS\${APPNAME}\uninstall.lnk" "$INSTDIR\uninstall.exe"
	!insertmacro MUI_STARTMENU_WRITE_END
SectionEnd


;----------------------------------------------------------
;   Uninstaller
;----------------------------------------------------------

function un.onInit
	SetShellVarContext all
	!insertmacro VerifyUserIsAdmin
functionEnd

section "uninstall"

	# Remove Start Menu launcher
	delete "$SMPROGRAMS\${APPNAME}\${APPNAME}.lnk"
	delete "$SMPROGRAMS\${APPNAME}\Pipeline Updater.lnk"

	# Remove files
	rmDir /r "$INSTDIR\daisy-pipeline"
	delete "$INSTDIR\pipeline2-gui.vbs"
	delete "$INSTDIR\pipeline2-webservice.vbs"
	delete "$INSTDIR\errorPrompt.vbs"
	#Remove data dir
	ReadEnvStr $0 APPDATA
	rmDir /r "$0\DAISY Pipeline 2"

	#delete conf file
	delete $INSTDIR\application.conf
	#delete logo
	delete $INSTDIR\logo.ico

	# Always delete uninstaller as the last action
	delete "$SMPROGRAMS\${APPNAME}\uninstall.lnk"
	rmDir "$SMPROGRAMS\${APPNAME}"
	delete $INSTDIR\uninstall.exe

	# Try to remove the install directory - this will only happen if it is empty
	rmDir $INSTDIR

	# Remove uninstaller information from the registry
	DeleteRegKey ${PRODUCT_REG_ROOT} "${PRODUCT_REG_KEY_UNINST}${COMPANYNAME} ${APPNAME}"
	#Remove pipeline home
	DeleteRegKey ${PRODUCT_REG_ROOT} "Software\${APPNAME}"
	#Remove cli for the path

	${un.EnvVarUpdate} $0 "PATH" "R" "HKLM" "$INSTDIR\daisy-pipeline\cli"
	${un.EnvVarUpdate} $0 "PATH" "R" "HKLM" "$INSTDIR\daisy-pipeline\bin"
sectionEnd
