<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">

    <xsl:output encoding="UTF-8" exclude-result-prefixes="#all" omit-xml-declaration="yes"/>

    <xsl:template match="/*">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:variable name="result">
                <xsl:analyze-string select="text()" regex="&lt;!--.*?[^-]--&gt;">
                    <xsl:matching-substring>
                        <substring is-comment="true">
                            <xsl:value-of select="."/>
                        </substring>
                    </xsl:matching-substring>
                    <xsl:non-matching-substring>
                        <substring>
                            <xsl:value-of select="."/>
                        </substring>
                    </xsl:non-matching-substring>
                </xsl:analyze-string>
            </xsl:variable>
            <xsl:variable name="result">
                <xsl:for-each select="$result/*">
                    <xsl:choose>
                        <xsl:when test="@is-comment='true'">
                            <xsl:copy-of select="."/>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:analyze-string select="text()" regex="&lt;[?!][^&gt;]*&gt;">
                                <xsl:matching-substring>
                                    <substring>
                                        <xsl:value-of select="."/>
                                    </substring>
                                </xsl:matching-substring>
                                <xsl:non-matching-substring>
                                    <substring contains-elements="{contains(.,'&lt;')}">
                                        <xsl:value-of select="."/>
                                    </substring>
                                </xsl:non-matching-substring>
                            </xsl:analyze-string>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:variable>
            <xsl:variable name="result">
                <xsl:for-each select="$result/*[not(preceding::*/@contains-elements='true')]">
                    <xsl:choose>
                        <xsl:when test="@contains-elements='true'">
                            <xsl:copy>
                                <xsl:variable name="find-end-tag">
                                    <substring>
                                        <xsl:value-of select="substring-before(.,'&lt;')"/>
                                        <xsl:text>&lt;</xsl:text>
                                    </substring>
                                    <xsl:for-each select="tokenize(substring-after(.,'&lt;'),'&quot;')">
                                        <xsl:for-each select="if (position() mod 2) then tokenize(.,'''') else .">
                                            <substring contains-end-tag="{position() mod 2 and contains(.,'&gt;')}">
                                                <xsl:copy-of select="if (position() mod 2 and contains(.,'&gt;')) then replace(concat(substring-before(.,'&gt;'),'/&gt;'),'//&gt;','/&gt;') else ."/>
                                            </substring>
                                            <xsl:if test="not(position() = last())">
                                                <substring position="{position()}">'</substring>
                                            </xsl:if>
                                        </xsl:for-each>
                                        <xsl:if test="not(position() = last())">
                                            <substring position="{position()}">"</substring>
                                        </xsl:if>
                                    </xsl:for-each>
                                </xsl:variable>
                                <xsl:value-of select="string-join($find-end-tag/*[not(preceding-sibling::*[@contains-end-tag='true'])]//text(),'')"/>
                            </xsl:copy>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:copy-of select="."/>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:for-each>
            </xsl:variable>
            <xsl:value-of select="string-join($result/*/text(),'')"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
