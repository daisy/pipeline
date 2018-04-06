<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step version="1.0" name="upgrade-dtbook" type="px:upgrade-dtbook"
    xmlns:p="http://www.w3.org/ns/xproc"
    xmlns:c="http://www.w3.org/ns/xproc-step" 
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:px="http://www.daisy.org/ns/pipeline/xproc" 
    xmlns:dc="http://purl.org/dc/terms/"
    exclude-inline-prefixes="#all">
    <!-- 
        TODO: 
        * copy referenced resources (such as images)
    -->
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
    	<h1 px:role="name">Upgrade DTBook</h1>
        <p px:role="desc">Upgrade a DTBook document from version 1.1.0, 2005-1, or 2005-2 to version 2005-3. This module was imported from the Pipeline 1.</p>
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a px:role="contact" href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <p:input port="source" primary="true" px:name="in" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        	<h2 px:role="name">in</h2>
        	<p px:role="desc">Single DTBook file</p>
        </p:documentation>
    </p:input>
    <p:input port="parameters" kind="parameter"/>
    <p:output port="result">
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
    <p:import href="http://www.daisy.org/pipeline/modules/dtbook-utils/library.xpl"/>
    <p:import href="http://www.daisy.org/pipeline/modules/validation-utils/library.xpl"/>
    
    <p:variable name="version" select="(/dtb:dtbook|/dtbook)/@version"/>
    
    <px:message severity="DEBUG">
        <p:with-option name="message" select="concat('Input document version: ', $version)"/>    
    </px:message>
    
    <p:choose name="main">
        <p:when test="$version = '1.1.0'">
            <p:output port="result"/>
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="dtbook110to2005-1.xsl"/>
                </p:input>
            </p:xslt>
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="dtbook2005-1to2.xsl"/>
                </p:input>
            </p:xslt>
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="dtbook2005-2to3.xsl"/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:when test="$version = '2005-1'">
            <p:output port="result"/>
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="dtbook2005-1to2.xsl"/>
                </p:input>
            </p:xslt>
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="dtbook2005-2to3.xsl"/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:when test="$version = '2005-2'">
            <p:output port="result"/>
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="dtbook2005-2to3.xsl"/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:when test="$version = '2005-3'">
            <p:output port="result"/>
            <px:message severity="DEBUG">
                <p:with-option name="message" select="concat('File is already the most recent version: ', $version)"/>
            </px:message>
            <p:identity/>
        </p:when>
        <p:otherwise>
            <p:output port="result"/>
            <px:message severity="DEBUG">
                <p:with-option name="message" select="concat('Version not identified: ', $version)"/>
            </px:message>
            <p:identity/>
        </p:otherwise>
    </p:choose>
    
    <p:identity name="dtbook-validation-input"/>
    <px:dtbook-validator.select-schema name="dtbook-schema" dtbook-version="2005-3" mathml-version="2.0"/>
    <px:validate-with-relax-ng-and-report name="validate-dtbook">
        <p:input port="source">
            <p:pipe port="result" step="dtbook-validation-input"/>
        </p:input>
        <p:input port="schema">
            <p:pipe port="result" step="dtbook-schema"/>
        </p:input>
        <p:with-option name="assert-valid" select="$assert-valid"/>
    </px:validate-with-relax-ng-and-report>
    
</p:declare-step>
