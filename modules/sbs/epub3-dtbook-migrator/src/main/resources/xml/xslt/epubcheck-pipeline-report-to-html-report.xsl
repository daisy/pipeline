<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="xs" version="2.0" xmlns="http://www.w3.org/1999/xhtml"
    xpath-default-namespace="http://www.w3.org/1999/xhtml" xmlns:d="http://www.daisy.org/ns/pipeline/data">

    <xsl:template match="/*">
        <html>
            <head>
                <meta encoding="utf-8"/>
                <title>EpubCheck report for <xsl:value-of select="d:document-info/d:document-name/replace(., '^file:.*/','')"/></title>
            </head>
            <body>
                <div class="document-validation-report" id="{generate-id()}">
                    <xsl:apply-templates select="d:document-info"/>
                    <xsl:apply-templates select="d:reports"/>
                </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="d:document-info">
        <div class="document-info">
            <xsl:apply-templates select="d:document-name"/>
            <xsl:choose>
                <xsl:when test="d:error-count/text() = '1'">
                    <p>1 issue found.</p>
                </xsl:when>
                <xsl:otherwise>
                    <p><xsl:value-of select="d:error-count/text()"/> issues found.</p>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:apply-templates select="d:document-type | d:document-path"/>
            <xsl:apply-templates select="d:properties"/>
        </div>
    </xsl:template>

    <xsl:template match="d:document-name">
        <h2>EpubCheck report for <xsl:value-of select="replace(., '^file:.*/','')"/></h2>
    </xsl:template>

    <xsl:template match="d:document-type">
        <p>Validated as <code><xsl:value-of select="text()"/></code></p>
    </xsl:template>

    <xsl:template match="d:document-path">
        <p>Path: <code><xsl:value-of select="text()"/></code></p>
    </xsl:template>

    <xsl:template match="d:properties | d:property[d:property]">
        <h3>
            <xsl:value-of select="if (self::d:properties) then 'Properties' else @name"/>
        </h3>
        <table class="table table-condensed">
            <thead>
                <tr>
                    <th>Property</th>
                    <th>Value</th>
                </tr>
            </thead>
            <tbody>
                <xsl:apply-templates select="d:property[not(d:property)]"/>
            </tbody>
        </table>
        <xsl:apply-templates select="d:property[d:property]"/>
    </xsl:template>

    <xsl:template match="d:property[not(d:property)]">
        <tr>
            <td>
                <xsl:value-of select="@name"/>
            </td>
            <td>
                <xsl:if test="@content">
                    <xsl:value-of select="@content"/>
                </xsl:if>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="d:reports">
        <xsl:if test="d:report/*">
            <div class="document-report">
                <xsl:apply-templates select="d:report"/>
            </div>
        </xsl:if>
    </xsl:template>

    <xsl:template match="d:report">
        <xsl:apply-templates select="*"/>
    </xsl:template>

    <xsl:template match="d:exceptions | d:errors | d:warnings | d:hints">
        <h3 id="{generate-id()}">
            <xsl:value-of select="concat(upper-case(substring(local-name(),1,1)),lower-case(substring(local-name(),2)))"/>
        </h3>
        <table class="document-{local-name()} table table-bordered">
            <thead>
                <tr>
                    <th>File</th>
                    <th>Line</th>
                    <th>Position</th>
                    <th>Message</th>
                </tr>
            </thead>
            <tbody>
                <xsl:apply-templates select="*"/>
            </tbody>
        </table>
    </xsl:template>

    <xsl:template match="d:exception | d:error | d:warn | d:hint">
        <tr class="{if (local-name()=('exception','error')) then 'error' else if (local-name()='warn') then 'warning' else ''}">
            <td>
                <xsl:value-of select="(d:file,d:location/@href,'-')[1]"/>
            </td>
            <td>
                <xsl:value-of select="(d:location/@line,'-')[1]"/>
            </td>
            <td>
                <xsl:value-of select="(d:location/@column,'-')[1]"/>
            </td>
            <td>
                <xsl:value-of select="d:desc"/>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
