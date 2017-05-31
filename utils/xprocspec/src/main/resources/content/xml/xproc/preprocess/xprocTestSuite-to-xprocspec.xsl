<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:t="http://xproc.org/ns/testsuite" xmlns:x="http://www.daisy.org/ns/xprocspec"
    xmlns:xprocspec="http://www.daisy.org/ns/xprocspec" xmlns:c="http://www.w3.org/ns/xproc-step" exclude-result-prefixes="#all">

    <xsl:output exclude-result-prefixes="#all" indent="yes" method="xml"/>

    <xsl:template match="/t:test-suite">
        <x:description>
            <xsl:for-each select="t:test">
                <x:import href="{@href}"/>
            </xsl:for-each>
        </x:description>
    </xsl:template>

    <xsl:template match="/t:test">
        <x:description>
            <xsl:choose>
                <xsl:when test="t:pipeline/@href">
                    <xsl:attribute name="script" select="t:pipeline/@href"/>
                </xsl:when>
                <xsl:otherwise>
                    <x:script>
                        <xsl:choose>
                            <xsl:when test="not(t:compare-pipeline)">
                                <xsl:for-each select="t:pipeline/*">
                                    <xsl:copy>
                                        <xsl:copy-of select="@*"/>

                                        <xsl:if test="not(@type)">
                                            <xsl:namespace name="xprocspec" select="'http://www.daisy.org/ns/xprocspec'"/>
                                            <xsl:attribute name="type" select="'xprocspec:step'"/>
                                        </xsl:if>
                                        <xsl:copy-of select="node()|comment()"/>
                                    </xsl:copy>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <p:declare-step xmlns:p="http://www.w3.org/ns/xproc" xmlns:c="http://www.w3.org/ns/xproc-step" xmlns:xprocspec="http://www.daisy.org/ns/xprocspec" type="xprocspec:step"
                                    name="main" version="1.0">
                                    <xsl:namespace name="xprocspec" select="'http://www.daisy.org/ns/xprocspec'"/>
                                    <xsl:copy-of select="t:pipeline/*/p:input" copy-namespaces="no"/>
                                    <xsl:for-each select="t:compare-pipeline/*/p:output">
                                        <xsl:copy copy-namespaces="no">
                                            <xsl:copy-of select="@*"/>
                                            <p:pipe step="compare-pipeline" port="{@port}"/>
                                        </xsl:copy>
                                    </xsl:for-each>
                                    <xsl:copy-of select="t:pipeline/*/p:option" copy-namespaces="no"/>

                                    <xsl:variable name="step1" select="t:pipeline/*/@type"/>
                                    <xsl:if test="$step1">
                                        <xsl:namespace name="{tokenize($step1,':')[1]}" select="namespace-uri-for-prefix(tokenize($step1,':')[1],t:pipeline/*)"/>
                                    </xsl:if>
                                    <xsl:variable name="step1" select="if ($step1) then $step1 else 'xprocspec:step1'"/>

                                    <xsl:variable name="step2" select="t:pipeline/*/@type"/>
                                    <xsl:if test="$step2">
                                        <xsl:namespace name="{tokenize($step2,':')[1]}" select="namespace-uri-for-prefix(tokenize($step2,':')[1],t:pipeline/*)"/>
                                    </xsl:if>
                                    <xsl:variable name="step2" select="if ($step2) then $step2 else 'xprocspec:step2'"/>

                                    <xsl:for-each select="t:pipeline/*">
                                        <xsl:copy>
                                            <xsl:copy-of select="@*"/>
                                            <xsl:attribute name="type" select="$step1"/>
                                            <xsl:if test="not(p:output)">
                                                <p:output port="xprocspec-depends-on">
                                                    <p:inline>
                                                        <dummy/>
                                                    </p:inline>
                                                </p:output>
                                            </xsl:if>
                                            <xsl:copy-of select="node()"/>
                                        </xsl:copy>
                                    </xsl:for-each>

                                    <xsl:for-each select="t:compare-pipeline/*">
                                        <xsl:copy>
                                            <xsl:copy-of select="@*"/>
                                            <xsl:attribute name="type" select="$step2"/>
                                            <p:option name="xprocspec-depends-on"/>
                                            <xsl:copy-of select="node()"/>
                                        </xsl:copy>
                                    </xsl:for-each>

                                    <xsl:element name="{$step1}">
                                        <xsl:attribute name="name" select="'pipeline'"/>
                                        <xsl:for-each select="t:pipeline/*/p:input">
                                            <p:input port="{@port}">
                                                <p:pipe step="main" port="{@port}"/>
                                            </p:input>
                                        </xsl:for-each>
                                        <xsl:for-each select="t:pipeline/*/p:option">
                                            <p:with-option name="{@name}" select="'{@value}'"/>
                                        </xsl:for-each>
                                    </xsl:element>
                                    <xsl:for-each select="t:pipeline/*/p:output">
                                        <p:sink>
                                            <p:input port="source">
                                                <p:pipe step="pipeline" port="{@port}"/>
                                            </p:input>
                                        </p:sink>
                                    </xsl:for-each>

                                    <p:wrap-sequence wrapper="wrapper" name="xprocspec-depends-on">
                                        <p:input port="source">
                                            <p:pipe step="pipeline" port="{if (t:pipeline/*/p:output) then (t:pipeline/*/p:output)[1]/@port else 'xprocspec-depends-on'}"/>
                                        </p:input>
                                    </p:wrap-sequence>
                                    <p:sink/>

                                    <xsl:element name="{$step2}">
                                        <xsl:attribute name="name" select="'compare-pipeline'"/>
                                        <p:with-option name="xprocspec-depends-on" select="'makes {$step1} execute before {$step2}'">
                                            <p:pipe step="xprocspec-depends-on" port="result"/>
                                        </p:with-option>
                                    </xsl:element>

                                </p:declare-step>
                            </xsl:otherwise>
                        </xsl:choose>
                    </x:script>
                </xsl:otherwise>
            </xsl:choose>
            <x:scenario label="{t:title}">
                <x:call>
                    <xsl:namespace name="xprocspec" select="'http://www.daisy.org/ns/xprocspec'"/>
                    <xsl:choose>
                        <xsl:when test="t:compare-pipeline or not(t:pipeline/*[@type])">
                            <xsl:attribute name="step" select="'xprocspec:step'"/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:attribute name="step" select="t:pipeline/*/@type"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:apply-templates select="t:option | t:parameter | t:input"/>
                </x:call>
                <xsl:apply-templates select="t:output"/>
                <xsl:if test="/t:test/@error">
                    <x:context label="the error document">
                        <x:document type="errors"/>
                    </x:context>
                    <x:expect label="should contain the error '{/t:test/@error}'" type="xpath" test="count(/c:errors/c:error[@code='{/t:test/@error}']) &gt; 0"/>
                </xsl:if>
            </x:scenario>
        </x:description>
    </xsl:template>

    <xsl:template match="t:option">
        <x:option name="{@name}" select="'{@value}'"/>
    </xsl:template>

    <xsl:template match="t:parameter">
        <x:param name="{@name}" select="'{@value}'"/>
    </xsl:template>

    <xsl:template match="t:input">
        <x:input port="{@port}">
            <xsl:choose>
                <xsl:when test="@href">
                    <x:document href="{@href}"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="*">
                        <xsl:choose>
                            <xsl:when test="self::t:document">
                                <x:document>
                                    <xsl:copy-of select="*" copy-namespaces="no"/>
                                </x:document>
                            </xsl:when>
                            <xsl:otherwise>
                                <x:document>
                                    <xsl:copy-of select="." copy-namespaces="no"/>
                                </x:document>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </x:input>
    </xsl:template>

    <xsl:template match="t:output">
        <x:context label="the output port '{@port}'">
            <x:document type="port" port="{@port}"/>
        </x:context>
        <xsl:variable name="document-count" select="count(*)"/>
        <x:expect type="compare" label="should contain the expected{if($document-count&gt;1)then concat(' ',$document-count) else ''} document{if($document-count=1) then '' else 's'}{if($document-count=0) then ' (an empty sequence)' else ''}">
            <xsl:choose>
                <xsl:when test="@href">
                    <x:document href="{@href}"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:for-each select="*">
                        <xsl:choose>
                            <xsl:when test="self::t:document">
                                <x:document>
                                    <xsl:copy-of select="*" copy-namespaces="no"/>
                                </x:document>
                            </xsl:when>
                            <xsl:otherwise>
                                <x:document>
                                    <xsl:copy-of select="." copy-namespaces="no"/>
                                </x:document>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:otherwise>
            </xsl:choose>
        </x:expect>
    </xsl:template>

</xsl:stylesheet>
