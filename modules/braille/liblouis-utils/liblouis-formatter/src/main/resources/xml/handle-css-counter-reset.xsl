<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:louis="http://liblouis.org/liblouis"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:variable name="COUNTER_RESET_RE" select="concat('(',$css:IDENT_RE,')(\s+(', $css:INTEGER_RE, '))?')"/>
    <xsl:variable name="COUNTER_RESET_RE_counter" select="1"/>
    <xsl:variable name="COUNTER_RESET_RE_value" select="$COUNTER_RESET_RE_counter + $css:IDENT_RE_groups + 2"/>
    <xsl:variable name="COUNTER_RESET_RE_groups" select="$COUNTER_RESET_RE_value + $css:INTEGER_RE_groups"/>
    
    <xsl:template match="*[contains(string(@style), 'counter-reset')]">
        <xsl:variable name="properties"
            select="css:specified-properties('#all counter-reset', true(), true(), true(), .)"/>
        <xsl:variable name="counter-reset" as="xs:string" select="$properties[@name='counter-reset']/@value"/>
        <xsl:copy>
            <xsl:sequence select="@*[not(name()='style')]"/>
            <xsl:if test="$counter-reset!='none'">
                <xsl:analyze-string select="$counter-reset" regex="{$COUNTER_RESET_RE}">
                    <xsl:matching-substring>
                        <xsl:if test="regex-group($COUNTER_RESET_RE_counter)='braille-page'">
                            <xsl:attribute name="louis:braille-page-reset"
                                           select="if (regex-group($COUNTER_RESET_RE_value)!='')
                                                   then regex-group($COUNTER_RESET_RE_value) else '1'"/>
                        </xsl:if>
                    </xsl:matching-substring>
                </xsl:analyze-string>
                <xsl:if test="not(@xml:id)">
                    <xsl:attribute name="xml:id" select="generate-id()"/>
                </xsl:if>
            </xsl:if>
            <xsl:sequence select="css:style-attribute(css:serialize-declaration-list(
                                    $properties[not(@name='counter-reset')]))"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
