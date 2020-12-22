# Configuration Files

## Logback

With the file `config-logback.xml` you can control most of the
logging: to which files log messages are written, log rolling, the
level of details that are logged, which log messages show up in the
user interface, etc. See
[Logback configuration](http://logback.qos.ch/manual/configuration.html)
for help on the format. The location of this file is specified in
[`system.properties`](#system-properties).

## System properties

All of the properties below (including the TTS properties) can also be
set through environment variables. For example, the system property
`org.daisy.pipeline.ws.host` can be set with the environment variable
`PIPELINE2_WS_HOST`. The environment variable settings will have
precedence over settings in the `system.properties` file.


`org.daisy.pipeline.procs`
: Maximum allowed number of jobs running simultaneously
: **Allowed values**: A positive number
: **Required**: No
: **Default**: "2"

`org.daisy.pipeline.ws.host`
: Host address for the web service
: **Allowed values**: A host address
: **Required**: No
: **Default**: "localhost"

`org.daisy.pipeline.ws.port`
: Port number for the web service
: **Allowed values**: Any available port, from 0 to 65535
: **Required**: No
: **Default**: "8181"

`org.daisy.pipeline.ws.path`
: Path for the web service
: **Allowed values**: Any URI path fragment
: **Required**: No
: **Default**: "/ws"

`org.daisy.pipeline.ws.localfs`
: Whether to allow local filesystem interaction when the client is
  running on the same machine as the server
: **Allowed values**: "true" or "false"
: **Required**: No
: **Default**: "true"
: May be overwritten by the
  [`local`](Pipeline-as-Service#arguments-for-pipeline2-executable) ("true")
  and [`remote`](Pipeline-as-Service#arguments-for-pipeline2-executable) ("false")
  arguments

`org.daisy.pipeline.ws.authentication`
: Whether the web service requires authentication
: **Allowed values**: "true" or "false"
: **Required**: No
: **Default**: "false"
: May be overwritten by the
  [`local`](Pipeline-as-Service#arguments-for-pipeline2-executable) ("false")
  and [`remote`](Pipeline-as-Service#arguments-for-pipeline2-executable) ("true")
  arguments

`org.daisy.pipeline.ws.authentication.key`
: Initial admin's authentication ID
: **Allowed values**: A non-empty string
: **Required**: Yes, if `org.daisy.pipeline.ws.authentication` is set

`org.daisy.pipeline.ws.authentication.secret`
: Initial admin's authentication secret
: **Allowed values**: A non-empty string
: **Required**: Yes, if `org.daisy.pipeline.ws.authentication` is set

`org.daisy.pipeline.ws.ssl`
: Makes the web service use the secure socket layer
: **Allowed values**: "true" or "false"
: **Required**: No
: **Default**: "false"

`org.daisy.pipeline.ws.ssl.keystore`
: Path to the SSL keystore file
: **Allowed values**: Local file path
: **Required**: Yes, if `org.daisy.pipeline.ws.ssl` is set

`org.daisy.pipeline.ws.ssl.keystorepassword`
: Keystore file password
: **Allowed values**: A non-empty string
: **Required**: Yes, if `org.daisy.pipeline.ws.ssl` is set

`org.daisy.pipeline.ws.ssl.keypassword`
: Key password
: **Allowed values**: A non-empty string
: **Required**: Yes, if `org.daisy.pipeline.ws.ssl` is set

`org.daisy.pipeline.ws.maxrequesttime`
: Maximum amount of time (in ms) that a web service request is considered valid
: **Allowed values**: A positive long number
: **Required**: No
: **Default**: "600000" (10 minutes)

`org.daisy.pipeline.ws.tmpdir`
: Path to a writable temporary directory
: **Allowed values**: Local directory path
: **Required**: No
: **Default**: "${java.io.tmpdir}" or "/tmp"

`org.daisy.pipeline.log.level`
: Disable job messages below this level
: **Default**: "INFO"

`org.daisy.pipeline.updater.updateSite`
: URL of the update service
: **Required**: No
: **Default**: "http://daisy.github.io/pipeline-assembly/releases/"

`org.daisy.pipeline.braille.liblouis.external`
: Whether to use the Liblouis library present on the system instead of the embedded version
: **Allowed values**: "true" or "false"
: **Required**: No
: **Default**: "false"

### TTS configuration

A number of system properties are specific to text-to-speech. These
properties are documented
[here](http://daisy.github.io/pipeline/Get-Help/User-Guide/Text-To-Speech/).

<!--

The following are used in persistence-mysql but persistence-mysql is not included

`org.daisy.pipeline.persistence.url`
: Database connection URL
: **Allowed values**: A JDBC url
: **Required**: No
: **Default**: "jdbc:mysql://localhost:3306/daisy_pipeline"

`org.daisy.pipeline.persistence.user`
: Database user
: **Allowed values**: A non-empty string
: **Required**: Yes

`org.daisy.pipeline.persistence.password`
: Database password
: **Allowed values**: A non-empty string
: **Required**: Yes


The following should not be edited

`org.daisy.pipeline.data`
: Path to a writeable directory for storing program data
: Automatically set based on the platform and the
  [`PIPELINE2_DATA`](Pipeline-as-Service#environment-variables)
  environment variable

`org.daisy.pipeline.iobase`
: Path to a writable directory for storing job data
: **Default**: "${org.daisy.pipeline.data}/jobs"

`org.daisy.pipeline.logdir`
: Path to a writeable directory for storing global logging info
: Property is used in [`config-logback.xml`](#logback)
: **Default**: "/var/log/daisy-pipeline2" (on Debian/Ubuntu) or
  "${org.daisy.pipeline.home}/log/" (on other platforms)

`derby.stream.error.file`
: Path to a writeable file for storing Derby specific logging info
: **Default**: "/var/log/daisy-pipeline2/derby.log" (on Debian/Ubuntu)
  or "${org.daisy.pipeline.home}/log/derby.log" (on other platforms)

`logback.configurationFile`
: Path (file URI) to Logback configuration file (see [`config-logback.xml`](#logback))
: **Default**: "file:/etc/opt/daisy-pipeline2/config-logback.xml" (on Debian/Ubuntu) or
  "file:${org.daisy.pipeline.home}/etc/config-logback.xml" (on other platforms)

`org.daisy.pipeline.updater.bin`
: **Default**: "${org.daisy.pipeline.home}/updater/${pipeline.updater}"

`org.daisy.pipeline.updater.deployPath`
: **Default**: "${org.daisy.pipeline.home}/"

`org.daisy.pipeline.updater.releaseDescriptor`
: **Default**: "${org.daisy.pipeline.home}/etc/releaseDescriptor.xml"

`org.daisy.pipeline.xproc.configuration`
: Path to Calabash configuration file
: **Default**: "${org.daisy.pipeline.home}/etc/config-calabash.xml"

`org.daisy.pipeline.messaging.cache`
: Time in seconds that messages are kept in the volatile memory
: **Default**: "60"

-->
