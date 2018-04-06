' OBJECTS
Set oShell = CreateObject("Wscript.Shell")
Set fso = CreateObject("Scripting.FileSystemObject")
Set oArgs = WScript.Arguments
' VARIANTS
Dim logPath: logPath = oShell.ExpandEnvironmentStrings("%APPDATA%") & "\DAISY Pipeline 2\log"
' ERROR CODES
Const USER_FIXABLE = "2"
Const FATAL = "3"
Const UNKNOWN = "1" 'or whatever else'

catchErrors oArgs(0)

' PROCEDURES
Sub catchErrors(ByVal exitCode)
    Dim msg
    If (checkLogs) Then
        viewLogs
        Select Case exitCode
            Case USER_FIXABLE
                msg = "DAISY Pipeline 2 was unable to start." & _
                        vbCrlf & vbCrlf & _
                        "We may have a solution for this error. " & vbCrlf & _
                        "Visit troubleshooting?"
                If errorPrompt(msg) Then visitTroubleshooting
            Case FATAL
                msg = "DAISY Pipeline 2 failed to start." & _
                      vbCrlf & vbCrlf & _
                      readLogs & vbCrlf & _
                      "Would you like to report this issue?"
                If errorPrompt(msg) Then reportNewIssue
            Case Else
                msg = "DAISY Pipeline 2 failed." & _
                        vbCrlf & "Error Code: " & exitCode & _
                        vbCrlf & vbCrlf & _
                        "Would you like to report this issue?"
                If errorPrompt(msg) Then reportNewIssue
        End Select
    Else
        msg = "DAISY Pipeline 2 was unable to start." & _
                vbCrlf & vbCrlf & _
                "No launch logs were created." & _
                vbCrlf & vbCrlf & _
                "Would you like to report this issue?"
        If errorPrompt(msg) Then reportNewIssue
    End If
End Sub

Function readLogs()
    Set objFile = fso.OpenTextFile("" & logPath & "\daisy-pipeline-launch.log""", 1)
    Do While Not objFile.AtEndOfStream
        readLogs = readLogs & objFile.ReadLine & vbCrLf 'return'
    Loop
    objFile.Close
End Function

Function errorPrompt(msg)
    Dim response: response = MsgBox(msg, vbYesNo + vbCritical + vbSystemModel, "Error")
    If response=vbYes Then
        errorPrompt = True 'return'
    Else
        errorPrompt = False 'return'
    End If
End Function

Sub reportNewIssue()
    Dim path
    path = "http://daisy.github.io/pipeline/Get-Help/Issue-Tracker.html"
    oShell.Run Path
End Sub

Sub visitTroubleshooting()
    Dim path
    path = "https://daisy.github.io/pipeline/Get-Help/Troubleshooting/Common-Errors-Windows.html"
End Sub

Sub viewLogs()
    Set fso = CreateObject("Scripting.FileSystemObject")
    Dim runCmd: runCmd = "explorer.exe """ & logPath & """" ' escape quotes
    oShell.Run runCmd, 2
End Sub

Function checkLogs()
    If (fso.FolderExists("" & logPath & "")) And (fso.FileExists("" & logPath & "\daisy-pipeline-launch.log" & "")) Then
        checkLogs = True 'return'
    Else
        checkLogs = False 'return'
    End If
End Function
