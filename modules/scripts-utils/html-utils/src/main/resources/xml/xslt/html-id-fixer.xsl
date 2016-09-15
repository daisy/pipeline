<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xpath-default-namespace="http://www.w3.org/1999/xhtml"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-functions"
    xmlns:epub="http://www.idpf.org/2007/ops" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="#all" version="2.0">

    <xsl:key name="ids" match="*[@id]" use="@id"/>
    
    <xsl:function name="f:generate-new-id" as="xs:string">
        <xsl:param name="node" as="node()"/>
        <xsl:sequence select="f:generate-new-id($node,0)"/>
    </xsl:function>
    
    <xsl:function name="f:generate-new-id" as="xs:string">
        <xsl:param name="node" as="node()"/>
        <xsl:param name="seq" as="xs:integer"/>
        <xsl:variable name="gid" select="concat(generate-id($node), '-', $seq)"/>
        <xsl:sequence
            select="if (exists($node/key('ids',$gid))) then 
            f:generate-new-id($node, $seq+1) else $gid"
        />
    </xsl:function>
    

    <!--Remove duplicate IDs-->
    <xsl:template match="@id">
        <xsl:attribute name="id" select="if (key('ids',.)[1] != ..) then f:generate-new-id(.) else ."/>
    </xsl:template>



    <xsl:template match="body|article|aside|nav|section">
        <xsl:copy>
            <xsl:if test="empty(@id)">
                <xsl:attribute name="id" select="f:generate-new-id(.)"/>
            </xsl:if>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="h1|h2|h3|h4|h5|h6|hgroup">
        <xsl:copy>
            <xsl:if test="empty(@id)">
                <xsl:attribute name="id" select="f:generate-new-id(.)"/>
            </xsl:if>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="*[@epub:type='pagebreak']">
        <!--TODO FIXME: epub:type can have several values-->
        <xsl:copy>
            <xsl:if test="empty(@id)">
                <xsl:attribute name="id" select="f:generate-new-id(.)"/>
            </xsl:if>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
