<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    exclude-result-prefixes="dtb tmp" version="2.0"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp">
    
    
    <xsl:output indent="yes" method="xml"/>
    
    
    <xsl:template match="/">
        
        <xsl:message>Renaming annotation elements to identify block or phrase variants.</xsl:message>
        <xsl:apply-templates/>
    </xsl:template>
    
    <!-- identity template -->
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="dtb:annotation">
        <xsl:variable name="block-level-contents"
            select="tokenize('div,sidebar,caption,code-block,hd,list,note,p,blockquote,table,dl,dateline,epigraph,address,imggroup,poem,linegroup,samp', ',')"/>
        
        <xsl:choose>
            <xsl:when test="child::*/local-name() = $block-level-contents">
                <xsl:element name="tmp:annotation-block">
                    <xsl:apply-templates/>
                </xsl:element>
            </xsl:when>
            <!-- if it doesn't have any of the above elements, then all the contents must be from the list of phrase-level elements:
            em,strong,dfn,code-phrase,cite,abbr,acronym,img,byline,line,a,sent,w,pagenum,annoref,noteref
            -->
            <xsl:otherwise>
                <xsl:element name="tmp:annotation-phrase">
                    <xsl:apply-templates/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
        
        
    </xsl:template>
    
</xsl:stylesheet>
