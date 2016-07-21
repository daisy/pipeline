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
    
    <xsl:template match="*[not(self::css:box) and not(ancestor::css:box[@type='inline'])]/@css:string-set"/>
    
    <xsl:template match="css:box">
        <xsl:variable name="pending" as="attribute()*"
                      select="for $e in (preceding::*|ancestor::*)[not(self::css:box) and not(ancestor::css:box[@type='inline'])]
                                                                  [@css:string-set]
                                        except (preceding::css:box|ancestor::css:box)
                                               [last()]/(preceding::*|ancestor::*)
                              return $e/@css:string-set"/>
        <xsl:choose>
            <xsl:when test="exists($pending)">
                <xsl:copy>
                    <xsl:apply-templates select="@* except @css:string-set"/>
                    <xsl:attribute name="css:string-set" select="string-join(($pending, @css:string-set), ', ')"/>
                    <xsl:apply-templates/>
                </xsl:copy>
            </xsl:when>
            <xsl:otherwise>
                <xsl:next-match/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
</xsl:stylesheet>
