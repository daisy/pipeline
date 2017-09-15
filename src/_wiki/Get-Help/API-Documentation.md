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
  <version>4.8.1</version>
</dependency>
<dependency>
  <groupId>org.daisy.pipeline</groupId>
  <artifactId>clientlib-java-httpclient</artifactId>
  <version>2.0.1</version>
</dependency>
~~~
  
The entry point of the library is the class
[`org.daisy.pipeline.client.http.WS`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/http/WS.html).

List of all packages:

- [`org.daisy.pipeline.client`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/package-summary.html)
- [`org.daisy.pipeline.client.filestorage`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/filestorage/package-summary.html)
- [`org.daisy.pipeline.client.http`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/http/package-summary.html)
- [`org.daisy.pipeline.client.models`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/models/package-summary.html)
- [`org.daisy.pipeline.client.models.datatypes`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/models/datatypes/package-summary.html)
- [`org.daisy.pipeline.client.utils`](http://daisy.github.io/pipeline/api/org/daisy/pipeline/client/utils/package-summary.html)
