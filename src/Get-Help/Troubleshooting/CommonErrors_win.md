---
layout: default
---
# Troubleshooting on Windows

If your issue isn't listed here, please visit our [issue tracker](https://daisy.github.io/pipeline/Contribute/#issue-tracker) to report it.

## ERROR: Unable to retrieve JAVA_HOME or JAVA_HOME is not valid

DAISY Pipeline 2 uses the Windows environment variable JAVA_HOME to find your Java installation folder. This error can occur if the JAVA_HOME variable is not set or pointing to an incorrect path. Please follow the steps below to set or correct your JAVA_HOME variable.

### Verify Java
Before following the solution steps, first verify that you have Java installed correctly.

Windows 10: Open the search box (Win+S), type __About Java__, and hit Enter. If nothing comes up, please [install the latest Java version](https://java.com/en/).

Alternatively, open a command prompt window (press Win+R, type __cmd__, and hit Enter) and enter the command: `java -version`. This will output the version of Java installed on your computer. If the command is not recognized, please [install the latest Java version](https://java.com/en/).

### Java Installation Path
You will need the path to your Java installation folder for the next steps, which can be found through File Explorer or Command Prompt.

Unless you specified a different location during Java installation, Java will by default be installed in __C:\Program Files\Java\jre1.8.X_XXX__ or __C:\Program Files (x86)\Java\jre1.8.X_XXX__.
- If you have a Java Development Kit installed, replace __jre1.8.X_XXX__ with __jdk1.8.X_XXX__.
- If you installed Java (or Windows) on another drive, replace the drive letter.

###### Command Prompt
1. Open a command prompt window and enter the command `where java` (Windows 2003+).
2. Press Ctrl+M to enter "mark" mode, highlight the path, and copy it.

###### File Explorer
Open File Explorer (Win+R, type __explorer__, hit Enter), and browse for your Java installation folder. See above for common Java installation locations.

Once you find your Java folder, open to it and copy the path in the navigation bar at the top.

### Setting JAVA_HOME
Users can set or correct their JAVA_HOME variable via one of two methods:

###### Control Panel

1. Open the Control Panel (Win+R or Win+S, type __control panel__, press enter), and navigate to __System and Security > System__.
2. Click on __Advanced system settings__ link in the top left.
3. Click on the __Environment Variables__ button. A new window will pop up.
4. In the bottom pane labeled __System variables__, click the __New...__ button.
5. For the variable name, enter __JAVA_HOME__.
6. For the variable value, paste the path to your [java installation folder](#java-installation-path).

###### Command Prompt

1. Open a Command Prompt window as administrator.
    - Windows 10: press Win+S, type __cmd__, then press Ctrl+Shift+Enter.
    - Or click __Start__, and click __All Programs__. Right-click __Command Prompt__, and then click __Run as administrator__.
2. Enter the command `setx -m "Path"`, where __"Path"__ is the path to your [Java installation directory](#java-installation-path).
