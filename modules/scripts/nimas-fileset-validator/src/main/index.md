<link rev="dp2:doc" href="resources/xml/nimas-fileset-validator.xpl"/>
<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="NIMAS Fileset Validator"/>

<!-- 
summary: NIMAS fileset validator module summary
-->

# NIMAS Fileset Validator

Validate a NIMAS Fileset. Supports inclusion of MathML.

## Synopsis

{{>synopsis}}

## Sample usage

This section shows how to use the NIMAS Fileset Validator via the Pipeline 2 command line interface.

Run the script like this: 
    
    $ ./dp2 nimas-fileset-validator
            --x-input-opf file:/path/to/book/package.opf
            --x-output-dir /tmp/dpout
            --o-package-doc-validation-report /tmp/t1
            --o-dtbook-validation-report /tmp/t2
            --o-result /tmp/t3
            --o-validation-status /tmp/t4

Make sure the directory `/tmp/dpout` already exists.

Also note the special syntax for the input file option, e.g.: `--x-input-opf file:/path/to/book/package.opf`

When execution is complete, you will see files like this in the output directory that you specified, named for the book files they are reporting on: `my-dtbook.xml-report.xml` `my-package-file.opf-report.xml` `validation-report.xhtml`

Note: If instead of using the `--x-output-dir` option to put all output in one directory, you would rather specify a file path for each file, then just use the output parameters (prefixed by `--o`). Above they are shown as temp files (e.g. `/tmp/t1`) but they could just as easily store their output anywhere (e.g. `/path/to/output/my-report.xml`).

Each generated file in this example is as follows:

### `my-dtbook.xml-report.xml`

Raw validation output from DTBook validation. See [ValidationReportXML](http://daisy.github.io/pipeline/wiki/ValidationReportXML) for details on the file format. This file is named for the relative path of the DTBook file, with slashes replaced by underscores, and `-report.xml` appended to the end. For example, if your package document refers to a DTBook file at `chapter_one/dtbook.xml`, the validation report for this DTBook file will be called `chapter_one_dtbook.xml-report.xml`.

There will be one report like this for each DTBook file in the publication.

Note that if the publication contains no DTBook files, or if the files were not correctly identified as application/x-dtbook+xml in the manifest, no DTBook validation reports will be produced.

### `my-package-file.opf-report.xml`

Raw validation output from package document validation. See ValidationReportXML for details on the file format. This file is named for the package document file name, plus `-report.xml`.

### `validation-report.xhtml`

XHTML file containing a summary of all validation errors for each file that was validated. Each file has its own section in the document. This file always has this name.

The top of the document contains a table listing information about each document that was validated.

## Notes

For the package document, the document type is detected as either OPF 1.2 or OPF 1.2 (MathML detected). The latter means that one or more DTBook files in the publication contains MathML, and therefore the package document is required to have certain metadata related to this.

The DTBook document type is listed as the DTBook version, e.g. 2005-3, and will say with MathML if the document contains MathML.

# See also

* [Developer notes](dev-notes.md)

