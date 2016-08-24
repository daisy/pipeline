<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
    xmlns:f="http://www.daisy.org/ns/pipeline/internal-function"
    xmlns:tmp="http://www.daisy.org/ns/pipeline/tmp"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">
    
    <xsl:include href="moveout-generic.xsl"/>
    
    <xsl:template match="/">
        <xsl:message>Move out inlined lists</xsl:message>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:function name="f:is-target" as="xs:boolean">
        <xsl:param name="elem" as="item()*"/>
        <xsl:sequence select="exists($elem/self::dtb:list)"/>
    </xsl:function>
    <xsl:function name="f:is-valid-parent" as="xs:boolean">
        <xsl:param name="elem" as="element()"/>
        <xsl:sequence
            select="exists($elem/(self::tmp:annotation-block|self::dtb:prodnote|self::dtb:sidebar|self::dtb:address|self::dtb:covertitle|self::dtb:div|self::dtb:epigraph|self::dtb:imggroup|self::dtb:caption|self::tmp:code-block|self::dtb:kbd|self::dtb:li|self::dtb:note|self::dtb:img|self::dtb:blockquote|self::dtb:level|self::dtb:level1|self::dtb:level2|self::dtb:level3|self::dtb:level4|self::dtb:level5|self::dtb:level6|self::dtb:td|self::dtb:th|self::dtb:samp|self::tmp:item))"
        />
    </xsl:function>
    
    
</xsl:stylesheet>