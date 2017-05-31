<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0" xmlns:x="http://www.daisy.org/ns/xprocspec" xmlns:c="http://www.w3.org/ns/xproc-step">

    <xsl:output indent="yes"/>

    <xsl:param name="test" required="yes"/>
    <xsl:param name="equals" required="yes"/>

    <xsl:template match="/x:test-result">
        <xsl:copy>
            <xsl:attribute name="result" select="if (not(some $result in (/x:test-result/c:result/@result) satisfies $result = 'failed')) then 'passed' else 'failed'"/>
            <x:expected>
                <xsl:attribute name="xml:space" select="'preserve'"/>
                <xsl:choose>
                    <xsl:when test="$equals">
                        <xsl:value-of select="concat('XPath: ',$equals)"/>
                        <xsl:choose>
                            <xsl:when test="count(c:result)=1">
                                <xsl:text>
</xsl:text>
                                <xsl:value-of select="concat('Value: ',c:result/@equals)"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:for-each select="c:result">
                                    <xsl:text>
</xsl:text>
                                    <xsl:value-of select="concat('Value #',position(),': ',@equals)"/>
                                </xsl:for-each>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:text>(expected test expression to evaluate to "true()")</xsl:text>
                    </xsl:otherwise>
                </xsl:choose>
            </x:expected>
            <x:was>
                <xsl:attribute name="xml:space" select="'preserve'"/>
                <xsl:value-of select="concat('XPath: ',$test)"/>
                <xsl:choose>
                    <xsl:when test="count(c:result)=1">
                        <xsl:text>
</xsl:text>
                        <xsl:value-of select="concat('Value: ',c:result/@test)"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:for-each select="c:result">
                            <xsl:text>
</xsl:text>
                            <xsl:value-of select="concat('Value #',position(),': ',@test)"/>
                        </xsl:for-each>
                    </xsl:otherwise>
                </xsl:choose>
            </x:was>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
