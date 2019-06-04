# Pipeline as a Service

Most people will use DAISY Pipeline 2 as a standalone desktop
application. It is easy to install and ready to go. But the Pipeline
can also be used as a service to which client programs can connect,
through a [web API](WebServiceAPI).

There are several use cases:

- You want to run batch jobs through the
  [command line interface](Command-Line).
- You want to use our
  [web application](https://github.com/daisy/pipeline-webui/wiki/User-Guide/).
- You want to include the Pipeline in a larger setup where it needs to
  talk to other components.

## Installation

Choose a package from the
[downloads page](http://daisy.github.io/pipeline/Download.html) that
includes the server, then follow the installation instructions for
that package.

## Starting and stopping the service

### Windows

**TBD**

### Debian/Ubuntu

If the Pipeline was installed via the Debian package, the service is
launched automatically on system startup. To start or stop the service
manually, use `service daisy-pipeline2 start|stop|restart|status`.

### Other distros

In order to start the service, execute the file `pipeline2`. Where
exactly this file is located on the file system depends on the
installation. To shut down the service press `Ctrl-C`.

## Configuration

### Configuration files

When using DAISY Pipeline 2 as a service, especially when integrating
it in a custom setup, you probably want to configure a thing or
two. Two files are intended to be edited by you for this purpose:
`system.properties` and `config-logback.xml`. See
[Configuration files](Configuration-Files) for details. Where on the
file system these files are located depends on the installation.

### Windows

**TBD**

### Debian/Ubuntu

If the Pipeline was installed via the Debian package,
`system.properties` and `config-logback.xml` are located in
`/etc/opt/daisy-pipeline2`. In addition there is a third configuration
file available: `/etc/default/daisy-pipeline2`. All available
[environment variables](#environment-variables) listed below, except
for `PIPELINE2_DATA`, can be specified in this file in the format
`export VAR=value`.

### Other distros

#### Environment variables

A number of environment variables will influence the program:

`JAVA`
: Location of the "java" executable. Must be Java 11 or higher.

`JAVA_HOME`
: Location where the Java JRE or JDK is installed. Must be Java 11 or higher.

`JAVA_MIN_MEM`
: Sets the initial size of the heap. See Java's
  [`-Xms` option](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABHDABI).

`JAVA_MAX_MEM`
: Sets the maximum size of the memory allocation pool. See Java's
  [`-Xmx` option](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABHDABI).

`JAVA_PERM_MEM`
: Sets the permanent generation space size. See Java's
  [`-XX:PermSize` option](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABDCEGG).

`JAVA_MAX_PERM_MEM`
: Sets the maximum permanent generation space size. See Java's
  [`-XX:MaxPermSize` option](https://docs.oracle.com/javase/8/docs/technotes/tools/windows/java.html#BABDCEGG).

`JAVA_OPTS`
: Any other Java options.

`JAVA_DEBUG_OPTS`
: Additional Java options to be passed on when `PIPELINE2_DEBUG` is set.

`PIPELINE2_DEBUG`
: When "true", passes debug options to Java ("true" or "false", default is "false").

`PIPELINE2_DATA`
: Directory for storing program data. Defaults to the "data" folder inside the Pipeline installation folder.


<!--
- `PIPELINE2_BASE`: what is this for?
- `PIPELINE2_HOME`: will be ignored?
- `PIPELINE2_CONFIG`: will be ignored?
- `MAX_FD`: ?
-->

In addition to the environment variable listed above, all available
[system properties](Configuration-Files/#system-properties)
that start with `org.daisy.pipeline` can be set through environment
variables as well. For example, the system property
`org.daisy.pipeline.ws.host` can be set with the environment variable
`PIPELINE2_WS_HOST`. The environment variable settings will have
precedence over settings in the `system.properties` file.

#### Arguments for `pipeline2` executable

The following command line arguments are available. Combinations are possible.

`remote`
: Run Pipeline server in "remote" mode. Has the same effect as
  setting `PIPELINE2_WS_LOCALFS=false` and `PIPELINE2_WS_AUTHENTICATION=true`.

`local`
: Run Pipeline server in "local" mode. Has the same effect as setting
  `PIPELINE2_WS_LOCALFS=true` and `PIPELINE2_WS_AUTHENTICATION=false`.

`clean`
: Clean the program data.

`debug`
: Enable debugging. Has the same effect as setting `PIPELINE2_DEBUG=true`.

`shell`
: Enable an interactive shell for low-level system manipulation. Run
  `help` for a list of commands, `help COMMAND` for more info about a
  specific command.
  
- `gui`
: Launch the graphical user interface instead of the web service. See
  also [Desktop Application](http://daisy.github.io/pipeline/Get-Help/User-Guide/Desktop-Application/DAISY-Pipeline-2-User-Guide/).
