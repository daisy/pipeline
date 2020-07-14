<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="merge-dtbook" type="px:merge-dtbook"
    xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
    exclude-inline-prefixes="#all">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Merge DTBook</h1>
        <p px:role="desc">Merge 2 or more DTBook documents.</p>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a href="mailto:marisa.demeglio@gmail.com" px:role="contact">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>
    <!-- 
        TODO: 
         * copy referenced resources (such as images)
         * deal with xml:lang (either copy once and put in dtbook/@xml:lang or, if different languages are used, copy the @xml:lang attr into the respective sections.
    -->

    <p:input port="source" primary="true" sequence="true" px:name="in"
        px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">in</h2>
            <p px:role="desc">Sequence of DTBook files</p>
        </p:documentation>
    </p:input>
    <p:input port="parameters" kind="parameter"/>
    <p:output port="result" primary="true">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <h2 px:role="name">out</h2>
            <p px:role="desc">The result</p>
        </p:documentation>
    </p:output>
    
    <p:option name="assert-valid" required="false" px:type="boolean" select="'true'">
        <p:documentation>
            Whether to stop processing and raise an error on validation issues.
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl"/>
    <p:import href="../validate-dtbook/dtbook-validator.select-schema.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>
    
    <!--Loads the DTBook schema-->
    <px:dtbook-validator.select-schema name="dtbook-schema" dtbook-version="2005-3" mathml-version="2.0"/>
    
    <!--Store the first DTBook for later reference-->    
    <p:split-sequence name="first-dtbook" initial-only="true" test="position()=1">
        <p:input port="source">
            <p:pipe port="source" step="merge-dtbook"/>
        </p:input>
    </p:split-sequence>
    <px:message severity="DEBUG" message="Merging DTBook documents"/>
    <p:sink/>

    <p:for-each name="validate-input">
        <p:output port="result"/>

        <p:iteration-source>
            <p:pipe port="source" step="merge-dtbook"/>
        </p:iteration-source>

        <px:validate-with-relax-ng-and-report>
            <p:input port="schema">
                <p:pipe port="result" step="dtbook-schema"/>
            </p:input>
            <p:with-option name="assert-valid" select="$assert-valid"/>
        </px:validate-with-relax-ng-and-report>

    </p:for-each>
    
    <p:xslt template-name="merge">
        <p:input port="stylesheet">
            <p:document href="merge-dtbook.xsl"/>
        </p:input>
        <p:with-option name="output-base-uri" select="base-uri(/*)">
            <p:pipe port="matched" step="first-dtbook"/>
        </p:with-option>
    </p:xslt>
    
    <px:validate-with-relax-ng-and-report name="validate-dtbook">
        <p:input port="schema">
            <p:pipe port="result" step="dtbook-schema"/>
        </p:input>
        <p:with-option name="assert-valid" select="$assert-valid"/>
    </px:validate-with-relax-ng-and-report>

</p:declare-step>
