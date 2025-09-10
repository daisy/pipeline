<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:pf="http://www.daisy.org/ns/pipeline/functions"
                exclude-result-prefixes="#all">

    <xsl:function name="pf:asciimath-to-ssml"
                  xmlns:ASCIIMathToSSML="org.daisy.pipeline.asciimath.saxon.impl.ASCIIMathToSSMLFunctionProvider$ASCIIMathToSSML">
        <xsl:param name="asciimath" as="text()"/>
        <xsl:param name="language" as="xs:string"/>
        <xsl:sequence select="ASCIIMathToSSML:transform($asciimath,$language)">
            <!--
                Implemented in ../../java/org/daisy/pipeline/asciimath/saxon/impl/ASCIIMathToSSMLFunctionProvider.java
            -->
        </xsl:sequence>
    </xsl:function>

</xsl:stylesheet>
