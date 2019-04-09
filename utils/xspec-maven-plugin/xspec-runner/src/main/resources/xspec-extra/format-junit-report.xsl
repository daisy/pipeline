<?xml version="1.0" encoding="UTF-8"?>
<!--

    Copyright (C) 2013 The DAISY Consortium

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:test="http://www.jenitennison.com/xslt/unit-test"
    xmlns:x="http://www.jenitennison.com/xslt/xspec" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    version="2.0">

    <xsl:import href="../xspec/reporter/format-utils.xsl"/>

    <xsl:output indent="yes"/>

    <xsl:param name="name" as="xs:string" required="yes"/>
    <xsl:param name="time" as="xs:double" required="no" select="0"/>

    <xsl:variable name="classname" as="xs:string" select="concat('xslt.',$name)"/>


    <xsl:template match="/">
        <testsuite name="{$classname}" errors="0" failures="{count(//x:test[@successful='false'])}"
            skipped="{count(//x:test[@pending='true'])}" time="{$time}">
            <xsl:apply-templates/>
        </testsuite>
    </xsl:template>

    <xsl:template match="x:test">
        <testcase classname="{$classname}" name="{string-join(ancestor-or-self::*/x:label[normalize-space()],' ')}">
            <xsl:if test="@pending = 'true'">
                <skipped/>
            </xsl:if>
            <xsl:if test="@successful = 'false'">
                <failure type="Expectation Error" xml:space="preserve">Expected:

<xsl:apply-templates select="../x:result" mode="value"/>

But Was:

<xsl:apply-templates select="x:expect" mode="value"/>
                </failure>
            </xsl:if>
        </testcase>
    </xsl:template>

    <xsl:template match="x:result"> </xsl:template>

    <xsl:template match="*" mode="value">
        <xsl:variable name="expected" as="xs:boolean" select=". instance of element(x:expect)"/>
        <xsl:choose>
            <xsl:when test="@href or node()">
                <xsl:if test="@select" xml:space="preserve">
XPath [<xsl:value-of select="@select"/>] from:
</xsl:if>
                <xsl:choose>
                    <xsl:when test="@href" xml:space="preserve"><xsl:value-of select="test:format-URI(@href)"/></xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="indentation"
                            select="string-length(substring-after(text()[1], '&#xA;'))"/>
                        <xsl:apply-templates select="node() except text()[not(normalize-space())]"
                            mode="test:serialize">
                            <xsl:with-param name="indentation" tunnel="yes" select="$indentation"/>
                        </xsl:apply-templates>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise xml:space="preserve"><xsl:value-of select="@select"/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="text()"/>

</xsl:stylesheet>
