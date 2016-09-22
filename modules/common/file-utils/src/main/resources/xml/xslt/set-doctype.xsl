<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" exclude-result-prefixes="#all" version="2.0">

    <xsl:param name="doctype" required="yes"/>

    <xsl:template match="/*">
        <xsl:variable name="find-doctype">
            <xsl:copy>
                <xsl:if test="text()">

                    <xsl:analyze-string select="text()" regex="&lt;!--.*?--&gt;" flags="s">

                        <xsl:matching-substring>
                            <!-- comment -->
                            <xsl:value-of select="."/>
                        </xsl:matching-substring>

                        <xsl:non-matching-substring>

                            <xsl:analyze-string select="."
                                regex="&lt;[:A-Z_a-z&#xC0;-&#xD6;&#xD8;-&#xF6;&#xF8;-&#x2FF;&#x370;-&#x37D;&#x37F;-&#x1FFF;&#x200C;-&#x200D;&#x2070;-&#x218F;&#x2C00;-&#x2FEF;&#x3001;-&#xD7FF;&#xF900;-&#xFDCF;&#xFDF0;-&#xFFFD;&#x10000;-&#xEFFFF;]">

                                <xsl:matching-substring>
                                    <!-- element -->
                                    <element>
                                        <xsl:value-of select="."/>
                                    </element>

                                </xsl:matching-substring>

                                <xsl:non-matching-substring>
                                    <xsl:choose>
                                        <xsl:when test="position() = 1">
                                            <!-- prolog -->

                                            <xsl:analyze-string select="." regex="&lt;\?.*?\?&gt;" flags="s">

                                                <xsl:matching-substring>
                                                    <!-- xml declaration or processing instruction -->
                                                    <xsl:choose>
                                                        <xsl:when test="matches(.,'&lt;?xml\s','s')">
                                                            <xml-declaration>
                                                                <xsl:value-of select="."/>
                                                            </xml-declaration>
                                                        </xsl:when>
                                                        <xsl:otherwise>
                                                            <xsl:value-of select="."/>
                                                        </xsl:otherwise>
                                                    </xsl:choose>
                                                </xsl:matching-substring>

                                                <xsl:non-matching-substring>

                                                    <xsl:analyze-string select="." regex="&lt;!DOCTYPE.*&gt;" flags="s">

                                                        <xsl:matching-substring>
                                                            <!-- doctype -->
                                                            <doctype>
                                                                <xsl:value-of select="."/>
                                                            </doctype>
                                                        </xsl:matching-substring>

                                                        <xsl:non-matching-substring>
                                                            <!-- whitespace or root element -->
                                                            <xsl:value-of select="."/>
                                                        </xsl:non-matching-substring>

                                                    </xsl:analyze-string>

                                                </xsl:non-matching-substring>
                                            </xsl:analyze-string>

                                        </xsl:when>
                                        <xsl:otherwise>
                                            <!-- not prolog -->
                                            <xsl:value-of select="."/>

                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:non-matching-substring>

                            </xsl:analyze-string>

                        </xsl:non-matching-substring>
                    </xsl:analyze-string>
                </xsl:if>
            </xsl:copy>
        </xsl:variable>

        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:if test="not($find-doctype/*/doctype[not(preceding-sibling::element)]) and not($find-doctype/*/xml-declaration[not(preceding-sibling::element)])">
                <xsl:value-of select="$doctype"/>
                <xsl:value-of select="'&#x0a;'"/>
            </xsl:if>
            <xsl:for-each select="$find-doctype/*/node()">
                <xsl:choose>
                    <xsl:when test="self::text()">
                        <xsl:copy-of select="."/>
                    </xsl:when>
                    <xsl:when test="self::* and preceding-sibling::element">
                        <xsl:copy-of select="./text()"/>
                    </xsl:when>
                    <xsl:when test="self::xml-declaration and not($find-doctype/*/doctype[not(preceding-sibling::element)])">
                        <xsl:copy-of select="./text()"/>
                        <xsl:value-of select="'&#x0a;'"/>
                        <xsl:value-of select="$doctype"/>
                    </xsl:when>
                    <xsl:when test="self::doctype">
                        <xsl:value-of select="$doctype"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:copy-of select="./text()"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
