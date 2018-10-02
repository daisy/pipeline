<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:h="http://www.w3.org/1999/xhtml" xmlns:d="http://www.daisy.org/ns/pipeline/data" version="2.0" exclude-result-prefixes="#all">

    <!-- TODO: move to html-utils -->

    <!--
        Note: this file is currently not used
    -->

    <xsl:template match="/*">
        <d:fileset>
            <xsl:attribute name="xml:base" select="replace(base-uri(.),'^(.+/)[^/]*','$1')"/>
            <xsl:for-each select="//h:a">
                <xsl:if test="@href">
                    <xsl:variable name="href" select="tokenize(@href,'#')[1]"/>
                    <xsl:variable name="type">
                        <xsl:choose>
                            <xsl:when test="@type">
                                <xsl:value-of select="@type"/>
                            </xsl:when>
                            <xsl:when test="ends-with(lower-case($href),'.html')"><![CDATA[text/html]]></xsl:when>
                            <xsl:when test="ends-with(lower-case($href),'.xhtml')"><![CDATA[application/xhtml+xml]]></xsl:when>
                            <xsl:otherwise><![CDATA[]]></xsl:otherwise>
                        </xsl:choose>
                    </xsl:variable>
                    <d:file href="{$href}">
                        <xsl:if test="string-length($type)&gt;0">
                            <xsl:attribute name="media-type" select="$type"/>
                        </xsl:if>
                    </d:file>
                </xsl:if>
            </xsl:for-each>
            <xsl:for-each select="/processing-instruction('xml-stylesheet')">
                <xsl:variable name="href" select="replace(.,'^.*href=(&amp;apos;|&quot;)(.*?)\1.*$','$2')"/>
                <xsl:variable name="type" select="replace(.,'^.*type=(&amp;apos;|&quot;)(.*?)\1.*$','$2')"/>
                <xsl:variable name="inferredType">
                    <xsl:choose>
                        <xsl:when test="$type">
                            <xsl:value-of select="$type"/>
                        </xsl:when>
                        <xsl:when test="ends-with(lower-case($href),'.css')">
                            <xsl:value-of select="'text/css'"/>
                        </xsl:when>
                        <xsl:when test="ends-with(lower-case($href),'.xsl') or ends-with(lower-case($href),'.xslt')">
                            <xsl:value-of select="'application/xslt+xml'"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="false()"/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:if test="$inferredType">
                    <d:file href="{$href}" media-type="{$inferredType}"/>
                </xsl:if>
            </xsl:for-each>
            <xsl:for-each select="//h:link[ends-with(lower-case(@href),'.css')]">
                <d:file href="{@href}" media-type="text/css"/>
            </xsl:for-each>
            <xsl:for-each select="//h:img">
                <xsl:variable name="type">
                    <xsl:choose>
                        <xsl:when test="ends-with(lower-case(@src),'.jpg')"><![CDATA[image/jpeg]]></xsl:when>
                        <xsl:when test="ends-with(lower-case(@src),'.jpeg')"><![CDATA[image/jpeg]]></xsl:when>
                        <xsl:when test="ends-with(lower-case(@src),'.png')"><![CDATA[image/png]]></xsl:when>
                        <xsl:when test="ends-with(lower-case(@src),'.gif')"><![CDATA[image/gif]]></xsl:when>
                        <xsl:otherwise/>
                    </xsl:choose>
                </xsl:variable>
                <xsl:if test="$type">
                    <d:file href="{@src}" media-type="{$type}"/>
                </xsl:if>
            </xsl:for-each>
        </d:fileset>
    </xsl:template>

</xsl:stylesheet>
