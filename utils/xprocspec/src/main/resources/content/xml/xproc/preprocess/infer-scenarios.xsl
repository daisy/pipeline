<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:p="http://www.w3.org/ns/xproc" xmlns:x="http://www.daisy.org/ns/xprocspec"
    xmlns:c="http://www.w3.org/ns/xproc-step" exclude-result-prefixes="#all">

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="/x:description">
        <xsl:variable name="documentation" select="x:documentation"/>
        <x:descriptions>
            <xsl:for-each select="//x:scenario">
                <xsl:variable name="scenarios" select="ancestor-or-self::x:scenario"/>
                <xsl:if test="not(descendant::x:scenario)">
                    <x:description>
                        <xsl:copy-of select="/*/@*"/>
                        <xsl:copy-of select="$documentation"/>
                        <xsl:copy>
                            <xsl:copy-of select="$scenarios/@*"/>
                            <xsl:attribute name="label" select="string-join($scenarios/@label,' ')"/>
                            <xsl:variable name="pending" select="x:is-pending(.)"/>
                            <xsl:if test="$pending">
                                <xsl:attribute name="pending" select="$pending"/>
                            </xsl:if>
                            <xsl:if test="$scenarios/x:documentation">
                                <x:documentation>
                                    <xsl:copy-of select="$scenarios/x:documentation/@*"/>
                                    <xsl:copy-of select="$scenarios/x:documentation/node()"/>
                                </x:documentation>
                            </xsl:if>
                            <xsl:if test="$scenarios/x:call">
                                <x:call>
                                    <xsl:copy-of select="$scenarios/x:call/@*"/>
                                    <xsl:if test="$scenarios/x:call/x:documentation">
                                        <x:documentation>
                                            <xsl:copy-of select="$scenarios/x:call/x:documentation/@*"/>
                                            <xsl:copy-of select="$scenarios/x:call/x:documentation/node()"/>
                                        </x:documentation>
                                    </xsl:if>
                                    <xsl:copy-of select="x:resolve-options($scenarios/x:call/x:option)"/>
                                    <xsl:copy-of select="x:resolve-params($scenarios/x:call/x:param)"/>
                                    <xsl:copy-of select="x:resolve-inputs($scenarios/x:call/x:input)"/>
                                </x:call>
                            </xsl:if>
                            <xsl:apply-templates select="* except (x:call | x:documentation)">
                                <xsl:with-param name="pending-scenario" select="$pending" tunnel="yes"/>
                            </xsl:apply-templates>
                        </xsl:copy>
                    </x:description>
                </xsl:if>
            </xsl:for-each>
        </x:descriptions>
    </xsl:template>

    <xsl:template match="x:expect">
        <xsl:param name="pending-scenario" tunnel="yes" required="yes"/>
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:variable name="pending" select="if ($pending-scenario) then $pending-scenario else x:is-pending(.)"/>
            <xsl:if test="$pending">
                <xsl:attribute name="pending" select="$pending"/>
            </xsl:if>
            <xsl:if test="@type='compare'">
                <xsl:attribute name="normalize-space" select="(@normalize-space,'true')[1]"/>
            </xsl:if>
            <xsl:attribute name="contextref" select="concat('context',count(preceding-sibling::x:context[1]/preceding::x:context))"/>
            <xsl:for-each select="node()">
                <xsl:choose>
                    <xsl:when test="self::x:document">
                        <xsl:copy>
                            <xsl:copy-of select="@*"/>
                            <xsl:attribute name="xml:space" select="'preserve'"/>
                            <xsl:copy-of select="node()"/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="x:context">
        <xsl:param name="pending-scenario" tunnel="yes" required="yes"/>
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:variable name="pending" select="if ($pending-scenario) then $pending-scenario else x:is-pending(.)"/>
            <xsl:if test="$pending">
                <xsl:attribute name="pending" select="$pending"/>
            </xsl:if>
            <xsl:attribute name="id" select="concat('context',count(preceding::x:context))"/>
            <xsl:for-each select="node()">
                <xsl:choose>
                    <xsl:when test="self::x:document">
                        <xsl:copy>
                            <xsl:copy-of select="@*"/>
                            <xsl:attribute name="xml:space" select="'preserve'"/>
                            <xsl:copy-of select="node()"/>
                        </xsl:copy>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

    <xsl:function name="x:is-pending">
        <xsl:param name="this"/>
        <xsl:choose>
            <xsl:when test="$this/ancestor-or-self::*[@pending or self::x:pending]">
                <xsl:variable name="pending" select="($this/ancestor-or-self::*[@pending or self::x:pending])[1]/string(if (self::x:pending) then @label else @pending)"/>
                <xsl:sequence select="if ($pending='') then 'Not implemented' else string($pending)"/>
            </xsl:when>
            <xsl:when test="$this/self::x:expect">
                <xsl:variable name="context" select="($this/preceding::x:context)[last()]"/>
                <xsl:variable name="pending" select="x:is-pending($context)"/>
                <xsl:choose>
                    <xsl:when test="$pending">
                        <xsl:sequence select="concat($pending, ' (because context is pending)')"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:sequence select="()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:sequence select="()"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="x:resolve-options">
        <xsl:param name="options"/>
        <xsl:for-each select="distinct-values($options/@name)">
            <xsl:variable name="name" select="."/>
            <xsl:sequence select="($options[@name=$name])[last()]"/>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="x:resolve-params">
        <xsl:param name="params"/>
        <xsl:for-each select="distinct-values($params/@name)">
            <xsl:variable name="name" select="."/>
            <xsl:variable name="param" select="($params[@name=$name])[last()]"/>
            <xsl:choose>
                <xsl:when test="$param[self::x:param]">
                    <xsl:sequence select="$param"/>
                </xsl:when>
                <xsl:otherwise>
                    <x:param name="{$param/@name}"
                        select="concat('&apos;',replace(replace(replace($param/@value,'&amp;','&amp;amp;'),&quot;'&quot;,&quot;&amp;apos;&quot;),'&quot;','&amp;quot;'),'&apos;')">
                        <xsl:if test="$param/@namespace">
                            <xsl:attribute name="ns" select="$param/@namespace"/>
                        </xsl:if>
                    </x:param>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>
    </xsl:function>

    <xsl:function name="x:resolve-inputs">
        <xsl:param name="inputs"/>
        <xsl:for-each select="distinct-values($inputs/@port)">
            <xsl:variable name="port" select="."/>
            <xsl:sequence select="($inputs[@port=$port])[last()]"/>
        </xsl:for-each>
    </xsl:function>

</xsl:stylesheet>
