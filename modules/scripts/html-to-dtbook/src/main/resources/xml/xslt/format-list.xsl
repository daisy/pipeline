<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:f="http://www.daisy.org/pipeline/modules/html-to-dtbook/format-list.xsl"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:html="http://www.w3.org/1999/xhtml"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="#all">

    <xsl:import href="http://www.daisy.org/pipeline/modules/common-utils/numeral-conversion.xsl"/>

    <!-- ==== -->
    <!-- HTML -->
    <!-- ==== -->

    <xsl:template mode="format-list"
                  match="html:ol|html:ul">
        <xsl:copy>
            <xsl:attribute name="class"
                           select="string-join(distinct-values((tokenize(@class,'\s+')[not(.='')],'preformatted')),' ')"/>
            <xsl:apply-templates mode="#current" select="@* except @class"/>
            <xsl:apply-templates mode="#current" />
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="format-list"
                  priority="1"
                  match="html:*[self::html:ol|self::html:ul]
                               [tokenize(@class,'\s+')=('preformatted','list-style-type-none')]
                         /html:li">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="format-list" match="html:ol/@type|
                                            html:ol/@start|
                                            html:li/@value"/>

    <xsl:template mode="format-list" match="html:ul/html:li">
        <xsl:call-template name="html:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:text>• </xsl:text>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="format-list" match="html:ol[not(@type) or @type='1']/html:li">
        <xsl:call-template name="html:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:value-of select="concat(f:li-value(.),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="format-list" match="html:ol[@type='a']/html:li">
        <xsl:call-template name="html:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:value-of select="concat(lower-case(pf:numeric-decimal-to-alpha(f:li-value(.))),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="format-list" match="html:ol[@type='A']/html:li">
        <xsl:call-template name="html:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:value-of select="concat(upper-case(pf:numeric-decimal-to-alpha(f:li-value(.))),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="format-list" match="html:ol[@type='i']/html:li">
        <xsl:call-template name="html:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:value-of select="concat(lower-case(pf:numeric-decimal-to-roman(f:li-value(.))),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="format-list" match="html:ol[@type='I']/html:li">
        <xsl:call-template name="html:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:value-of select="concat(upper-case(pf:numeric-decimal-to-roman(f:li-value(.))),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="html:insert-marker">
        <xsl:param name="marker" as="node()" required="yes"/>
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*"/>
            <xsl:choose>
                <!-- prepend marker to first p if present -->
                <xsl:when test="(*|text()[normalize-space()])[1]/self::html:p">
                    <xsl:for-each-group select="node()" group-ending-with="html:p">
                        <xsl:choose>
                            <xsl:when test="position()=1">
                                <xsl:for-each select="current-group()">
                                    <xsl:choose>
                                        <xsl:when test="self::html:p">
                                            <xsl:call-template name="html:insert-marker">
                                                <xsl:with-param name="marker" select="$marker"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:apply-templates mode="#current" select="."/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates mode="#current" select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$marker"/>
                    <xsl:apply-templates mode="#current" select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <!-- ====== -->
    <!-- DTBook -->
    <!-- ====== -->

    <xsl:template mode="format-list" match="dtb:list/@type">
        <xsl:attribute name="type" select="'pl'"/>
    </xsl:template>

    <xsl:template mode="format-list" match="dtb:list/@enum|
                                            dtb:list/@start"/>

    <xsl:template mode="format-list" match="dtb:list[@type='pl']/dtb:li">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template mode="format-list" match="dtb:list[@type='ul']/dtb:li">
        <xsl:call-template name="dtb:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:text>• </xsl:text>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="format-list" match="dtb:list[@type='ol'][not(@enum) or @enum='1']/dtb:li">
        <xsl:call-template name="dtb:insert-marker">
            <xsl:with-param name="marker" as="text()">
            <xsl:value-of select="concat(f:li-value(.),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="format-list" match="dtb:list[@type='ol'][@enum='a']/dtb:li">
        <xsl:call-template name="dtb:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:value-of select="concat(lower-case(pf:numeric-decimal-to-alpha(f:li-value(.))),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="format-list" match="dtb:list[@type='ol'][@enum='A']/dtb:li">
        <xsl:call-template name="dtb:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:value-of select="concat(upper-case(pf:numeric-decimal-to-alpha(f:li-value(.))),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>
    
    <xsl:template mode="format-list" match="dtb:list[@type='ol'][@enum='i']/dtb:li">
        <xsl:call-template name="dtb:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:value-of select="concat(lower-case(pf:numeric-decimal-to-roman(f:li-value(.))),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template mode="format-list" match="dtb:list[@type='ol'][@enum='I']/dtb:li">
        <xsl:call-template name="dtb:insert-marker">
            <xsl:with-param name="marker" as="text()">
                <xsl:value-of select="concat(upper-case(pf:numeric-decimal-to-roman(f:li-value(.))),'. ')"/>
            </xsl:with-param>
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="dtb:insert-marker">
        <xsl:param name="marker" as="node()" required="yes"/>
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*"/>
            <xsl:choose>
                <!-- prepend marker to first p if present -->
                <xsl:when test="(*|text()[normalize-space()])[1]/self::dtb:p">
                    <xsl:for-each-group select="node()" group-ending-with="dtb:p">
                        <xsl:choose>
                            <xsl:when test="position()=1">
                                <xsl:for-each select="current-group()">
                                    <xsl:choose>
                                        <xsl:when test="self::dtb:p">
                                            <xsl:call-template name="dtb:insert-marker">
                                                <xsl:with-param name="marker" select="$marker"/>
                                            </xsl:call-template>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:apply-templates mode="#current" select="."/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:apply-templates mode="#current" select="."/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each-group>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:sequence select="$marker"/>
                    <xsl:apply-templates mode="#current" select="node()"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:copy>
    </xsl:template>

    <!-- ========================================== -->

    <xsl:template mode="format-list" match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates mode="#current" select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:function name="f:li-value" as="xs:integer">
        <xsl:param name="li" as="element()"/>
        <xsl:choose>
            <xsl:when test="not($li)">
                <xsl:value-of select="1"/>
            </xsl:when>
            <xsl:when test="$li/@value">
                <xsl:value-of select="$li/@value"/>
            </xsl:when>
            <xsl:when test="not($li/preceding-sibling::*)">
                <xsl:value-of select="if ($li/parent::*/@start)
                                      then $li/parent::*/@start
                                      else if ($li/parent::*/@reversed)
                                           then count($li/parent::*/*)
                                           else 1"/>
            </xsl:when>
            <xsl:when test="$li/parent::*/@reversed">
                <xsl:value-of select="if ($li/preceding-sibling::*[@value])
                                      then f:li-value(($li/preceding-sibling::*[@value])[last()])
                                           - 1
                                           - count($li/preceding-sibling::*
                                                   intersect ($li/preceding-sibling::*[@value])[last()]/following-sibling::*)
                                      else ($li/parent::*/@start/number(.),count($li/parent::*/*))[1]
                                           - count($li/preceding-sibling::*)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="if ($li/preceding-sibling::*[@value])
                                      then f:li-value(($li/preceding-sibling::*[@value])[last()])
                                           + 1
                                           + count($li/preceding-sibling::*
                                                   intersect ($li/preceding-sibling::*[@value])[last()]/following-sibling::*)
                                      else ($li/parent::*/@start/number(.),1)[1]
                                           + count($li/preceding-sibling::*)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

</xsl:stylesheet>
