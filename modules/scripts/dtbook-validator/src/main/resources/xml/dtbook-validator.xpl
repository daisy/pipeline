<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="dtbook-validator" type="px:dtbook-validator"
    px:input-filesets="dtbook"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:l="http://xproc.org/library" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xhtml="http://www.w3.org/1999/xhtml" xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:m="http://www.w3.org/1998/Math/MathML" exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">DTBook Validator</h1>
        <p px:role="desc">Validates DTBook documents. Supports inclusion of MathML.</p>
        <a px:role="homepage" href="http://daisy.github.io/pipeline/modules/dtbook-validator">
            Online documentation
        </a>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a px:role="contact" href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->

    <!-- NOTE: the "input" here is given by an option string "input-opf" -->


    <!--<p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">result</h1>
            <p px:role="desc">A copy of the input document; may include PSVI annotations.</p>
        </p:documentation>
        <p:pipe port="result" step="if-dtbook-wellformed"/>
     </p:output>-->

    <p:output port="report" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">XML reports</h1>
            <p px:role="desc" xml:space="preserve">Raw XML-formatted reports from all types of validation used (RelaxNG, Schematron, custom).

[More details on the file format](http://daisy.github.io/pipeline/wiki/ValidationReportXML).</p>
        </p:documentation>
        <p:pipe port="xml-report" step="if-dtbook-wellformed"/>
    </p:output>

    <p:output port="html-report" px:media-type="application/vnd.pipeline.report+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation report</h1>
        </p:documentation>
        <p:pipe port="html-report" step="if-dtbook-wellformed"/>
    </p:output>

    <p:output port="validation-status" px:media-type="application/vnd.pipeline.status+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation status</h1>
            <p px:role="desc" xml:space="preserve">An XML document describing, briefly, whether the validation was successful.

[More details on the file format](http://daisy.github.io/pipeline/wiki/ValidationStatusXML).</p>
        </p:documentation>
        <p:pipe port="validation-status" step="if-dtbook-wellformed"/>
    </p:output>

    <!-- we are using a string option instead of an XML input source because
        the wellformedness of the document cannot be taken for granted -->
    <p:option name="input-dtbook" required="true" px:type="anyFileURI" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">DTBook</h2>
            <p px:role="desc">The input DTBook document.</p>
        </p:documentation>
    </p:option>

    <p:option name="output-dir" required="false" px:output="result" px:type="anyDirURI" select="''">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Validation reports</h2>
            <p px:role="desc">All validation reports.</p>
        </p:documentation>
    </p:option>

    <p:option name="mathml-version" required="false" px:type="string" select="'3.0'">
        <p:pipeinfo>
            <px:data-type>
                <choice>
                    <value>3.0</value>
                    <value>2.0</value>
                    <value>1.01</value>
                    <value>1.0</value>
                </choice>
            </px:data-type>
        </p:pipeinfo>
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">MathML version</h2>
            <p px:role="desc">Version of MathML in the DTBook file.</p>
        </p:documentation>
    </p:option>

    <p:option name="check-images" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Check that images exist</h2>
            <p px:role="desc">Check to see that referenced images exist on disk.</p>
        </p:documentation>
    </p:option>

    <p:option name="nimas" required="false" px:type="boolean" select="'false'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">Validate against NIMAS 1.1</h2>
            <p px:role="desc">Validate using NIMAS 1.1 rules for DTBook.</p>
        </p:documentation>
    </p:option>
    
    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:import
        href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation> Collection of utilities for validation and reporting. </p:documentation>
    </p:import>

    <p:import href="http://www.daisy.org/pipeline/modules/fileset-utils/library.xpl">
        <p:documentation>Utilities for representing a fileset.</p:documentation>
    </p:import>

    <p:import href="dtbook-validator.validate.xpl"/>
    <p:import href="dtbook-validator.store.xpl"/>
    <p:variable name="dtbook-filename" select="tokenize($input-dtbook, '/')[last()]"/>

    <px:message>
        <p:with-option name="message" select="concat('DTBook validator: ', $input-dtbook)"/>
        <p:input port="source">
            <p:empty/>
        </p:input>
    </px:message>
    <p:sink/>
    
    <px:fileset-create>
        <p:with-option name="base" select="replace($input-dtbook,'[^/]+$','')"/>
    </px:fileset-create>
    <px:fileset-add-entry>
        <p:with-option name="href" select="$input-dtbook"/>
    </px:fileset-add-entry>
    <p:identity name="create-fileset-for-dtbook-doc"/>
    <p:sink/>
    
    <!--check that the package document is well-formed XML -->
    <p:identity>
        <p:input port="source">
            <p:pipe port="result" step="create-fileset-for-dtbook-doc"/>
        </p:input>
    </p:identity>
    <px:message message="DTBook validator: Checking that DTBook document exists and is well-formed"/>
    <px:check-files-wellformed name="check-dtbook-wellformed"/>
    <p:identity>
        <p:input port="source">
            <p:pipe port="validation-status" step="check-dtbook-wellformed"/>
        </p:input>
    </p:identity>
    <px:message message="DTBook validator: Done checking that DTBook document exists and is well-formed"/>

    <p:choose name="if-dtbook-wellformed">
        
        <!-- if the dtbook file was well-formed -->
        <p:when test="d:validation-status/@result = 'ok'">

            <p:output port="result">
                <p:pipe port="result" step="validate-dtbook"/>
            </p:output>
            <p:output port="xml-report">
                <p:pipe port="xml-report" step="validate-dtbook"/>
            </p:output>
            <p:output port="html-report">
                <p:pipe port="html-report" step="validate-dtbook"/>
            </p:output>
            <p:output port="validation-status">
                <p:pipe port="validation-status" step="validate-dtbook"/>
            </p:output>

            <p:load name="load-dtbook-doc">
                <p:with-option name="href" select="$input-dtbook"/>
            </p:load>

            <pxi:dtbook-validator.validate name="validate-dtbook">
                <p:input port="source">
                    <p:pipe port="result" step="load-dtbook-doc"/>
                </p:input>
                <p:with-option name="output-dir" select="$output-dir"/>
                <p:with-option name="mathml-version" select="$mathml-version"/>
                <p:with-option name="check-images" select="$check-images"/>
                <p:with-option name="base-uri" select="$input-dtbook"/>
                <p:with-option name="nimas" select="$nimas"/>
            </pxi:dtbook-validator.validate>

            <pxi:dtbook-validator.store name="store-dtbook-validation-results">
                <p:input port="xml-report">
                    <p:pipe port="xml-report" step="validate-dtbook"/>
                </p:input>
                <p:input port="html-report">
                    <p:pipe port="html-report" step="validate-dtbook"/>
                </p:input>
                <p:with-option name="output-dir" select="$output-dir"/>
            </pxi:dtbook-validator.store>
        </p:when>

        <!-- otherwise, just store a report from the wellformedness check -->
        <p:otherwise>
            <p:output port="result">
                <!-- we have to put something on this port... -->
                <p:inline>
                    <tmp:error/>
                </p:inline>
            </p:output>
            <p:output port="xml-report">
                <p:pipe port="result" step="wrap-report"/>
            </p:output>
            <p:output port="html-report">
                <p:pipe port="result" step="format-as-html"/>
            </p:output>
            <p:output port="validation-status">
                <p:pipe port="validation-status" step="check-dtbook-wellformed"/>
            </p:output>

            <px:message message="DTBook Validator: DTBook document is missing or not well-formed">
                <p:input port="source">
                    <p:inline>
                        <p:empty/>
                    </p:inline>
                </p:input>
            </px:message>
            <p:sink/>

            <px:combine-validation-reports name="wrap-report">
                <p:with-option name="document-name" select="$dtbook-filename"/>
                <p:with-option name="document-type" select="'N/A'"/>
                <p:with-option name="document-path" select="$input-dtbook"/>
                <p:input port="source">
                    <p:pipe port="report" step="check-dtbook-wellformed"/>
                </p:input>
            </px:combine-validation-reports>

            <px:validation-report-to-html name="format-as-html">
                <p:input port="source">
                    <p:pipe port="result" step="wrap-report"/>
                </p:input>
                <p:with-option name="toc" select="'false'"/>
            </px:validation-report-to-html>

            <pxi:dtbook-validator.store>
                <p:input port="xml-report">
                    <p:pipe port="result" step="wrap-report"/>
                </p:input>
                <p:input port="html-report">
                    <p:pipe port="result" step="format-as-html"/>
                </p:input>
                <p:with-option name="output-dir" select="$output-dir"/>
            </pxi:dtbook-validator.store>
        </p:otherwise>
    </p:choose>
</p:declare-step>
