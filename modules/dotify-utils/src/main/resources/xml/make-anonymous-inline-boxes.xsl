<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                exclude-result-prefixes="#all"
                version="2.0">
    
    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>
    
    <xsl:template match="/*" priority="2">
        <xsl:next-match>
            <xsl:with-param name="source-style" select="()" tunnel="yes"/>
            <xsl:with-param name="result-style" select="()" tunnel="yes"/>
        </xsl:next-match>
    </xsl:template>
    
    <xsl:template match="css:_">
        <xsl:copy>
            <xsl:sequence select="@*"/>
            <xsl:call-template name="apply-templates"/>
        </xsl:copy>
    </xsl:template>
    
    <!--
        unwrap inline boxes that contain block/table/table-cell boxes
    -->
    <xsl:template match="css:box[@type='inline']
                                [descendant::css:box[@type=('block','table','table-cell')]]">
        <xsl:param name="source-style" as="item()?" tunnel="yes"/>
        <xsl:variable name="source-style" as="item()?" select="css:parse-stylesheet(string(@style),$source-style)"/>
        <xsl:choose>
            <xsl:when test="@css:* or not(parent::*)">
                <xsl:element name="css:_">
                    <xsl:sequence select="@css:*"/>
                    <xsl:call-template name="apply-templates">
                        <xsl:with-param name="source-style" select="$source-style" tunnel="yes"/>
                        <xsl:with-param name="pending-lang" select="@xml:lang" tunnel="yes"/>
                    </xsl:call-template>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="apply-templates">
                    <xsl:with-param name="source-style" select="$source-style" tunnel="yes"/>
                    <xsl:with-param name="pending-lang" select="@xml:lang" tunnel="yes"/>
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match="css:box">
        <xsl:param name="source-style" as="item()?" tunnel="yes"/>
        <xsl:param name="result-style" as="item()?" tunnel="yes"/>
        <xsl:param name="pending-lang" as="attribute(xml:lang)?" select="()" tunnel="yes"/>
        <xsl:variable name="source-style" as="item()?" select="css:parse-stylesheet(string(@style),$source-style)"/>
        <xsl:copy>
            <xsl:sequence select="@* except @style"/>
            <xsl:sequence select="css:style-attribute(css:serialize-stylesheet($source-style,$result-style))"/>
            <xsl:if test="not(@xml:lang)">
                <xsl:sequence select="$pending-lang"/>
            </xsl:if>
            <xsl:call-template name="apply-templates">
                <xsl:with-param name="source-style" select="$source-style" tunnel="yes"/>
                <xsl:with-param name="result-style" select="$source-style" tunnel="yes"/>
                <xsl:with-param name="pending-lang" select="()" tunnel="yes"/>
            </xsl:call-template>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="apply-templates">
        <xsl:param name="source-style" as="item()?" tunnel="yes"/>
        <xsl:param name="result-style" as="item()?" tunnel="yes"/>
        <xsl:param name="pending-lang" as="attribute(xml:lang)?" select="()" tunnel="yes"/>
        <xsl:variable name="this" as="element()" select="."/>
        <xsl:for-each-group select="*|text()"
                            group-adjacent="boolean(descendant-or-self::css:box[@type=('block','table','table-cell')])">
            <xsl:choose>
                <xsl:when test="current-grouping-key()">
                    <xsl:for-each select="current-group()">
                        <xsl:apply-templates select="."/>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="$this/ancestor-or-self::css:box[@type='inline']
                                                               [not(descendant::css:box[@type=('block','table','table-cell')])]
                                or not(
                                  current-group()/
                                    (descendant-or-self::text()[not(matches(.,'^[\s&#x2800;]*$'))]
                                     |descendant-or-self::css:white-space
                                     |descendant-or-self::css:string
                                     |descendant-or-self::css:counter
                                     |descendant-or-self::css:text
                                     |descendant-or-self::css:content
                                     |descendant-or-self::css:leader
                                     |descendant-or-self::css:custom-func
                                     )[not(ancestor::css:box[@type='inline']
                                                            [not(descendant::css:box[@type=('block','table','table-cell')])])])">
                    <xsl:apply-templates select="current-group()"/>
                </xsl:when>
                <xsl:otherwise>
                    <css:box type="inline">
                        <xsl:sequence select="css:style-attribute(css:serialize-stylesheet($source-style,$result-style))"/>
                        <xsl:sequence select="$pending-lang"/>
                        <xsl:sequence select="current-group()"/>
                    </css:box>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each-group>
    </xsl:template>
    
    <xsl:template match="node()">
        <xsl:sequence select="."/>
    </xsl:template>
    
</xsl:stylesheet>
