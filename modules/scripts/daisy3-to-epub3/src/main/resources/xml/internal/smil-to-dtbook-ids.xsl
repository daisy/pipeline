<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                xmlns:smil="http://www.w3.org/2001/SMIL20/"
                xmlns:dtbook="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

    <!--
        It is assumed that SMIL and content documents have been prepared so that all ID attributes
        are unique in the whole publication.
    -->

    <xsl:variable name="dtbook-smils" select="collection()[/smil:smil]"/>
    <xsl:variable name="dtbooks" select="collection()[/dtbook:dtbook]"/>

    <xsl:template name="create-id-map">
        <d:fileset>
            <xsl:variable name="ids" as="element(d:anchor)*">
                <xsl:for-each select="$dtbook-smils">
                    <xsl:variable name="smil-uri" select="base-uri(.)"/>
                    <xsl:apply-templates select="/*">
                        <xsl:with-param name="smil-uri" tunnel="yes" select="$smil-uri"/>
                    </xsl:apply-templates>
                </xsl:for-each>
            </xsl:variable>
            <xsl:for-each-group select="$ids" group-by="@original-href">
                <xsl:for-each-group select="current-group()" group-by="@original-href">
                    <d:file>
                        <xsl:sequence select="@href|@original-href"/>
                        <xsl:for-each select="current-group()">
                            <xsl:copy>
                                <xsl:sequence select="@id|@original-id"/>
                            </xsl:copy>
                        </xsl:for-each>
                    </d:file>
                </xsl:for-each-group>
            </xsl:for-each-group>
        </d:fileset>
    </xsl:template>

    <xsl:template match="smil:text[@id]|
                         smil:*[@id][count(smil:text)=1]">
        <xsl:param name="smil-uri" tunnel="yes" required="yes"/>
        <xsl:variable name="smil-id" select="@id"/>
        <xsl:variable name="dtbook-id" select="substring-after(descendant-or-self::smil:text/@src,'#')"/>
        <xsl:variable name="dtbook-uri" select="substring-before(descendant-or-self::smil:text/@src,'#')"/>
        <d:anchor original-href="{$smil-uri}"
                  original-id="{$smil-id}"
                  href="{$dtbook-uri}"
                  id="{$dtbook-id}"/>
    </xsl:template>

    <xsl:template match="smil:seq[@id]">
        <xsl:param name="smil-uri" tunnel="yes" required="yes"/>
        <xsl:variable name="smil-id" select="@id"/>
        <xsl:variable name="dtbook-smilref" as="attribute()?"
                      select="$dtbooks//@smilref[substring-after(.,'#')=$smil-id]
                                                [resolve-uri(substring-before(.,'#'),base-uri(..))=$smil-uri]
                                                [1]"/>
        <xsl:if test="exists($dtbook-smilref/../@id)">
            <xsl:variable name="dtbook-id" select="$dtbook-smilref/../@id"/>
            <xsl:variable name="dtbook-uri" select="base-uri($dtbook-smilref/..)"/>
            <d:anchor original-href="{$smil-uri}"
                      original-id="{$smil-id}"
                      href="{$dtbook-uri}"
                      id="{$dtbook-id}"/>
        </xsl:if>
        <xsl:next-match/>
    </xsl:template>

    <xsl:template match="@*|*">
        <xsl:apply-templates select="@*|*"/>
    </xsl:template>

</xsl:stylesheet>
