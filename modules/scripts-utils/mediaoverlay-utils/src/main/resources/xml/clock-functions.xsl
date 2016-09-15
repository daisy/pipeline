<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">

    <xsl:function name="pf:mediaoverlay-clock-value-to-seconds" as="xs:double">
        <xsl:param name="string" as="xs:string"/>
        <xsl:variable name="stringTokenized"
            select="reverse(
                        subsequence(
                            tokenize(
                                replace(
                                    replace($string, '^.*=(.*)$', '$1'),
                                    '^(.+?)[^\d]*$',
                                '$1'),
                            ':'),
                        1, 3)
                    )"/>
        <xsl:variable name="number"
            select=" (number($stringTokenized[1]) + (if (count($stringTokenized)&gt;=2) then number($stringTokenized[2])*60 else 0) + (if (count($stringTokenized)=3) then number($stringTokenized[3])*3600 else 0))
            *(if (ends-with($string,'ms')) then 0.001 else (if (ends-with($string,'min')) then 60 else (if (ends-with($string,'h')) then 3600 else 1)))"/>
        <xsl:value-of select="$number"/>
    </xsl:function>

    <xsl:function name="pf:mediaoverlay-seconds-to-timecount" as="xs:string">
        <xsl:param name="number" as="xs:double"/>
        <xsl:value-of select="pf:mediaoverlay-seconds-to-timecount($number,'s')"/>
    </xsl:function>

    <xsl:function name="pf:mediaoverlay-seconds-to-timecount" as="xs:string">
        <xsl:param name="number" as="xs:double"/>
        <xsl:param name="metric" as="xs:string"/>
        <xsl:choose>
            <xsl:when test="$metric='s'">
                <xsl:value-of select="concat(string($number),'s')"/>
            </xsl:when>
            <xsl:when test="$metric='ms'">
                <xsl:value-of select="concat(string($number * 1000),'ms')"/>
            </xsl:when>
            <xsl:when test="$metric='min'">
                <xsl:value-of select="concat(string(round(($number div 60)*1000) div 1000),'min')"/>
            </xsl:when>
            <xsl:when test="$metric='h'">
                <xsl:value-of select="concat(string(round(($number div 3600)*1000) div 1000),'h')"/>
            </xsl:when>
            <xsl:otherwise>
                <!-- TODO: throw either warning or error about the invalid metric -->
                <xsl:value-of select="concat(string($number),'s')"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

    <xsl:function name="pf:mediaoverlay-seconds-to-full-clock-value" as="xs:string">
        <xsl:param name="number" as="xs:double"/>
        <xsl:variable name="HH"
            select="concat(if (($number div 3600) &lt; 10) then '0' else '', string(floor($number div 3600)))"/>
        <xsl:variable name="MM"
            select="concat(if ((($number mod 3600) div 60) &lt; 10) then '0' else '', string(floor(($number mod 3600) div 60)))"/>
        <xsl:variable name="SS"
            select="concat(if (($number mod 60) &lt; 10) then '0' else '', string(round(($number mod 60)*1000) div 1000))"/>
        <xsl:value-of select="concat($HH,':',$MM,':',$SS)"/>
    </xsl:function>

    <xsl:function name="pf:mediaoverlay-seconds-to-partial-clock-value" as="xs:string">
        <xsl:param name="number" as="xs:double"/>
        <!-- TODO: Throw error or warning if $number > 3600 ? -->
        <xsl:variable name="MM"
            select="concat(if (($number div 60) &lt; 10) then '0' else '', string(floor($number div 60)))"/>
        <xsl:variable name="SS"
            select="concat(if (($number mod 60) &lt; 10) then '0' else '', string(round(($number mod 60)*1000) div 1000))"/>
        <xsl:value-of select="concat($MM,':',$SS)"/>
    </xsl:function>

    <xsl:function name="pf:mediaoverlay-seconds-to-clock-value" as="xs:string">
        <xsl:param name="number" as="xs:double"/>
        <xsl:choose>
            <xsl:when test="$number &lt; 60">
                <xsl:value-of select="pf:mediaoverlay-seconds-to-timecount($number)"/>
            </xsl:when>
            <xsl:when test="$number &lt; 3600">
                <xsl:value-of select="pf:mediaoverlay-seconds-to-partial-clock-value($number)"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="pf:mediaoverlay-seconds-to-full-clock-value($number)"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:function>

</xsl:stylesheet>
