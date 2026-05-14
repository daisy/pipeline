<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                exclude-result-prefixes="xs">


    <xsl:param name="css" required="no" select="''"/>

    <xsl:variable name="hasMath" select="count(//*[namespace-uri() = xs:anyURI('http://www.w3.org/1998/Math/MathML')]) &gt; 0" />
    <xsl:variable name="namespacesToRemove">
        xs
        <xsl:if test="not($hasMath)">mml</xsl:if>
    </xsl:variable>

    <xsl:template match="/">

        <!-- Check version attribute -->
        <xsl:variable name="version">
            <xsl:choose>
                <xsl:when test="/dtb:dtbook/@*:version">
                    <xsl:value-of select="/dtb:dtbook/@*:version"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>2005-3</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:result-document>
            <xsl:text>&#10;</xsl:text>
            <xsl:if test="$css != ''">
                <xsl:text disable-output-escaping="yes">&lt;?xml-stylesheet href="</xsl:text>
                <xsl:value-of select="$css" />
                <xsl:text disable-output-escaping="yes">" type="text/css"?&gt;</xsl:text>
                <xsl:text>&#10;</xsl:text>
            </xsl:if>
            <xsl:text disable-output-escaping="yes">&lt;!DOCTYPE dtbook PUBLIC '-//NISO//DTD dtbook </xsl:text>
            <xsl:value-of select="$version"/>
            <xsl:text disable-output-escaping="yes">//EN' 'http://www.daisy.org/z3986/2005/dtbook-</xsl:text>
            <xsl:value-of select="$version"/>
            <xsl:text disable-output-escaping="yes">.dtd' [</xsl:text>
            <!-- Add mathml if mml elements exists in the dtbook -->
            <xsl:if test="$hasMath">
                <xsl:text disable-output-escaping="yes">
    &lt;!ENTITY % MATHML.prefixed "INCLUDE" &gt;
    &lt;!ENTITY % MATHML.prefix "mml"&gt;
    &lt;!ENTITY % Schema.prefix "sch"&gt;
    &lt;!ENTITY % XLINK.prefix "xlp"&gt;
    &lt;!ENTITY % MATHML.Common.attrib
        "xlink:href    CDATA       #IMPLIED
        xlink:type     CDATA       #IMPLIED
        class          CDATA       #IMPLIED
        style          CDATA       #IMPLIED
        id             ID          #IMPLIED
        xref           IDREF       #IMPLIED
        other          CDATA       #IMPLIED
        xmlns:dtbook   CDATA       #FIXED
            'http://www.daisy.org/z3986/2005/dtbook/'
        dtbook:smilref CDATA       #IMPLIED"&gt;
    &lt;!ENTITY % mathML2 PUBLIC '-//W3C//DTD MathML 2.0//EN' 'http://www.w3.org/Math/DTD/mathml2/mathml2.dtd'&gt;
    %mathML2;
    &lt;!ENTITY % externalFlow "| mml:math"&gt;
    &lt;!ENTITY % externalNamespaces "xmlns:mml CDATA #FIXED
              'http://www.w3.org/1998/Math/MathML'" &gt;</xsl:text>
                <xsl:text>&#10;</xsl:text>
            </xsl:if>
            <xsl:text disable-output-escaping="yes">] &gt;</xsl:text>
            <xsl:text>&#10;</xsl:text>
            <xsl:copy>
                <xsl:apply-templates />
            </xsl:copy>
        </xsl:result-document>

    </xsl:template>
    <!-- Element -->
    <xsl:template match="*">
        <xsl:copy copy-namespaces="no">
            <xsl:for-each select="namespace::*">
                <xsl:if test="not(contains(normalize-space($namespacesToRemove), name()))">
                    <xsl:copy />
                </xsl:if>
            </xsl:for-each>
            <xsl:apply-templates select="@*" />
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="@*">
        <xsl:copy />
    </xsl:template>

    <xsl:template match="comment()|processing-instruction()">
        <xsl:copy>
            <xsl:copy-of select="@*"/>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>