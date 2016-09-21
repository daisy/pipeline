## XSpec Maven Plugin

The XSpec Maven Plugin is used to run [XSpec](http://code.google.com/p/xspec/)
tests for XSLT. It is intended to be used during the `test` phase of the build
lifecycle. It generates reports in several formats:

 * Plain text test log (`OUT-*.txt`)
 * XSpec XML report (`XSPEC-*.xml`)
 * XSpec HTML report (`HTML-*.html`)
 * Surefire/Junit XML report (`TEST-*.xml`)

By default, these files are generated at `${basedir}/target/surefire-reports`.

The [Maven Surefire Report
Plugin](http://maven.apache.org/plugins/maven-surefire-report-plugin/) can be
used to generate an HTML format of the Junit/Surefire report during the
project reporting phase.

### What is XSpec ?

XSpec is a Behavior Driven Development (BDD) framework for XSLT and XQuery.
For more information, please refer to the [XSpec
site](http://code.google.com/p/xspec/).

### Goals Overview

The XSpec Maven Plugin has two goals:

 * [xspec:test](test-mojo.html) runs the XSpec unit tests of the project
 * [xspec:help](help-mojo.html) displays help information

### Usage

General instructions on how to use the plugin can be found on the [usage
page](usage.html). Some more specific use cases are described in the examples
given below. Please refer to the [XSpec site](http://code.google.com/p/xspec/)
for any information related to how to use XSpec itself. If you have any
question, please have a look at the [FAQ](faq.html), or feel free to contact
[the developers](team-list.html).

If you feel like the plugin is missing a feature or has a defect, you can fill
a feature request or bug report in our [issue tracker](issue-tracking.html).
When creating a new issue, please provide a comprehensive description of your
concern. Especially for fixing bugs it is crucial that the developers can
reproduce your problem. For this reason, entire debug logs, POMs or most
preferably little demo projects attached to the issue are very much
appreciated. Of course, patches are welcome, too. Contributors can check out
or fork the project from our GitHub [source
repository](source-repository.html).

### Examples

The following examples show how to use the XSpec Maven Plugin:

 * [Running XSPec tests](examples/basic.html)
 * [Mocking external dependencies](examples/mocks.html)
