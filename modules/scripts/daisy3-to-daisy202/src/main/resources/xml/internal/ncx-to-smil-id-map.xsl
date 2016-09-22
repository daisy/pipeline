<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:d="http://www.daisy.org/ns/pipeline/data"
    xpath-default-namespace="http://www.daisy.org/z3986/2005/ncx/" exclude-result-prefixes="#all"
    version="2.0">

    <!--
    Extract the ID-based references to SMIL documents from the NCX, grouped by document.
    
    The result has the form:
    
    <d:idrefs xmlns:d="http://www.daisy.org/ns/pipeline/data">
       <d:doc href="file:/tmp/daisy3/some.smil">
          <d:idref id="navPoint_001" idref="par_0001" navmap="true"/>
          <d:idref id="pageTarget_001" idref="par_0003"/>
          <d:idref id="navTarget_001" idref="par_0002"/>
       </d:doc>
       ...
    </d:idrefs>
    -->

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="text()" mode="#all"/>

    <xsl:template match="ncx">
        <xsl:variable name="idrefs" as="element()">
            <d:idrefs>
                <xsl:apply-templates mode="preprocess"/>
            </d:idrefs>
        </xsl:variable>
        <d:idrefs>
            <xsl:for-each-group select="$idrefs//d:idref" group-by="@href">
                <d:doc href="{current-grouping-key()}">
                    <xsl:for-each select="current-group()">
                        <xsl:copy>
                            <xsl:copy-of select="@id|@idref|@navmap[.='true']"/>
                        </xsl:copy>
                    </xsl:for-each>
                </d:doc>
            </xsl:for-each-group>
        </d:idrefs>
    </xsl:template>

    <xsl:template match="content" mode="preprocess">
        <d:idref href="{resolve-uri(substring-before(@src,'#'),base-uri())}"
            idref="{substring-after(@src,'#')}" id="{../@id}" navmap="{exists(ancestor::navMap)}"/>
    </xsl:template>

</xsl:stylesheet>
