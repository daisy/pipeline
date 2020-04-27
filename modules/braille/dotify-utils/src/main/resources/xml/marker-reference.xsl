<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:obfl="http://www.daisy.org/ns/2011/obfl"
                xmlns="http://www.daisy.org/ns/2011/obfl"
                exclude-result-prefixes="#all">

    <xsl:include href="http://www.daisy.org/pipeline/modules/common-utils/generate-id.xsl"/>

    <xsl:template match="css:string[@name][not(@target)]" mode="marker-reference" as="element(obfl:marker-reference)*">
        <xsl:param name="white-space" as="xs:string?" tunnel="yes" select="()"/>
        <xsl:param name="text-transform" as="xs:string" tunnel="yes" select="'auto'"/>
        <xsl:param name="page-side" as="xs:string" tunnel="yes" select="'both'"/> <!-- right|left -->
        <xsl:variable name="scope" select="(@scope,'first')[1]"/>
        <xsl:variable name="var-name" as="xs:string">
            <xsl:call-template name="pf:generate-id"/>
        </xsl:variable>
        <xsl:variable name="text-style" as="xs:string*">
            <xsl:if test="not($text-transform=('none','auto'))">
                <xsl:sequence select="concat('text-transform: ',$text-transform)"/>
            </xsl:if>
            <xsl:if test="$white-space[not(.='normal')]">
                <xsl:sequence select="concat('white-space: ',$white-space)"/>
            </xsl:if>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$scope=('first','page-first')">
                <marker-reference marker="{@name}" direction="forward" scope="page"
                                  text-style="{string-join((concat('-dotify-def:',$var-name),$text-style),'; ')}"/>
                <!--
                    FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-defifndef:',$var-name),$text-style),'; ')}"/>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-ifndef:',$var-name),$text-style),'; ')}"/>
            </xsl:when>
            <xsl:when test="$scope=('start','page-start')">
                <!--
                    Note that "start" behaves like "first" when no assignments have been made yet,
                    which is not exactly according to the spec but is needed in practice (see
                    https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49 and
                    https://github.com/sbsdev/pipeline-mod-sbs/issues/42). An alternative solution
                    could be to use "start" inside @page and "first" inside @page:first.
                -->
                <marker-reference marker="{@name}/prev" direction="forward" scope="page-content"
                                  text-style="{string-join((concat('-dotify-def:',$var-name),$text-style),'; ')}"/>
                <!--
                    TODO: check that this does not match too much at the end of the page!
                    FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-defifndef:',$var-name),$text-style),'; ')}"/>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-ifndef:',$var-name),$text-style),'; ')}"/>
            </xsl:when>
            <xsl:when test="$scope=('start-except-last','page-start-except-last')">
                <!--
                    Note that "start" behaves like "first" when no assignments have been made yet,
                    which is not exactly according to the spec but is needed in practice (see
                    https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49 and
                    https://github.com/sbsdev/pipeline-mod-sbs/issues/42).
                -->
                <marker-reference marker="{@name}/prev" direction="forward" scope="page-content">
                    <xsl:if test="exists($text-style)">
                        <xsl:attribute name="text-style" select="string-join($text-style,'; ')"/>
                    </xsl:if>
                </marker-reference>
            </xsl:when>
            <!--
                FIXME: these keywords are not documented
            -->
            <xsl:when test="$scope=('start-except-first','page-start-except-first','volume-start-except-first')">
                <marker-reference marker="{@name}/if-not-set-next/prev" direction="forward" scope="page-content"
                                  text-style="{string-join((concat('-dotify-def:',$var-name),$text-style),'; ')}"/>
                <!--
                    FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                -->
                <marker-reference marker="{@name}/if-not-set-next"
                                  direction="backward" scope="sequence" offset="-1"
                                  text-style="{string-join((concat('-dotify-defifndef:',$var-name),$text-style),'; ')}"/>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-ifndef:',$var-name),$text-style),'; ')}"/>
            </xsl:when>
            <xsl:when test="$scope=('last','page-last')">
                <!--
                    FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-def:',$var-name),$text-style),'; ')}"/>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-ifndef:',$var-name),$text-style),'; ')}"/>
            </xsl:when>
            <xsl:when test="$scope=('last-except-start','page-last-except-start')">
                <xsl:message terminate="yes"
                             select="concat('string(',@name,', ',$scope,') currently not supported. If you want to use it in combination with string(start), please consider using the combination start-except-last/last instead.')"/>
            </xsl:when>
            <xsl:when test="$scope='spread-first'">
                <marker-reference marker="{@name}" direction="forward" scope="spread"
                                  text-style="{string-join((concat('-dotify-def:',$var-name),$text-style),'; ')}">
                    <xsl:if test="$page-side='right'">
                        <xsl:attribute name="start-offset" select="'-1'"/>
                    </xsl:if>
                </marker-reference>
                <!--
                    FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-defifndef:',$var-name),$text-style),'; ')}">
                    <xsl:if test="$page-side='right'">
                        <xsl:attribute name="start-offset" select="'-1'"/>
                    </xsl:if>
                </marker-reference>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-ifndef:',$var-name),$text-style),'; ')}"/>
            </xsl:when>
            <xsl:when test="$scope='spread-start'">
                <!--
                    Note that "start" behaves like "first" when no assignments have been made yet,
                    which is not exactly according to the spec but is needed in practice (see
                    https://github.com/snaekobbi/pipeline-mod-dedicon/issues/49 and
                    https://github.com/sbsdev/pipeline-mod-sbs/issues/42).
                -->
                <marker-reference marker="{@name}/prev" direction="forward" scope="spread-content"
                                  text-style="{string-join((concat('-dotify-def:',$var-name),$text-style),'; ')}">
                    <xsl:if test="$page-side='right'">
                        <xsl:attribute name="start-offset" select="'-1'"/>
                    </xsl:if>
                </marker-reference>
                <!--
                    TODO: check that this does not match too much at the end of the page!
                    FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-defifndef:',$var-name),$text-style),'; ')}">
                    <xsl:if test="$page-side='left'">
                        <xsl:attribute name="start-offset" select="'1'"/>
                    </xsl:if>
                </marker-reference>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-ifndef:',$var-name),$text-style),'; ')}"/>
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
                    <xsl:if test="exists($text-style)">
                        <xsl:attribute name="text-style" select="string-join($text-style,'; ')"/>
                    </xsl:if>
                </marker-reference>
            </xsl:when>
            <xsl:when test="$scope='spread-last'">
                <!--
                    FIXME: replace with scope="document" (not implemented yet) and remove second marker-reference
                -->
                <marker-reference marker="{@name}" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-def:',$var-name),$text-style),'; ')}">
                    <xsl:if test="$page-side='left'">
                        <xsl:attribute name="start-offset" select="'1'"/>
                    </xsl:if>
                </marker-reference>
                <marker-reference marker="{@name}/entry" direction="backward" scope="sequence"
                                  text-style="{string-join((concat('-dotify-ifndef:',$var-name),$text-style),'; ')}"/>
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
    </xsl:template>

</xsl:stylesheet>
