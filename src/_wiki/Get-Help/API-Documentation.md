# API Documentation

## Web Service API

If you want to use DAISY Pipeline 2 as a web server and implement your
own client, you need to know about the [web service API](WebServiceAPI).

## Java Client Library

We provide a Java HTTP client library for communicating with a
Pipeline server. To use it in your Maven project include the following
dependencies:

~~~xml
<dependency>
  <groupId>org.daisy.pipeline</groupId>
  <artifactId>clientlib-java</artifactId>
  <version>4.8.2</version>
</dependency>
<dependency>
  <groupId>org.daisy.pipeline</groupId>
  <artifactId>clientlib-java-httpclient</artifactId>
  <version>2.0.3</version>
</dependency>
~~~
  
The entry point of the library is the class
[`org.daisy.pipeline.client.http.WS`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/http/WS.html).

## Go Client Library

We also provide a HTTP client library for the Go language. Run the
following command to download and install the "pipeline" package:

~~~sh
go get github.com/daisy/pipeline-clientlib-go
~~~

## Java API

DAISY Pipeline 2 may also be used directly as a Java library instead
of via client-server communication.

These are the most important classes in the Java API:

[`org.daisy.pipeline.script.ScriptRegistry`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/script/ScriptRegistry.html)
: List available scripts.

[`org.daisy.pipeline.script.XProcScript`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/script/XProcScript.html)
: List available inputs, options and outputs of a script.

[`org.daisy.pipeline.datatypes.DatatypeRegistry`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/datatypes/DatatypeRegistry.html)
: Get data types of script options.

[`org.daisy.pipeline.script.BoundXProcScript`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/script/BoundXProcScript.html)
[`org.daisy.common.xproc.XProcInput`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/XProcInput.html)
: Specify job inputs/options.

[`org.daisy.pipeline.job.JobManagerFactory`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/job/JobManagerFactory.html)
[`org.daisy.pipeline.job.JobManager`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/job/JobManager.html)
: Create, delete and queue jobs.

[`org.daisy.pipeline.job.Job`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/job/Job.html)
: Run and monitor jobs and access job results.

[`org.daisy.common.messaging.MessageAccessor`](http://daisy.github.io/pipeline/api/org/daisy/common/messaging/MessageAccessor.html)
: Access job messages.

<!--
FIXME: should not be in API but is currently needed to create a JobManager
[`org.daisy.pipeline.clients.ClientStorage`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/clients/ClientStorage.html)
: Create, delete and access clients.
-->

The `ScriptRegistry`, `DatatypeRegistry` and `JobManagerFactory`
objects are OSGi services that can be injected as follows:

~~~java
import org.daisy.pipeline.script.ScriptRegistry;

import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

...

@Reference(
    name = "ScriptRegistry",
    service = ScriptRegistry.class,
    cardinality = ReferenceCardinality.MANDATORY,
    policy = ReferencePolicy.STATIC
)
public void setScriptRegistry(ScriptRegistry registry) {
    ...
}
~~~

## Complete Javadocs

<!--
FIXME: add short description for every package
-->

- [`org.daisy.common.fuzzy`](http://daisy.github.io/pipeline/api/org/daisy/common/fuzzy/package-summary.html)
- [`org.daisy.common.messaging`](http://daisy.github.io/pipeline/api/org/daisy/common/messaging/package-summary.html)
- [`org.daisy.common.priority`](http://daisy.github.io/pipeline/api/org/daisy/common/priority/package-summary.html)
- [`org.daisy.common.properties`](http://daisy.github.io/pipeline/api/org/daisy/common/properties/package-summary.html)
- [`org.daisy.common.shell`](http://daisy.github.io/pipeline/api/org/daisy/common/shell/package-summary.html)
- [`org.daisy.common.stax`](http://daisy.github.io/pipeline/api/org/daisy/common/stax/package-summary.html)
- [`org.daisy.common.transform`](http://daisy.github.io/pipeline/api/org/daisy/common/transform/package-summary.html)
- [`org.daisy.common.xproc`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/package-summary.html)
- [`org.daisy.common.xslt`](http://daisy.github.io/pipeline/api/org/daisy/common/xslt/package-summary.html)
- [`org.daisy.common.zip`](http://daisy.github.io/pipeline/api/org/daisy/common/zip/package-summary.html)
- [`org.daisy.pipeline.client.filestorage`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/filestorage/package-summary.html)
- [`org.daisy.pipeline.client.http`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/http/package-summary.html)
- [`org.daisy.pipeline.client.models`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/models/package-summary.html)
- [`org.daisy.pipeline.client.models.datatypes`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/models/datatypes/package-summary.html)
- [`org.daisy.pipeline.client.utils`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/utils/package-summary.html)
- [`org.daisy.pipeline.client`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/package-summary.html)
- [`org.daisy.pipeline.clients`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/clients/package-summary.html)
- [`org.daisy.pipeline.datatypes`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/datatypes/package-summary.html)
- [`org.daisy.pipeline.event`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/event/package-summary.html)
- [`org.daisy.pipeline.job`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/job/package-summary.html)
- [`org.daisy.pipeline.modules`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/package-summary.html)
- [`org.daisy.pipeline.script`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/script/package-summary.html)
