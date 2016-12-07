---
layout: default
---
# Pipeline as a Service

Most people will use DAISY Pipeline 2 as a standalone desktop
application. It is easy to install and ready to go. But the Pipeline
can also be used as a service to which client programs can connect,
through a web API.

There are several use cases:

- You want to run batch jobs through the
  [command line interface](../Command-Line).
- You want to use our
  [web application]({{site.baseurl}}/wiki/webui/User-Guide/).
- You want to include the Pipeline in a larger setup where it needs to
  talk to other components.

## Installation

Choose a package from the
[downloads page]({{site.baseurl}}/Download.html) that includes the
server, then follow the installation instructions for that
package.

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

### Windows

**TBD**

### Debian/Ubuntu

Three configuration files are available if the Pipeline was installed
via the Debian package:

- `/etc/default/daisy-pipeline2`
- `/etc/opt/daisy-pipeline2/system.properties`
- `/etc/opt/daisy-pipeline2/config-logback.xml`

For the latter two, see
[Configuration]({{site.baseurl}}/wiki/Configuration-Files). The former
supports the following settings:

`REMOTE`
: When "true", run the server in "remote" mode ("true" or
  "false", default is "false"). Has the same effect as setting
  environment variables `PIPELINE2_LOCAL=false` and `PIPELINE2_AUTH=true`.

In addition, all available [environment variables](#environment-variables)
listed below, except for `PIPELINE2_DATA`, can be specified in this file in
the format `export VAR=value`.

### Other distros

#### Environment variables

A number of environment variables will influence the program:

`JAVA`
: Location of the "java" executable. Must be Java 8 or higher.

`JAVA_HOME`
: Location where the Java JRE or JDK is installed. Must be Java 8 or higher.

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

`PIPELINE2_LOCAL`
: Sets the
  [`org.daisy.pipeline.ws.localfs`]({{site.baseurl}}/wiki/Configuration-Files)
  system property ("true" or "false", default is "true"). <!-- does this have precedence over system.properties? -->

`PIPELINE2_AUTH`
: Sets the
  [`org.daisy.pipeline.ws.authentication`]({{site.baseurl}}/wiki/Configuration-Files)
  system property ("true" or "false", default is "false"). <!-- does this have precedence over system.properties? -->

`PIPELINE2_DEBUG`
: When "true", passes debug options to Java ("true" or "false", default is "false").

`PIPELINE2_DATA`
: Directory for storing program data.

<!--
- `PIPELINE2_BASE`: what is this for?
- `PIPELINE2_HOME`: will be ignored?
- `PIPELINE2_CONFIG`: will be ignored?
- `MAX_FD`: ?
-->

#### Arguments for `pipeline2` executable

The following command line arguments are available. Combinations are possible.

`remote`
: Run Pipeline server in "remote" mode. Has the same effect as
  setting `PIPELINE2_LOCAL=false` and `PIPELINE2_AUTH=true`.

`local`
: Run Pipeline server in "local" mode. Has the same effect as setting
  `PIPELINE2_LOCAL=true` and `PIPELINE2_AUTH=false`.

`clean`
: Clean the program data.

`debug`
: Enable debugging. Has the same effect as setting `PIPELINE2_DEBUG=true`.

`shell`
: Enable an interactive shell for low-level system manipulation. Run
  `help` for a list of commands, `help COMMAND` for more info about a
  specific command.
  
<!--
- `gui`: Launch the graphical user interface instead of the web service.
-->

#### Configuration files

See [Configuration]({{site.baseurl}}/wiki/Configuration-Files).
