<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:re="regex-utils"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <!-- ============ -->
    <!-- Query Syntax -->
    <!-- ============ -->
    
    <!--
        [ <ident> | <string> | <integer> ]
    -->
    <xsl:variable name="css:ANY_RE" select="re:or(($css:IDENT_RE,$css:STRING_RE,$css:INTEGER_RE))"/>
    <xsl:variable name="css:ANY_RE_ident" select="1"/>
    <xsl:variable name="css:ANY_RE_string" select="$css:ANY_RE_ident + $css:IDENT_RE_groups + 1"/>
    <xsl:variable name="css:ANY_RE_integer" select="$css:ANY_RE_string + $css:STRING_RE_groups + 1"/>
    <xsl:variable name="css:ANY_RE_groups" select="$css:ANY_RE_integer + $css:INTEGER_RE_groups"/>
    
    <!--
        <feature> = '(' <ident> [ ':' <value> ]? ')'
    -->
    <xsl:variable name="css:FEATURE_RE" select="concat('\(\s*(',$css:IDENT_RE,')(\s*:\s*(',re:space-separated($css:ANY_RE),'))?\s*\)')"/>
    <xsl:variable name="css:FEATURE_RE_ident" select="1"/>
    <xsl:variable name="css:FEATURE_RE_value" select="$css:FEATURE_RE_ident + $css:IDENT_RE_groups + 2"/>
    <xsl:variable name="css:FEATURE_RE_groups" select="$css:FEATURE_RE_value + re:space-separated-groups($css:ANY_RE_groups)"/>
    
    <!--
        <query> = <feature> +
    -->
    <xsl:variable name="css:QUERY_RE" select="concat('(',$css:FEATURE_RE,'\s*)+')"/>
    <xsl:variable name="css:QUERY_RE_groups" select="1 + $css:FEATURE_RE_groups"/>
    
</xsl:stylesheet>
