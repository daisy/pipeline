<link rev="dp2:doc" href="resources/xml/dtbook-validator.xpl"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="DTBook Validator"/>

<!--
summary: DTBook validator module summary
-->

# DTBook Validator

This module validates a single DTBook document. It supports the following:

- DTBook 2005-3 (with or without MathML 2/3)
- DTBook 2005-2 (with or without MathML 2/3)
- DTBook 2005-1
- 1.1.0

The input document is validated against RelaxNG schema and additional schematron rules. If the `check-images` option is `true`, then the validator ensures that any referenced images exist on disk.

## Synopsis

{{>synopsis}}

## Sample usage

This section shows how to use the DTBook Validator via the Pipeline 2 command line interface.

Run the script like this:

    $ ./dp2 dtbook-validator --x-input-dtbook file:/path/to/book/dtbook.xml --o-result /tmp/t1 --o-report /tmp/t2 --o-html-report /tmp/t3 --o-validation-status /tmp/t4 --x-output-dir /tmp/dpout 

Make sure the directory `/tmp/dpout` already exists.

Also note the special syntax for the input file option, which has changed in this update (July 2013): 

    --x-input-dtbook file:/path/to/book/dtbook.xml

When execution is complete, you will see these files in the output directory that you specified:

    dtbook-validation-report.xml
    dtbook-validation-report.xhtml

Note: If instead of using the `--x-output-dir` option to put all output in one directory, you would rather specify a file path for each file, then just use the output parameters (prefixed by `--o`). Above they are shown as temp files (e.g. `/tmp/t1`) but they could just as easily store their output anywhere (e.g. `/path/to/output/my-report.xml`).

They are as follows:

### `dtbook-validation-report.xml`

Raw validation output from DTBook validation. See [ValidationReportXML](http://daisy.github.io/pipeline/wiki/ValidationReportXML) for details on the file format.

### `dtbook-validation-report.xhtml`

XHTML file containing a summary of all validation errors for the DTBook file. 
