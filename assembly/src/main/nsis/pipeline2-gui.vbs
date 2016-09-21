Set oShell = CreateObject ("Wscript.Shell") 
Dim strArgs
strArgs = "cmd /c pipeline2.bat gui"
oShell.Run strArgs, 0, false
