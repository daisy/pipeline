<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns="http://www.daisy.org/ns/2011/obfl"
                exclude-result-prefixes="#all">

    <xsl:template match="css:string[@name][not(@target)]" mode="marker-reference" as="element()"> <!-- marker-reference|compound-marker-reference|style -->
        <xsl:param name="white-space" as="xs:string?" tunnel="yes" select="()"/>
        <xsl:param name="text-transform" as="xs:string" tunnel="yes" select="'auto'"/>
        <xsl:param name="page-side" as="xs:string" tunnel="yes" select="'both'"/> <!-- right|left -->
        <xsl:param name="allow-style-element" as="xs:boolean" tunnel="yes" select="false()"/>
        <xsl:variable name="scope" select="(@scope,'first')[1]"/>
        <xsl:variable name="marker-reference" as="element()"> <!-- marker-reference|compound-marker-reference -->
            <xsl:choose>
                <xsl:when test="$scope=('first','page-first')">
                    <compound-marker-reference>
                        <marker-reference marker="{@name}" direction="forward" scope="page"/>
                        <!--
                            FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                        -->
                        <marker-reference marker="{@name}" direction="backward" scope="sequence"/>
                        <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"/>
                    </compound-marker-reference>
                </xsl:when>
                <xsl:when test="$scope=('start','page-start')">
                    <compound-marker-reference>
                        <!--
                            Note that "start" behaves like "first" when no assignments have been made yet,
                            which is not exactly according to the spec but is needed in practice (see
                            https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49 and
                            https://github.com/sbsdev/pipeline-mod-sbs/issues/42). An alternative solution
                            could be to use "start" inside @page and "first" inside @page:first.
                        -->
                        <marker-reference marker="{@name}/prev" direction="forward" scope="page-content"/>
                        <!--
                            TODO: check that this does not match too much at the end of the page!
                            FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                        -->
                        <marker-reference marker="{@name}" direction="backward" scope="sequence"/>
                        <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"/>
                    </compound-marker-reference>
                </xsl:when>
                <xsl:when test="$scope=('start-except-last','page-start-except-last')">
                    <!--
                        Note that "start" behaves like "first" when no assignments have been made yet,
                        which is not exactly according to the spec but is needed in practice (see
                        https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49 and
                        https://github.com/sbsdev/pipeline-mod-sbs/issues/42).
                    -->
                    <marker-reference marker="{@name}/prev" direction="forward" scope="page-content"/>
                </xsl:when>
                <!--
                    FIXME: these keywords are not documented
                -->
                <xsl:when test="$scope=('start-except-first','page-start-except-first','volume-start-except-first')">
                    <compound-marker-reference>
                        <marker-reference marker="{@name}/if-not-set-next/prev" direction="forward" scope="page-content"/>
                        <!--
                            FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                        -->
                        <marker-reference marker="{@name}/if-not-set-next" direction="backward" scope="sequence" offset="-1"/>
                        <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"/>
                    </compound-marker-reference>
                </xsl:when>
                <xsl:when test="$scope=('last','page-last')">
                    <compound-marker-reference>
                        <!--
                            FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                        -->
                        <marker-reference marker="{@name}" direction="backward" scope="sequence"/>
                        <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"/>
                    </compound-marker-reference>
                </xsl:when>
                <xsl:when test="$scope=('last-except-start','page-last-except-start')">
                    <xsl:message terminate="yes"
                                 select="concat('string(',@name,', ',$scope,') currently not supported. If you want to use it in combination with string(start), please consider using the combination start-except-last/last instead.')"/>
                </xsl:when>
                <xsl:when test="$scope='spread-first'">
                    <compound-marker-reference>
                        <marker-reference marker="{@name}" direction="forward" scope="spread">
                            <xsl:if test="$page-side='right'">
                                <xsl:attribute name="start-offset" select="'-1'"/>
                            </xsl:if>
                        </marker-reference>
                        <!--
                            FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                        -->
                        <marker-reference marker="{@name}" direction="backward" scope="sequence">
                            <xsl:if test="$page-side='right'">
                                <xsl:attribute name="start-offset" select="'-1'"/>
                            </xsl:if>
                        </marker-reference>
                        <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"/>
                    </compound-marker-reference>
                </xsl:when>
                <xsl:when test="$scope='spread-start'">
                    <compound-marker-reference>
                        <!--
                            Note that "start" behaves like "first" when no assignments have been made yet,
                            which is not exactly according to the spec but is needed in practice (see
                            https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49 and
                            https://github.com/sbsdev/pipeline-mod-sbs/issues/42).
                        -->
                        <marker-reference marker="{@name}/prev" direction="forward" scope="spread-content">
                            <xsl:if test="$page-side='right'">
                                <xsl:attribute name="start-offset" select="'-1'"/>
                            </xsl:if>
                        </marker-reference>
                        <!--
                            TODO: check that this does not match too much at the end of the page!
                            FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                        -->
                        <marker-reference marker="{@name}" direction="backward" scope="sequence">
                            <xsl:if test="$page-side='left'">
                                <xsl:attribute name="start-offset" select="'1'"/>
                            </xsl:if>
                        </marker-reference>
                        <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"/>
                    </compound-marker-reference>
                </xsl:when>
                <xsl:when test="$scope='spread-start-except-last'">
                    <!--
                        Note that "start" behaves like "first" when no assignments have been made yet,
                        which is not exactly according to the spec but is needed in practice (see
                        https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49 and
                        https://github.com/sbsdev/pipeline-mod-sbs/issues/42).
                    -->
                    <marker-reference marker="{@name}/prev" direction="forward" scope="spread-content">
                        <xsl:if test="$page-side='right'">
                            <xsl:attribute name="start-offset" select="'-1'"/>
                        </xsl:if>
                    </marker-reference>
                </xsl:when>
                <xsl:when test="$scope='spread-last'">
                    <compound-marker-reference>
                        <!--
                            FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                        -->
                        <marker-reference marker="{@name}" direction="backward" scope="sequence">
                            <xsl:if test="$page-side='left'">
                                <xsl:attribute name="start-offset" select="'1'"/>
                            </xsl:if>
                        </marker-reference>
                        <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"/>
                    </compound-marker-reference>
                </xsl:when>
                <xsl:when test="$scope='spread-last-except-start'">
                    <xsl:message terminate="yes"
                                 select="concat('string(',@name,', ',$scope,') currently not supported. If you want to use it in combination with string(start), please consider using the combination start-except-last/last instead.')"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="pf:error">
                        <xsl:with-param name="msg">in function string({}, {}): unknown keyword "{}"</xsl:with-param>
                        <xsl:with-param name="args" select="(@name,
                                                            $scope)"/>
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="text-style" as="xs:string*">
            <xsl:if test="not($text-transform=('none','auto'))">
                <xsl:sequence select="concat('text-transform: ',$text-transform)"/>
            </xsl:if>
            <xsl:if test="$white-space[not(.='normal')]">
                <xsl:sequence select="concat('white-space: ',$white-space)"/>
            </xsl:if>
        </xsl:variable>
        <xsl:variable name="text-style" as="xs:string?" select="string-join($text-style,'; ')[not(.='')]"/>
        <xsl:for-each select="$marker-reference">
            <xsl:choose>
                <xsl:when test="not(exists($text-style))">
                    <xsl:sequence select="."/>
                </xsl:when>
                <xsl:when test="$allow-style-element">
                    <style name="{$text-style}">
                        <xsl:sequence select="."/>
                    </style>
                </xsl:when>
                <xsl:when test="self::obfl:marker-reference">
                    <xsl:copy>
                        <xsl:sequence select="@*"/>
                        <xsl:attribute name="text-style" select="$text-style"/>
                    </xsl:copy>
                </xsl:when>
                <xsl:otherwise> <!-- self::obfl:compound-marker-reference -->
                    <xsl:copy>
                        <xsl:for-each select="*">
                            <xsl:copy>
                                <xsl:sequence select="@*"/>
                                <xsl:attribute name="text-style" select="$text-style"/>
                            </xsl:copy>
                        </xsl:for-each>
                    </xsl:copy>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>
