<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:dc="http://purl.org/dc/terms/"
    xpath-default-namespace="http://www.daisy.org/z3986/2005/dtbook/" version="2.0">

    <xsl:output method="xml" indent="yes"/>

    <xsl:template name="merge">
        <dtbook version="2005-3">
            <head>
                <xsl:variable name="metas" select="collection()/dtbook/head/meta"/>
                
                <!-- create our own identifier to represent the merged version of the book. -->
                <!-- TODO: this should be a user-customizable option -->
                <meta name="dc:Identifier" content="{$metas[@name='dc:Identifier'][1]/@content}-merged"/>
                
                <!-- copy all non-duplicate metadata except identifiers -->
                <xsl:for-each-group select="$metas[not(@name = 'dc:Identifier') and not(@name = 'dtb:uid')]" group-by="concat(@name,@content)">
                    <xsl:apply-templates select="current()"/>
                </xsl:for-each-group>                
            </head>
            <book>
                <!--TODO merge @class, copy @xml:space, @xml:id, @dir, @xml:lang-->
                <xsl:if test="collection()/dtbook/book/frontmatter">
                    <frontmatter>
                        <!-- only one top-level doctitle is allowed, copy the first -->
                        <xsl:apply-templates select="(collection()/dtbook/book/frontmatter)[1]/doctitle"/>
                        <!-- only one top-level covertitle is allowed, copy the first -->
                        <xsl:apply-templates select="(collection()/dtbook/book/frontmatter/covertitle)[1]"/>
                        <!-- multiple docauthors allowed, just filter the duplicates -->
                        <xsl:for-each-group select="collection()/dtbook/book/frontmatter/docauthor" group-by="normalize-space()">
                            <xsl:apply-templates select="current()"/>
                        </xsl:for-each-group>
                        <xsl:apply-templates select="collection()/dtbook/book/frontmatter/(level|level1)"/>
                    </frontmatter>
                </xsl:if>
                <xsl:if test="collection()/dtbook/book/bodymatter">
                    <bodymatter>
                        <xsl:apply-templates select="collection()/dtbook/book/bodymatter/*"/>
                    </bodymatter>
                </xsl:if>
                <xsl:if test="collection()/dtbook/book/rearmatter">
                    <rearmatter>
                        <xsl:apply-templates select="collection()/dtbook/book/rearmatter/*"/>
                    </rearmatter>
                </xsl:if>
            </book>
        </dtbook>
    </xsl:template>

    <xsl:template match="node() | @*">
        <xsl:copy>
            <xsl:apply-templates select="node() | @*"/>
        </xsl:copy>
    </xsl:template>


</xsl:stylesheet>
