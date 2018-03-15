<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:d="http://www.daisy.org/ns/pipeline/data"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template name="main">
        <d:xml-stylesheets>
            <xsl:apply-templates select="/processing-instruction('xml-stylesheet')"/>
        </d:xml-stylesheets>
    </xsl:template>
    
    <xsl:variable name="XML_STYLESHEET_PSEUDO_ATTR_RE">(href|type|title|media|charset|alternate)=("([^"]+)"|'([^']+)')</xsl:variable>
        
    <xsl:variable name="XML_STYLESHEET_RE"
                  select="concat('^\s*',$XML_STYLESHEET_PSEUDO_ATTR_RE,'(\s+',$XML_STYLESHEET_PSEUDO_ATTR_RE,')*\s*$')"/>
    
    <xsl:template match="/processing-instruction('xml-stylesheet')" as="element()?">
        <xsl:if test="matches(., $XML_STYLESHEET_RE)">
            <d:xml-stylesheet>
                <xsl:analyze-string select="." regex="{$XML_STYLESHEET_PSEUDO_ATTR_RE}">
                    <xsl:matching-substring>
                        <xsl:attribute name="{regex-group(1)}" select="concat(regex-group(3),regex-group(4))"/>
                    </xsl:matching-substring>
                </xsl:analyze-string>
            </d:xml-stylesheet>
        </xsl:if>
    </xsl:template>
    
</xsl:stylesheet>
