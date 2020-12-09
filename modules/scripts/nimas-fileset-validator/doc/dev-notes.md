<link rel="rdf:type" href="http://www.daisy.org/ns/pipeline/userdoc"/>
<meta property="dc:title" content="NIMAS Fileset Validator Development Notes"/>

<!-- 
summary: NIMAS fileset validator development notes
-->
# NIMAS Fileset Validator Development Notes


## This script can

_(via DTBook validator)_

* Validate the XML content file
* Validate MathML in the content file
* Enforce `@alttext` and `@altimg` 
* Check that all images linked to from the XML content file exist on disk 

_(via the [Package Doc Validator](https://github.com/daisy-consortium/pipeline-scripts/blob/master/nimas-fileset-validator/src/main/resources/xml/nimas-fileset-validator.validate-package-doc.xpl) step)_

* Validate the Package file against its schema
* Enforce metadata rules in the package file:
  * "Metadata must be added to indicate the presence of MathML in a source file." 
       ~~~xml
       <meta name="z39-86-extension-version" scheme="http://www.w3.org/1998/Math/MathML" content="1.0" /> 
       <meta name="DTBook-XSLTFallback" scheme="http://www.w3.org/1998/Math/MathML" content="xslt-file-name" />
       ~~~
    * "If MathML is not present, then metadata must not be present that indicates that it is (for example, automated processes must not add MathML metadata by default)."
    * NIMAS-specific metadata (from http://aim.cast.org/experience/technologies/spec-v1_1): 
      * one or more of `dc:Title` 
      * zero or more of `dc:Creator`
      * one or more of `dc:Publisher` 
      * one or more of `dc:Date` in ISO 8601 format 
      * one or more of `dc:Identifier` (no format specified) 
      * one or more of `dc:Language`

* Special NIMAS consideration: 
 * `dc:Format` ="NIMAS 1.1" 
 * one or more of `dc:Rights` 
 * one or more of `dc:Source` 
 * `nimas-SourceEdition` (under `x-metadata`) 
 * `nimas-SourceDate` (under `x-metadata`)
* Verify that all PDFs linked to from the package document exist on disk 
* Verify that that the package document references at least one item of type `application/pdf`

## This script CANNOT

* Validate the contents of the PDF or image files 
* Check if the XML content file contains an updated DTD containing the MathML module

## Caveats

* We only verify the presence of PDF and image files; we do not verify that all linked files exist on disk. 
* `dc:Date` is validated as an ISO-8601 formatted date. This is more lenient than what NIMAS says (yyyy-mm-dd), as it would also allow yyyymmdd. 
* RelaxNG validation results sometimes give incorrect line and column numbers. This is a known issue with Jing.

## Notes about NIMAS

* NIMAS element markup is a subset of DTBook
* Package file has more required metadata than in DAISY
* DTBook requirements:
  * "The DTBook DTD must be updated to include the MathML module."
  * "The actual math content itself is provided, encoded in MathML, within the XML source file."
* MathML in NIMAS points to version MathML v3 whereas MathML in DAISY points to version MathML v2. We support both.
* Requires the use of `@alttext`, `@altimg` on the Math element (as does MathML in DAISY)
* Says "the XSLT file must be present and must be listed in the package file" (mime type `application/xslt+xml`). Note that OPF schema allows `application/xml` as well. We follow the stricter NIMAS requirement but could consider relaxing it.

### Fileset overview

* XML content file
* Package file (OPF)
* PDF-format copy of title page and ISBN and copyright information pages
* Full set of content images in SVG, JPG, or PNG format.


### Links

* [NIMAS](http://aim.cast.org/experience/technologies/spec-v1_1)
* [NIMAS Guidelines](http://aim.cast.org/learn/practice/production/creatingnimas)
* [Math in DAISY](http://www.daisy.org/projects/mathml/mathml-in-daisy-spec.html)
* [MathML 2](http://www.w3.org/TR/MathML2/)
* [MathML 3](http://www.w3.org/TR/MathML/)
* [DTBookValidator](http://daisy.github.io/pipeline/Get-Help/User-Guide/Scripts/dtbook-validator/)
