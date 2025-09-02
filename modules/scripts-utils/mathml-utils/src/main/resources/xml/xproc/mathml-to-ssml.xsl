<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                xmlns:m="http://www.w3.org/1998/Math/MathML"
                exclude-result-prefixes="#all">

    <xsl:function name="pf:mathml-to-ssml" as="element()?"
                  xmlns:MathMLToSSML="org.daisy.pipeline.mathml.tts.saxon.impl.MathMLToSSMLFunctionProvider$MathMLToSSML">
        <xsl:param name="mathml" as="element(m:math)"/>
        <xsl:param name="language" as="xs:string"/>
        <xsl:variable name="ssml" select="MathMLToSSML:transform($mathml,$language)">
            <!-- Implemented in ../../java/org/daisy/pipeline/mathml/tts/saxon/impl/MathMLToSSMLFunctionProvider.java  -->
        </xsl:variable>
        <xsl:sequence select="if ($ssml instance of document-node()) then $ssml/* else $ssml"/>
    </xsl:function>

    <xsl:template match="*[@xml:lang]" priority="1">
        <xsl:next-match>
            <xsl:with-param name="lang" tunnel="yes" select="string(@xml:lang)"/>
        </xsl:next-match>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- px:mathml-to-ssml ensures every m:math has a @id -->
    <xsl:template match="m:math[@id]" priority="1.1">
        <xsl:param name="lang" tunnel="yes" as="xs:string" select="'und'"/>
        <xsl:variable name="mathml" as="element()" select="."/>
        <xsl:variable name="ssml" as="element()?"
                      select="pf:mathml-to-ssml(.,(@xml:lang/string(.),$lang)[1])"/>
        <!-- make sure the SSML has the same id as the input -->
        <xsl:choose>
            <xsl:when test="$ssml/@id[string(.)=string($mathml/@id)]
                            and ($ssml/@xml:lang/string(.),$lang)[1]=(@xml:lang/string(.),$lang)[1]">
                <xsl:sequence select="$ssml"/>
            </xsl:when>
            <xsl:otherwise>
                <xsl:for-each select="$ssml">
                    <xsl:copy>
                        <xsl:sequence select="$mathml/(@id,@xml:lang)"/>
                        <xsl:apply-templates select="(@* except @id|@xml:lang)|node()"/>
                    </xsl:copy>
                </xsl:for-each>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
