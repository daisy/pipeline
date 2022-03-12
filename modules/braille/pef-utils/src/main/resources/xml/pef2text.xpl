<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step xmlns:p="http://www.w3.org/ns/xproc" version="1.0"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pef="http://www.daisy.org/ns/2008/pef"
                xmlns:c="http://www.w3.org/ns/xproc-step"
                type="pef:pef2text">

    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Convert a PEF document into a textual (ASCII-based) format.</p>
    </p:documentation>

    <p:input port="source" sequence="false" primary="true"/>
    <p:option name="output-dir" required="true"/> <!-- URI -->
    <p:option name="file-format" required="false" select="''"/> <!-- query format -->
    <p:option name="line-breaks" required="false" select="''"/>
    <p:option name="page-breaks" required="false" select="''"/>
    <p:option name="pad" required="false" select="''"/>
    <p:option name="charset" required="false" select="''"/>
    <!-- the name for a single volume -->
    <p:option name="single-volume-name" required="false" select="''"/>
    <!--
        the name for multiple volumes, {} is replaced by the volume number
        if empty, then the PEF is not split
    -->
    <p:option name="name-pattern" required="false" select="''"/>
    <!--
        the width of the volume number,
        if 0, then the volume number is not padded with zeroes
    -->
    <p:option name="number-width" required="false" select="''"/>

    <!--
        Implemented in ../../java/org/daisy/pipeline/braille/pef/calabash/impl/PEF2TextStep.java
    -->
    <p:declare-step type="pxi:pef2text">
        <p:input port="source" sequence="false" primary="true"/>
        <p:option name="output-dir"/>
        <p:option name="file-format"/>
        <p:option name="line-breaks"/>
        <p:option name="page-breaks"/>
        <p:option name="pad"/>
        <p:option name="charset"/>
        <p:option name="single-volume-name"/>
        <p:option name="name-pattern"/>
        <p:option name="number-width"/>
    </p:declare-step>

    <p:import href="http://www.daisy.org/pipeline/modules/braille/common-utils/library.xpl">
        <p:documentation>
            px:parse-query
        </p:documentation>
    </p:import>

    <p:choose>
        <p:xpath-context>
            <p:pipe step="file-format" port="result"/>
        </p:xpath-context>
        <p:when test="//c:param[@name='blank-last-page'][lower-case(@value)=('true','yes')]">
            <!--
                ensure each volume has a blank backside
            -->
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="blank-last-page.xsl"/>
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

    <p:choose>
        <p:xpath-context>
            <p:pipe step="file-format" port="result"/>
        </p:xpath-context>
        <p:when test="//c:param[@name='sheets-multiple-of-two'][lower-case(@value)=('true','yes')]">
            <!--
                ensure volumes have a number of sheets that is a multiple of 2
            -->
            <p:xslt>
                <p:input port="stylesheet">
                    <p:document href="sheets-multiple-of-two.xsl"/>
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

    <pxi:pef2text>
        <p:with-option name="output-dir" select="$output-dir"/>
        <p:with-option name="file-format" select="$file-format"/>
        <p:with-option name="line-breaks" select="$line-breaks"/>
        <p:with-option name="page-breaks" select="$page-breaks"/>
        <p:with-option name="pad" select="$pad"/>
        <p:with-option name="charset" select="$charset"/>
        <p:with-option name="single-volume-name" select="$single-volume-name"/>
        <p:with-option name="name-pattern" select="$name-pattern"/>
        <p:with-option name="number-width" select="$number-width"/>
    </pxi:pef2text>

    <px:parse-query name="file-format">
        <p:with-option name="query" select="$file-format"/>
    </px:parse-query>
    <p:sink/>

</p:declare-step>
