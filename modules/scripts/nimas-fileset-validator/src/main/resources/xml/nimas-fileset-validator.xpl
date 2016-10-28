<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:d="http://www.daisy.org/ns/pipeline/data" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:pkg="http://openebook.org/namespaces/oeb-package/1.0/"
    xmlns:l="http://xproc.org/library"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:m="http://www.w3.org/1998/Math/MathML" xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    version="1.0" name="nimas-fileset-validator" type="px:nimas-fileset-validator"
    px:input-filesets="nimas dtbook daisy3"
    exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">NIMAS Fileset Validator</h1>
        <p px:role="desc">Validate a NIMAS Fileset. Supports inclusion of MathML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/nimas-fileset-validator">
            Online documentation
        </a>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a px:role="contact" href="mailto:marisa.demeglio@gmail.com"
                >marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->

    <!-- NOTE: the "input" here is given by an option string "input-opf" -->

    <p:output port="result" primary="true" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">HTML report</h1>
            <p px:role="desc">An HTML-formatted validation report comprising all documents'
                reports.</p>
        </p:documentation>
        <p:pipe step="if-package-wellformed" port="result"/>
    </p:output>

    <p:output port="package-doc-validation-report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Package Document XML Report</h1>
            <p px:role="desc">Raw validation output for the package document.</p>
        </p:documentation>
        <p:pipe step="if-package-wellformed" port="package-doc-validation-report"/>
    </p:output>

    <p:output port="dtbook-validation-report" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">DTBook XML Report</h1>
            <p px:role="desc">Raw validation output for the DTBook file(s).</p>
        </p:documentation>
        <p:pipe step="if-package-wellformed" port="dtbook-validation-report"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation Status</h1>
            <p px:role="desc" xml:space="preserve">Validation status

See [http://code.google.com/p/daisy-pipeline/wiki/ValidationStatusXML](http://code.google.com/p/daisy-pipeline/wiki/ValidationStatusXML).</p>
        </p:documentation>
        <p:pipe step="if-package-wellformed" port="validation-status"/>
    </p:output>

    <!-- we are using a string option instead of an XML input source because
        the wellformedness of the document cannot be taken for granted -->
    <p:option name="input-opf" required="true" px:type="anyFileURI" px:media-type="application/oebps-package+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Package Document</h2>
            <p px:role="desc">The input package document (*.opf).</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="false" px:output="result" px:type="anyDirURI" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Validation reports</h2>
            <p px:role="desc">All validation reports.</p>
        </p:documentation>
    </p:option>

    <p:option name="mathml-version" required="false" px:type="string" select="'3.0'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">MathML version</h2>
            <p px:role="desc">Version of MathML in the DTBook file(s).</p>
        </p:documentation>
    </p:option>

    <p:option name="check-images" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Check that images exist</h2>
            <p px:role="desc">Check to see that images referenced by DTBook file(s) exist on
                disk.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:import
        href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation>Collection of utilities for validation and reporting. </p:documentation>
    </p:import>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>Utilities for representing a fileset.</p:documentation>
    </p:import>

    <p:import href="nimas-fileset-validator.validate.xpl"/>
    <p:import href="nimas-fileset-validator.store.xpl"/>

    <p:variable name="package-doc-filename" select="tokenize($input-opf, '/')[last()]"/>

    <px:message>
        <p:with-option name="message" select="concat('Nimas fileset validator: ', $input-opf)"/>
        <p:input port="source">
            <p:empty/>
        </p:input>
    </px:message>
    <p:sink/>

    <!-- ***************************************************** -->
    <!-- VALIDATION STEPS -->
    <!-- ***************************************************** -->

    <px:fileset-add-entry name="create-fileset-for-package-doc">
        <p:with-option name="href" select="$input-opf"/>
        <p:input port="source">
            <p:inline>
                <d:fileset/>
            </p:inline>
        </p:input>
    </px:fileset-add-entry>


    <px:message message="Nimas fileset validator: Checking that package document is well-formed"/>
    <p:sink/>

    <!--check that the package document is well-formed XML -->
    <px:check-files-wellformed name="check-package-wellformed">
        <p:input port="source">
            <p:pipe port="result" step="create-fileset-for-package-doc"/>
        </p:input>
    </px:check-files-wellformed>

    <p:choose name="if-package-wellformed">
        <p:xpath-context>
            <p:pipe port="validation-status" step="check-package-wellformed"/>
        </p:xpath-context>

        <!-- if the package file was well-formed -->
        <p:when test="d:validation-status/@result = 'ok'">

            <p:output port="result">
                <p:pipe port="result" step="validate-nimas-fileset"/>
            </p:output>
            <p:output port="dtbook-validation-report" sequence="true">
                <p:pipe port="dtbook-validation-report" step="validate-nimas-fileset"/>
            </p:output>
            <p:output port="package-doc-validation-report">
                <p:pipe port="package-doc-validation-report" step="validate-nimas-fileset"/>
            </p:output>
            <p:output port="validation-status">
                <p:pipe port="validation-status" step="validate-nimas-fileset"/>
            </p:output>

            <p:load name="load-package-doc">
                <p:with-option name="href" select="$input-opf"/>
            </p:load>

            <pxi:nimas-fileset-validator.validate name="validate-nimas-fileset">
                <p:input port="source">
                    <p:pipe port="result" step="load-package-doc"/>
                </p:input>
                <p:with-option name="mathml-version" select="$mathml-version"/>
                <p:with-option name="check-images" select="$check-images"/>
                <p:with-option name="base-uri" select="$input-opf"/>
            </pxi:nimas-fileset-validator.validate>

            <pxi:nimas-fileset-validator.store name="store-nimas-validation-results">
                <p:input port="html-report">
                    <p:pipe port="result" step="validate-nimas-fileset"/>
                </p:input>
                <p:input port="xml-reports">
                    <p:pipe port="package-doc-validation-report" step="validate-nimas-fileset"/>
                    <p:pipe port="dtbook-validation-report" step="validate-nimas-fileset"/>
                </p:input>
                <p:with-option name="output-dir" select="$output-dir"/>
            </pxi:nimas-fileset-validator.store>
        </p:when>

        <!-- otherwise, just store a report from the wellformedness check -->
        <p:otherwise>
            <p:output port="result">
                <p:pipe port="result" step="format-as-html"/>
            </p:output>
            <p:output port="dtbook-validation-report" sequence="true">
                <!-- we need to output something here -->
                <p:inline>
                    <tmp:error/>
                </p:inline>
            </p:output>
            <p:output port="package-doc-validation-report">
                <p:pipe port="result" step="wrap-report"/>
            </p:output>
            <p:output port="validation-status">
                <p:pipe port="validation-status" step="check-package-wellformed"/>
            </p:output>

            <px:message message="Package document is not well-formed">
                <p:input port="source">
                    <p:inline>
                        <p:empty/>
                    </p:inline>
                </p:input>
            </px:message>
            <p:sink/>

            <p:group name="wrap-report">
                <p:output port="result"/>
                <px:combine-validation-reports>
                    <p:with-option name="document-name" select="$package-doc-filename"/>
                    <p:with-option name="document-type" select="'OPF 1.2'"/>
                    <p:with-option name="document-path" select="$input-opf"/>
                    <p:input port="source">
                        <p:pipe port="report" step="check-package-wellformed"/>
                    </p:input>
                </px:combine-validation-reports>
                <!-- add the report path -->
                <p:insert position="last-child" match="d:document-info">
                    <p:input port="insertion">
                        <p:inline>
                            <d:report-path>@@</d:report-path>
                        </p:inline>
                    </p:input>
                </p:insert>
                <p:string-replace match="d:document-info/d:report-path/text()">
                    <p:with-option name="replace"
                        select="concat('&quot;', $package-doc-filename, '-report.xml&quot;')"/>
                </p:string-replace>
            </p:group>

            <px:validation-report-to-html name="format-as-html">
                <p:input port="source">
                    <p:pipe port="result" step="wrap-report"/>
                </p:input>
                <p:with-option name="toc" select="'true'"/>
            </px:validation-report-to-html>

            <pxi:nimas-fileset-validator.store>
                <p:input port="html-report">
                    <p:pipe port="result" step="format-as-html"/>
                </p:input>
                <p:input port="xml-reports">
                    <p:pipe port="result" step="wrap-report"/>
                </p:input>
                <p:with-option name="output-dir" select="$output-dir"/>
            </pxi:nimas-fileset-validator.store>
        </p:otherwise>
    </p:choose>


</p:declare-step>
