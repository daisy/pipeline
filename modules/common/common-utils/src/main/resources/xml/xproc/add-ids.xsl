<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pxi="http://www.daisy.org/ns/pipeline/xproc/internal"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all">

    <xsl:include href="../xslt/generate-id.xsl"/>

    <xsl:key name="ids" match="*[@id]" use="@id"/>
    
    <xsl:template match="/*" priority="1">
        <xsl:call-template name="pf:next-match-with-generated-ids">
            <xsl:with-param name="prefix" select="'id_'"/>
            <xsl:with-param name="for-elements"
                            select="//*[@id][not(key('ids',@id)[1] is .)]|
                                    //*[@pxi:need-id][not(@id)]"/>
        </xsl:call-template>
    </xsl:template>

    <!-- Remove duplicate IDs -->

    <xsl:template match="@id">
        <xsl:choose>
            <xsl:when test="key('ids',.)[1] is ..">
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
            <d:fileset>
                <xsl:for-each select="*">
                    <d:file href="{base-uri()}">
                        <xsl:for-each select=".//*[@id][not(key('ids',@id)[1] is .)]">
                            <d:anchor>
                                <xsl:call-template name="pf:generate-id"/>
                                <xsl:attribute name="original-id" select="@id"/>
                            </d:anchor>
                        </xsl:for-each>
                    </d:file>
                </xsl:for-each>
            </d:fileset>
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

    <!-- in order to delete the pxi namespace -->
    <xsl:template match="/pxi:wrapper">
        <xsl:element name="_">
            <xsl:apply-templates select="@*|node()"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="@*|*">
        <xsl:copy copy-namespaces="no">
            <xsl:for-each select="namespace::*[not(.='http://www.daisy.org/ns/pipeline/xproc/internal')]">
                <xsl:sequence select="."/>
            </xsl:for-each>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
