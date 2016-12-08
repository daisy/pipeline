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

`org.daisy.pipeline.procs`
: Maximum allowed number of jobs running simultaneously
: **Allowed values**: A positive number
: **Initial setting**: "2"
: **Required**: No, defaults to "2"

`org.daisy.pipeline.ws.host`
: Host address for the web service
: **Allowed values**: A host address
: **Initial setting**: "localhost"
: **Required**: No, defaults to "localhost"

`org.daisy.pipeline.ws.port`
: Port number for the web service
: **Allowed values**: Any available port, from 0 to 65535
: **Required**: No, defaults to "8181"

`org.daisy.pipeline.ws.path`
: Path for the web service
: **Allowed values**: Any URI path fragment
: **Required**: No, defaults to "/ws"

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
: **Required**: No, defaults to "false"

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
: **Required**: No, defaults to "600000" (10 minutes)

`org.daisy.pipeline.ws.tmpdir`
: Path to a writable temporary directory
: **Allowed values**: Local directory path
: **Required**: No, defaults to "${java.io.tmpdir}" or "/tmp"

`org.daisy.pipeline.liblouis.external`
: Whether to use the Liblouis library present on the system instead of the embedded version
: **Allowed values**: "true" or "false"
: **Required**: No, defaults to "false"

`tts.config`
: File to load TTS configuration properties from at start-up
: **Allowed values**: Local file path
: **Required**: No

`espeak.path`
: Path to eSpeak executable
: **Allowed values**: Local file path
: **Required**: Yes, if you wish to use the eSpeak TTS engine and the
  executable can not be found in one of the directories specified by
  the environment variable "PATH"

`osxspeech.path`
: Alternative path to OSX's command line program "say"
: **Allowed values**: Local file path
: **Required**: No, defaults to "/usr/bin/say"

<!--
`att.bin.priority`
`att.servers`
`att.client.path`
-->

<!--
`host.protection`
-->

<!-- The following are used in persistence-mysql but persistence-mysql is not included -->

<!--
`org.daisy.pipeline.persistence.url`
: Database connection URL
: **Allowed values**: A JDBC url
: **Initial setting**: "jdbc:mysql://localhost:3306/daisy_pipeline"
: **Required**: Yes

`org.daisy.pipeline.persistence.user`
: Database user
: **Allowed values**: A non-empty string
: **Required**: Yes

`org.daisy.pipeline.persistence.password`
: Database password
: **Allowed values**: A non-empty string
: **Required**: Yes
-->

---

The following properties should not be edited! <!-- FIXME: move them to a different file -->

`org.daisy.pipeline.home`
: Automatically set to the program's root directory

`org.daisy.pipeline.version`
: The program's version number

`org.daisy.pipeline.data`
: Path to a writeable directory for storing program data
: Automatically set based on the platform and the
  [`PIPELINE2_DATA`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Pipeline-as-Service#environment-variables)
  environment variable

`org.daisy.pipeline.ws.localfs`
: Whether or not to allow local filesystem interaction when the client is running on the same machine as the server
: Automatically set based on the
  [`PIPELINE2_LOCAL`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Pipeline-as-Service#environment-variables)
  environment variable and the
  [`local`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Pipeline-as-Service#arguments-for-pipeline2-executable)
  argument

`org.daisy.pipeline.ws.authentication`
: Whether or not the web service requires authentication
: Automatically set based on the
  [`PIPELINE2_AUTH`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Pipeline-as-Service#environment-variables)
  environment variable and the
  [`remote`](http://daisy.github.io/pipeline/Get-Help/User-Guide/Pipeline-as-Service#arguments-for-pipeline2-executable)
  argument

`org.daisy.pipeline.iobase`
: Path to a writable directory for storing job data
: **Set to**: "${org.daisy.pipeline.data}/jobs"

`org.daisy.pipeline.logdir`
: Path to a writeable directory for storing global logging info
: Property is used in [`config-logback.xml`](#logback)
: **Set to**: "/var/log/daisy-pipeline2" (on Debian/Ubuntu) or
  "${org.daisy.pipeline.home}/log/" (on other platforms)

`derby.stream.error.file`
: Path to a writeable file for storing Derby specific logging info
: **Set to**: "/var/log/daisy-pipeline2/derby.log" (on Debian/Ubuntu)
  or "${org.daisy.pipeline.home}/log/derby.log" (on other platforms)

`logback.configurationFile`
: Path (file URI) to Logback configuration file (see [`config-logback.xml`](#logback))
: **Set to**: "file:/etc/opt/daisy-pipeline2/config-logback.xml" (on Debian/Ubuntu) or
  "file:${org.daisy.pipeline.home}/etc/config-logback.xml" (on other platforms)

`org.pipeline.updater.bin`
: **Set to**: "${org.daisy.pipeline.home}/updater/${pipeline.updater}"

`org.pipeline.updater.deployPath`
: **Set to**: "${org.daisy.pipeline.home}/"

`org.pipeline.updater.releaseDescriptor`
: **Set to**: "${org.daisy.pipeline.home}/etc/releaseDescriptor.xml"

`org.pipeline.updater.updateSite`
: **Set to**: "http://daisy.github.io/pipeline-assembly/releases/"

`org.daisy.pipeline.xproc.configuration`
: Path to Calabash configuration file
: **Set to**: "${org.daisy.pipeline.home}/etc/config-calabash.xml"

`java.awt.headless`
: **Set to**: "true"

`com.xmlcalabash.config.user`
: **Set to**: ""

`file.encoding`
: **Set to**: "UTF8"

`org.ops4j.pax.logging.DefaultServiceLog.level` <!-- what is this for? -->
: **Set to**: "WARN"

`org.ops4j.pax.logging.service.frameworkEventsLogLevel` <!-- what is this for? -->
: **Set to**: "TRACE"

<!--
`org.daisy.pipeline.base`
-->
