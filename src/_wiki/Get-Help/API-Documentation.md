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
- [`org.daisy.pipeline.braille.common`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/package-summary.html)
- [`org.daisy.pipeline.braille.common.calabash`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/calabash/package-summary.html)
- [`org.daisy.pipeline.braille.common.saxon`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/saxon/package-summary.html)
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

## XProc Utility Libraries

- [`http://www.daisy.org/pipeline/modules/asciimath-utils/library.xpl`](http://daisy.github.io/pipeline/modules/asciimath-utils/xprocdoc/org/daisy/pipeline/modules/asciimath-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/library.xpl`](http://daisy.github.io/pipeline/modules/braille/dtbook-to-pef/xprocdoc/org/daisy/pipeline/modules/braille/dtbook-to-pef/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/epub3-to-pef/library.xpl`](http://daisy.github.io/pipeline/modules/braille/epub3-to-pef/xprocdoc/org/daisy/pipeline/modules/braille/epub3-to-pef/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/html-to-pef/library.xpl`](http://daisy.github.io/pipeline/modules/braille/html-to-pef/xprocdoc/org/daisy/pipeline/modules/braille/html-to-pef/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/xml-to-pef/library.xpl`](http://daisy.github.io/pipeline/modules/braille/xml-to-pef/xprocdoc/org/daisy/pipeline/modules/braille/xml-to-pef/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/zedai-to-pef/library.xpl`](http://daisy.github.io/pipeline/modules/braille/zedai-to-pef/xprocdoc/org/daisy/pipeline/modules/braille/zedai-to-pef/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl`](http://daisy.github.io/pipeline/modules/braille/common-utils/xprocdoc/org/daisy/pipeline/modules/braille/common-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl`](http://daisy.github.io/pipeline/modules/braille/css-utils/xprocdoc/org/daisy/pipeline/modules/braille/css-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl`](http://daisy.github.io/pipeline/modules/braille/dotify-utils/xprocdoc/org/daisy/pipeline/modules/braille/dotify-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/liblouis-utils/library.xpl`](http://daisy.github.io/pipeline/modules/braille/liblouis-utils/xprocdoc/org/daisy/pipeline/modules/braille/liblouis-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/obfl-utils/library.xpl`](http://daisy.github.io/pipeline/modules/braille/obfl-utils/xprocdoc/org/daisy/pipeline/modules/braille/obfl-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl`](http://daisy.github.io/pipeline/modules/braille/pef-utils/xprocdoc/org/daisy/pipeline/modules/braille/pef-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/file-utils/library.xpl`](http://daisy.github.io/pipeline/modules/file-utils/xprocdoc/org/daisy/pipeline/modules/file-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/common-utils/library.xpl`](http://daisy.github.io/pipeline/modules/common-utils/xprocdoc/org/daisy/pipeline/modules/common-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/css-speech/library.xpl`](http://daisy.github.io/pipeline/modules/css-speech/xprocdoc/org/daisy/pipeline/modules/css-speech/library.xpl)
- [`http://www.daisy.org/pipeline/modules/daisy202-utils/library.xpl`](http://daisy.github.io/pipeline/modules/daisy202-utils/xprocdoc/org/daisy/pipeline/modules/daisy202-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/daisy202-validator/library.xpl`](http://daisy.github.io/pipeline/modules/daisy202-validator/xprocdoc/org/daisy/pipeline/modules/daisy202-validator/library.xpl)
- [`http://www.daisy.org/pipeline/modules/daisy3-to-daisy202/library.xpl`](http://daisy.github.io/pipeline/modules/daisy3-to-daisy202/xprocdoc/org/daisy/pipeline/modules/daisy3-to-daisy202/library.xpl)
- [`http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl`](http://daisy.github.io/pipeline/modules/daisy3-utils/xprocdoc/org/daisy/pipeline/modules/daisy3-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-break-detection/library.xpl`](http://daisy.github.io/pipeline/modules/dtbook-break-detection/xprocdoc/org/daisy/pipeline/modules/dtbook-break-detection/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-to-daisy3/library.xpl`](http://daisy.github.io/pipeline/modules/dtbook-to-daisy3/xprocdoc/org/daisy/pipeline/modules/dtbook-to-daisy3/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-to-odt/library.xpl`](http://daisy.github.io/pipeline/modules/dtbook-to-odt/xprocdoc/org/daisy/pipeline/modules/dtbook-to-odt/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-to-ssml/library.xpl`](http://daisy.github.io/pipeline/modules/dtbook-to-ssml/xprocdoc/org/daisy/pipeline/modules/dtbook-to-ssml/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl`](http://daisy.github.io/pipeline/modules/dtbook-to-zedai/xprocdoc/org/daisy/pipeline/modules/dtbook-to-zedai/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-tts/library.xpl`](http://daisy.github.io/pipeline/modules/dtbook-tts/xprocdoc/org/daisy/pipeline/modules/dtbook-tts/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl`](http://daisy.github.io/pipeline/modules/dtbook-utils/xprocdoc/org/daisy/pipeline/modules/dtbook-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-validator/library.xpl`](http://daisy.github.io/pipeline/modules/dtbook-validator/xprocdoc/org/daisy/pipeline/modules/dtbook-validator/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-nav-utils/library.xpl`](http://daisy.github.io/pipeline/modules/epub3-nav-utils/xprocdoc/org/daisy/pipeline/modules/epub3-nav-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-ocf-utils/library.xpl`](http://daisy.github.io/pipeline/modules/epub3-ocf-utils/xprocdoc/org/daisy/pipeline/modules/epub3-ocf-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-pub-utils/library.xpl`](http://daisy.github.io/pipeline/modules/epub3-pub-utils/xprocdoc/org/daisy/pipeline/modules/epub3-pub-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-to-daisy202/xproc/library.xpl`](http://daisy.github.io/pipeline/modules/epub3-to-daisy202/xprocdoc/org/daisy/pipeline/modules/epub3-to-daisy202/xproc/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-to-ssml/library.xpl`](http://daisy.github.io/pipeline/modules/epub3-to-ssml/xprocdoc/org/daisy/pipeline/modules/epub3-to-ssml/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-tts/library.xpl`](http://daisy.github.io/pipeline/modules/epub3-tts/xprocdoc/org/daisy/pipeline/modules/epub3-tts/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-validator/library.xpl`](http://daisy.github.io/pipeline/modules/epub3-validator/xprocdoc/org/daisy/pipeline/modules/epub3-validator/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epubcheck-adapter/library.xpl`](http://daisy.github.io/pipeline/modules/epubcheck-adapter/xprocdoc/org/daisy/pipeline/modules/epubcheck-adapter/library.xpl)
- [`http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl`](http://daisy.github.io/pipeline/modules/fileset-utils/xprocdoc/org/daisy/pipeline/modules/fileset-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/html-break-detection/library.xpl`](http://daisy.github.io/pipeline/modules/html-break-detection/xprocdoc/org/daisy/pipeline/modules/html-break-detection/library.xpl)
- [`http://www.daisy.org/pipeline/modules/html-utils/library.xpl`](http://daisy.github.io/pipeline/modules/html-utils/xprocdoc/org/daisy/pipeline/modules/html-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/mathml-to-ssml/library.xpl`](http://daisy.github.io/pipeline/modules/mathml-to-ssml/xprocdoc/org/daisy/pipeline/modules/mathml-to-ssml/library.xpl)
- [`http://www.daisy.org/pipeline/modules/mediaoverlay-utils/library.xpl`](http://daisy.github.io/pipeline/modules/mediaoverlay-utils/xprocdoc/org/daisy/pipeline/modules/mediaoverlay-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl`](http://daisy.github.io/pipeline/modules/mediatype-utils/xprocdoc/org/daisy/pipeline/modules/mediatype-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/metadata-utils/library.xpl`](http://daisy.github.io/pipeline/modules/metadata-utils/xprocdoc/org/daisy/pipeline/modules/metadata-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/nlp-break-detection/library.xpl`](http://daisy.github.io/pipeline/modules/nlp-break-detection/xprocdoc/org/daisy/pipeline/modules/nlp-break-detection/library.xpl)
- [`http://www.daisy.org/pipeline/modules/odt-utils/library.xpl`](http://daisy.github.io/pipeline/modules/odt-utils/xprocdoc/org/daisy/pipeline/modules/odt-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/ssml-to-audio/library.xpl`](http://daisy.github.io/pipeline/modules/ssml-to-audio/xprocdoc/org/daisy/pipeline/modules/ssml-to-audio/library.xpl)
- [`http://www.daisy.org/pipeline/modules/text-to-ssml/library.xpl`](http://daisy.github.io/pipeline/modules/text-to-ssml/xprocdoc/org/daisy/pipeline/modules/text-to-ssml/library.xpl)
- [`http://www.daisy.org/pipeline/modules/tts-helpers/library.xpl`](http://daisy.github.io/pipeline/modules/tts-helpers/xprocdoc/org/daisy/pipeline/modules/tts-helpers/library.xpl)
- [`http://www.daisy.org/pipeline/modules/validation-utils/library.xpl`](http://daisy.github.io/pipeline/modules/validation-utils/xprocdoc/org/daisy/pipeline/modules/validation-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zedai-break-detection/library.xpl`](http://daisy.github.io/pipeline/modules/zedai-break-detection/xprocdoc/org/daisy/pipeline/modules/zedai-break-detection/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zedai-to-epub3/library.xpl`](http://daisy.github.io/pipeline/modules/zedai-to-epub3/xprocdoc/org/daisy/pipeline/modules/zedai-to-epub3/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zedai-to-html/library.xpl`](http://daisy.github.io/pipeline/modules/zedai-to-html/xprocdoc/org/daisy/pipeline/modules/zedai-to-html/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zedai-to-ssml/library.xpl`](http://daisy.github.io/pipeline/modules/zedai-to-ssml/xprocdoc/org/daisy/pipeline/modules/zedai-to-ssml/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zedai-utils/library.xpl`](http://daisy.github.io/pipeline/modules/zedai-utils/xprocdoc/org/daisy/pipeline/modules/zedai-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zip-utils/library.xpl`](http://daisy.github.io/pipeline/modules/zip-utils/xprocdoc/org/daisy/pipeline/modules/zip-utils/library.xpl)


## APIs of Individual Modules

- [`org.daisy.pipeline.modules.braille:common-utils`](http://daisy.github.io/pipeline/modules/braille/common-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:css-core`](http://daisy.github.io/pipeline/modules/braille/css-core/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:css-utils`](http://daisy.github.io/pipeline/modules/braille/css-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:dotify-calabash`](http://daisy.github.io/pipeline/modules/braille/dotify-calabash/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:dotify-core`](http://daisy.github.io/pipeline/modules/braille/dotify-core/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:dotify-formatter`](http://daisy.github.io/pipeline/modules/braille/dotify-formatter/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:dotify-saxon`](http://daisy.github.io/pipeline/modules/braille/dotify-saxon/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:dotify-utils`](http://daisy.github.io/pipeline/modules/braille/dotify-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:dtbook-to-pef`](http://daisy.github.io/pipeline/modules/braille/dtbook-to-pef/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:epub3-to-pef`](http://daisy.github.io/pipeline/modules/braille/epub3-to-pef/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:html-to-pef`](http://daisy.github.io/pipeline/modules/braille/html-to-pef/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:libhyphen-core`](http://daisy.github.io/pipeline/modules/braille/libhyphen-core/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:libhyphen-saxon`](http://daisy.github.io/pipeline/modules/braille/libhyphen-saxon/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:libhyphen-utils`](http://daisy.github.io/pipeline/modules/braille/libhyphen-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:liblouis-calabash`](http://daisy.github.io/pipeline/modules/braille/liblouis-calabash/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:liblouis-core`](http://daisy.github.io/pipeline/modules/braille/liblouis-core/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:liblouis-formatter`](http://daisy.github.io/pipeline/modules/braille/liblouis-formatter/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:liblouis-mathml`](http://daisy.github.io/pipeline/modules/braille/liblouis-mathml/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:liblouis-saxon`](http://daisy.github.io/pipeline/modules/braille/liblouis-saxon/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:liblouis-tables`](http://daisy.github.io/pipeline/modules/braille/liblouis-tables/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:liblouis-utils`](http://daisy.github.io/pipeline/modules/braille/liblouis-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:obfl-utils`](http://daisy.github.io/pipeline/modules/braille/obfl-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:pef-calabash`](http://daisy.github.io/pipeline/modules/braille/pef-calabash/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:pef-core`](http://daisy.github.io/pipeline/modules/braille/pef-core/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:pef-saxon`](http://daisy.github.io/pipeline/modules/braille/pef-saxon/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:pef-utils`](http://daisy.github.io/pipeline/modules/braille/pef-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:texhyph-core`](http://daisy.github.io/pipeline/modules/braille/texhyph-core/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:texhyph-saxon`](http://daisy.github.io/pipeline/modules/braille/texhyph-saxon/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:texhyph-utils`](http://daisy.github.io/pipeline/modules/braille/texhyph-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:xml-to-pef`](http://daisy.github.io/pipeline/modules/braille/xml-to-pef/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:zedai-to-pef`](http://daisy.github.io/pipeline/modules/braille/zedai-to-pef/src/main/README.html)
