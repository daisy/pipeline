<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:include href="../xslt/generate-id.xsl"/>

    <xsl:param name="next-doc" as="xs:integer"/>

    <xsl:variable name="all-docs" select="collection()"/>

    <xsl:key name="ids" match="*[@id]" use="@id"/>

    <xsl:function name="pf:first-element-in-all-docs">
        <xsl:param name="id" as="xs:string"/>
        <xsl:sequence select="(for $d in $all-docs return $d/key('ids',$id))[1]"/>
    </xsl:function>

    <xsl:template name="main">
        <xsl:apply-templates select="collection()[position()=$next-doc]"/>
    </xsl:template>

    <xsl:template match="/">
        <xsl:copy>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="/*" priority="1">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'id_'"/>
            <xsl:with-param name="for-elements"
                            select="//*[@id][not(pf:first-element-in-all-docs(@id) is .)]|
                                    //*[@pxi:need-id][not(@id)]"/>
            <xsl:with-param name="in-use" select="$all-docs//@id"/>
        </xsl:call-template>
    </xsl:template>

    <!-- Remove duplicate IDs -->

    <xsl:template match="@id">
        <xsl:choose>
            <xsl:when test="pf:first-element-in-all-docs(.) is ..">
                <xsl:sequence select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="..">
                    <xsl:call-template name="pf:generate-id"/>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="/*" priority=".9">
        <xsl:next-match/>
        <xsl:result-document href="mapping">
            <d:file href="{base-uri()}">
                <xsl:for-each select=".//*[@id][not(pf:first-element-in-all-docs(@id) is .)]">
                    <d:anchor>
                        <xsl:call-template name="pf:generate-id"/>
                        <xsl:attribute name="original-id" select="@id"/>
                    </d:anchor>
                </xsl:for-each>
            </d:file>
        </xsl:result-document>
    </xsl:template>

    <!-- Add missing IDs -->

    <xsl:template match="*[@pxi:need-id][not(@id)]">
        <xsl:copy copy-namespaces="no">
            <xsl:for-each select="namespace::*[not(.='http://www.daisy.org/ns/pipeline/xproc/internal')]">
                <xsl:sequence select="."/>
            </xsl:for-each>
            <xsl:call-template name="pf:generate-id"/>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@pxi:need-id"/>

    <xsl:template match="@*|*">
        <xsl:copy copy-namespaces="no">
            <xsl:for-each select="namespace::*[not(.='http://www.daisy.org/ns/pipeline/xproc/internal')]">
                <xsl:sequence select="."/>
            </xsl:for-each>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="text()|processing-instruction()|comment()">
        <xsl:sequence select="."/>
    </xsl:template>

</xsl:stylesheet>
