<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="nimas-fileset-validator.validate"
    type="pxi:nimas-fileset-validator.validate" xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xmlns:l="http://xproc.org/library" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xhtml="http://www.w3.org/1999/xhtml"
    xmlns:pkg="http://openebook.org/namespaces/oeb-package/1.0/"
    xmlns:dc="http://purl.org/dc/elements/1.1/" xmlns:m="http://www.w3.org/1998/Math/MathML"
    exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">NIMAS Fileset Validator: Validate</h1>
        <p px:role="desc">Internal step.</p>
    </p:documentation>

    <!-- ***************************************************** -->
    <!-- INPUTS / OUTPUTS / OPTIONS -->
    <!-- ***************************************************** -->
    <p:input port="source" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">source</h1>
            <p px:role="desc">A package document (.opf).</p>
        </p:documentation>
    </p:input>

    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">HTML Report</h1>
            <p px:role="desc">An HTML-formatted validation report comprising all documents'
                reports.</p>
        </p:documentation>
        <p:pipe step="format-as-html" port="result"/>
    </p:output>

    <p:output port="package-doc-validation-report">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Package Document XML Report</h1>
            <p px:role="desc">Raw validation output for the package document.</p>
        </p:documentation>
        <p:pipe step="validate-package-doc" port="result"/>
    </p:output>

    <p:output port="dtbook-validation-report" sequence="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">DTBook XML Report</h1>
            <p px:role="desc">Raw validation output for the DTBook file(s).</p>
        </p:documentation>
        <p:pipe step="validate-dtbooks" port="result"/>
    </p:output>

    <p:output port="validation-status">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h1 px:role="name">Validation Status</h1>
            <p px:role="desc">Validation status
                (http://daisy.github.io/pipeline/ValidationStatusXML)</p>
        </p:documentation>
        <p:pipe step="format-validation-status" port="result"/>
    </p:output>

    <p:option name="mathml-version"/>
    <p:option name="check-images"/>
    <p:option name="base-uri"/>


    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>

    <p:import
        href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl">
        <p:documentation>Collection of utilities for validation and reporting. </p:documentation>
    </p:import>

    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-validator/dtbook-validator.xpl">
        <p:documentation>DTBook + MathML validator</p:documentation>
    </p:import>

    <p:import href="nimas-fileset-validator.validate-package-doc.xpl">
        <p:documentation>Package doc validation step.</p:documentation>
    </p:import>

    <p:import href="nimas-fileset-validator.fileset-filter.xpl"/>

    <p:variable name="package-doc-filename" select="tokenize($base-uri, '/')[last()]"/>

    <!-- ***************************************************** -->
    <!-- VALIDATION STEPS -->
    <!-- ***************************************************** -->

    <p:for-each name="validate-dtbooks">
        <p:output port="result" sequence="true">
            <p:pipe port="result" step="validate-dtbook-group"/>
        </p:output>

        <p:iteration-source select="//pkg:item[@media-type = 'application/x-dtbook+xml']">
            <p:pipe port="source" step="nimas-fileset-validator.validate"/>
        </p:iteration-source>

        <p:variable name="dtbook-href" select="resolve-uri(*/@href, $base-uri)"/>
        <p:variable name="dtbook-filename" select="tokenize($dtbook-href, '/')[last()]"/>

        <!-- TODO: will this be unique? 
            We should actually relativize $dtbook-href wrt the package file rather than just take the filename part of the path. -->
        <p:variable name="report-filename"
            select="concat(replace($dtbook-filename, '/', '_'), '-report.xml')"/>

        <p:group name="validate-dtbook-group">
            <p:output port="result"/>

            <px:dtbook-validator name="validate-dtbook">
                <p:with-option name="input-dtbook" select="$dtbook-href"/>
                <p:with-option name="check-images" select="$check-images"/>
                <p:with-option name="mathml-version" select="$mathml-version"/>
                <p:with-option name="nimas" select="'true'"/>
            </px:dtbook-validator>

            <!-- add the report path -->
            <p:insert position="last-child" match="//d:document-info">
                <p:input port="insertion">
                    <p:inline>
                        <d:report-path>@@</d:report-path>
                    </p:inline>
                </p:input>
                <p:input port="source">
                    <p:pipe port="report" step="validate-dtbook"/>
                </p:input>
            </p:insert>
            <p:string-replace match="d:document-info/d:report-path/text()">
                <p:with-option name="replace" select="concat('&quot;', $report-filename, '&quot;')"
                />
            </p:string-replace>
        </p:group>
    </p:for-each>
    
    <!-- wrap all the document-info elements -->
    <p:wrap-sequence name="wrap-doc-infos" wrapper="results" wrapper-prefix="tmp"
        wrapper-namespace="http://www.daisy.org/ns/pipeline/tmp">
        <p:input port="source" select="//d:document-info">
            <p:pipe port="result" step="validate-dtbooks"/>
        </p:input>
    </p:wrap-sequence>

    <p:group name="validate-package-doc">
        <p:output port="result"/>
        <p:choose name="validate-package-doc-choose">
            <p:xpath-context>
                <p:pipe port="result" step="wrap-doc-infos"/>
            </p:xpath-context>
            <!-- did any dtbook docs have mathml? if so, then we will say that this book has mathml.-->
            <p:when test="//d:document-info[@internal='mathml']">
                <p:output port="result">
                    <p:pipe port="report" step="run-package-doc-validation"/>
                </p:output>
                
                <pxi:nimas-fileset-validator.validate-package-doc name="run-package-doc-validation">
                    <p:input port="source">
                        <p:pipe port="source" step="nimas-fileset-validator.validate"/>
                    </p:input>
                    <p:with-option name="math" select="'true'"/>
                </pxi:nimas-fileset-validator.validate-package-doc>
                <p:sink/>
            </p:when>
            <p:otherwise>
                <p:output port="result">
                    <p:pipe port="report" step="run-package-doc-validation"/>
                </p:output>
                
                <pxi:nimas-fileset-validator.validate-package-doc name="run-package-doc-validation">
                    <p:input port="source">
                        <p:pipe port="source" step="nimas-fileset-validator.validate"/>
                    </p:input>
                    <p:with-option name="math" select="'false'"/>
                </pxi:nimas-fileset-validator.validate-package-doc>
                <p:sink/>
            </p:otherwise>
        </p:choose>

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

    <px:message message="Nimas fileset validator: Formatting report as HTML."/>
    <p:sink/>

    <px:validation-report-to-html name="format-as-html">
        <p:input port="source">
            <p:pipe port="result" step="validate-package-doc"/>
            <p:pipe port="result" step="validate-dtbooks"/>
        </p:input>
        <p:with-option name="toc" select="'true'"/>
    </px:validation-report-to-html>

    <px:validation-status name="format-validation-status">
        <p:input port="source">
            <p:pipe port="result" step="validate-package-doc"/>
            <p:pipe port="result" step="validate-dtbooks"/>
        </p:input>
    </px:validation-status>


</p:declare-step>
