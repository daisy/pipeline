<?xml version="1.0" encoding="UTF-8"?>
<p:declare-step type="obfl:compare" name="main"
                xmlns:p="http://www.w3.org/ns/xproc"
                xmlns:px="http://www.daisy.org/ns/pipeline/xproc"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-inline-prefixes="px pxi xsl"
                version="1.0">
    
    <p:documentation xmlns="http://www.w3.org/1999/xhtml">
        <p>Compare two <a href="http://code.google.com/p/obfl">OBFL</a> documents.</p>
    </p:documentation>
    
    <p:input port="source" primary="true" px:media-type="application/x-obfl+xml"/>
    <p:input port="alternate" px:media-type="application/x-obfl+xml"/>
    <p:output port="result" primary="false" sequence="false">
        <p:pipe step="compare" port="result"/>
    </p:output>
    
    <p:option name="fail-if-not-equal" select="'false'"/>
    
    <p:declare-step type="pxi:normalize-obfl">
        <p:input port="source"/>
        <p:output port="result"/>
        <p:xslt>
            <p:input port="stylesheet">
                <p:inline>
                    <xsl:stylesheet version="2.0">
                        <xsl:variable name="layout_names" as="xs:string*" select="/obfl:obfl/obfl:layout-master/@name/string()"/>
                        <xsl:variable name="toc_names" as="xs:string*" select="/obfl:obfl/obfl:table-of-contents/@name/string()"/>
                        <xsl:variable name="ids" as="xs:string*" select="//@id/string()"/>
                        <xsl:template match="@*|node()">
                            <xsl:copy>
                                <xsl:apply-templates select="@*|node()"/>
                            </xsl:copy>
                        </xsl:template>
                        <xsl:template match="obfl:layout-master/@name|
                                             obfl:sequence/@master|
                                             obfl:toc-sequence/@master|
                                             obfl:dynamic-sequence/@master">
                            <xsl:attribute name="{name(.)}" select="concat('layout',index-of($layout_names,string(.)))"/>
                        </xsl:template>
                        <xsl:template match="obfl:table-of-contents/@name|
                                             obfl:toc-sequence/@toc">
                            <xsl:attribute name="{name(.)}" select="concat('toc',index-of($toc_names,string(.)))"/>
                        </xsl:template>
                        <xsl:template match="@id|@ref-id|obfl:anchor/@item">
                            <xsl:attribute name="{name(.)}" select="concat('id',index-of($ids,string(.))[1])"/>
                        </xsl:template>
                        <xsl:template match="obfl:toc-entry-on-resumed/@range">
                            <xsl:variable name="range-start" as="xs:string"  select="replace(string(.),'^\[(.+?),(.*?)\)$','$1')"/>
                            <xsl:variable name="range-end"   as="xs:string?" select="replace(string(.),'^\[(.+?),(.*?)\)$','$2')"/>
                            <xsl:attribute name="{name(.)}" select="concat('[',
                                                                           concat('id',index-of($ids,$range-start)[1]),
                                                                           ',',
                                                                           if ($range-end)
                                                                             then concat('id',index-of($ids,$range-end)[1])
                                                                             else '',
                                                                           ')')"/>
                        </xsl:template>
                    </xsl:stylesheet>
                </p:inline>
            </p:input>
            <p:input port="parameters">
                <p:empty/>
            </p:input>
        </p:xslt>
        <p:delete match="/*/@xml:space"/>
        <p:string-replace match="text()" replace="normalize-space(.)"/>
    </p:declare-step>
    
    <pxi:normalize-obfl name="normalize-source">
        <p:input port="source">
            <p:pipe step="main" port="source"/>
        </p:input>
    </pxi:normalize-obfl>
    
    <pxi:normalize-obfl name="normalize-alternate">
        <p:input port="source">
            <p:pipe step="main" port="alternate"/>
        </p:input>
    </pxi:normalize-obfl>
    
    <p:compare name="compare">
        <p:input port="source">
            <p:pipe step="normalize-source" port="result"/>
        </p:input>
        <p:input port="alternate">
            <p:pipe step="normalize-alternate" port="result"/>
        </p:input>
        <p:with-option name="fail-if-not-equal" select="$fail-if-not-equal"/>
    </p:compare>
    
</p:declare-step>
