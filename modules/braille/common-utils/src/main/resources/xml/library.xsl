<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:java="implemented-in-java">

    <xsl:import href="encoding-functions.xsl">
        <!--
            * brl:unicode-braille-to-escape-sequence
            * brl:unicode-braille-to-nabcc
            * brl:nabcc-to-unicode-braille
        -->
    </xsl:import>

    <xsl:function name="pf:error">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:error($msg, ())"/>
    </xsl:function>

    <xsl:function name="pf:error">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:error($msg, $args, true())"/>
    </xsl:function>

    <xsl:function name="pf:error">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:param name="terminate" as="xs:boolean"/>
        <xsl:sequence select="pf:message('ERROR', $msg, $args)"/>
        <xsl:if test="$terminate">
            <xsl:message terminate="yes"/>
        </xsl:if>
    </xsl:function>

    <xsl:template name="pf:error">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:param name="terminate" as="xs:boolean" required="no" select="true()"/>
        <xsl:sequence select="pf:error($msg, $args, $terminate)"/>
    </xsl:template>

    <xsl:function name="pf:warn">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:warn($msg, ())"/>
    </xsl:function>

    <xsl:function name="pf:warn">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:message('WARN', $msg, $args)"/>
    </xsl:function>

    <xsl:template name="pf:warn">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:sequence select="pf:warn($msg, $args)"/>
    </xsl:template>

    <xsl:function name="pf:info">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:info($msg, ())"/>
    </xsl:function>

    <xsl:function name="pf:info">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:message('INFO', $msg, $args)"/>
    </xsl:function>

    <xsl:template name="pf:info">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:sequence select="pf:info($msg, $args)"/>
    </xsl:template>

    <xsl:function name="pf:debug">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:debug($msg, ())"/>
    </xsl:function>

    <xsl:function name="pf:debug">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:message('DEBUG', $msg, $args)"/>
    </xsl:function>

    <xsl:template name="pf:debug">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:sequence select="pf:debug($msg, $args)"/>
    </xsl:template>

    <xsl:function name="pf:trace">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:sequence select="pf:trace($msg, ())"/>
    </xsl:function>

    <xsl:function name="pf:trace">
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args"/>
        <xsl:sequence select="pf:message('TRACE', $msg, $args)"/>
    </xsl:function>

    <xsl:template name="pf:trace">
        <xsl:param name="msg" as="xs:string" required="yes"/>
        <xsl:param name="args" required="no" select="()"/>
        <xsl:sequence select="pf:trace($msg, $args)"/>
    </xsl:template>

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Create a log message.</p>
        </desc>
    </doc>
    <java:function name="pf:message">
        <xsl:param name="level" as="xs:string"/>
        <xsl:param name="msg" as="xs:string"/>
    </java:function>
    <java:function name="pf:message">
        <xsl:param name="level" as="xs:string"/>
        <xsl:param name="msg" as="xs:string"/>
        <xsl:param name="args" as="xs:string*"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/common/saxon/impl/MessageDefinition.java
        -->
    </java:function>

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Update the progress.</p>
        </desc>
    </doc>
    <java:function name="pf:progress">
        <xsl:param name="progress" as="xs:string"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/common/saxon/impl/ProgressDefinition.java
        -->
    </java:function>

    <xsl:template name="pf:progress">
        <xsl:param name="progress" as="xs:string" required="yes"/>
        <xsl:sequence select="pf:progress($progress)"/>
    </xsl:template>

    <doc xmlns="http://www.oxygenxml.com/ns/doc/xsl">
        <desc>
            <p>Apply a text transformer to a string sequence.</p>
        </desc>
    </doc>
    <java:function name="pf:text-transform" as="xs:string*">
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="text" as="xs:string*"/>
    </java:function>
    <java:function name="pf:text-transform" as="xs:string*">
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="text" as="xs:string*"/>
        <xsl:param name="style" as="xs:string*"/>
    </java:function>
    <java:function name="pf:text-transform" as="xs:string*">
        <xsl:param name="query" as="xs:string"/>
        <xsl:param name="text" as="xs:string*"/>
        <xsl:param name="style" as="xs:string*"/>
        <xsl:param name="lang" as="xs:string*"/>
        <!--
            Implemented in ../../java/org/daisy/pipeline/braille/common/saxon/impl/TextTransformDefinition.java
        -->
    </java:function>

</xsl:stylesheet>
