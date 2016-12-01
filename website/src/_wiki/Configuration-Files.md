# Configuration

When using DAISY Pipeline 2 as a service, especially when integrating
it in a custom setup, you probably want to configure a thing or
two. Two files are intended to be edited by you for this
purpose. Where on the file system these files are located depends on
the distribution.

## `config-logback.xml`

With this file you can control the logging: to which files log
messages are written, log rolling, the level of details that are
logged, which log messages show up in the user interface, etc.

## `system.properties`

**This is old information. Needs to be updated!**

<table>
<thead>
<tr>
<th>Property</th>
<th>Description</th>
<th>Allowed values</th>
<th>Default value</th>
<th>Required</th>
</tr>
</thead>
<tbody>
<tr>
<td><code>org.daisy.pipeline.mode</code></td>
<td>Indicates whether the Pipeline will run in web service or command line interface mode</td>
<td>"ws" or "cmd"</td>
<td>None</td>
<td>Yes</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.iobase</code></td>
<td>Path to a writable temporary directory</td>
<td>Local directory path</td>
<td>None</td>
<td>TBD</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.home</code></td>
<td>Path to the framework root directory</td>
<td>Local directory</td>
<td>None (automatically detected)</td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.logdir</code></td>
<td>Path to output the logging info</td>
<td>Local directory path</td>
<td><code>org.daisy.pipeline.home/log/</code></td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.xproc.configuration</code></td>
<td>Extras for calabash configuration</td>
<td>Path to a valid calabash configuration file</td>
<td><code>org.daisy.pipline.home/etc/conf_calabash.xml</code></td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.local</code></td>
<td>Tells if the framework runs on local mode</td>
<td>true or false</td>
<td><code>false</code></td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.port</code></td>
<td>Port number for the web service</td>
<td>Any available port, from 0 to 65535.</td>
<td><code>8181</code> (local mode) or <code>8182</code> (remote mode)</td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.path</code></td>
<td>Path for the web service</td>
<td>Any URI path fragment</td>
<td><code>/ws</code></td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.maxrequesttime</code></td>
<td>Maximum amount of time (in ms) that a web service request is considered valid</td>
<td>Any long number</td>
<td><code>600,000</code> (10 minutes)</td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.tmpdir</code></td>
<td>Path to a writable temporary directory</td>
<td>Local directory path</td>
<td><code>/tmp</code></td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.authentication</code></td>
<td>Indicates whether the web service requires authentication</td>
<td>"true" or "false"</td>
<td><code>true</code></td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.ssl</code></td>
<td>Makes the WS to use the secure socket layer</td>
<td>"true" or "false"</td>
<td><code>false</code></td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.ssl.keystore</code></td>
<td>Path to the keystore file</td>
<td>Local file path</td>
<td>None</td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.ssl.keystorepassword</code></td>
<td>Keystore file password</td>
<td>string</td>
<td>None</td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.ws.ssl.keypassword</code></td>
<td>Key password</td>
<td>string</td>
<td>None</td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.persistence.url</code> (<code>*</code>)(<code>p</code>)</td>
<td>Database connection URL</td>
<td>jdbc urls</td>
<td>None</td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.persistence.user</code> (<code>*</code>)(<code>p</code>)</td>
<td>Database user</td>
<td>string</td>
<td>None</td>
<td>No</td>
</tr>

<tr>
<td><code>org.daisy.pipeline.persistence.password</code> (<code>*</code>)(<code>p</code>)</td>
<td>Database password</td>
<td>string</td>
<td>None</td>
<td>No</td>
</tr>
</tbody>
</table>


- `*` = New feature not available in all builds yet
- `p` = Only when using persistence models which actually require configuration ( namely persistence-mysql )
