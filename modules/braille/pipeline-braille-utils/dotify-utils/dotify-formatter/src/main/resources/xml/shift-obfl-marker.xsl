<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[not(ancestor-or-self::css:box[@type='inline'])]/@css:_obfl-marker"/>
    
    <xsl:template match="css:box[@type='inline']">
        <xsl:variable name="pending" as="attribute()*"
                      select="for $e in (preceding::*|ancestor::*)
                                          [not(ancestor-or-self::css:box[@type='inline'])]
                                          [@css:_obfl-marker]
                                        except (preceding::css:box|ancestor::css:box)
                                                 [@type='inline']
                                                 [last()]
                                               /(preceding::*|ancestor::*)
                              return $e/@css:_obfl-marker"/>
        
        <xsl:choose>
            <xsl:when test="exists($pending)">
                <xsl:copy>
                    <xsl:apply-templates select="@* except @css:_obfl-marker"/>
                    <xsl:attribute name="css:_obfl-marker" select="string-join(($pending, @css:_obfl-marker), ' ')"/>
                    <xsl:apply-templates/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
