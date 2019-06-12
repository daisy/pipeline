<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0" xmlns="http://www.w3.org/1999/xhtml"
    xpath-default-namespace="http://www.w3.org/1999/xhtml" xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <!--
        Improve HTML reports readability
        
        - xpath expressions are not useful for most people; make it more readable and put it into the "hover" text of the list item (title attribute)
        - delete <h3>Location (XPath)</h3> headlines
        - don't use full file: URIs in headlines
        - remove technical info from RNG messages
        - add information about inaccurate line numbers
        - add horizontal lines (<hr/>) between reports
    -->

    <xsl:template match="@* | node()">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@class[tokenize(.,'\s+')=('error','fatal','exception','message-error','message-fatal','message-exception')]">
        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
        <xsl:attribute name="style" select="concat(parent::*/@style, 'background-color: #f2dede; ')"/>
    </xsl:template>

    <xsl:template match="@class[tokenize(.,'\s+')=('warn','warning','message-warn','message-warning')]">
        <xsl:copy-of select="." exclude-result-prefixes="#all"/>
        <xsl:attribute name="style" select="concat(parent::*/@style, 'background-color: #fcf8e3; ')"/>
    </xsl:template>

    <xsl:template match="li[.//pre[starts-with(text(),'/*')]]">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="title" select="replace(replace((.//pre[starts-with(text(),'/*')]/text())[1],'\*:',''),'\[namespace[^\]]*\]','')"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="pre[starts-with(text(),'/*')]"/>

    <xsl:template match="h2[ancestor::div[@class='document-validation-report']]">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="style" select="string-join((@style,'font-size: 24px;'),' ')"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="code[parent::h2]">
        <span>
            <xsl:apply-templates select="@*"/>
            <xsl:value-of select="if (starts-with(text()[1],'file:')) then replace(text()[1],'.*/','') else text()[1]"/>
        </span>
    </xsl:template>

    <xsl:template match="h3[text()='Location (XPath)']" priority="2"/>
    <xsl:template match="h3[ancestor::div[@class='document-validation-report']]">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="style" select="string-join((@style,'font-size: 18px;'),' ')"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="p[text()='0 issues found.']">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="style" select="string-join((@style, 'background-color: #AAFFAA;'),' ')"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="p[starts-with(text()[1],'org.xml.sax.SAXParseException')]">
        <xsl:variable name="parts" select="tokenize(text()[1],'; ')"/>
        <xsl:variable name="filename" select="replace($parts[starts-with(.,'systemId:')][1],'.*/','')"/>
        <xsl:variable name="lineNumber" select="replace($parts[starts-with(.,'lineNumber:')][1],'[^\d]','')"/>
        <xsl:variable name="columnNumber" select="replace($parts[starts-with(.,'columnNumber:')][1],'[^\d]','')"/>
        <xsl:variable name="message"
            select="string-join($parts[not(starts-with(.,'org.xml.sax.SAXParseException') or starts-with(.,'systemId:') or starts-with(.,'lineNumber:') or starts-with(.,'columnNumber:'))],'; ')"/>
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@*"/>
            <xsl:value-of select="$filename"/>
            <xsl:if test="$lineNumber or $columnNumber">
                <xsl:text> </xsl:text>
                <xsl:value-of select="if ($filename) then '(' else ''"/>
                <xsl:value-of select="if ($lineNumber) then concat('line: ',$lineNumber) else ''"/>
                <xsl:value-of select="if ($lineNumber and $columnNumber) then ', ' else ''"/>
                <xsl:value-of select="if ($columnNumber) then concat('column: ',$columnNumber) else ''"/>
                <xsl:value-of select="if ($filename) then ')' else ''"/>
            </xsl:if>
            <xsl:value-of select="if ($filename or $lineNumber or $columnNumber) then ' ' else ''"/>
            <xsl:value-of select="$message"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="ul[.//text()[contains(., 'lineNumber')]]">
        <p>Note that line numbers tend to be offset by a couple of lines because the doctype and xml declarations are not counted as lines.</p>
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@* | node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="div[@class='document-validation-report' and count(*)=0]" priority="2"/>
    <xsl:template match="div[@class='document-validation-report']">
        <xsl:copy exclude-result-prefixes="#all">
            <xsl:apply-templates select="@* | node()"/>
            <hr/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
