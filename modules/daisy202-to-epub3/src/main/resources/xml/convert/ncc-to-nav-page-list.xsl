<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns="http://www.w3.org/1999/xhtml" xmlns:epub="http://www.idpf.org/2007/ops" version="2.0" exclude-result-prefixes="#all">

    <xsl:output indent="yes" include-content-type="no"/>

    <xsl:template match="/*">
        <nav epub:type="page-list" id="page-list">
            <xsl:apply-templates select="*"/>
        </nav>
    </xsl:template>
    
    <xsl:template match="*[local-name()='head']"/>

    <xsl:template match="*[local-name()='body']">
            <xsl:if test="child::*[local-name()='span' and starts-with(@class,'page-')]">
                    <ol>
                        <xsl:for-each select="child::*[local-name()='span' and starts-with(@class,'page-')]">
                            <li>
                                <a href="{child::*[local-name()='a'][1]/@href}">
                                    <xsl:if test="@id">
                                        <xsl:attribute name="id" select="@id"/>
                                    </xsl:if>
                                    <xsl:value-of select="child::*[local-name()='a'][1]"/>
                                </a>
                            </li>
                        </xsl:for-each>
                    </ol>
            </xsl:if>
    </xsl:template>

</xsl:stylesheet>
