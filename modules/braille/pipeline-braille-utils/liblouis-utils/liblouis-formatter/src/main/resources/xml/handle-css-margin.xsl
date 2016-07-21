<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:louis="http://liblouis.org/liblouis"
    xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
    exclude-result-prefixes="#all"
    version="2.0">
    
    <!--
      * Combine margin-left and margin-right with left and right
      * Add @width attribute on louis:box
      * Insert line of dots in louis:border
      * Lower-bound text-indent
    -->
    <xsl:param name="louis:page-width" as="xs:string"/>
    
    <!--
        css-utils [2.0.0,3.0.0)
    -->
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="@*|text()|comment()|processing-instruction()">
        <xsl:copy/>
    </xsl:template>
    
    <xsl:template match="louis:page-layout">
        <xsl:sequence select="."/>
    </xsl:template>
    
    <xsl:template match="louis:border">
        <xsl:param name="width" as="xs:integer" select="xs:integer(number($louis:page-width))" tunnel="yes"/>
        <xsl:copy>
            <xsl:value-of select="string-join(for $x in 1 to $width return string(@pattern), '')"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="louis:box">
        <xsl:param name="width" as="xs:integer" select="xs:integer(number($louis:page-width))" tunnel="yes"/>
        <xsl:param name="used-left" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:param name="used-right" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:param name="box-offset-left" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:param name="box-offset-right" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:copy>
            <xsl:variable name="width" as="xs:integer"
                          select="$width
                                  - (if (@border-left='none') then 0 else 1)
                                  - (if (@border-right='none') then 0 else 1)"/>
            <xsl:apply-templates select="@*"/>
            <xsl:attribute name="width" select="$width"/>
            <xsl:apply-templates select="node()">
                <xsl:with-param name="actual-left" select="0" tunnel="yes"/>
                <xsl:with-param name="actual-right" select="0" tunnel="yes"/>
                <xsl:with-param name="actual-text-indent" select="0" tunnel="yes"/>
                <xsl:with-param name="width" select="$width" tunnel="yes"/>
                <xsl:with-param name="box-offset-left" select="$box-offset-left + $used-left" tunnel="yes"/>
                <xsl:with-param name="box-offset-right" select="$box-offset-right + $used-right" tunnel="yes"/>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[matches(string(@style), 'margin-left|margin-right|left|right|text-indent')]">
        <xsl:param name="used-left" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:param name="used-right" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:param name="actual-left" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:param name="actual-right" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:param name="actual-text-indent" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:param name="width" as="xs:integer" select="xs:integer(number($louis:page-width))" tunnel="yes"/>
        <xsl:param name="box-offset-left" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:param name="box-offset-right" as="xs:integer" select="0" tunnel="yes"/>
        <xsl:variable name="properties" select="css:specified-properties(
                                                  '#all margin-left margin-right text-indent left right',
                                                  true(), true(), true(), .)"/>
        <xsl:variable name="margin-left" as="xs:integer" select="xs:integer(number($properties[@name='margin-left']/@value))"/>
        <xsl:variable name="margin-right" as="xs:integer" select="xs:integer(number($properties[@name='margin-right']/@value))"/>
        <xsl:variable name="text-indent" as="xs:integer" select="xs:integer(number($properties[@name='text-indent']/@value))"/>
        <xsl:variable name="left" as="xs:string" select="$properties[@name='left']/@value"/>
        <xsl:variable name="right" as="xs:string" select="$properties[@name='right']/@value"/>
        <xsl:variable name="used-left" as="xs:integer"
            select="(if (not($left='auto')) then xs:integer(number($left)) else $used-left) + $margin-left"/>
        <xsl:variable name="used-right" as="xs:integer"
            select="(if (not($right='auto')) then xs:integer(number($right)) else $used-right) + $margin-right"/>
        <xsl:variable name="parent-actual-left" as="xs:integer" select="$actual-left"/>
        <xsl:variable name="parent-actual-right" as="xs:integer" select="$actual-right"/>
        <xsl:variable name="parent-actual-text-indent" as="xs:integer" select="$actual-text-indent"/>
        <xsl:variable name="actual-left" as="xs:integer" select="max((0, $used-left - $box-offset-left))"/>
        <xsl:variable name="actual-right" as="xs:integer" select="max((0, $used-right - $box-offset-right))"/>
        <xsl:variable name="actual-text-indent" as="xs:integer" select="max((- $actual-left, $text-indent))"/>
        <xsl:copy>
          <xsl:apply-templates select="@*[not(name()='style')]"/>
          <xsl:sequence
              select="css:style-attribute(
                        css:serialize-declaration-list((
                          $properties[not(@name=('left', 'right', 'margin-left', 'margin-right','text-indent'))],
                          if ($actual-left != $parent-actual-left)
                            then css:property('left', $actual-left) else (),
                          if ($actual-right != $parent-actual-right)
                            then css:property('right', $actual-right) else (),
                          if ($actual-text-indent != $parent-actual-text-indent)
                            then css:property('text-indent', $actual-text-indent) else ())))"/>
          <xsl:apply-templates select="node()">
            <xsl:with-param name="used-left" select="$used-left" tunnel="yes"/>
            <xsl:with-param name="used-right" select="$used-right" tunnel="yes"/>
            <xsl:with-param name="actual-left" select="$actual-left" tunnel="yes"/>
            <xsl:with-param name="actual-right" select="$actual-right" tunnel="yes"/>
            <xsl:with-param name="actual-text-indent" select="$actual-text-indent" tunnel="yes"/>
            <xsl:with-param name="width" tunnel="yes"
                            select="$width + $parent-actual-left + $parent-actual-right - $actual-left - $actual-right"/>
          </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
</xsl:stylesheet>
