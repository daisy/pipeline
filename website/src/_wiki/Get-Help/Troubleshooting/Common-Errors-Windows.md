# Troubleshooting on Windows

This article covers the most common errors Windows users may
experience when starting DAISY Pipeline 2. If your issue isn't listed
here, please visit our
[issue tracker](http://daisy.github.io/pipeline/Get-Help/Issue-Tracker.html)
to report it.

## ERROR: Unable to retrieve JAVA_HOME or JAVA_HOME is not valid

DAISY Pipeline 2 uses the Windows environment variable **JAVA\_HOME**
to find your Java installation folder. This error can occur if the
variable is not set or pointing to an incorrect path. Please follow
the steps below to set or correct your **JAVA\_HOME** variable.

### Verify Java

Before following the solution steps, first verify that you have Java
installed correctly.

Windows 10: Open the search box (press **Win&#8862; + S**, type
**About Java**, and hit **Enter**. If nothing comes up, please
[install the latest Java version](https://java.com/en/).

Alternatively, open a command prompt window (press **Win&#8862; + R**,
type **cmd**, and hit **Enter**) and enter the command: `java -version`.
This will output the version of Java installed on your
computer. If the command is not recognized, please
[install the latest Java version](https://java.com/en/).

### Java Installation Path

You will need the path to your Java installation folder for the next
steps, which can be found through File Explorer or Command Prompt.

Your Java installation path will likely resemble: **C:\Program Files\Java\jre1.8.X\_XXX**.

- If you have a Java Development Kit installed, replace
  **jre1.8.X\_XXX** with **jdk1.8.X\_XXX**.

You can find your Java installation path via one of these two methods:

#### Command Prompt (Windows 2003+)

1. Open a command prompt window and enter the command `where java` (Windows 2003+).

2. Press **Ctrl + M** to enter "mark" mode, highlight the path to your
   installation directory (e.g. **jre1.8.X\_XXX**), and copy it.

#### File Explorer

Open File Explorer (**Win&#8862; + R**, type **explorer**, hit
**Enter**), and browse for your Java installation folder. It will
likely be installed in either **Program Files\Java** or **Program
Files (x86)\Java** on your OS drive.

Once you find your Java folder,

1. Open to it.
2. If there is a **JRE** installation folder (e.g. **jre1.8.X\_XXX**), open it.
3. Otherwise, open the **JDK** installation folder (e.g. **jdk1.8.X\_XXX**).
3. Copy the full path in the navigation bar at the top.

### Setting JAVA_HOME

You can now set or correct your **JAVA\_HOME** variable via one of two methods:

#### Command Prompt

1. Open a Command Prompt window as administrator.
   - Windows 10: press **Win&#8862; + S**, type **cmd**, then press
     **Ctrl + Shift + Enter**.
   - Or click **Start**, and click **All Programs**. Right-click
     **Command Prompt**, and then click **Run as administrator**.
2. Enter the command `setx JAVA_HOME -m "Path"`. For **"Path"**, paste
   in your [Java installation path](#java-installation-path) .

#### Control Panel

1. Open the Control Panel (**Win&#8862; + R**, type **Control Panel**,
   press **Enter**), and navigate to **System and Security > System**.
2. Click on **Advanced system settings** link in the top left.
3. Click on the **Environment Variables** button. A new window will pop up.
4. In the bottom pane labeled **System variables**, click the **New...** button.
5. For the variable name, enter **JAVA\_HOME**.
6. For the variable value, paste the path to your [Java installation path](#java-installation-path).

### Verify JAVA_HOME

Confirm that your **JAVA\_HOME** variable is now set correctly.

1. Open a Command Prompt window (**Win&#8862; + R**, type **cmd**, hit
   **Enter**).
2. Enter the command `echo %JAVA_HOME%`. This should output the path
   to your [Java installation folder](#java-installation-path).
   - If it doesn't, your **JAVA\_HOME** variable was not set
     correctly. Please make sure you're using the correct
     [Java installation folder](#java-installation-path), or repeat
     the steps [above](#verify-java).

Once you have verified that your **JAVA\_HOME** is set correctly,
DAISY Pipeline 2 should start correctly. If you experience this
problem again or any others, please visit our
[issue tracker](http://daisy.github.io/pipeline/Get-Help/Issue-Tracker.html).
