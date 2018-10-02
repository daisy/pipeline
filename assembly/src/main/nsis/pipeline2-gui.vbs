' OBJECTS
Set oShell = CreateObject("Wscript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
' VARIANTS
Dim strArgs
Dim exitCode
Dim appDataPath: appDataPath = oShell.ExpandEnvironmentStrings("%APPDATA%") & "\DAISY Pipeline 2"
Dim logDir: logDir = appDataPath & "\log"
Dim logFile: logFile = logDir & "\daisy-pipeline-launch.log"

If (NOT fso.FolderExists(logDir)) Then
    fso.CreateFolder appDataPath
    fso.CreateFolder logDir
    fso.CreateTextFile logFile, false 'false: don't overwrite
End If

' START
strArgs = "cmd.exe /c daisy-pipeline\bin\pipeline2.bat gui >> """ & logFile & """"
exitCode = oShell.Run(strArgs, 0, true)
If exitCode<>0 Then oShell.Run "errorPrompt.vbs " & exitCode
