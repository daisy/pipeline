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
  <version>5.0.1</version>
</dependency>
<dependency>
  <groupId>org.daisy.pipeline</groupId>
  <artifactId>clientlib-java-httpclient</artifactId>
  <version>2.1.1</version>
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

[`org.daisy.pipeline.script.Script`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/script/Script.html)
: List available inputs, options and outputs of a script.

[`org.daisy.pipeline.datatypes.DatatypeRegistry`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/datatypes/DatatypeRegistry.html)
: Get data types of script options.

[`org.daisy.pipeline.script.BoundScript`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/script/BoundScript.html)
: Specify job inputs/options.

[`org.daisy.pipeline.job.JobManagerFactory`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/job/JobManagerFactory.html)
: Create, delete and queue jobs.

[`org.daisy.pipeline.job.Job`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/job/Job.html)
: Run and monitor jobs and access job results.

Providers of the `ScriptRegistry`, `DatatypeRegistry` and
`JobManagerFactory` services can be loaded using the
[`ServiceLoader`](https://docs.oracle.com/javase/8/docs/api/java/util/ServiceLoader.html)
mechanism or using OSGi.

## Complete Javadocs

<!--
FIXME: add short description for every package
-->

- [`org.daisy.common.fuzzy`](http://daisy.github.io/pipeline/api/org/daisy/common/fuzzy/package-summary.html)
- [`org.daisy.common.messaging`](http://daisy.github.io/pipeline/api/org/daisy/common/messaging/package-summary.html)
- [`org.daisy.common.priority`](http://daisy.github.io/pipeline/api/org/daisy/common/priority/package-summary.html)
- [`org.daisy.common.saxon`](http://daisy.github.io/pipeline/api/org/daisy/common/saxon/package-summary.html)
- [`org.daisy.common.saxon.xslt`](http://daisy.github.io/pipeline/api/org/daisy/common/saxon/xslt/package-summary.html)
- [`org.daisy.common.shell`](http://daisy.github.io/pipeline/api/org/daisy/common/shell/package-summary.html)
- [`org.daisy.common.slf4j`](http://daisy.github.io/pipeline/api/org/daisy/common/slf4j/package-summary.html)
- [`org.daisy.common.stax`](http://daisy.github.io/pipeline/api/org/daisy/common/stax/package-summary.html)
- [`org.daisy.common.transform`](http://daisy.github.io/pipeline/api/org/daisy/common/transform/package-summary.html)
- [`org.daisy.common.xpath.saxon`](http://daisy.github.io/pipeline/api/org/daisy/common/xpath/saxon/package-summary.html)
- [`org.daisy.common.xproc`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/package-summary.html)
- [`org.daisy.common.xproc.calabash`](http://daisy.github.io/pipeline/api/org/daisy/common/xproc/calabash/package-summary.html)
- [`org.daisy.common.zip`](http://daisy.github.io/pipeline/api/org/daisy/common/zip/package-summary.html)
- [`org.daisy.pipeline.braille.common`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/braille/common/package-summary.html)
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
- [`org.daisy.pipeline.tts`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/tts/package-summary.html)


## XProc Utility Libraries

- [`http://www.daisy.org/pipeline/modules/asciimath-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/asciimath-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/dtbook-to-pef/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/braille/dtbook-to-pef/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/epub3-to-pef/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/braille/epub3-to-pef/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/html-to-pef/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/braille/html-to-pef/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/zedai-to-pef/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/braille/zedai-to-pef/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/braille/common-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/css-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/braille/css-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/dotify-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/braille/dotify-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/obfl-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/braille/obfl-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/braille/pef-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/braille/pef-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/file-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/file-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/common-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/common-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/daisy202-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/daisy202-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/daisy202-validator/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/daisy202-validator/library.xpl)
- [`http://www.daisy.org/pipeline/modules/daisy3-to-daisy202/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/daisy3-to-daisy202/library.xpl)
- [`http://www.daisy.org/pipeline/modules/daisy3-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/daisy3-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-break-detection/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/dtbook-break-detection/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-to-daisy3/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/dtbook-to-daisy3/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-to-odt/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/dtbook-to-odt/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-to-zedai/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/dtbook-to-zedai/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-tts/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/dtbook-tts/library.xpl)
- [`http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/dtbook-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/epub-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub2-to-epub3/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/epub2-to-epub3/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-to-daisy202/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/epub3-to-daisy202/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-to-daisy3/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/epub3-to-daisy3/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-to-html/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/epub3-to-html/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epub3-tts/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/epub3-tts/library.xpl)
- [`http://www.daisy.org/pipeline/modules/epubcheck-adapter/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/epubcheck-adapter/library.xpl)
- [`http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/fileset-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/html-break-detection/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/html-break-detection/library.xpl)
- [`http://www.daisy.org/pipeline/modules/html-to-dtbook/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/html-to-dtbook/library.xpl)
- [`http://www.daisy.org/pipeline/modules/html-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/html-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/mathml-to-ssml/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/mathml-to-ssml/library.xpl)
- [`http://www.daisy.org/pipeline/modules/mediatype-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/mediatype-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/metadata-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/metadata-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/nlp-common/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/nlp-common/library.xpl)
- [`http://www.daisy.org/pipeline/modules/odf-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/odf-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/smil-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/smil-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/tts-common/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/tts-common/library.xpl)
- [`http://www.daisy.org/pipeline/modules/validation-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/validation-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zedai-to-epub3/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/zedai-to-epub3/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zedai-to-html/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/zedai-to-html/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zedai-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/zedai-utils/library.xpl)
- [`http://www.daisy.org/pipeline/modules/zip-utils/library.xpl`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/modules/zip-utils/library.xpl)


## APIs of Individual Modules

- [`org.daisy.pipeline.modules.braille:braille-common`](http://daisy.github.io/pipeline/modules/braille/braille-common/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:braille-css-utils`](http://daisy.github.io/pipeline/modules/braille/braille-css-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:dotify-utils`](http://daisy.github.io/pipeline/modules/braille/dotify-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:dtbook-to-pef`](http://daisy.github.io/pipeline/modules/braille/dtbook-to-pef/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:epub3-to-pef`](http://daisy.github.io/pipeline/modules/braille/epub3-to-pef/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:html-to-pef`](http://daisy.github.io/pipeline/modules/braille/html-to-pef/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:libhyphen-utils`](http://daisy.github.io/pipeline/modules/braille/libhyphen-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:liblouis-utils`](http://daisy.github.io/pipeline/modules/braille/liblouis-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:obfl-utils`](http://daisy.github.io/pipeline/modules/braille/obfl-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:pef-utils`](http://daisy.github.io/pipeline/modules/braille/pef-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:texhyph-utils`](http://daisy.github.io/pipeline/modules/braille/texhyph-utils/src/main/README.html)
- [`org.daisy.pipeline.modules.braille:zedai-to-pef`](http://daisy.github.io/pipeline/modules/braille/zedai-to-pef/src/main/README.html)
