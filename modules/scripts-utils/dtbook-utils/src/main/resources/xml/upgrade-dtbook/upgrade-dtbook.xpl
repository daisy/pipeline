<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-inline-prefixes="#all"
                type="px:dtbook-upgrade">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <h1 px:role="name">Upgrade DTBook</h1>
        <p px:role="desc">Upgrade a DTBook document from version 1.1.0, 2005-1, or 2005-2 to version
        2005-1, 2005-2 or 2005-3.</p>
        <!-- This module was imported from Pipeline 1 -->
        <div px:role="author maintainer">
            <p px:role="name">Marisa DeMeglio</p>
            <a px:role="contact" href="mailto:marisa.demeglio@gmail.com">marisa.demeglio@gmail.com</a>
            <p px:role="organization">DAISY Consortium</p>
        </div>
    </p:documentation>

    <p:input port="source" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>A single DTBook document</p>
        </p:documentation>
    </p:input>
    <p:output port="result" px:media-type="application/x-dtbook+xml">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The result DTBook document</p>
        </p:documentation>
    </p:output>

    <p:option name="version" select="'2005-3'">
        <p:documentation xmlns="http://www.w3.org/1999/xhtml">
            <p>The version of the output DTBook</p>
            <p>Supported values are 2005-1, 2005-2 and 2005-3.</p>
        </p:documentation>
    </p:option>

    <p:import href="http://www.daisy.org/pipeline/modules/common-utils/library.xpl">
        <p:documentation>
            px:assert
        </p:documentation>
    </p:import>

    <p:variable name="input-version" select="(/dtb:dtbook|/dtbook)/@version"/>

    <px:assert error-code="XXXXX" message="The output DTBook version must be '2005-1', '2005-2' or '2005-3', but got '$1'">
        <p:with-option name="test" select="$version=('2005-1','2005-2','2005-3')"/>
        <p:with-option name="param1" select="$version"/>
    </px:assert>
    <p:identity px:message="Input document version: {$input-version}" px:message-severity="DEBUG"/>

    <p:choose>
        <p:when test="not($input-version=('1.1.0','2005-1','2005-2','2005-3'))">
            <p:identity px:message="Version not identified: {$input-version}" px:message-severity="DEBUG"/>
        </p:when>
        <p:when test="$input-version=$version">
            <p:identity px:message="Version is already the desired version: {$input-version}" px:message-severity="DEBUG"/>
        </p:when>
        <p:when test="$input-version='2005-2' and $version='2005-1' or
                      $input-version='2005-3' and $version=('2005-1','2005-2')">
            <p:identity px:message="Version ({$input-version}) is already higher than the desired version ({$version})"
                        px:message-severity="DEBUG"/>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <!-- 1.1.0 to 2005-1 -->
    <p:choose>
        <p:when test="$input-version='1.1.0'">
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="dtbook110to2005-1.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <!-- 2005-1 to 2005-2 -->
    <p:choose>
        <p:when test="$input-version=('1.1.0','2005-1') and $version=('2005-2','2005-3')">
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="dtbook2005-1to2.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

    <!-- 2005-2 to 2005-3 -->
    <p:choose>
        <p:when test="$input-version=('1.1.0','2005-1','2005-2') and $version='2005-3'">
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="dtbook2005-2to3.xsl"/>
                </p:input>
                <p:input port="parameters">
                    <p:empty/>
                </p:input>
            </p:xslt>
        </p:when>
        <p:otherwise>
            <p:identity/>
        </p:otherwise>
    </p:choose>

</p:declare-step>
