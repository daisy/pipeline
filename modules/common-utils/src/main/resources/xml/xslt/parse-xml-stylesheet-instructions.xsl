<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all" >

    <xsl:template match="/">
        <xsl:copy>
            <d:xml-stylesheet-instructions>
                <xsl:apply-templates select="/processing-instruction('xml-stylesheet')"/>
            </d:xml-stylesheet-instructions>
        </xsl:copy>
    </xsl:template>

    <xsl:variable name="XML_STYLESHEET_PSEUDO_ATTR_RE">((href|type|title|media|charset|alternate)|\w+)=("([^"]+)"|'([^']+)')</xsl:variable>

    <xsl:variable name="XML_STYLESHEET_RE"
                  select="concat('^\s*',$XML_STYLESHEET_PSEUDO_ATTR_RE,'(\s+',$XML_STYLESHEET_PSEUDO_ATTR_RE,')*\s*$')"/>

    <xsl:template match="/processing-instruction('xml-stylesheet')" as="element()?">
        <xsl:if test="matches(., $XML_STYLESHEET_RE)">
            <d:xml-stylesheet-instruction>
                <xsl:analyze-string select="." regex="{$XML_STYLESHEET_PSEUDO_ATTR_RE}">
                    <xsl:matching-substring>
                        <xsl:if test="regex-group(2)!=''">
                            <xsl:attribute name="{regex-group(2)}" select="normalize-space(concat(regex-group(4),regex-group(5)))"/>
                        </xsl:if>
                    </xsl:matching-substring>
                </xsl:analyze-string>
            </d:xml-stylesheet-instruction>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
