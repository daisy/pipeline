<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="3.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:css="http://www.daisy.org/ns/pipeline/braille-css"
                xmlns:f="f"
                exclude-result-prefixes="#all">

    <xsl:include href="http://www.daisy.org/pipeline/modules/braille/css-utils/library.xsl"/>

    <xsl:param name="all-docs" as="document-node()*"/>

    <!-- elements participating in a flow and consumer elements of that flow, all sorted by original position -->
    <xsl:variable name="flow-contents-and-consumers" as="map(xs:string,element()*)">
        <xsl:map>
            <xsl:for-each select="distinct-values($all-docs//css:flow/@from)[not(.='normal')]">
                <xsl:variable name="flow" as="xs:string" select="."/>
                <xsl:map-entry key="$flow" select="f:sort-by-original-position((
                                                     $all-docs/*[@css:flow=$flow]/*,
                                                     $all-docs//css:flow[@from=$flow]))"/>
            </xsl:for-each>
        </xsl:map>
    </xsl:variable>

    <xsl:template match="css:flow[@from]">
        <xsl:variable name="flow" as="xs:string" select="@from"/>
        <xsl:choose>
            <xsl:when test="not(@scope)">
                <xsl:variable name="flow-contents-and-consumers" as="element()*" select="$flow-contents-and-consumers($flow)"/>
                <xsl:variable name="index" as="xs:integer" select="f:index-of($flow-contents-and-consumers,.)"/>
                <xsl:variable name="resolved" as="element()*">
                    <xsl:iterate select="reverse(subsequence($flow-contents-and-consumers,1,$index - 1))">
                        <xsl:param name="items" select="()"/>
                        <xsl:on-completion select="$items"/>
                        <xsl:choose>
                            <xsl:when test="self::css:flow[@from]">
                                <xsl:break select="$items"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:next-iteration>
                                    <xsl:with-param name="items" select="(.,$items)"/>
                                </xsl:next-iteration>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:iterate>
                </xsl:variable>
                <xsl:apply-templates select="$resolved"/>
            </xsl:when>
            <xsl:when test="@scope=('document')">
                <xsl:apply-templates select="$all-docs/*[@css:flow=$flow]/*"/>
            </xsl:when>
            <xsl:when test="@scope=('volume','page')">
                <!-- evaluate later -->
                <xsl:sequence select="."/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:message terminate="yes">coding error</xsl:message>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- flow of an element -->
    <xsl:function name="f:flow" as="xs:string">
        <xsl:param name="element" as="element()"/>
        <xsl:sequence select="(($element/ancestor-or-self::*)[1]/@css:flow,'normal')[1]"/>
    </xsl:function>

    <!-- original position of an element within the given flow -->
    <xsl:function name="f:original-position" as="element()?">
        <xsl:param name="element" as="element()"/>
        <xsl:param name="flow" as="xs:string"/>
        <xsl:iterate select="1 to 100">
            <xsl:param name="position" as="element()" select="$element"/>
            <xsl:on-completion>
                <xsl:message terminate="yes"/>
            </xsl:on-completion>
            <xsl:variable name="current-flow" select="f:flow($position)"/>
            <xsl:choose>
                <xsl:when test="$current-flow='normal' and not($flow='normal')">
                    <xsl:break select="()"/>
                </xsl:when>
                <xsl:when test="$current-flow=$flow">
                    <xsl:break select="$position"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="anchor" as="xs:string" select="($position/ancestor-or-self::*)[2]/@css:anchor"/>
                    <xsl:variable name="anchor" as="element()" select="$all-docs//*[@css:id=$anchor]"/>
                    <xsl:next-iteration>
                        <xsl:with-param name="position" select="$anchor"/>
                    </xsl:next-iteration>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:iterate>
    </xsl:function>

    <!-- sort element by their original position -->
    <xsl:function name="f:sort-by-original-position" as="element()*">
        <xsl:param name="elements" as="element()*"/>
        <xsl:perform-sort select="$elements">
            <!-- first sort by position in the normal flow -->
            <!-- sorting elements doesn't seem to work, so we count the preceding elements -->
            <xsl:sort select="count(f:original-position(.,'normal')/(preceding::*|ancestor::*))"/>
            <!-- if two elements have the same anchor element in the normal flow and are in different
                 flows, the element in the flow that comes first in the alphabet wins (arbitrary choice) -->
            <xsl:sort select="f:flow(.)"/>
            <!-- if two elements have the same anchor element in the normal flow and are in the same
                 flow, sort by position in that flow -->
            <xsl:sort select="count(f:original-position(.,f:flow(.))/(preceding::*|ancestor::*))"/>
        </xsl:perform-sort>
    </xsl:function>

    <!-- version of index-of that works for elements (and returns at most one index, for the first match) -->
    <xsl:function name="f:index-of" as="xs:integer?">
        <xsl:param name="seq" as="element()*"/>
        <xsl:param name="search" as="element()"/>
        <xsl:iterate select="$seq">
            <xsl:param name="i" as="xs:integer" select="1"/>
            <xsl:on-completion select="()"/>
            <xsl:choose>
                <xsl:when test=". is $search">
                    <xsl:break select="$i"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:next-iteration>
                        <xsl:with-param name="i" select="$i + 1"/>
                    </xsl:next-iteration>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:iterate>
    </xsl:function>

</xsl:stylesheet>
